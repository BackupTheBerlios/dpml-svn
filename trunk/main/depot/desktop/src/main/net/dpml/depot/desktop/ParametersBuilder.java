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

import javax.swing.JTree;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.JPanel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.dpml.profile.ApplicationRegistry;
import net.dpml.profile.ApplicationProfile;

import net.dpml.transit.model.Value;
import net.dpml.transit.model.Construct;

/**
 * Application profile tree node. 
 */
public final class ParametersBuilder
{
    JComponent buildParametersPanel( ApplicationProfile profile ) throws Exception
    {

        DefaultMutableTreeNode root = new DefaultMutableTreeNode( profile );
        DefaultTreeModel model = new DefaultTreeModel( root );

        Value[] values = profile.getParameters();
        for( int i=0; i<values.length; i++ )
        {
            Value value = values[i];
            ValueTreeNode node = new ValueTreeNode( value );
            root.add( node );
        }

        JTree tree = new JTree( model );
        tree.setRootVisible( false );
        tree.setShowsRootHandles( true );
        tree.setCellRenderer( new ValueCellRenderer() );

        JSplitPane panel = new JSplitPane();
        panel.setOrientation( JSplitPane.VERTICAL_SPLIT );
        panel.setTopComponent( tree );
        panel.setBottomComponent( new JPanel() );
        panel.setDividerLocation( 200 );
        
        JScrollPane scrollPane = new JScrollPane( panel );
        scrollPane.getViewport().setBackground( tree.getBackground() );
        //scrollPane.setBorder( null );
        return scrollPane;
    }

    private class ValueTreeNode extends DefaultMutableTreeNode
    {
        private final Value m_value;

        private ValueTreeNode( final Value value )
        {
            super( value );

            m_value = value;

            if( value instanceof Construct )
            {
                Construct construct = (Construct) value;
                if( construct.getBaseValue() != null )
                {
                    //
                    // add value node
                    //

                    String base = construct.getBaseValue();
                    add( new StringTreeNode( base ) );
                }
                else
                {
                    //
                    // add subsidiary value nodes
                    //

                    Value[] values = construct.getValues();
                    for( int i=0; i< values.length; i++ )
                    {
                        Value v = values[i];
                        add( new ValueTreeNode( v ) );
                    }
                }
            }
        }

        public String getBaseClassname()
        {
            return m_value.getBaseClassname();
        }

        public String toString()
        {
            return m_value.getTypeClassname();
        }
    }

    private class StringTreeNode extends DefaultMutableTreeNode
    {
        private StringTreeNode( final String value )
        {
            super( value );
        }
    }

    private class ValueCellRenderer extends DefaultTreeCellRenderer
    {
        /*
        public Component getTreeCellRendererComponent( 
          JTree tree, Object value, boolean selected, boolean expanded, 
          boolean leaf, int row, boolean focus )
        {
            Component label = super.getTreeCellRendererComponent( 
                tree, value, selected, expanded, leaf, row, focus );
            if( value instanceof ValueTreeNode )
            {
                ValueTreeNode vtn = (ValueTreeNode) value;
                String base = vtn.getBaseClassname();
                setToolTipText( base );
            }
            else
            {
                setToolTipText( null );
            }
            return label;
        }
        */
    }

}
