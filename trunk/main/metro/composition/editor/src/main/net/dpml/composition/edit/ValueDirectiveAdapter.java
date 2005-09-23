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

package net.dpml.composition.edit;

import java.awt.Component;
import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JComboBox;
import javax.swing.tree.TreeNode;
import javax.swing.tree.DefaultMutableTreeNode;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import net.dpml.part.Part;
import net.dpml.part.PartEditor;
import net.dpml.part.PartReference;

import net.dpml.component.control.Controller;

import net.dpml.composition.info.Type;
import net.dpml.composition.info.InfoDescriptor;
import net.dpml.composition.data.DeploymentDirective;
import net.dpml.composition.data.ValueDirective;

import net.dpml.transit.Logger;

/**
 * ComponentDirective datatype editor. 
 */
public final class ValueDirectiveAdapter extends DefaultMutableTreeNode implements PartEditor
{
    private String m_key;
    private ClassLoader m_classloader;
    private ValueDirective m_directive;
    private Logger m_logger;

    private DefaultMutableTreeNode[] m_nodes;
    private Component m_component;
    private Component[] m_panels;

    ValueDirectiveAdapter( ClassLoader classloader, Logger logger, ValueDirective directive, String key )
    {
        super( directive );

        m_key = key;
        m_logger = logger;
        m_directive = directive;
        m_classloader = classloader;

        String classname = directive.getClassname();
        String local = directive.getLocalValue();
        if( null == local )
        {
            //
            // its a composite value
            //

            ValueDirective[] values = directive.getValues();
            m_component = buildComponent( classname, values );
        }
        else
        {
            m_component = buildComponent( classname, local );
        }
        m_panels = new Component[]{ m_component };
    }

    public Component getComponent()
    {
        return m_component;
    }

    public Component[] getPartPanels()
    {
        return m_panels;
    }

    public TreeNode[] getPartNodes()
    {
        return getMutableNodes();
    }

    public Part getPart()
    {
        return m_directive;
    }

    public String toString()
    {
        return m_key;
    }

    private Component buildComponent( String classname, String value )
    {
        return new JLabel( value );
    }

    private Component buildComponent( String classname, ValueDirective[] values )
    {
        JPanel panel = new JPanel();
        for( int i=0; i < values.length; i++ )
        {
            ValueDirective value = values[i];
            if( value.getValues().length > 0 )
            {
                String cname = value.getClassname();
                Component c = buildComponent( cname, value.getValues() );
                panel.add( c );
            }
            else
            {
                panel.add( new JLabel( value.getLocalValue() ) );
            }
        }
        return panel;
    }

    private DefaultMutableTreeNode[] getMutableNodes()
    {
        return new DefaultMutableTreeNode[0];
    }

}
