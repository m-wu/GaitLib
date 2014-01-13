
package org.spin.gaitlib.filter;

import java.util.Arrays;

/**
 * A filter that returns the median of a set of input values.
 * 
 * @author Mike
 */
public class MedianFilter implements IFilter {

    /**
     * Return the median of <code>input</code>.
     */
    public float getFilteredValue(float[] input) {
        int length = input.length;
        if (length == 0) {
            return 0;
        }

        float[] sortedArray = new float[length];

        System.arraycopy(input, 0, sortedArray, 0, length);
        Arrays.sort(sortedArray);

        int middle = length / 2;
        if (length % 2 == 1) {
            return sortedArray[middle];
        } else {
            return (sortedArray[middle - 1] + sortedArray[middle]) / 2;
        }
    }
}
