
package org.spin.gaitlib.core;

import org.spin.gaitlib.util.Logger;

/**
 * Interface for classes where logging can be performed by {@link Logger}.
 * 
 * @author Mike
 */
public interface ILoggable {

    public void setLoggingEnabled(boolean enabled);

    public boolean isLogging();

}
