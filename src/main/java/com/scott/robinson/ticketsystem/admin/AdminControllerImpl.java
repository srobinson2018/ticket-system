package com.scott.robinson.ticketsystem.admin;

import com.scott.robinson.ticketsystem.customer.SeatHold;
import com.scott.robinson.ticketsystem.customer.SeatReservation;
import com.scott.robinson.ticketsystem.customer.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Implementation of the {@link AdminController} containing endpoints which allow the administrator to gain insights
 * into the state of the ticketing system.
 */
@RestController
@RequestMapping(path = "/admin")
public final class AdminControllerImpl implements AdminController {

    private final TicketService ticketService;

    @Autowired
    AdminControllerImpl(final TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * Returns the internal map of reservations.
     */
    @RequestMapping(path = "/reservations", produces = "application/json")
    @Override
    public Map<String, SeatReservation> showReservations() {
        return ticketService.getReservations();
    }

    /**
     * Returns the internal map of holds.
     */
    @RequestMapping(path = "/holds", produces = "application/json")
    @Override
    public Map<Integer, SeatHold> showHolds() {
        return ticketService.getHolds();
    }
}
