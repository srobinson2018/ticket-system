package com.scott.robinson.ticketsystem;

import lombok.Getter;
import org.springframework.context.annotation.Configuration;

/**
 * {@link Configuration} providing the values for the venue size and hold timeout.
 */
@Configuration
@Getter
public class ConfigurationProvider {

    /**
     * Number of rows of seating for the venue. Defaults to {@link #DEFAULT_ROWS}.
     */
    private final int rows;

    /**
     * Number of seats per row. Defaults to {@link #DEFAULT_SEATS_PER_ROW}.
     */
    private final int seatsPerRow;

    /**
     * Number of seconds before a hold becomes invalid. Defaults to {@link #DEFAULT_HOLD_TIMEOUT_SEC}.
     */
    private final int holdTimeoutSeconds;

    public ConfigurationProvider() {
        // Build configurations
        rows = getSystemPropertyOrDefault(VENUE_ROWS, DEFAULT_ROWS);
        seatsPerRow = getSystemPropertyOrDefault(VENUE_SEATS_PER_ROW, DEFAULT_SEATS_PER_ROW);
        holdTimeoutSeconds = getSystemPropertyOrDefault(HOLD_TIMEOUT_SEC, DEFAULT_HOLD_TIMEOUT_SEC);
    }

    /**
     * Checks for the system property or returns the provided default value.
     *
     * @param key The system property key for which to check for the configuration
     * @param defaultValue The default value to use in the case no system property is found
     * @return The system property converted to an {@link Integer} or the default value
     */
    private int getSystemPropertyOrDefault(final String key, final int defaultValue) {
        final String sysProp = System.getProperty(key);
        if (sysProp != null) {
            return Integer.parseInt(sysProp);
        } else {
            return defaultValue;
        }
    }

    private final int DEFAULT_ROWS = 9;
    private final int DEFAULT_SEATS_PER_ROW = 33;
    private final int DEFAULT_HOLD_TIMEOUT_SEC = 60;
    private final String VENUE_ROWS = "venue.rows";
    private final String VENUE_SEATS_PER_ROW = "venue.seats";
    private final String HOLD_TIMEOUT_SEC = "timeout.hold";
}
