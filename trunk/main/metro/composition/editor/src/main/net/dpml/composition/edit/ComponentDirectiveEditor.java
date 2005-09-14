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

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import net.dpml.part.Part;
import net.dpml.part.PartEditor;

import net.dpml.component.control.Controller;

import net.dpml.composition.info.Type;
import net.dpml.composition.info.InfoDescriptor;
import net.dpml.composition.data.DeploymentDirective;
import net.dpml.composition.data.ComponentDirective;
import net.dpml.composition.data.ClassLoaderDirective;
import net.dpml.composition.model.TypeManager;

import net.dpml.transit.Logger;

/**
 * ComponentDirective datatype editor. 
 */
public final class ComponentDirectiveEditor implements PartEditor
{
    private Logger m_logger;
    private TypeManager m_manager;
    private ComponentDirective m_directive;
    private Class m_class;
    private Type m_type;

    ComponentDirectiveEditor( Logger logger, TypeManager manager, ComponentDirective directive )
    {
        m_directive = directive;
        m_manager = manager;
        m_logger = logger;

        ClassLoader anchor = getClass().getClassLoader();
        ClassLoader classloader = m_manager.createClassLoader( anchor, directive );
        String classname = directive.getClassname();

        try
        {
            m_class = classloader.loadClass( classname );         
        }
        catch( Throwable e )
        {
            final String error = 
              "Unable to load component class: " + classname;
            throw new RuntimeException( error, e );
        }

        try
        {
            m_type = Type.loadType( m_class );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unable to load component type: " + classname;
            throw new RuntimeException( error, e );
        }
    }

    public Part getPart()
    {
        // TODO: track changes are returned an updated part suitable for 
        // subsequent externalization
        return m_directive;
    }

    public Component[] getPartPanels()
    {
        ArrayList list = new ArrayList();
        list.add( buildTypeComponent() );
        list.add( buildContextComponent() );
        return (Component[]) list.toArray( new Component[0] );
    }

    private Component buildTypeComponent()
    {
        JTabbedPane tabs = new JTabbedPane();
        tabs.add( "Component", buildTypeStaticComponent() );
        tabs.add( "ClassLoader", buildClassLoaderComponent() );
        tabs.add( "Logging", buildCategoriesComponent());
        tabs.add( "Services", buildServicesComponent());
        tabs.add( "State", buildStateGraphComponent());
        tabs.setName( "Type" );
        return tabs;
    }

    private Component buildClassLoaderComponent() 
    {
        ClassLoaderDirective classloaderDirective = m_directive.getClassLoaderDirective();
        ClassLoaderBuilder builder = new ClassLoaderBuilder();
        return builder.buildPanel( classloaderDirective );
    }

    private Component buildCategoriesComponent() 
    {
        return new JPanel();
    }

    private Component buildServicesComponent() 
    {
        return new JPanel();
    }

    private Component buildStateGraphComponent() 
    {
        return new JPanel();
    }

    private Component buildContextComponent() 
    {
        JPanel context = new JPanel();
        context.setName( "Context" );
        return context;
    }

    private Component buildTypeStaticComponent() 
    {
        FormLayout layout = new FormLayout(
          "right:pref, 3dlu, fill:max(120dlu;pref)", 
          "pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, "
             + "3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref" );

        PanelBuilder builder = new PanelBuilder( layout );
        builder.setDefaultDialogBorder();
        CellConstraints cc = new CellConstraints();

        //builder.addSeparator( "Type", cc.xyw( 1, 1, 3 ) );

        builder.addLabel( "Component:", cc.xy( 1, 3 ) ); 
        builder.add( getClassnameComponent(), cc.xy( 3, 3 ) );

        builder.addLabel( "Version:", cc.xy( 1, 5 ) ); 
        builder.add( getVersionComponent(), cc.xy( 3, 5 ) );
         
        builder.addLabel( "Name:", cc.xy( 1, 7 ) ); 
        builder.add( getPartNameComponent(), cc.xy( 3, 7 ) );
         
        builder.addLabel( "Lifestyle:", cc.xy( 1, 9 ) ); 
        builder.add( getLifestyleComponent(), cc.xy( 3, 9 ) );
         
        builder.addLabel( "Threadsafe:", cc.xy( 1, 11 ) ); 
        builder.add( getThreadSafeComponent(), cc.xy( 3, 11 ) );
         
        builder.addLabel( "Collection Policy:", cc.xy( 1, 13 ) ); 
        builder.add( getCollectionPolicyComponent(), cc.xy( 3, 13 ) );

        builder.addLabel( "Activation Policy:", cc.xy( 1, 15 ) ); 
        builder.add( getActivationPolicyComponent(), cc.xy( 3, 15 ) );

        return builder.getPanel();
    }

    private Component getControllerURIComponent()
    {
        return new JLabel( m_directive.getPartHandlerURI().toString() );
    }

    private Component getActivationPolicyComponent()
    {
        return new JLabel( m_directive.getActivationPolicy().toString() );
    }

    private Component getClassnameComponent()
    {
        return new JLabel( m_type.getInfo().getClassname() );
    }

    private Component getVersionComponent()
    {
        return new JLabel( m_type.getInfo().getVersion().toString() );
    }

    private Component getPartNameComponent()
    {
        return new JLabel( m_type.getInfo().getName() );
    }

    private Component getLifestyleComponent()
    {
        return new JLabel( m_type.getInfo().getLifestyle() );
    }

    private Component getThreadSafeComponent()
    {
        return new JLabel( "" + m_type.getInfo().isThreadsafe() );
    }

    private Component getCollectionPolicyComponent()
    {
        return new JLabel( 
          InfoDescriptor.getCollectionPolicyKey( 
            m_type.getInfo().getCollectionPolicy() ) );
    }

   /**
    * The PolicyComboBox is a volotile component that maintains the 
    * presentation of the application profile startup policy. 
    */
    /*
    private class ActivationPolicyComboBox extends JComboBox
    {
        private ActivationPolicyComboBox()
        {
            super( Control.ACTIVATION_POLICIES );
            setSelectedItem( m_directive.getActivationPolicy() );
        }

        public void selectedItemChanged()
        {
            super.selectedItemChanged();
            System.out.println( "TODO: " + getClass().getName() );
        }
    }
    */
}
