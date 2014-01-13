
package org.spin.gaitlib.core;

/**
 * Interface for listeners that receives notification when current gait is updated.
 * 
 * @author Mike
 */
public interface IGaitUpdateListener {

    /**
     * @param data {@link GaitData} that contains cadence information (cadence, stride length,
     *            speed) and classified gait.
     */
    public void onGaitUpdated(GaitData data);

}
