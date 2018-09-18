package com.scott.robinson.ticketsystem.customer;

/**
 * {@link org.springframework.stereotype.Controller} to provide a REST API to the ticketing system.
 */
interface TicketController {

    /**
     * @return {@link String} summary of the currently available seats.
     */
    String seeAvailableSeats();

    /**
     * @return {@link SeatHold} for the requested number of seats for the provided user
     */
    SeatHold requestSeats(final int numSeats, final String customerEmail);

    /**
     * @return {@link String} confirmation code for the reservation
     */
    String reserveSeats(final int id, final String customerEmail);

    /**
     * @return {@link String} confirmation of the results of the cancellation attempt
     */
    String cancelSeats(final int id, final String customerEmail);

    /**
     * @return {@link SeatHold} associated with the provided id and email
     */
    SeatHold getHold(final int id, final String customerEmail);

}
