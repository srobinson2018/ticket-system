package com.scott.robinson.ticketsystem.customer;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.time.LocalDateTime;

@EqualsAndHashCode
@Getter
abstract class SeatAction {
    /**
     * The user's email address which is used as a secondary key.
     */
    @NonNull
    private final String email;

    /**
     * A representation of the seats being held.
     */
    @NonNull
    private final Seats seats;

    /**
     * The time of creation.
     */
    @NonNull
    private final LocalDateTime creationTime;

    SeatAction(final String email, final Seats seats) {
        this.email = email;
        this.seats = seats;
        this.creationTime = LocalDateTime.now();
    }
}
