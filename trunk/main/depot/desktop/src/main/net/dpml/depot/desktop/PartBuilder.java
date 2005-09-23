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
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.lang.reflect.Constructor;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import javax.swing.border.EmptyBorder;

import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import net.dpml.profile.ApplicationProfile;

import net.dpml.part.Control;
import net.dpml.part.Part;
import net.dpml.part.PartContentHandler;
import net.dpml.part.PartHandler;
import net.dpml.part.PartEditor;
import net.dpml.part.context.Context;

import net.dpml.transit.Logger;
import net.dpml.transit.Repository;
import net.dpml.transit.Transit;

import net.dpml.station.Application;

/**
 * Application profile tree node. 
 */
public final class PartBuilder
{
    private Application m_application;
    private Logger m_logger;
    private PartEditor m_editor;

    PartBuilder( Logger logger, Application application )
    {
        m_application = application;
        m_logger = logger;

        init();
    }

    void init()
    {
        try
        {
            PartContentHandler contentHandler = new PartContentHandler( m_logger );
            URI codebase = m_application.getProfile().getCodeBaseURI();
            m_editor = contentHandler.getPartEditor( codebase );
        }
        catch( Throwable e )
        {
            e.printStackTrace();
        }
    }

    Component[] getPartPanels()
    {
        if( null != m_editor )
        {
            return m_editor.getPartPanels();
        }
        else
        {
            JPanel panel = new JPanel();
            panel.add( new JLabel( "Part type does not declare an editor." ) );
            return new Component[]{ panel };
        }
    }
    
    TreeNode[] getPartNodes()
    {
        try
        {
            Context context = m_application.getContext();
            String[] keys = context.getChildKeys();
            TreeNode[] nodes = new TreeNode[ keys.length ];
            for( int i=0; i < keys.length; i++ )
            {
                String key = keys[i];
                Context child = context.getChild( key );
                nodes[i] = new ContextTreeNode( key, child );
            }
            return nodes;
        }
        catch( Throwable e )
        {
            e.printStackTrace();
            return new TreeNode[0];
        }
    }

    private static class ContextTreeNode extends Node 
    {
        private String m_key;
        private Context m_context;
        private Component m_component;
        
        private ContextTreeNode( String key, Context context )
        {
            super( context );
            m_context = context;
            m_key = key;
            m_component = new ContextBuilder( context ).getComponent();
        }
        
        public Component getComponent()
        {
            return m_component;
        }
        
        public String getName()
        {
            return m_key;
        }
    }
    
    //TreeNode[] getPartNodes()
    //{
    //    if( null != m_editor )
    //    {
    //        return m_editor.getPartNodes();
    //    }
    //    else
    //    {
    //        return new TreeNode[0];
    //    }
    //}
}
