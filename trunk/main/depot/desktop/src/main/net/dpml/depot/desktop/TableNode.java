
package net.dpml.depot.desktop;

import javax.swing.table.TableColumn;

/**
 * Application profile tree node. 
 */
public abstract class TableNode extends Node
{
    public TableNode( Object object )
    {
        super( object );
    }

    protected static TableColumn createTableColumn( String name, int index, int width )
    {
	  TableColumn column = 
          new TableColumn( index, width, new StandardCellRenderer(), null );
        column.setHeaderValue( name );
        return column;
    }
}