
package net.dpml.depot.desktop;

import javax.swing.table.TableColumn;

/**
 * Application profile tree node. 
 */
public interface Volotile
{
    boolean isModified();

    boolean apply();

    boolean undo();
}