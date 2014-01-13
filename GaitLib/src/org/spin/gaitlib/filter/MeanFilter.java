
package org.spin.gaitlib.filter;

/**
 * A filter that returns the mean of a set of input values.
 * 
 * @author Mike
 */
public class MeanFilter implements IFilter {

    /**
     * Return the mean of <code>input</code>.
     */
    public float getFilteredValue(float[] input) {
        int length = input.length;
        if (length == 0) {
            return 0;
        }
        float sum = 0;
        for (float f : input) {
            sum += f;
        }
        return sum / length;
    }
}
