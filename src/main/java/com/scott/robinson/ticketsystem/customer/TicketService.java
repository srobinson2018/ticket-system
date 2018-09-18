package com.scott.robinson.ticketsystem.customer;

import java.util.Map;

/**
 * This interface was provided by Walmart Labs as a part of the contract of the desired service. Additional methods have
 * been added to allow for some additional functionality.
 */
public interface TicketService {
    /**
     * The number of seats in the venue that are neither held nor reserved
     *
     * @return the number of tickets available in the venue
     */
    int numSeatsAvailable();

    /**
     * Find and hold the best available seats for a customer
     *
     * @param numSeats      the number of seats to find and hold
     * @param customerEmail unique identifier for the customer
     * @return a SeatHold object identifying the specific seats and related
     * information
     */
    SeatHold findAndHoldSeats(final int numSeats, final String customerEmail);

    /**
     * Commit seats held for a specific customer
     *
     * @param seatHoldId    the seat hold identifier
     * @param customerEmail the email address of the customer to which the
     *                      seat hold is assigned
     * @return a reservation confirmation code
     */
    String reserveSeats(final int seatHoldId, final String customerEmail);


    /**
     * Added by [Scott Robinson]. This additional method to return a text-based map which helps visualize in lieu
     * of an actual UI.
     *
     * @return {@link String} representation of the current venue seating map using the {@link SeatState#abbreviation}
     */
    String seatMap();

    /**
     * Added by [Scott Robinson]. This additional method allows for a user to cancel a seat hold request before the
     * scheduled timeout.
     *
     * @return {@link boolean} representing whether the cancellation was successful
     */
    boolean cancelSeatHold(final int seatHoldId, final String customerEmail);

    /**
     * Added by [Scott Robinson]. This additional method is to be used only by the
     * {@link com.scott.robinson.ticketsystem.admin.AdminController} to give insight into the internal map of seat
     * holds.
     *
     * @return The internal {@link Map} of seat holds
     */
    Map<Integer, SeatHold> getHolds();

    /**
     * Added by [Scott Robinson]. This additional method is to be used only by the
     * {@link com.scott.robinson.ticketsystem.admin.AdminController} to give insight into the internal map of seat
     * reservations.
     *
     * @return The internal {@link Map} of seat reservations
     */
    Map<String, SeatReservation> getReservations();

    /**
     * Added by [Scott Robinson]. This additional method allows the customer to review their seat hold by providing the
     * seat hold id.
     *
     * @param seatHoldId The Id of the seat hold
     * @param customerEmail The customer's email address
     * @return
     */
    SeatHold getHold(final int seatHoldId, final String customerEmail);

}