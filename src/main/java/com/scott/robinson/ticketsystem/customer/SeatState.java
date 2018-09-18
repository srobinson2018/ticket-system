package com.scott.robinson.ticketsystem.customer;

import lombok.Getter;

/**
 * {@link Enum} used to represent a seat in the venue.
 */
enum SeatState {
    OPEN('O'),
    HELD('H'),
    RESERVED('R');

    @Getter
    final char abbreviation;

    SeatState(final char abbreviation) {
        this.abbreviation = abbreviation;
    }
}
