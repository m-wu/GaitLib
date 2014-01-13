
package org.spin.gaitlib.cadence;

import org.spin.gaitlib.sensor.LocationReading;
import org.spin.gaitlib.sensor.SignalListener;
import org.spin.gaitlib.sensor.ThreeAxisSensorReading;
import org.spin.gaitlib.util.SpectralAnalyses;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * A concrete implementation of {@link CadenceDetector}, using algorithm by Idin Karuei.
 * 
 * @author Idin
 * @author Mike
 */
public class DefaultCadenceDetector extends CadenceDetector {

    private static final float ACCEL_RANGE_THRESHOLD = 10;

    public DefaultCadenceDetector() {
        super();
    }

    @Override
    protected CadenceState estimateCadence(SignalListener signalListener) {
        long nanoTime = System.nanoTime();
        long timeSinceStart = signalListener.getTimeSinceStart(nanoTime, TimeUnit.NANOSECONDS);

        float[] signalX, signalY, signalZ, signalTime;
        float hifac = (float) 0.25;
        float ofac = 4;

        ThreeAxisSensorReading[] accelArr = signalListener.getAccelReadingsArray();

        signalX = new float[accelArr.length];
        signalY = new float[accelArr.length];
        signalZ = new float[accelArr.length];
        signalTime = new float[accelArr.length];

        Float maxX = Float.MAX_VALUE * -1;
        Float minX = Float.MAX_VALUE;
        Float maxY = Float.MAX_VALUE * -1;
        Float minY = Float.MAX_VALUE;
        Float maxZ = Float.MAX_VALUE * -1;
        Float minZ = Float.MAX_VALUE;

        for (int i = 0; i < accelArr.length; i++) {
            float x = accelArr[i].getX();
            float y = accelArr[i].getY();
            float z = accelArr[i].getZ();

            signalX[i] = x;
            signalY[i] = y;
            signalZ[i] = z;
            signalTime[i] = accelArr[i].getTimeSinceStartInS();

            if (x > maxX) {
                maxX = x;
            }
            if (x < minX) {
                minX = x;
            }
            if (y > maxY) {
                maxY = y;
            }
            if (y < minY) {
                minY = y;
            }
            if (z > maxZ) {
                maxZ = z;
            }
            if (z < minZ) {
                minZ = z;
            }
        }

        if (maxX - minX < ACCEL_RANGE_THRESHOLD && maxY - minY < ACCEL_RANGE_THRESHOLD
                && maxZ - minZ < ACCEL_RANGE_THRESHOLD) {
            // resting state
            return new CadenceState(0, 0, 1, nanoTime, timeSinceStart);
        }

        float[][] fasperResults = SpectralAnalyses.fasperArray(hifac, ofac, signalX, signalY,
                signalZ, signalTime, signalX.length);

        CadenceState currentCadenceState = getCurrentCadenceState();

        float cadence = SpectralAnalyses.fasperResultsMaxFreq(fasperResults);
        float speed = currentCadenceState.getSpeed();
        int indexLastLocation = signalListener.getLocations().size() - 1;
        if (indexLastLocation >= 0) {
            ArrayList<LocationReading> locs = signalListener.getLocations();
            speed = locs.get(indexLastLocation).getLocation().getSpeed();
        }
        float cadenceConfidence = SpectralAnalyses.confidence(fasperResults);

        return new CadenceState(cadence, speed, cadenceConfidence, nanoTime, timeSinceStart);
    }
}
