
package org.spin.gaitlib.filter;

public interface IFilter {

    /**
     * Y(i) = F(X(i),Y(i-1),...)
     * 
     * @param input
     * @return the filtered value
     */
    public float getFilteredValue(float[] input);

}
