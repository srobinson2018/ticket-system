package com.scott.robinson.ticketsystem.admin;

import com.scott.robinson.ticketsystem.customer.SeatHold;
import com.scott.robinson.ticketsystem.customer.SeatReservation;

import java.util.Map;

/**
 * Administrative functionality to provide insight into the current state of the ticketing system.
 */
public interface AdminController {

    /**
     * The current seat reservations.
     */
    Map<String, SeatReservation> showReservations();

    /**
     * The current seat holds.
     */
    Map<Integer, SeatHold> showHolds();
}
