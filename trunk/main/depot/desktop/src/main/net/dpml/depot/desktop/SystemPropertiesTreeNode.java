
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
import javax.swing.tree.DefaultMutableTreeNode;
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
public final class SystemPropertiesTreeNode extends TableNode
{
    private static final int NAME_COLUMN = 0;
    private static final int VALUE_COLUMN = 1;
    private static final int COLUMN_COUNT = 2;

    private static final TableColumnModel TABLE_COLUMN_MODEL = createColumnModel();

    private ApplicationProfile m_profile;
    private String m_id;
    private JLabel m_label;
    private Component m_component;
    private Properties m_properties;

    private ImageIcon m_sysprops = readImageIcon( "16/mime.png" );

    public SystemPropertiesTreeNode( ApplicationProfile profile )
    {
        super( profile );
        m_profile = profile;
        try
        {
            m_properties = profile.getSystemProperties();
        }
        catch( Exception e )
        {
            m_properties = new Properties();
            e.printStackTrace();
        }

        m_label = new JLabel( "System Properties", m_sysprops, SwingConstants.LEFT );
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
            return m_properties.size();
        }

        public Object getValueAt( int row, int col ) 
        { 
            switch( col )
            {
                case NAME_COLUMN :
                  return m_properties.keySet().toArray()[ row ];
                case VALUE_COLUMN :
                  return m_properties.values().toArray()[ row ];
                default: 
                  return null;
            }
        }
    }

    private static TableColumnModel createColumnModel()
    {
        TableColumnModel model = new DefaultTableColumnModel();
	  model.addColumn( createTableColumn( "Name", NAME_COLUMN, 60 ) );
	  model.addColumn( createTableColumn( "Value", VALUE_COLUMN, 350 ) );
	  return model;
    }
}