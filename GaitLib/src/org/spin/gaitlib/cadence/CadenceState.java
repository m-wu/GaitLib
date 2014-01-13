/*
 * Author: Bryan Stern 2011
 * This is wrapper class for the current gait state that is returned by the GaitAnalysis class.
 */

package org.spin.gaitlib.cadence;

import java.util.concurrent.TimeUnit;

/**
 * A CadenceState contains cadence, speed, confidence of cadence calculation and the timestamp of
 * the cadence state.
 * 
 * @author Mike
 */
public class CadenceState {

    private final float cadence;
    private final float speed;
    private final float cadenceConfidence;
    private final long timestamp;
    private final long timeSinceStart;

    /**
     * @param cadence
     * @param speed
     * @param cadenceConfidence
     * @param timestamp current system time in nanoseconds
     * @param timeSinceStart time since start, in nanoseconds.
     */
    public CadenceState(float cadence, float speed, float cadenceConfidence,
            long timestamp, long timeSinceStart) {
        this.cadence = cadence;
        this.speed = speed;
        this.cadenceConfidence = cadenceConfidence;
        this.timestamp = timestamp;
        this.timeSinceStart = timeSinceStart;
    }

    /**
     * @return unfiltered cadence.
     */
    public float getCadence() {
        return this.cadence;
    }

    /**
     * @return un-filtered cadence.
     */
    public float getStrideLength() {
        return this.speed / this.cadence;
    }

    public float getSpeed() {
        return this.speed;
    }

    public float getCadenceConfidence() {
        return this.cadenceConfidence;
    }

    /**
     * @return timestamp in nanoseconds
     */
    public long getTimestamp() {
        return this.timestamp;
    }

    /**
     * @return time since start, in nanoseconds.
     */
    public long getTimeSinceStart() {
        return this.timeSinceStart;
    }

    /**
     * @return an array consisted of time since start (in milliseconds), cadence, speed, and cadence
     *         confidence.
     */
    public String[] getStringArray() {
        String[] array = new String[4];
        array[0] = String.valueOf(TimeUnit.MILLISECONDS.convert(timeSinceStart,
                TimeUnit.NANOSECONDS));
        array[1] = String.valueOf(cadence);
        array[2] = String.valueOf(speed);
        array[3] = String.valueOf(cadenceConfidence);
        return array;
    }

    @Override
    public String toString() {
        Float confidence = Float.valueOf(this.cadenceConfidence);
        return String.valueOf(timestamp) + "," + String.valueOf(cadence) + ","
                + String.valueOf(getStrideLength()) + "," + String.valueOf(speed) + ","
                + confidence.toString();
    }
}
