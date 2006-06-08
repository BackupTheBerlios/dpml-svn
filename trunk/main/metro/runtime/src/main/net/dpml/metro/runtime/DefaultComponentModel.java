/*
 * Copyright 2005-2006 Stephen J. McConnell.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dpml.metro.runtime;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.EventObject;
import java.util.EventListener;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;

import net.dpml.component.Directive;
import net.dpml.component.ActivationPolicy;
import net.dpml.component.ControlException;
import net.dpml.component.ModelListener;
import net.dpml.component.ModelEvent;

import net.dpml.lang.Classpath;
import net.dpml.lang.UnknownKeyException;

import net.dpml.metro.data.ComponentDirective;
import net.dpml.metro.data.ContextDirective;
import net.dpml.metro.data.CategoriesDirective;
import net.dpml.metro.data.CategoryDirective;
import net.dpml.metro.info.CollectionPolicy;
import net.dpml.metro.info.LifestylePolicy;
import net.dpml.metro.info.Type;
import net.dpml.metro.info.PartReference;
import net.dpml.metro.info.Composite;
import net.dpml.metro.ComponentModel;
import net.dpml.metro.ContextModel;
import net.dpml.metro.ComponentModelManager;
import net.dpml.metro.ContextModelManager;

import net.dpml.util.Logger;

/**
 * Default implementation of a mutable component model.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class DefaultComponentModel extends UnicastEventSource 
  implements ComponentModelManager
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private final ComponentController m_controller;
    
    private final Type m_type;
    private final ComponentDirective m_directive;
    private final ClassLoader m_classloader;
    private final Classpath m_classpath;
    private final DefaultContextModel m_context;
    private final String m_path;
    private final PartReference[] m_references;
    private final Class m_class;
    private final String m_classname;
    
    private ActivationPolicy m_activation;
    private LifestylePolicy m_lifestyle;
    private CollectionPolicy m_collection;
    
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------
    
    public DefaultComponentModel( 
      Logger logger, ComponentController controller, DefaultComposition composition, String partition ) 
      throws ControlException, IOException, RemoteException
    {
        super( logger );
        
        m_classpath = composition.getClasspath();
        m_controller = controller;
        m_directive = composition.getComponentDirective();
        m_classloader = composition.getClassLoader();
        m_classname = m_directive.getClassname();
        m_class = m_controller.loadComponentClass( m_classloader, m_classname );
        m_type = m_controller.loadType( m_class );
        m_path = setupPath( partition );
        m_activation = m_directive.getActivationPolicy();
        m_lifestyle = setupLifestyle();
        m_collection = m_directive.getCollectionPolicy();
        ContextDirective context = m_directive.getContextDirective();
        m_context = new DefaultContextModel( this, logger, m_classloader, m_type, context );
        m_references = buildPartReferences();
    }
    
    public DefaultComponentModel( 
      Logger logger, ClassLoader anchor, ComponentController controller, Classpath classpath, 
      ComponentDirective directive, String partition ) 
      throws ControlException, IOException, RemoteException
    {
        super( logger );
        
        m_classpath = classpath;
        m_controller = controller;
        m_directive = directive;
        m_classloader = m_controller.getClassLoader( anchor, classpath );
        m_classname = directive.getClassname();
        m_class = m_controller.loadComponentClass( m_classloader, m_classname );
        m_type = m_controller.loadType( m_class );
        m_path = setupPath( partition );
        m_activation = directive.getActivationPolicy();
        m_lifestyle = setupLifestyle();
        m_collection = directive.getCollectionPolicy();
        ContextDirective context = directive.getContextDirective();
        m_context = new DefaultContextModel( this, logger, m_classloader, m_type, context );
        m_references = buildPartReferences();
    }
    
   /**
    * Return the inital array of part references.
    * @return the part reference array
    */
    public PartReference[] getPartReferences()
    {
        return m_references;
    }
    
    public Classpath getClasspath()
    {
        return m_classpath;
    }
    
    protected void processEvent( EventObject event )
    {
        EventListener[] listeners = super.listeners();
        for( int i=0; i < listeners.length; i++ )
        {
            EventListener listener = listeners[i];
            if( listener instanceof ModelListener )
            {
                ModelListener l = (ModelListener) listener;
                if( event instanceof ModelEvent )
                {
                    try
                    {
                        ModelEvent e = (ModelEvent) event;
                        l.modelChanged( e );
                    }
                    catch( Throwable e )
                    {
                        final String error =
                          "ModelListener change notification error.";
                        getLogger().error( error, e );
                    }
                }
            }
        }
    }
    
   /**
    * Add a listener to the component model.
    * @param listener the model listener
    */
    public void addModelListener( ModelListener listener )
    {
        super.addListener( listener );
        m_context.addModelListener( listener );
    }
    
   /**
    * Remove a listener from the component model.
    * @param listener the model listener
    */
    public void removeModelListener( ModelListener listener )
    {
        super.removeListener( listener );
        m_context.removeListener( listener );
    }
    
    // ------------------------------------------------------------------------
    // ComponentModel
    // ------------------------------------------------------------------------
    
   /**
    * Return the component name.
    * @return the name
    */
    public String getName()
    {
        String name = m_directive.getName();
        if( null != name )
        {
            return name;
        }
        else
        {
            return m_type.getInfo().getName();
        }
    }
    
   /**
    * Return the path identifying the context.  A context path commences with the
    * PARTITION_SEPARATOR character and is followed by a context name.  If the 
    * context exposed nested context objects, the path is component of context names
    * separated by the PARTITION_SEPARATOR as in "/main/web/handler".
    *
    * @return the context path
    */
    public String getContextPath()
    {
        return m_path;
    }
    
   /**
    * Return the component implementation class name.
    *
    * @return the classname of the implementation 
    */
    public String getImplementationClassName()
    {
        return m_classname;
    }
    
   /**
    * Return the activation policy for the component.
    * @return the activation policy value
    */
    public ActivationPolicy getActivationPolicy()
    {
        return m_activation;
    }

   /**
    * Set the component activation policy to the supplied value.
    * @return the new activation policy
    */
    public void setActivationPolicy( ActivationPolicy policy )
    {
        ActivationPolicy old = m_activation;
        m_activation = policy;
        ModelEvent event = new ModelEvent( this, "activation.policy", old, policy );
        enqueueEvent( event );
    }

   /**
    * Return the component lifestyle policy.
    *
    * @return the lifestyle policy value
    */
    public LifestylePolicy getLifestylePolicy()
    {
        return m_lifestyle;
    }

   /**
    * Return the current component collection policy.  If null, the component
    * type collection policy will be returned.
    *
    * @return a HARD, WEAK, SOFT or SYSTEM
    */
    public CollectionPolicy getCollectionPolicy()
    {
        return m_collection;
    }

   /**
    * Override the assigned collection policy.
    * @param policy the collection policy value
    */
    public void setCollectionPolicy( CollectionPolicy policy )
    {
        CollectionPolicy old = m_collection;
        m_collection = policy;
        ModelEvent event = new ModelEvent( this, "collection.policy", old, policy );
        enqueueEvent( event );
    }

   /**
    * Return the current context model.
    *
    * @return the context model
    */
    public ContextModel getContextModel()
    {
        return m_context;
    }
    
   /**
    * Return the context manager.
    * @return the context manager
    */
    public ContextModelManager getContextManager()
    {
        if( m_context instanceof ContextModelManager )
        {
            return (ContextModelManager) m_context;
        }
        else
        {
            final String error = 
              "Cannot cast context model to the manager interface.";
            throw new IllegalStateException( error );
        }
    }
     
   /**
    * Return the component logging categories.
    * @return the categories
    * @exception RemoteException if a remote exception occurs
    */
    public CategoryDirective[] getCategoryDirectives()
    {
        CategoriesDirective categories = m_directive.getCategoriesDirective();
        if( null != categories )
        {
            return categories.getCategories();
        }
        else
        {
            return new CategoryDirective[0];
        }
    }
    
    // ------------------------------------------------------------------------
    // DefaultComponentModel
    // ------------------------------------------------------------------------
    
    ClassLoader getClassLoader()
    {
        return m_classloader;
    }
    
    // ------------------------------------------------------------------------
    // Disposable
    // ------------------------------------------------------------------------

    public void dispose()
    {
        m_context.dispose();
        super.dispose();
    }
    
    // ------------------------------------------------------------------------
    // internals
    // ------------------------------------------------------------------------
    
    private String setupPath( String partition )
    {
        String name = m_directive.getName();
        if( null == name )
        {
            return partition + m_type.getInfo().getName();
        }
        else
        {
            return partition + name;
        }
    }

    private LifestylePolicy setupLifestyle()
    {
        LifestylePolicy lifestyle = m_directive.getLifestylePolicy();
        if( null == lifestyle )
        {
            return m_type.getInfo().getLifestylePolicy();
        }
        else
        {
            return lifestyle;
        }
    }

    private PartReference[] buildPartReferences()
    {
        List list = new LinkedList();
        Map map = new Hashtable();
        populatePartReferences( list, map, m_type );
        populatePartReferences( list, map, m_directive );
        PartReference[] refs = new PartReference[ list.size() ];
        String[] names = (String[]) list.toArray( new String[0] );
        for( int i=0; i<names.length; i++ )
        {
            String name = names[i];
            PartReference ref = (PartReference) map.get( name );
            refs[ i ] = ref;
        }
        return refs;
    }
    
    private void populatePartReferences( List names, Map map, Composite composite )
    {
        PartReference[] references = composite.getPartReferences();
        for( int i=0; i < references.length; i++ )
        {
            PartReference ref = references[i];
            String key = ref.getKey();
            if( !names.contains( key ) )
            {
                names.add( key );
            }
            map.put( key, ref );
        }
    }
}

