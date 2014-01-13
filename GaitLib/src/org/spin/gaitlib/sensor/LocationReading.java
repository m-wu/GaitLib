
package org.spin.gaitlib.sensor;

import android.location.Location;

import org.spin.gaitlib.GaitAnalysis;

import java.util.concurrent.TimeUnit;

public class LocationReading extends SignalReading {

    private final Location location;

    /**
     * @param timeSinceStart Amount of time elapsed since {@link GaitAnalysis} instance is created,
     *            in nanoseconds.
     * @param absoluteTime Timestamp of the reading, measured in Unix time.
     * @param absoluteTimeUnit Unit of Unix time.
     */
    public LocationReading(Location location, long timeSinceStart, long absoluteTime,
            TimeUnit absoluteTimeUnit) {
        super(timeSinceStart, absoluteTime, absoluteTimeUnit);
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

}
