package com.scott.robinson.ticketsystem.customer;

import lombok.EqualsAndHashCode;
import lombok.Value;

import java.io.Serializable;

/**
 * Represents a group of continuous seats within a specific row. The seats span from the {@link #firstSeat} to the
 * {@link #lastSeat} including all seats in between.
 */
@EqualsAndHashCode
@Value
final class Seats implements Serializable {

    /**
     * Index of the row within the venue for which these seats exist.
     */
    private final int row;

    /**
     * Index of the first seat for this group of seats within the venue.
     */
    private final int firstSeat;

    /**
     * Index of the last seat for this group of seats within the venue.
     */
    private final int lastSeat;

}
