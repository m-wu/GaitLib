
package org.spin.gaitlib.filter;

import org.spin.gaitlib.cadence.CadenceState;

/**
 * Given a set of recent cadence states, the filter returns the state with the highest calculation
 * confidence.
 * 
 * @author Mike
 */
public class CadenceConfidenceFilter {

    /**
     * @param input a list of <code>CadenceState</code>
     * @return the <code>CadenceState</code> with the highest confidence, or the most recent in case
     *         of a tie.
     */
    public CadenceState getMostConfidentCadence(CadenceState[] input) {
        CadenceState result = null;
        for (CadenceState state : input) {
            if (result == null || state.getCadenceConfidence() > result.getCadenceConfidence()
                    || (state.getCadenceConfidence() == result.getCadenceConfidence()
                    && state.getTimestamp() > result.getTimestamp())) {
                result = state;
            }
        }
        return result;
    }

}
