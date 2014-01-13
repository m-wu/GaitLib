
package org.spin.gaitlib.util;

/**
 * This class interacts via the JNI (Java Native Interface) to the C code which performs the
 * spectral analysis.
 * 
 * @author Bryan Stern, 2011
 */
public class SpectralAnalyses {

    private static final String TAG = "SpectralAnalysis";

    static {
        System.loadLibrary("spectral-analysis");
    }

    // See jni/calcfreq.c for the implementation of this method
    public static native float[][] fasperArray(float hifac, float ofac, float[] x, float[] y,
            float[] z, float[] time,
            int size);

    public static float fasperResultsMaxFreq(float[][] results) {
        float max = Float.MAX_VALUE * -1;
        int index = 0;
        for (int i = 0; i < results.length; i++) {
            if (results[i][1] > max) {
                index = i;
                max = results[i][1];
            }
        }

        return results[index][0];
    }

    public static float fasperResultsMinFreq(float[][] results) {
        float min = Float.MAX_VALUE;
        int index = 0;
        for (int i = 0; i < results.length; i++) {
            if (results[i][1] < min) {
                index = i;
                min = results[i][1];
            }
        }

        return results[index][0];
    }

    /**
     * @return an array that contains the frequencies associated with the strongest, second
     *         strongest and the weakest power.
     */
    public static float[] fasperResultsMaxMinFreq(float[][] results) {
        float max = Float.MAX_VALUE * -1;
        float secondMax = Float.MAX_VALUE * -1;
        float min = Float.MAX_VALUE;

        int indexMax = 0;
        int indexSecondMax = 0;
        int indexMin = 0;

        for (int i = 0; i < results.length; i++) {
            if (results[i][1] > max) {
                indexSecondMax = indexMax;
                indexMax = i;
                secondMax = max;
                max = results[i][1];
            } else if (results[i][1] > secondMax) {
                indexSecondMax = i;
                secondMax = results[i][1];
            }
            if (results[i][1] < min) {
                indexMin = i;
                min = results[i][1];
            }
        }

        float[] maxMinFreq = new float[3];
        maxMinFreq[0] = results[indexMax][0];
        maxMinFreq[1] = results[indexSecondMax][0];
        maxMinFreq[2] = results[indexMin][0];

        return maxMinFreq;
    }

    public static float fasperResultsWeightedAverageFreq(float[][] results) {
        float weightedSum = 0;
        float sumPower = 0;
        for (int i = 0; i < results.length; i++) {
            weightedSum += results[i][0] * results[i][1];
            sumPower += results[i][1];
        }
        return weightedSum / sumPower;
    }

    public static double[] fasperResultsFrequenciesAsDoubles(float[][] results) {
        double[] r = new double[results.length];
        for (int i = 0; i < results.length; i++) {
            r[i] = results[i][0];
        }

        return r;
    }

    public static double[] fasperResultsPowersAsDoubles(float[][] results) {
        double[] r = new double[results.length];
        for (int i = 0; i < results.length; i++) {
            r[i] = results[i][1];
        }

        return r;
    }

    public static float confidence(float[][] results) {
        for (int i = 0; i < results.length; i++) {
            if (results[i].length == 3) {
                return (float) 1.000000000000000000000 - results[i][2];
            }
        }
        return -1;
    }
}
