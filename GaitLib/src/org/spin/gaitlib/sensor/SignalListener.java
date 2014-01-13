
package org.spin.gaitlib.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import org.spin.gaitlib.GaitAnalysis;
import org.spin.gaitlib.core.ILoggable;
import org.spin.gaitlib.util.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A listener that can listen for events from accelerometer, gyroscope and location service. It
 * caches recent sensor reading that are within a time period set by <code>windowSize</code>.
 */
public class SignalListener implements SensorEventListener, LocationListener, ILoggable {

    private static final String TAG = "SignalListener";

    /**
     * Time at which {@link SignalListener} is created, measured in nanoseconds.
     */
    private final long startTime;

    /**
     * Window size measured in milliseconds. The default value is set by {@link GaitAnalysis} class;
     * it can be changed on the fly by calling {@link #setWindowSize(int)}.
     */
    private int windowSize = GaitAnalysis.DEFAULT_WINDOW_SIZE_MS;

    /**
     * A buffer for {@link LocationReading}'s.
     */
    private final ArrayList<LocationReading> locs = new ArrayList<LocationReading>();

    /**
     * A buffer for accelerometer readings.
     */
    private final ArrayList<ThreeAxisSensorReading> accel = new ArrayList<ThreeAxisSensorReading>();

    /**
     * A buffer for gyroscope readings.
     */
    private final ArrayList<ThreeAxisSensorReading> gyro = new ArrayList<ThreeAxisSensorReading>();

    private final Logger accelReadingLogger = new Logger(null, ".acsv",
            "TimeSinceStart(ms), x, y, z");

    public SignalListener() {
        startTime = System.nanoTime();
    }

    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                addAccelReading(createThreeAxisReading(event.values[0], event.values[1],
                        event.values[2], event.timestamp, TimeUnit.NANOSECONDS));
                break;
            case Sensor.TYPE_GYROSCOPE:
                addGyroReading(createThreeAxisReading(event.values[0], event.values[1],
                        event.values[2], event.timestamp, TimeUnit.NANOSECONDS));
                break;
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void onLocationChanged(Location location) {
        addLocationReading(createLocationReading(location));
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    public void onProviderEnabled(String provider) {

    }

    public void onProviderDisabled(String provider) {

    }

    private ThreeAxisSensorReading createThreeAxisReading(float x, float y, float z,
            long timestamp, TimeUnit timestampUnit) {
        return new ThreeAxisSensorReading(x, y, z, getTimeSinceStart(timestamp, timestampUnit),
                timestamp, timestampUnit);
    }

    private LocationReading createLocationReading(Location location) {
        long timestamp = location.getTime(); // TODO: Check for correctness
        TimeUnit timestampUnit = TimeUnit.MILLISECONDS;
        return new LocationReading(location, getTimeSinceStart(timestamp, timestampUnit),
                timestamp, timestampUnit);
    }

    /**
     * @param currentNanoTime
     * @return current time since signal listener is created, measured in nanoseconds.
     */
    public long getTimeSinceStart(long currentTime, TimeUnit currentTimeUnit) {
        if (!TimeUnit.NANOSECONDS.equals(currentTimeUnit)) {
            currentTime = TimeUnit.NANOSECONDS.convert(currentTime, currentTimeUnit);
        }
        return currentTime - this.startTime;
    }

    private void addAccelReading(ThreeAxisSensorReading reading) {
        if (reading != null) {
            accel.add(reading);
            trimToWindowSize(accel, windowSize);
            accelReadingLogger.printRow(reading.getStringArray());
        }
    }

    private void addGyroReading(ThreeAxisSensorReading reading) {
        if (reading != null) {
            gyro.add(reading);
            trimToWindowSize(gyro, windowSize);
        }
    }

    private void addLocationReading(LocationReading location) {
        locs.add(location);
        trimToWindowSize(locs, windowSize);
    }

    private void trimToWindowSize(List<? extends SignalReading> readings, int windowSizeMS) {

        long trimmedWindowStartTimestamp = readings.get(readings.size() - 1).getAbsoluteTime(
                TimeUnit.MILLISECONDS) - windowSizeMS;

        int endIndex = 0;
        for (SignalReading r : readings) {
            if (r.getAbsoluteTime(TimeUnit.MILLISECONDS) < trimmedWindowStartTimestamp) {
                endIndex++;
            } else {
                break;
            }
        }
        if (endIndex > 0) {
            // Remove elements that are outside the range we are looking at
            readings.subList(0, endIndex).clear();
        }
    }

    public List<ThreeAxisSensorReading> getAccelReadings() {
        List<ThreeAxisSensorReading> accelCopy = new ArrayList<ThreeAxisSensorReading>(
                accel);
        removeNullElements(accelCopy); // sometimes the last few elements of the list could be null.
        return accelCopy;
    }

    private List<? extends SignalReading> removeNullElements(List<? extends SignalReading> list) {
        int lastIndex = list.size() - 1;
        if (list.get(lastIndex) == null) {
            list.remove(lastIndex);
            return removeNullElements(list);
        }
        return list;
    }

    public ThreeAxisSensorReading[] getAccelReadingsArray() {
        List<ThreeAxisSensorReading> accelCopy = getAccelReadings();
        ThreeAxisSensorReading[] array = accelCopy.toArray(new ThreeAxisSensorReading[accelCopy
                .size()]);
        return array;
    }

    public ArrayList<ThreeAxisSensorReading> getGyroReadings() {
        return this.gyro;
    }

    public ArrayList<LocationReading> getLocations() {
        return this.locs;
    }

    /**
     * @return the time this <code>SignalListener</code> was created, measured in Unix time in
     *         milliseconds.
     */
    public long getStartTime() {
        return this.startTime;
    }

    /**
     * @return the window size for data used in gait and cadence analysis, measured in millisecond.
     */
    public int getWindowSize() {
        return windowSize;
    }

    /**
     * @param windowSize the window size in millisecond.
     */
    public void setWindowSize(int windowSize) {
        this.windowSize = windowSize;
    }

    public void updateLocation(Location loc) {
        locs.add(createLocationReading(loc));
    }

    public void clearLogs() {
        accel.clear();
        locs.clear();
    }

    public void setLoggingEnabled(boolean enabled) {
        accelReadingLogger.setEnabled(enabled);
    }

    public boolean isLogging() {
        return accelReadingLogger.isEnabled();
    }

    /*
     * Methods below are inherited from the original implementation of GaitLib. Logging is now
     * handled by org.spin.gaitlib.util.Logger.
     */

    public void writeLocationsToFile() {
        this.writeDataFile(locs, "locs");
    }

    public void writeAccelDataToFile() {
        this.writeDataFile(accel, "accel");
    }

    public void writeFasperDataToFile(float[][] data) {
        ArrayList<FasperPoint> results = new ArrayList<FasperPoint>();

        for (int i = 0; i < data.length; i++) {
            results.add(new FasperPoint(data[i]));
        }

        writeDataFile(results, "fasper");
    }

    public void writeDataFile(@SuppressWarnings("rawtypes")
    ArrayList objs, String prefix) {
        writeDataFile(objs, prefix, null);
    }

    public void writeDataFile(@SuppressWarnings("rawtypes")
    ArrayList objs, String prefix, String[] header) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), prefix);
            root.mkdirs();
            if (root.canWrite()) {
                DateFormat dateFormat = new SimpleDateFormat("yyyyMMMdd-HH-mm-ss");
                Date date = new Date();
                File gpxfile = new File(root, "log_" + dateFormat.format(date) + ".txt");
                Log.v(TAG, "Writing: " + gpxfile.getName());
                FileWriter gpxwriter = new FileWriter(gpxfile);
                BufferedWriter out = new BufferedWriter(gpxwriter);

                // write header first
                if (header != null) {
                    for (String s : header) {
                        out.write(s + "\n");
                    }
                }

                // now write data
                for (Object obj : objs) {
                    out.write(obj + "\n");
                }
                out.close();
                Log.v(TAG, "Wrote to file:" + gpxfile.getAbsolutePath());
            } else {
                Log.v(TAG, "Can't Write to File");
            }
        } catch (IOException e) {
            Log.v("INFO", "Could not write file " + e.getMessage());
        }
    }

    class FasperPoint {
        private final float freq;
        private final float power;

        public FasperPoint(float[] arr) {
            freq = arr[0];
            power = arr[1];
        }

        @Override
        public String toString() {
            return freq + "," + power;
        }
    }

}
