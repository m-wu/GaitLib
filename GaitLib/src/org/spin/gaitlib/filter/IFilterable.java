
package org.spin.gaitlib.filter;

/**
 * An interface for objects to which a filter can be applied.
 * 
 * @author Mike
 */
public interface IFilterable {

    public void setFilter(IFilter filter);

    public IFilter getFilter();
}
