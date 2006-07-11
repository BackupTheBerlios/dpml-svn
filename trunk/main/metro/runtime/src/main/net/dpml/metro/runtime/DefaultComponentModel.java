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
import java.util.Arrays;

import net.dpml.component.ActivationPolicy;
import net.dpml.component.ControlException;
import net.dpml.component.ModelListener;
import net.dpml.component.ModelEvent;

import net.dpml.lang.Classpath;

import net.dpml.metro.data.ComponentDirective;
import net.dpml.metro.data.ContextDirective;
import net.dpml.metro.data.CategoriesDirective;
import net.dpml.metro.data.CategoryDirective;

import net.dpml.metro.info.CollectionPolicy;
import net.dpml.metro.info.LifestylePolicy;
import net.dpml.metro.info.Type;
import net.dpml.metro.info.PartReference;

import net.dpml.metro.ContextModel;
import net.dpml.metro.ComponentModelManager;
import net.dpml.metro.ContextModelManager;

import net.dpml.util.Logger;
import net.dpml.util.EventQueue;

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
    private final LifestylePolicy m_lifestyle;
    private final CollectionPolicy m_collection;
    
    private ActivationPolicy m_activation;
    
    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------
    
    public DefaultComponentModel( 
      EventQueue queue, Logger logger, ClassLoader classloader, Classpath classpath, 
      ComponentController controller, ComponentDirective directive, String partition, String name ) 
      throws ControlException, IOException, RemoteException
    {
        super( queue, logger );
        
        m_classpath = classpath;
        m_controller = controller;
        m_directive = directive;
        m_classloader = classloader;
        m_classname = getClassname( directive );
        m_class = m_controller.loadComponentClass( m_classloader, m_classname );
        m_type = m_controller.loadType( m_class );
        m_path = setupPath( partition, name );
        m_activation = getActivationPolicy( directive );
        m_lifestyle = getLifestylePolicy( directive );
        m_collection = getCollectionPolicy( directive );
        ContextDirective context = getContextDirective( directive );
        m_context =
          new DefaultContextModel( 
            queue, 
            this, logger, m_classloader, m_type, context );
        m_references = getPartReferences( directive );
    }
    
    private String getClassname( ComponentDirective directive )
    {
        if( null == directive )
        {
            throw new NullPointerException( "directive" );
        }
        else
        {
            ComponentDirective base = directive.getBaseDirective();
            if( null == base )
            {
                return directive.getClassname();
            }
            else
            {
                return getClassname( base );
            }
        }
    }

    private ActivationPolicy getActivationPolicy( ComponentDirective directive )
    {
        if( null == directive )
        {
            throw new NullPointerException( "directive" );
        }
        if( null != directive.getActivationPolicy() )
        {
            return directive.getActivationPolicy();
        }
        else
        {
            ComponentDirective base = directive.getBaseDirective();
            return getActivationPolicy( base );
        }
    }

    private CollectionPolicy getCollectionPolicy( ComponentDirective directive )
    {
        if( null == directive )
        {
            throw new NullPointerException( "directive" );
        }
        if( null != directive.getCollectionPolicy() )
        {
            return directive.getCollectionPolicy();
        }
        else
        {
            ComponentDirective base = directive.getBaseDirective();
            return getCollectionPolicy( base );
        }
    }

    private ContextDirective getContextDirective( ComponentDirective directive )
    {
        if( null == directive )
        {
            throw new NullPointerException( "directive" );
        }
        if( null != directive.getContextDirective() )
        {
            return directive.getContextDirective();
        }
        else
        {
            ComponentDirective base = directive.getBaseDirective();
            return getContextDirective( base );
        }
    }

    private LifestylePolicy getLifestylePolicy( ComponentDirective directive )
    {
        if( null == directive )
        {
            throw new NullPointerException( "directive" );
        }
        if( null != directive.getLifestylePolicy() )
        {
            return directive.getLifestylePolicy();
        }
        else
        {
            ComponentDirective base = directive.getBaseDirective();
            if( null != base )
            {
                return getLifestylePolicy( base );
            }
            else
            {
                return m_type.getInfo().getLifestylePolicy();
            }
        }
    }

    private PartReference[] getPartReferences( ComponentDirective directive )
    {
        Map map = new Hashtable();
        populatePartReferences( map, directive );
        PartReference[] refs = (PartReference[]) map.values().toArray( new PartReference[0] );
        Arrays.sort( refs );
        return refs;
    }
    
    private void populatePartReferences( Map map, ComponentDirective directive )
    {
        PartReference[] references = directive.getPartReferences();
        for( int i=0; i < references.length; i++ )
        {
            PartReference ref = references[i];
            String key = ref.getKey();
            if( !map.containsKey( key ) )
            {
                map.put( key, ref );
            }
        }
        ComponentDirective parent = directive.getBaseDirective();
        if( null != parent )
        {
            populatePartReferences( map, parent );
        }
        else
        {
            populateTypeReferences( map );
        }
    }

    private void populateTypeReferences( Map map )
    {
        PartReference[] typeRefs = m_type.getPartReferences();
        for( int i=0; i < typeRefs.length; i++ )
        {
            PartReference ref = typeRefs[i];
            String key = ref.getKey();
            if( !map.containsKey( key ) )
            {
                map.put( key, ref );
            }
        }
    }

   /**
    * Return the component thread-safe status.
    *
    * @return the threadsafe status
    * @exception RemoteException if a remote exception occurs
    */
    public boolean isThreadSafe() throws RemoteException
    {
        return m_type.getInfo().isThreadSafe();
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
    
    public void processEvent( EventObject event )
    {
        EventListener[] listeners = super.getEventListeners();
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
        if( getLogger().isTraceEnabled() )
        {
            getLogger().trace( "adding component model listener [" + listener + "]" );
        }
        super.addListener( listener );
        //m_context.addModelListener( listener ); // ???? why not directly on context model ??
    }
    
   /**
    * Remove a listener from the component model.
    * @param listener the model listener
    */
    public void removeModelListener( ModelListener listener )
    {
        if( getLogger().isTraceEnabled() )
        {
            getLogger().trace( "removing component model listener [" + listener + "]" );
        }
        super.removeListener( listener );
        //m_context.removeListener( listener ); // ???? why not directly on context model ??
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
    
    private String setupPath( final String partition, final String name )
    {
        String local = resolveName( name );
        return partition + local;
    }
    
    private String resolveName( final String name )
    {
        if( null != name )
        {
            return name;
        }
        else
        {
            String alt = m_directive.getName();
            if( null == alt )
            {
                return m_type.getInfo().getName();
            }
            else
            {
                return alt;
            }
        }
    }

}

