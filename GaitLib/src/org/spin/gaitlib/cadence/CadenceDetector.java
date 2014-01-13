
package org.spin.gaitlib.cadence;

import android.location.Location;
import android.util.Log;

import org.spin.gaitlib.GaitAnalysis;
import org.spin.gaitlib.core.ILoggable;
import org.spin.gaitlib.filter.FilterNotSetException;
import org.spin.gaitlib.filter.IFilter;
import org.spin.gaitlib.filter.IFilterable;
import org.spin.gaitlib.sensor.SignalListener;
import org.spin.gaitlib.util.Logger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Abstract class for cadence estimation.
 * 
 * @author Mike
 */
public abstract class CadenceDetector implements IFilterable, ILoggable {

    private static final String TAG = "CadenceDetector";
    private final ArrayBlockingQueue<CadenceState> filterBuffer;
    private final int filterBufferSize = 3;
    private SignalListener signalListener;
    private CadenceState currentCadenceState;
    private IFilter filter;
    private final Logger cadenceResultLogger = new Logger(null, ".ccsv",
            "TimeSinceStart(ms), cadence, speed, cadence confidence");

    public CadenceDetector() {
        currentCadenceState = new CadenceState(1, 0, 0, 0, 0);
        filterBuffer = new ArrayBlockingQueue<CadenceState>(filterBufferSize);
    }

    /**
     * Update the current cadence state.
     */
    public void updateCadenceState() {
        CadenceState cadenceState = estimateCadence(signalListener);
        setCurrentCadenceState(cadenceState);
    }

    /**
     * Perform calculation and return the current CadenceState.
     */
    protected abstract CadenceState estimateCadence(SignalListener signalListener);

    protected void updateLocation(Location loc) {
        float speed = loc.getSpeed();
        float cadence = currentCadenceState.getCadence();
        float cadenceConfidence = currentCadenceState.getCadenceConfidence();

        long nanoTime = System.nanoTime();
        long timeSinceStart = signalListener.getTimeSinceStart(nanoTime, TimeUnit.NANOSECONDS);
        setCurrentCadenceState(new CadenceState(cadence, speed, cadenceConfidence, nanoTime,
                timeSinceStart));
    }

    public void setSignalListener(SignalListener signalListener) {
        this.signalListener = signalListener;
    }

    public void setFilter(IFilter filter) {
        this.filter = filter;
    }

    public IFilter getFilter() {
        return filter;
    }

    /**
     * See {@link GaitAnalysis#getCadence(boolean)}.
     */
    public float getCadence(boolean filtered) throws FilterNotSetException {
        if (!filtered) {
            return currentCadenceState.getCadence();
        } else {
            if (filter == null) {
                throw new FilterNotSetException();
            }
            float[] bufferedValues = new float[filterBufferSize];
            int i = 0;
            for (CadenceState c : filterBuffer) {
                bufferedValues[i++] = c.getCadence();
            }
            return filter.getFilteredValue(bufferedValues);
        }
    }

    /**
     * See {@link GaitAnalysis#getStrideLength(boolean)}.
     */
    public float getStrideLength(boolean filtered) throws FilterNotSetException {
        return getSpeed() / getCadence(filtered);
    }

    /**
     * See {@link GaitAnalysis#getSpeed()}.
     */
    public float getSpeed() {
        return currentCadenceState.getSpeed();
    }

    /**
     * See {@link GaitAnalysis#getCadenceConfidence()}.
     */
    public float getCadenceConfidence() {
        return currentCadenceState.getCadenceConfidence();
    }

    public CadenceState getCurrentCadenceState() {
        return currentCadenceState;
    }

    private void setCurrentCadenceState(CadenceState currentCadenceState) {
        this.currentCadenceState = currentCadenceState;

        if (filterBuffer.remainingCapacity() == 0) {
            filterBuffer.poll();
        }
        try {
            filterBuffer.put(currentCadenceState);
        } catch (InterruptedException e) {
            Log.v(TAG, e.toString());
        }

        cadenceResultLogger.printRow(currentCadenceState.getStringArray());
    }

    public void setLoggingEnabled(boolean enabled) {
        cadenceResultLogger.setEnabled(enabled);
    }

    public boolean isLogging() {
        return cadenceResultLogger.isEnabled();
    }
}
