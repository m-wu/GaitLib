
package org.spin.gaitlib.sensor;

import org.spin.gaitlib.GaitAnalysis;

import java.util.concurrent.TimeUnit;

/**
 * Signal reading that includes a set of three values.
 */
public class ThreeAxisSensorReading extends SignalReading {

    private final float x, y, z;

    /**
     * @param timeSinceStart Amount of time elapsed since {@link GaitAnalysis} instance is created,
     *            in nanoseconds.
     * @param absoluteTime Timestamp of the reading, measured in Unix time.
     * @param absoluteTimeUnit Unit of Unix time.
     */
    public ThreeAxisSensorReading(float signalX, float signalY, float signalZ, long timeSinceStart,
            long absoluteTime, TimeUnit absoluteTimeUnit) {
        super(timeSinceStart, absoluteTime, absoluteTimeUnit);
        this.x = signalX;
        this.y = signalY;
        this.z = signalZ;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getZ() {
        return this.z;
    }

    /**
     * @return A 5-element array, containing the Unix time in milliseconds, time since
     *         {@link GaitAnalysis} instance is created in nanoseconds, and the 3 sensor values.
     */
    public String[] getStringArray() {
        String[] array = new String[4];
        array[0] = String.valueOf(TimeUnit.MILLISECONDS.convert(getTimeSinceStart(),
                TimeUnit.NANOSECONDS));
        array[1] = String.valueOf(x);
        array[2] = String.valueOf(y);
        array[3] = String.valueOf(z);
        return array;
    }

    /**
     * @return "[timeSinceStart], [x], [y], [x]"
     */
    @Override
    public String toString() {
        return String.valueOf(this.getTimeSinceStartInS()) + "," + String.valueOf(this.x) + ","
                + String.valueOf(this.y) + ","
                + String.valueOf(this.z);
    }

}
