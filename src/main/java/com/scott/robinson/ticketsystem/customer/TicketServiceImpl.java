package com.scott.robinson.ticketsystem.customer;

import com.scott.robinson.ticketsystem.ConfigurationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The {@link Service} which is responsible for managing the ticket operations as well as maintaining the in-memory
 * venue object.
 */
@Service
final class TicketServiceImpl implements TicketService {

    /**
     * {@link SeatState} array used to represent the venue. The array is initialized in the
     * {@link #TicketServiceImpl(ConfigurationProvider)} constructor and uses the row and seat width defined in the
     * {@link ConfigurationProvider}. Each seat in represented by an entry in the array and it's state defined by it's
     * associated {@link SeatState}.
     */
    private final SeatState[][] venue;

    /**
     * Locking {@link Object} used manage concurrent modification of the {@link #venue}.
     */
    private final Object venueLock = new Object();

    /**
     * {@link Map} to maintain the existing {@link SeatHold}. The key to this {@link Map} is the {@link SeatHold#id}.
     */
    private Map<Integer, SeatHold> seatHoldMap = new HashMap<>();

    /**
     * Locking {@link Object} used manage concurrent modification of the {@link #seatHoldMap}.
     */
    private final Object seatHoldMapLock = new Object();

    /**
     * {@link Map} to maintain the existing {@link #seatReservedMap}. The key to this {@link Map} is the reservation
     * confirmation code.
     */
    private Map<String, SeatReservation> seatReservedMap = new HashMap<>();

    private final int holdExpirationSec;

    @Autowired
    TicketServiceImpl(ConfigurationProvider configurationProvider) {
        // Initialize the configurable variables
        holdExpirationSec = configurationProvider.getHoldTimeoutSeconds();
        venue = new SeatState[configurationProvider.getRows()][configurationProvider.getSeatsPerRow()];
        for (int row = 0; row < venue.length; row++) {
            for (int seat = 0; seat < venue[0].length; seat++) {
                venue[row][seat] = SeatState.OPEN;
            }
        }

        // Trigger the expiration logic on a times interval
        final Timer expireHoldTimer = new Timer();
        expireHoldTimer.schedule(new ExpireHoldsTask(), 0, 1000);
    }

    /**
     * Calculates the total number of seats available in the {@link #venue}.
     *
     * @return The number of seats currently available for hold or reservation in the {@link #venue}
     */
    @Override
    public int numSeatsAvailable() {
        int numSeatsAvailable = 0;
        synchronized (venueLock) {
            for (int rowNum = 0; rowNum < venue.length; rowNum++) {
                final SeatState[] row = venue[rowNum];
                for (int seat = 0; seat < row.length; seat++) {
                    if (row[seat] == SeatState.OPEN) {
                        numSeatsAvailable++;
                    }
                }
            }
        }
        return numSeatsAvailable;
    }

    /**
     * Builds a {@link String} representation of the two-dimensional {@link #venue} showing the open, held, and reserved
     * seats via the associated {@link SeatState#abbreviation}.
     *
     * @return {@link String} representation of the {@link #venue}
     */
    @Override
    public String seatMap() {
        final StringBuilder sb = new StringBuilder();

        // Header
        sb.append("row\t");
        for (int i = 0; i < venue[0].length; i++) {
            sb.append(i).append('\t');
        }
        sb.append("\n");

        // Rows
        synchronized (venueLock) {
            for (int rowNum = 0; rowNum < venue.length; rowNum++) {
                sb.append(rowNum);
                sb.append(":\t");
                final SeatState[] row = venue[rowNum];
                for (SeatState seatState : row) {
                    sb.append(seatState.getAbbreviation()).append('\t');
                }
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    /**
     * Finds and returns the best available seats given the number of seats requested.
     *
     * @param numSeats      The number of seats requested by the user
     * @param customerEmail The user's email address
     * @return {@link SeatHold} representing the seats that have been places on hold by the user
     */
    @Override
    public SeatHold findAndHoldSeats(final int numSeats, final String customerEmail) {
        if (numSeats < 1) {
            throw new RuntimeException("Number of seats must be greater than one.");
        }

        // Find the best available seats
        final Seats seats = holdBestSeats(numSeats);
        if (seats == null) {
            throw new RuntimeException("No consecutive seats available for [" + numSeats + "] seats.");
        }

        // Create a hold of the found seats
        final SeatHold seatHold = new SeatHold(customerEmail, seats);

        // Add the seat hold to the map
        synchronized (seatHoldMapLock) {
            seatHoldMap.put(seatHold.getId(), seatHold);
        }

        return seatHold;
    }

    /**
     * Converts a seat hold into a seat reservation. If no seat hold is found for the provided Id and email address then
     * a {@link RuntimeException} is thrown.
     *
     * @return A confirmation code as a {@link String}
     */
    @Override
    public String reserveSeats(final int seatHoldId, final String customerEmail) {
        final String confirmationCode;

        // Synchronize on the seat hold map because we are removing an entry from the map
        synchronized (seatHoldMapLock) {
            // Synchronize on the venue because we are updating entry values in the array
            synchronized (venueLock) {
                // Ensure the seat hold exists with the provided email address
                final SeatHold seatHold = seatHoldMap.get(seatHoldId);
                if (seatHold == null || !seatHold.getEmail().equalsIgnoreCase(customerEmail)) {
                    throw new RuntimeException("No seat hold was found for id [" + seatHoldId + "] and email [" + customerEmail + "]");
                }

                // Set associated seats to reserved
                final Seats seats = seatHold.getSeats();
                for (int i = seats.getFirstSeat(); i <= seats.getLastSeat(); i++) {
                    venue[seats.getRow()][i] = SeatState.RESERVED;
                }

                // Remove the hold since we are converting it into a reservation
                seatHoldMap.remove(seatHoldId);

                // Generate the SeatReservation
                SeatReservation seatReservation = new SeatReservation(customerEmail, seats);

                // Store the new reservation
                confirmationCode = seatReservation.getConfirmationCode();
                seatReservedMap.put(confirmationCode, seatReservation);
            }
        }

        return confirmationCode;
    }

    /**
     * Finds and holds the best available seats based on the number of seats requested and the availability within
     * the {@link #venue}.
     *
     * @param numSeats The number of seats the user has requested
     * @return {@link Seats} containing row and seat information matching the user's requirements or null if no matching seats were found
     */
    private Seats holdBestSeats(final int numSeats) {
        synchronized (venueLock) {
            final List<Integer> firstSeatOptions = new ArrayList<>();
            int rowNum;
            for (rowNum = 0; rowNum < venue.length; rowNum++) {
                final SeatState[] row = venue[rowNum];

                // Find all possible seat combinations for row
                for (int seat = 0; seat < row.length; seat++) {
                    // Iterate through each seat to see if it is a possible first seat
                    boolean isPossibility = true;
                    boolean hasMorePossibilities = true;
                    for (int j = 0; j < numSeats; j++) {
                        final int additionalSeat = seat + j;
                        if (additionalSeat > row.length - 1) {
                            // Reached the end of the row before fulfilling seat count requirement
                            hasMorePossibilities = false;

                            // Do not continue to check additional seats because we have already reached the end of the row
                            break;
                        } else if (row[additionalSeat] != SeatState.OPEN) {
                            // Seat required is unavailable, so this potential first seat is not a possibility
                            isPossibility = false;

                            // Do not continue to check because it has already failed to meet the consecutive seating criteria
                            break;
                        }
                    }

                    if (!hasMorePossibilities) {
                        // Reached a point in the row where there are no more possibilities due to the requested seat count
                        break;
                    }
                    if (isPossibility) {
                        // All required seats following the first seat are available, this is an option
                        firstSeatOptions.add(seat);
                    }
                }

                // Stop checking additional rows if the current row has available seating matching the criteria
                if (!firstSeatOptions.isEmpty()) {
                    break;
                }
            }

            // If there are no available options for any row, then return null to indicate that no seats could be with with the criteria
            if (firstSeatOptions.isEmpty()) {
                return null;
            }

            // Get the seat selection that is most in the middle of the row
            final int middle = venue[0].length / 2;
            int distance = Math.abs(firstSeatOptions.get(0) - middle);
            int idx = 0;
            for(int c = 1; c < firstSeatOptions.size(); c++){
                int cDistance = Math.abs(firstSeatOptions.get(c) - middle);
                if(cDistance < distance){
                    idx = c;
                    distance = cDistance;
                }
            }

            final int firstSeat = firstSeatOptions.get(idx);
            final int lastSeat = firstSeat + numSeats - 1;

            // Update the seats within the venue array to be HELD
            final Seats seats = new Seats(rowNum, firstSeat, lastSeat);
            for (int i = seats.getFirstSeat(); i <= seats.getLastSeat(); i++) {
                venue[seats.getRow()][i] = SeatState.HELD;
            }

            return seats;
        }
    }

    /**
     * Cancels the seat hold for the provided seat hold Id and customer email. If no seat hold is found for the provided
     * Id and email address then a {@link RuntimeException} is thrown.
     *
     * @param seatHoldId    The Id of the seat hold to be cancelled
     * @param customerEmail The email of the customer who created the seat hold
     * @return {@link boolean} indicating whether the cancellation was successful
     */
    @Override
    public boolean cancelSeatHold(final int seatHoldId, final String customerEmail) {
        SeatHold canceledSeatHold = null;
        synchronized (seatHoldMapLock) {
            synchronized (venueLock) {
                SeatHold seatHold = seatHoldMap.get(seatHoldId);
                if (seatHold != null && seatHold.getEmail().equalsIgnoreCase(customerEmail)) {
                    canceledSeatHold = seatHoldMap.remove(seatHoldId);
                    final Seats canceledSeats = canceledSeatHold.getSeats();
                    final int canceledFirstSeat = canceledSeats.getFirstSeat();
                    final int canceledLastSeat = canceledSeats.getLastSeat();
                    for (int seatIndex = canceledFirstSeat; seatIndex <= canceledLastSeat; seatIndex++) {
                        venue[canceledSeats.getRow()][seatIndex] = SeatState.OPEN;
                    }
                }
            }
        }
        return canceledSeatHold != null;
    }

    /**
     * To only be used by the {@link com.scott.robinson.ticketsystem.admin.AdminController} for insight into the state
     * of the holds.
     *
     * @return The internal {@link #seatHoldMap} representing the current seat holds.
     */
    @Override
    public Map<Integer, SeatHold> getHolds() {
        return seatHoldMap;
    }

    /**
     * To only be used by the {@link com.scott.robinson.ticketsystem.admin.AdminController} for insight into the state
     * of the reservations.
     *
     * @return The internal {@link #seatReservedMap} representing the current seat reservations.
     */
    @Override
    public Map<String, SeatReservation> getReservations() {
        return seatReservedMap;
    }

    /**
     * Obtains an existing seat hold based on the provided id and email. If the id and email do not match a seat hold
     * within the {@link #seatHoldMap} then a {@link RuntimeException} is thrown.
     *
     * @param seatHoldId The Id of the seat hold
     * @param customerEmail The customer's email address
     * @return The associated seat hold
     */
    @Override
    public SeatHold getHold(final int seatHoldId, final String customerEmail) {
        final SeatHold seatHold = seatHoldMap.get(seatHoldId);
        if (seatHold != null && seatHold.getEmail().equalsIgnoreCase(customerEmail)) {
            return seatHold;
        }
        throw new RuntimeException("No seat hold was found for that combination of Id and email address.");
    }

    /**
     * Internal class used to expire seat holds which have existed beyond the configured expiration threshold
     * {@link ConfigurationProvider#holdTimeoutSeconds}.
     */
    final class ExpireHoldsTask extends TimerTask {
        @Override
        public void run() {
            // Calculate the cutoff time for which all seat hold created before should be removed
            final LocalDateTime cutoff = LocalDateTime.now().minusSeconds(holdExpirationSec);

            // Create a Set to hold the ids of the seat holds which need to be removed
            final Set<Integer> expired = new LinkedHashSet<>();

            // Synchronize on  the map of seat holds since we are potentially be deleting entries
            synchronized (seatHoldMapLock) {
                // Synchronize on the venue since we are potentially updating the array values
                synchronized (venueLock) {
                    // Iterate through each seat hold and check if it was created before the cutoff time
                    seatHoldMap.values().stream().forEach(seatHold -> {
                        if (seatHold.getCreationTime().isBefore(cutoff)) {
                            // Add to the Set of seat hold ids which need to be removed
                            expired.add(seatHold.getId());

                            // Update the seats within the venue to be OPEN
                            final Seats seats = seatHold.getSeats();
                            final int canceledFirstSeat = seats.getFirstSeat();
                            final int canceledLastSeat = seats.getLastSeat();
                            for (int seatIndex = canceledFirstSeat; seatIndex <= canceledLastSeat; seatIndex++) {
                                venue[seats.getRow()][seatIndex] = SeatState.OPEN;
                            }
                        }
                    });

                    // Remove any expired seat holds
                    for (Integer key : expired) {
                        seatHoldMap.remove(key);
                    }
                }
            }
        }
    }
}


