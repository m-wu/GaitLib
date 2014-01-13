
package org.spin.gaitlib.filter;

public class LowPassFilter implements IFilter {

    private float alpha;

    public LowPassFilter(float dt, float rc) {
        this(rc / (rc + dt));
    }

    public LowPassFilter(float alpha) {
        this.alpha = alpha;
    }

    public float[] getFilteredValues(float[] input) {
        float[] result = new float[input.length];

        result[0] = input[0];

        for (int i = 1; i <= input.length; i++) {
            result[i] = alpha * input[i] + (1 - alpha) * result[i - 1];
        }

        return result;
    }

    public float getFilteredValue(float[] input) {
        // TODO Auto-generated method stub
        return 0;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public void setAlpha(float dt, float rc) {
        setAlpha(rc / (rc + dt));
    }
}
