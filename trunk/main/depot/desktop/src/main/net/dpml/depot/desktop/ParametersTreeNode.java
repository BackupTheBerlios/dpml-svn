
package net.dpml.depot.desktop;

import java.awt.Component;
import java.net.URL;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import net.dpml.profile.ApplicationRegistry;
import net.dpml.profile.ApplicationProfile;

/**
 * Application profile tree node. 
 */
public final class ParametersTreeNode extends TableNode
{
    private static final int KEY_COLUMN = 0;
    private static final int BASE_CLASS_COLUMN = 1;
    private static final int COLUMN_COUNT = 2;

    private static final TableColumnModel TABLE_COLUMN_MODEL = createColumnModel();

    private ApplicationProfile m_profile;
    private String m_id;
    private JLabel m_label;
    private Component m_component;

    private ImageIcon m_icon = readImageIcon( "16/parameters.png" );

    public ParametersTreeNode( ApplicationProfile profile )
    {
        super( profile );
        m_profile = profile;

        m_label = new JLabel( "Parameters", m_icon, SwingConstants.LEFT );
        m_label.setBorder( new EmptyBorder( 3, 3, 2, 2 ) );
        TableModel tableModel = new DataModel();
        JTable table = new JTable( tableModel, TABLE_COLUMN_MODEL );
        JScrollPane scrollPane = new JScrollPane( table );
        scrollPane.getViewport().setBackground( table.getBackground() );
        scrollPane.setBorder( null );
        m_component = scrollPane;
    }

    Component getLabel()
    {
        return m_label;
    }

    Component getComponent()
    {
        return m_component;
    }

    private class DataModel extends AbstractTableModel
    {
        public int getColumnCount()
        {
            return COLUMN_COUNT;
        }

        public int getRowCount()
        { 
            try
            {
                return m_profile.getParameters().length;
            }
            catch( Throwable e )
            {
                return 0;
            }
        }

        public Object getValueAt( int row, int col ) 
        { 
            switch( col )
            {
                case KEY_COLUMN :
                  getKey( row );
                case BASE_CLASS_COLUMN :
                  return getBaseClassname( row );
                default: 
                  return null;
            }
        }
    }

    private String getKey( int row )
    {
        try
        {
            return m_profile.getParameters()[ row ].getKey();
        }
        catch( Throwable e )
        {
            return "error";
        }
    }

    private String getBaseClassname( int row )
    {
        try
        {
            return m_profile.getParameters()[ row ].getValue().getBaseClassname();
        }
        catch( Throwable e )
        {
            return "error";
        }
    }

    private static TableColumnModel createColumnModel()
    {
        TableColumnModel model = new DefaultTableColumnModel();
	  model.addColumn( createTableColumn( "Key", KEY_COLUMN, 60 ) );
	  model.addColumn( createTableColumn( "Type", BASE_CLASS_COLUMN, 350 ) );
	  return model;
    }
}
