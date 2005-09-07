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
import javax.swing.JComponent;
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
public final class ParametersBuilder
{
    private static final TableColumnModel TABLE_COLUMN_MODEL = createColumnModel();

    private static final int KEY_COLUMN = 0;
    private static final int BASE_CLASS_COLUMN = 1;
    private static final int COLUMN_COUNT = 2;

    JComponent buildParametersTablePanel( ApplicationProfile profile )
    {
        TableModel tableModel = new DataModel( profile );
        JTable table = new JTable( tableModel, TABLE_COLUMN_MODEL );
        JScrollPane scrollPane = new JScrollPane( table );
        scrollPane.getViewport().setBackground( table.getBackground() );
        scrollPane.setBorder( null );
        return scrollPane;
    }

    private class DataModel extends AbstractTableModel
    {
        private final ApplicationProfile m_profile;

        private DataModel( ApplicationProfile profile ) 
        {
            m_profile = profile;
        }

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
    }

    private static TableColumnModel createColumnModel()
    {
        TableColumnModel model = new DefaultTableColumnModel();
        model.addColumn( createTableColumn( "Key", KEY_COLUMN, 60 ) );
        model.addColumn( createTableColumn( "Type", BASE_CLASS_COLUMN, 350 ) );
        return model;
    }

    private static TableColumn createTableColumn( String name, int index, int width )
    {
        TableColumn column = 
          new TableColumn( index, width, new StandardCellRenderer(), null );
        column.setHeaderValue( name );
        return column;
    }
}
