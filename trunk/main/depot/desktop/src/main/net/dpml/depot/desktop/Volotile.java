
package net.dpml.depot.desktop;

import javax.swing.table.TableColumn;

/**
 * Application profile tree node. 
 */
public interface Volotile
{
    boolean isModified();

    void apply() throws Exception;

    void revert() throws Exception;
}