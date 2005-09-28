/*
 * Copyright 2005 Stephen McConnell
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import javax.swing.table.TableCellEditor;
import javax.swing.DefaultCellEditor;

import net.dpml.part.Context;
import net.dpml.part.Directive;
import net.dpml.part.EntryDescriptor;

import net.dpml.transit.model.Value;

/**
 * Application profile tree node. 
 */
public final class ContextBuilder
{
    private static final int NAME_COLUMN = 0;
    private static final int TYPE_COLUMN = 1;
    private static final int VALUE_COLUMN = 2;
    private static final int REQUIRED_COLUMN = 3;
    private static final int COLUMN_COUNT = 4;

    private static final TableColumnModel TABLE_COLUMN_MODEL = createColumnModel();

    private Context m_context;
    private int m_size;
    private EntryDescriptor[] m_entries;
    private Component m_component;

    public ContextBuilder( Context context )
    {
        m_context = context;
        
        try
        {
            m_entries = context.getEntries();
            m_size = m_entries.length;
        }
        catch( Exception e )
        {
            e.printStackTrace();
            m_entries = new EntryDescriptor[0];
            m_size = 0;
        }

        TableModel tableModel = new DataModel();
        JTable table = new JTable( tableModel, TABLE_COLUMN_MODEL );
        JScrollPane scrollPane = new JScrollPane( table );
        scrollPane.getViewport().setBackground( table.getBackground() );
        scrollPane.setBorder( null );
        m_component = scrollPane;
        m_component.setName( "Context" );
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
            return m_size;
        }

        public boolean isCellEditable( int row, int column )
        {
            if( column == VALUE_COLUMN )
            {
                return true;
            }
            else
            {
                return false;
            }
        }

        public void setValueAt( Object value, int row, int col ) 
        {
            try
            {
                EntryDescriptor entry = m_entries[ row ];
                String key = entry.getKey();
                Object currentValue = getValue( key );
                if( !currentValue.equals( value ) )
                {
                    System.out.println( "# set (" + row + "," + col + ") " + currentValue + " --> " + value );
                }
            }
            catch( Throwable e )
            {
                // throw an error dialog
            }
        }

        public Object getValueAt( int row, int column )
        {
            try
            {
                EntryDescriptor entry = m_entries[ row ];
                switch( column )
                {
                    case NAME_COLUMN :
                        return entry.getKey();
                    case TYPE_COLUMN :
                        return entry.getClassname();
                    case VALUE_COLUMN :
                        String key = entry.getKey();
                        return getValue( key );
                    case REQUIRED_COLUMN :
                        if( entry.isRequired() )
                        {
                            return "required";
                        }
                        else
                        {
                            return null;
                        }
                    default: 
                        return null;
                }
            }
            catch( Throwable e )
            {
                e.printStackTrace();
                return null;
            }
        }
        
        private Object getValue( String key )
        {
            try
            {
                Directive directive = m_context.getDirective( key );
                if( directive instanceof Value )
                {
                    Value value = (Value) directive;
                    return value.getBaseValue();
                }
                else
                {
                    return "COMPOUND";
                }
            }
            catch( Exception e )
            {
                e.printStackTrace();
                return null;
            }
        }
    }

    //--------------------------------------------------------------------------
    // utilities
    //--------------------------------------------------------------------------

    private static TableColumnModel createColumnModel()
    {
        TableColumnModel model = new DefaultTableColumnModel();
        model.addColumn( createTableColumn( "Name", NAME_COLUMN, 100 ) );
        model.addColumn( createTableColumn( "Type", TYPE_COLUMN, 150 ) );
        model.addColumn( createTableColumn( "Value", VALUE_COLUMN, 150 ) );
        model.addColumn( createTableColumn( "Required", REQUIRED_COLUMN, 60 ) );
        return model;
    }

    private static TableColumn createTableColumn( String name, int index, int width )
    {
        TableColumn column = 
          new TableColumn( index, width, new StandardCellRenderer(), null );
        column.setHeaderValue( name );
        return column;
    }

    private static class StandardCellRenderer extends DefaultTableCellRenderer
    {
        public Component getTableCellRendererComponent(
          JTable table, Object object, boolean selected, boolean focus, int row, int column )
        {
            JLabel label = 
              (JLabel) super.getTableCellRendererComponent( 
                table, object, selected, focus, row, column );
            label.setBorder( EMPTY_BORDER );
            label.setHorizontalAlignment( SwingConstants.LEFT );
            return label;
        }
    }

    private static final EmptyBorder EMPTY_BORDER = new EmptyBorder(1,5,1,3);
}