package com.scott.robinson.ticketsystem.customer;

import lombok.Getter;
import lombok.NonNull;

import java.util.UUID;

/**
 * Immutable object to represent a group of seats reserved for a specific user.
 */
@Getter
public final class SeatReservation extends SeatAction {

    /**
     * The UUID, primary key which is generated in the constructor.
     */
    @NonNull
    private final String confirmationCode;

    SeatReservation(final String email, final Seats seats) {
        super(email, seats);
        this.confirmationCode = UUID.randomUUID().toString();
    }

}
