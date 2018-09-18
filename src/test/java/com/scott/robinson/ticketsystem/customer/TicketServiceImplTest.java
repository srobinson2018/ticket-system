package com.scott.robinson.ticketsystem.customer;

import com.scott.robinson.ticketsystem.ConfigurationProvider;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Tests for {@link TicketServiceImpl}.
 */
public final class TicketServiceImplTest {

    // Build ConfigurationProvider with defaults
    private final ConfigurationProvider configurationProvider = new ConfigurationProvider();

    // Build TicketService with default ConfigurationProvider
    private TicketService ticketService;

    private final int testSeatNum = 2;
    private final String testEmail = "test@email.com";
    private final Seats testSeats = new Seats(0, 16, 17); // Middle two

    @Before
    public void setup() {
        ticketService = new TicketServiceImpl(configurationProvider);
    }

    @Test
    public void numSeatsAvailable() {
        // Calculate the expected number of seats
        final int rows = configurationProvider.getRows();
        final int seatsPerRow = configurationProvider.getSeatsPerRow();
        final int seats = rows * seatsPerRow;

        // Verify
        assertThat(ticketService.numSeatsAvailable(), is(seats));
    }

    @Test
    public void findAndHoldSeats() {
        // Create the hold
        final SeatHold seatHold = ticketService.findAndHoldSeats(testSeatNum, testEmail);
        assertThat(seatHold, notNullValue());

        // Verify that the hold has the correct data
        assertThat(seatHold.getCreationTime(), notNullValue());
        assertThat(seatHold.getEmail(), is(testEmail));
        assertThat(seatHold.getSeats(), is(testSeats));
    }

    @Test(expected = RuntimeException.class)
    public void findAndHoldSeats_tooManyConsecutiveSeats() {
        // Attempt to create the hold with an unrealistic number of seats
        ticketService.findAndHoldSeats(100000, testEmail);
        fail("Exception expected");
    }

    @Test(expected = RuntimeException.class)
    public void findAndHoldSeats_noSeats() {
        // Attempt to create the hold with an unrealistic number of no seats
        ticketService.findAndHoldSeats(0, testEmail);
        fail("Exception expected");
    }

    @Test
    public void findAndHoldSeats_oneSeat() {
        // Create the hold
        final SeatHold seatHold = ticketService.findAndHoldSeats(1, testEmail);
        assertThat(seatHold, notNullValue());

        // Verify that the hold has the correct data
        assertThat(seatHold.getCreationTime(), notNullValue());
        assertThat(seatHold.getEmail(), is(testEmail));

        final Seats singleMiddleSeat = new Seats(0, 16, 16);
        assertThat(seatHold.getSeats(), is(singleMiddleSeat));
    }

    @Test
    public void reserveSeats() {
        // Create the hold
        final SeatHold seatHold = ticketService.findAndHoldSeats(testSeatNum, testEmail);
        assertThat(seatHold, notNullValue());

        // Promote the hold to a reservation
        final int seatHoldId = seatHold.getId();
        final String confirmationCode = ticketService.reserveSeats(seatHoldId, testEmail);
        assertThat(confirmationCode, notNullValue());
        assertThat(confirmationCode.length(), is(36));

        // Verify that the reservation has the correct data
        final SeatReservation seatReservation = ticketService.getReservations().get(confirmationCode);
        assertThat(seatReservation, notNullValue());
        assertThat(seatReservation.getEmail(), is(testEmail));
        assertThat(seatReservation.getSeats(), is(testSeats));
    }

    @Test(expected = RuntimeException.class)
    public void reserveSeats_invalidId() {
        // Create the hold
        final SeatHold seatHold = ticketService.findAndHoldSeats(testSeatNum, testEmail);
        assertThat(seatHold, notNullValue());

        // Attempt to promote ro reservation with invalid Id but valid email
        ticketService.reserveSeats(12345, testEmail);
        fail("Exception expected");
    }

    @Test(expected = RuntimeException.class)
    public void reserveSeats_invalidEmail() {
        // Create the hold
        final SeatHold seatHold = ticketService.findAndHoldSeats(testSeatNum, testEmail);
        assertThat(seatHold, notNullValue());

        // Attempt to promote ro reservation with invalid email but valid Id
        final int seatHoldId = seatHold.getId();
        ticketService.reserveSeats(seatHoldId, "invalidEmail");
        fail("Exception expected");
    }

    @Test
    public void cancelSeatHold() {
        // Create the hold
        final SeatHold seatHold = ticketService.findAndHoldSeats(testSeatNum, testEmail);
        assertThat(seatHold, notNullValue());

        // Cancel the hold
        final int seatHoldId = seatHold.getId();
        final boolean success = ticketService.cancelSeatHold(seatHoldId, testEmail);
        assertThat(success, is(true));

        // Verify that the hold is not in the list
        assertThat(ticketService.getHolds().get(seatHoldId), nullValue());
    }

    @Test
    public void cancelSeatHold_invalidId() {
        // Create the hold
        final SeatHold seatHold = ticketService.findAndHoldSeats(testSeatNum, testEmail);
        assertThat(seatHold, notNullValue());

        // Attempt to cancel the hold with invalid Id but valid email
        final boolean success = ticketService.cancelSeatHold(12345, testEmail);
        assertThat(success, is(false));

        // Verify that the hold is not in the list
        final int seatHoldId = seatHold.getId();
        assertThat(ticketService.getHolds().get(seatHoldId), is(seatHold));
    }

    @Test
    public void cancelSeatHold_invalidEmail() {
        // Create the hold
        final SeatHold seatHold = ticketService.findAndHoldSeats(testSeatNum, testEmail);
        assertThat(seatHold, notNullValue());

        // Attempt to cancel the hold with invalid email but valid Id
        final int seatHoldId = seatHold.getId();
        final boolean success = ticketService.cancelSeatHold(seatHoldId, "invalidEmail");
        assertThat(success, is(false));

        // Verify that the hold is not in the list
        assertThat(ticketService.getHolds().get(seatHoldId), is(seatHold));
    }

    @Test
    public void getHold() {
        // Create the hold
        final SeatHold seatHold = ticketService.findAndHoldSeats(testSeatNum, testEmail);
        assertThat(seatHold, notNullValue());

        // Read the hold
        final int seatHoldId = seatHold.getId();
        final SeatHold readSeatHold = ticketService.getHold(seatHoldId, testEmail);

        // Verify
        assertThat(readSeatHold, notNullValue());
        assertThat(readSeatHold, is(seatHold));
    }

    @Test(expected = RuntimeException.class)
    public void getHold_invalidId() {
        // Create the hold
        final SeatHold seatHold = ticketService.findAndHoldSeats(testSeatNum, testEmail);
        assertThat(seatHold, notNullValue());

        // Attempt to read the hold
        ticketService.getHold(12345, testEmail);
    }

    @Test(expected = RuntimeException.class)
    public void getHold_invalidEmail() {
        // Create the hold
        final SeatHold seatHold = ticketService.findAndHoldSeats(testSeatNum, testEmail);
        assertThat(seatHold, notNullValue());

        // Attempt to read the hold
        final int seatHoldId = seatHold.getId();
        ticketService.getHold(seatHoldId, "invalidEmail");
    }
}