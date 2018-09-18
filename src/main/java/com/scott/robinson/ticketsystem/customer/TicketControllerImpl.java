package com.scott.robinson.ticketsystem.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * NOTE: [SR] This REST controller is primarily for demo purposes and would be more complete if it were to be put into
 * production code. The objective of this class is to allow code reviewers some additional plugs to test the
 * functionality of the underlying {@link TicketService}.
 *
 * <p>This {@link RestController} contains the endpoints a customer would use to view, request, reserve, or cancel a
 * seat hold.
 */
@RestController
@RequestMapping("/tickets")
final class TicketControllerImpl implements TicketController {

    private final TicketService ticketService;

    @Autowired
    TicketControllerImpl(final TicketService ticketService) {
        this.ticketService = ticketService;
    }

    /**
     * Builds and returns the number of seats available and a {@link String} map representation of the venue.
     *
     * @return {@link String} representation of the venue containing both the total seats available and a seat map.
     */
    @RequestMapping(path = "/map", method = RequestMethod.GET, produces = "text/plain")
    @Override
    public String seeAvailableSeats() {
        try {
            final int seatsAvailable = ticketService.numSeatsAvailable();
            final String seatMap = ticketService.seatMap();
            return "Seats Available: " + seatsAvailable + "\n\n" + seatMap;
        } catch (RuntimeException e) {
            return errorMessage(e.getMessage());
        }
    }

    /**
     * Finds and holds the best available seats.
     *
     * @param numSeats Number of seats requested
     * @param customerEmail User's email address
     * @return {@link SeatHold} representing the seat hold for the user
     */
    @RequestMapping(path = "/request", method = RequestMethod.GET, produces = "application/json")
    @Override
    public SeatHold requestSeats(@RequestParam final int numSeats, @RequestHeader(name = "customer") final String customerEmail) {
        return ticketService.findAndHoldSeats(numSeats, customerEmail);
    }

    /**
     * Promotes a specific seat hold to a reservation.
     *
     * @param id The Id of the seat hold
     * @param customerEmail The user's email address
     * @return {@link String} confirmation code
     */
    @RequestMapping(path = "/{id}/reserve", method = RequestMethod.POST, produces = "text/plain")
    @Override
    public String reserveSeats(@PathVariable final int id, @RequestHeader(name = "customer") final String customerEmail) {
        try {
            final String confirmation = ticketService.reserveSeats(id, customerEmail);
            return confirmation == null ?
                    "Failed to confirm reservation [" + id + "]. Please check your email and try again!" :
                    "Your reservation was successful. Confirmation code: " + confirmation;
        } catch (RuntimeException e) {
            return errorMessage(e.getMessage());
        }

    }

    /**
     * Cancel a specific seat hold.
     *
     * @param id The Id of the seat hold
     * @param customerEmail The user's email address
     * @return The result of the cancellation
     */
    @RequestMapping(path = "/{id}/cancel", method = RequestMethod.DELETE, produces = "text/plain")
    @Override
    public String cancelSeats(@PathVariable final int id, @RequestHeader(name = "customer") final String customerEmail) {
        try {
            final boolean canceled = ticketService.cancelSeatHold(id, customerEmail);
            return canceled ?
                    "Successfully canceled reservation [" + id + "]. Thank you!" :
                    "Failed to cancel reservation [" + id + "]. Please check your reservation id and try again!";
        } catch (RuntimeException e) {
            return errorMessage(e.getMessage());
        }
    }

    /**
     * Get a specific seat hold.
     *
     * @param id The Id of the seat hold
     * @param customerEmail The user's email address
     * @return The associated {@link SeatHold}
     */
    @RequestMapping(path = "/{id}", method = RequestMethod.GET, produces = "application/json")
    @Override
    public SeatHold getHold(@PathVariable final int id, @RequestHeader(name = "customer") final String customerEmail) {
        return ticketService.getHold(id, customerEmail);
    }

    /**
     * Wrap an exception nicely for requests that produce text/plain.
     */
    private String errorMessage(String reason) {
        return "Request failed due to the following: " + reason;
    }
}
