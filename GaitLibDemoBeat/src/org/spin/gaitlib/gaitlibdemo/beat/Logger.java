
package org.spin.gaitlib.gaitlibdemo.beat;

import java.util.ArrayList;
import java.util.List;

public class Logger {

    private List<Long> timeStamps = new ArrayList<Long>();
    private List<Float> cadences = new ArrayList<Float>();
    private List<Float> speeds = new ArrayList<Float>();
    private List<Float> strideLengths = new ArrayList<Float>();
    private List<Float> confidences = new ArrayList<Float>();
    private List<String> gaits = new ArrayList<String>();

    public void addTimeStamp(long input) {
        timeStamps.add(input);
    }

    public void addCadence(float input) {
        cadences.add(input);
    }

    public void addSpeed(float input) {
        speeds.add(input);
    }

    public void addStrideLength(float input) {
        strideLengths.add(input);
    }

    public void addConfidence(float input) {
        confidences.add(input);
    }

    public void addGait(String input) {
        if (input != null) {
            gaits.add(input);
        }
    }

    public long[] getTimeStamps() {
        return convertToLongArray(timeStamps);
    }

    public double[] getCadences() {
        return convertToDoubleArray(cadences);
    }

    public double[] getSpeeds() {
        return convertToDoubleArray(speeds);
    }

    public double[] getStrideLengths() {
        return convertToDoubleArray(strideLengths);
    }

    public double[] getConfidences() {
        return convertToDoubleArray(confidences);
    }

    public ArrayList<String> getGaits() {
        return new ArrayList<String>(gaits);
    }

    private double[] convertToDoubleArray(List<Float> list) {
        double[] result = new double[list.size()];
        int i = 0;
        for (Float f : list) {
            result[i++] = f.doubleValue();
        }
        return result;
    }

    private long[] convertToLongArray(List<Long> list) {
        long[] result = new long[list.size()];
        int i = 0;
        for (Long f : list) {
            result[i++] = f.longValue();
        }
        return result;
    }

    public void clearAll() {
        timeStamps.clear();
        cadences.clear();
        speeds.clear();
        strideLengths.clear();
        confidences.clear();
        gaits.clear();
    }
}
