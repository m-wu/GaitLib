
package org.spin.gaitlib.core;

import org.spin.gaitlib.cadence.CadenceState;

/**
 * An instance of GaitData contains a {@link CadenceState} and the name of the current gait, along
 * with the timestamp.
 * 
 * @author Mike
 */
public class GaitData {
    private final CadenceState cadenceState;
    private final String gait;
    /**
     * Unix time timestamp, in nanoseconds.
     */
    private final long timeStamp;

    /**
     * @param cadenceState
     * @param gait
     * @param timeStamp Unix time timestamp, in nanoseconds.
     */
    public GaitData(CadenceState cadenceState, String gait, long timeStamp) {
        this.cadenceState = cadenceState;
        this.gait = gait;
        this.timeStamp = timeStamp;
    }

    public CadenceState getCadenceState() {
        return cadenceState;
    }

    public String getGait() {
        return gait;
    }

    /**
     * @return Unix time timestamp, in nanoseconds.
     */
    public long getTimeStamp() {
        return timeStamp;
    }
}
