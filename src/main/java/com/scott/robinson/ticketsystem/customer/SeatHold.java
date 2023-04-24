package com.scott.robinson.ticketsystem.customer;

import java.security.SecureRandom;
import lombok.Getter;

import java.util.Random;

/**
 * Immutable object to represent a group of seats being held for a specific user.
 */
@Getter
public final class SeatHold extends SeatAction {

    /**
     * The random int, primary key of this object generated in the constructor
     */
    private final int id;

    SeatHold(final String email, final Seats seats) {
        super(email, seats);
        this.id = new SecureRandom().nextInt(9999999) + 1;
    }

}
