
package org.spin.gaitlib.gait;

/**
 * An interface for listeners that respond to gait classifier model loading events, including start
 * and end, success or fail.
 * 
 * @author Mike
 */
public interface IClassifierModelLoadingListener {

    public void onLoadingStart();

    public void onModelLoaded(boolean success);
}
