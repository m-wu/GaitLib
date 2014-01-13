
package org.spin.gaitlib;

import android.util.Log;

import org.spin.gaitlib.cadence.CadenceDetector;
import org.spin.gaitlib.cadence.DefaultCadenceDetector;
import org.spin.gaitlib.core.GaitData;
import org.spin.gaitlib.core.IGaitUpdateListener;
import org.spin.gaitlib.core.ILoggable;
import org.spin.gaitlib.filter.FilterNotSetException;
import org.spin.gaitlib.filter.IFilter;
import org.spin.gaitlib.gait.DefaultGaitClassifier;
import org.spin.gaitlib.gait.GaitClassifier;
import org.spin.gaitlib.gait.IClassifierModelLoadingListener;
import org.spin.gaitlib.sensor.SignalListener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * GaitAnalysis is the main class for using GaitLib. GaitUpdateListeners can be registered here to
 * receive notification when new information is computed. Sampling window size and sampling interval
 * can be set when starting gait analysis.
 * 
 * @author Mike
 */
public class GaitAnalysis {

    private static final String TAG = "GaitAnalysis";

    /**
     * Default size of the window used for sensor data processing, measured in milliseconds.
     */
    public static final int DEFAULT_WINDOW_SIZE_MS = 4000;

    /**
     * Default interval between instances of data processing to update cadence and gait.
     */
    public static final int DEFAULT_SAMPLING_INTERVAL_MS = 1000;

    private final CadenceDetector cadenceDetector;
    private final GaitClassifier gaitClassifier;
    private final SignalListener signalListener = new SignalListener();
    private final Set<IGaitUpdateListener> gaitUpdateListeners = new HashSet<IGaitUpdateListener>();
    private final Set<ILoggable> loggables = new HashSet<ILoggable>();
    private boolean isGaitAnalysisRunning = false;
    private boolean isLoggingEnabled = false;
    private Timer mTimer;

    /**
     * Set up GaitAnalysis with the default Cadence Detector, the default Gait Classifier using the
     * default model file.
     */
    public GaitAnalysis() {
        this(getDefaultCadenceDetector(), getDefaultGaitClassifier());
    }

    /**
     * Set up GaitAnalysis with defined Cadence Detector and Gait Classifier.
     * 
     * @param cadenceDetector the CadenceDetector to use; <code>null</code> if not to detect
     *            cadence.
     * @param gaitClassifier the GaitClassifier to use; <code>null</code> if not to classify gait.
     */
    public GaitAnalysis(CadenceDetector cadenceDetector, GaitClassifier gaitClassifier) {
        this.cadenceDetector = cadenceDetector;
        this.gaitClassifier = gaitClassifier;

        if (cadenceDetector != null) {
            cadenceDetector.setSignalListener(signalListener);
            loggables.add(cadenceDetector);
        }
        if (gaitClassifier != null) {
            gaitClassifier.setSignalListener(signalListener);
            loggables.add(gaitClassifier);
        }
        loggables.add(signalListener);
    }

    public static CadenceDetector getDefaultCadenceDetector() {
        return new DefaultCadenceDetector();
    }

    public static GaitClassifier getDefaultGaitClassifier() {
        return new DefaultGaitClassifier();
    }

    /**
     * @param modelFileLocation
     * @param modelXMLFileLocation
     * @return a new instance of the default gait classifier with a defined model.
     */
    public static GaitClassifier getDefaultGaitClassifier(String modelFileLocation,
            String modelXMLFileLocation) {
        return new DefaultGaitClassifier(modelFileLocation, modelXMLFileLocation);
    }

    /**
     * @param filtered whether to apply defined filter for measuring cadence
     * @return the current cadence value
     * @throws FilterNotSetException if trying to get filtered result without setting a filter first
     *             by calling {@link #setCadenceFilter(IFilter)}.
     */
    public float getCadence(boolean filtered) throws FilterNotSetException {
        return cadenceDetector.getCadence(filtered);
    }

    /**
     * @param filtered whether to apply defined filter to cadence value during calculation.
     * @return the current stride length
     * @throws FilterNotSetException if trying to get filtered result without setting a filter first
     *             by calling {@link #setCadenceFilter(IFilter)}.
     */
    public float getStrideLength(boolean filtered) throws FilterNotSetException {
        return cadenceDetector.getStrideLength(filtered);
    }

    /**
     * @return the speed at which the user is moving
     */
    public float getSpeed() {
        return cadenceDetector.getSpeed();
    }

    /**
     * @return the confidence level of the latest cadence calculation
     */
    public float getCadenceConfidence() {
        return cadenceDetector.getCadenceConfidence();
    }

    /**
     * @return the latest result of gait classification
     */
    public String getCurrentGait() {
        return gaitClassifier.getCurrentGait();
    }

    /**
     * @return a list of all gaits among which to classify
     */
    public List<String> getDefinedGaits() {
        return gaitClassifier.getAllGaits();
    }

    public SignalListener getSignalListener() {
        return signalListener;
    }

    /**
     * @param filter the filter to be applied to cadence measurements
     */
    public void setCadenceFilter(IFilter filter) {
        cadenceDetector.setFilter(filter);
    }

    public boolean shouldDetectCadence() {
        return cadenceDetector != null;
    }

    public boolean shouldClassifyGait() {
        return gaitClassifier != null && gaitClassifier.isModelLoaded();
    }

    /**
     * Register a GaitUpdateListener to receive gait update events.
     * 
     * @param listener implements <code>IGaitUpdateListener</code>
     * @return <code>true</code> if the listener is added successfully; <code>false</code>
     *         otherwise.
     */
    public boolean registerGaitUpdateListener(IGaitUpdateListener listener) {
        return gaitUpdateListeners.add(listener);
    }

    /**
     * Remove a GaitUpdateListener to stop receiving gait update events.
     * 
     * @param listener the listener to be removed
     * @return <code>true</code> if the listener is removed successfully; <code>false</code>
     *         otherwise.
     */
    public boolean removeGaitUpdateListener(IGaitUpdateListener listener) {
        return gaitUpdateListeners.remove(listener);
    }

    private void notifyGaitUpdateListeners() {
        GaitData data = new GaitData(cadenceDetector.getCurrentCadenceState(),
                gaitClassifier.getCurrentGait(), cadenceDetector.getCurrentCadenceState()
                        .getTimestamp());
        for (IGaitUpdateListener listener : gaitUpdateListeners) {
            listener.onGaitUpdated(data);
        }
    }

    /**
     * Start gait analysis with default sampling window size and default sampling interval.
     */
    public void startGaitAnalysis() {
        startGaitAnalysis(DEFAULT_WINDOW_SIZE_MS, DEFAULT_SAMPLING_INTERVAL_MS);
    }

    /**
     * Start gait analysis with user defined sampling window size and sampling interval.
     * 
     * @param windowSize the length of time in which data is used for gait analysis, measured in
     *            milliseconds.
     * @param samplingInterval the length of time between consecutive updates, measured in
     *            milliseconds.
     */
    public void startGaitAnalysis(int windowSize, int samplingInterval) {
        signalListener.setWindowSize(windowSize);
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                boolean shouldClassifyGait = shouldClassifyGait();
                boolean shouldDetectCadence = shouldDetectCadence();

                if (shouldDetectCadence) {
                    cadenceDetector.updateCadenceState();
                }
                if (shouldClassifyGait) {
                    try {
                        gaitClassifier.classifyGait();
                    }
                    catch (Exception e) {
                        Log.v(TAG, e.toString());
                    }
                }
                if (shouldClassifyGait || shouldDetectCadence) {
                    notifyGaitUpdateListeners();
                }
            }
        }, windowSize, samplingInterval);
        setGaitAnalysisRunning(true);
    }

    /**
     * Stop gait analysis
     */
    public void stopGaitAnalysis() {
        if (isGaitAnalysisRunning()) {
            mTimer.cancel();
            mTimer.purge();
            setGaitAnalysisRunning(false);
            setLoggingEnabled(false);
        }
    }

    /**
     * Enable or disable logging for GaitLib.
     * 
     * @param enabled <code>true</code> to enable, <code>false</code> to disable.
     */
    public void setLoggingEnabled(boolean enabled) {
        for (ILoggable loggable : loggables) {
            if (loggable != null) {
                loggable.setLoggingEnabled(enabled);
            }
        }
        isLoggingEnabled = enabled;
    }

    /**
     * @return <code>true</code> if user has enabled logging, <code>false</code> otherwise.
     */
    public boolean isLoggingEnabled() {
        return isLoggingEnabled;
    }

    /**
     * @return <code>true</code> if all logging service is working properly, <code>false</code>
     *         otherwise.
     */
    public boolean isLogging() {
        boolean isLogging = true;
        for (ILoggable loggable : loggables) {
            isLogging &= loggable.isLogging();
        }
        return isLogging;
    }

    public boolean isGaitAnalysisRunning() {
        return isGaitAnalysisRunning;
    }

    /**
     * Set a flag to determine whether gait analysis is running at the moment.
     * 
     * @param isGaitAnalysisRunning
     */
    private void setGaitAnalysisRunning(boolean isGaitAnalysisRunning) {
        this.isGaitAnalysisRunning = isGaitAnalysisRunning;
    }

    /**
     * Register listener to receive updates during the gait classifier model loading process,
     * including start and end, success or fail.
     * 
     * @param listener
     */
    public void addGaitClassifierModelLoadingListener(IClassifierModelLoadingListener listener) {
        gaitClassifier.addModelLoadingListener(listener);
    }

    public void removeClassifierModelLoadingListener(IClassifierModelLoadingListener listener) {
        gaitClassifier.removeModelLoadingListener(listener);
    }

}
