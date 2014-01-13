/*
 * Author: Bryan Stern 2011
 * A wrapper class for accelerometer readings
 */

package org.spin.gaitlib.sensor;

import org.spin.gaitlib.GaitAnalysis;

import java.util.concurrent.TimeUnit;

/**
 * An abstract class for signal reading, handles timestamp management.
 * 
 * @author Mike
 */
public abstract class SignalReading {

    /**
     * Amount of time elapsed since {@link GaitAnalysis} instance is created, in nanoseconds.
     */
    private final long timeSinceStart;
    /**
     * {@link #timeSinceStart} in seconds, with precision equivalent to nanosecond.
     */
    private final float timeSinceStartInS;
    /**
     * Timestamp of the reading, measured in Unix time.
     */
    private final long absoluteTime;
    /**
     * Unit of {@link #absoluteTime}.
     */
    private final TimeUnit absoluteTimeUnit;

    /**
     * @param timeSinceStart Amount of time elapsed since {@link GaitAnalysis} instance is created,
     *            in nanoseconds.
     * @param absoluteTime Timestamp of the reading, measured in Unix time.
     * @param absoluteTimeUnit Unit of Unix time.
     */
    public SignalReading(long timeSinceStart, long absoluteTime, TimeUnit absoluteTimeUnit) {
        this.timeSinceStart = timeSinceStart;
        this.timeSinceStartInS = this.timeSinceStart / 1000000000f;
        this.absoluteTime = absoluteTime;
        this.absoluteTimeUnit = absoluteTimeUnit;
    }

    /**
     * @return the time elapsed since the <code>GaitAnalysis</code> instance is created, measured in
     *         nanoseconds.
     */
    public long getTimeSinceStart() {
        return timeSinceStart;
    }

    /**
     * @return the time elapsed since the <code>GaitAnalysis</code> instance is created, measured in
     *         seconds, with precision equivalent to nanosecond.
     */
    public float getTimeSinceStartInS() {
        return this.timeSinceStartInS;
    }

    /**
     * @param unit {@link TimeUnit} to which the timestamp to be converted.
     * @return Unix time timestamp converted to the given {@link TimeUnit}.
     */
    public long getAbsoluteTime(TimeUnit unit) {
        return unit.convert(this.absoluteTime, absoluteTimeUnit);
    }

}
