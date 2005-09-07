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

import java.net.URL;
import java.net.URI;
import java.util.Properties;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;

import net.dpml.component.Component;
import net.dpml.component.Container;

/**
 * Application profile tree node. 
 */
public class ComponentTreeNode extends Node
{
    public static final Icon DPML_DESKTOP_LEAF_ICON = readImageIcon( "16/binary.png" );

    private final String m_name;

    private Component m_component;

    public ComponentTreeNode( Component component ) throws Exception
    {
        super( component );

        m_component= component;

        m_name = component.getName();

        if( component instanceof Container )
        {
            Container container = (Container) component;
            Component[] components = container.getComponents();
            for( int i=0; i<components.length; i++ )
            {
                Component c = components[i];
                ComponentTreeNode node = new ComponentTreeNode( c );
                add( node );
            }
        }
    }

    public String toString()
    {
        return m_name;
    }

    java.awt.Component getComponent()
    {
        JTabbedPane pane = new JTabbedPane();
        pane.addTab( "Component", new JPanel() );
        return pane;
    }

    public void dispose()
    {
        m_component = null;
    }
}
