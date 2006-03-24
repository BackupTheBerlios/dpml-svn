/*
 * Copyright 2005 Stephen J. McConnell.
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
import java.util.HashMap;
import java.util.EventObject;
import java.util.EventListener;
import java.util.Map;

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

import net.dpml.component.Directive;
import net.dpml.component.ActivationPolicy;
import net.dpml.component.ControlException;
import net.dpml.component.ModelListener;
import net.dpml.component.ModelEvent;

import net.dpml.configuration.Configuration;
import net.dpml.configuration.Configurable;
import net.dpml.configuration.ConfigurationException;
import net.dpml.configuration.impl.DefaultConfiguration;

import net.dpml.logging.Logger;

import net.dpml.parameters.Parameters;
import net.dpml.parameters.impl.DefaultParameters;

import net.dpml.lang.Classpath;
import net.dpml.lang.UnknownKeyException;

/**
 * Default implementation of a mutable component model.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class DefaultComponentModel extends UnicastEventSource 
  implements ComponentModelManager, Configurable
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private final ComponentController m_controller;
    
    private final Type m_type;
    private final ComponentDirective m_directive;
    private final ClassLoader m_classloader;
    private final Classpath m_classpath;
    
    private final HashMap m_parts = new HashMap();
    private final DefaultContextModel m_context;
    private final String m_path;
    
    private String m_classname;
    private ActivationPolicy m_activation;
    private LifestylePolicy m_lifestyle;
    private CollectionPolicy m_collection;
    
    private Parameters m_parameters;  // <------------ remove this (covered by context)
    private Configuration m_configuration; // <----- move to a context entry where the resolved value is a Configuration 
    private Class m_class;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    public DefaultComponentModel( 
      ClassLoader anchor, ComponentController controller, Classpath classpath, 
      ComponentDirective directive, String partition ) 
      throws ControlException, IOException, RemoteException
    {
        super( new StandardLogger( partition.replace( '/', '.' ) ) );
        
        m_classpath = classpath;
        m_controller = controller;
        m_directive = directive;

        m_classloader = m_controller.getClassLoader( anchor, classpath );
        m_classname = directive.getClassname();
        m_class = m_controller.loadComponentClass( m_classloader, m_classname );
        m_type = m_controller.loadType( m_class );
        
        String name = directive.getName();
        if( null == name )
        {
            m_path = partition + m_type.getInfo().getName();
        }
        else
        {
            m_path = partition + name;
        }
        
        m_activation = directive.getActivationPolicy();
        m_lifestyle = directive.getLifestylePolicy();
        m_collection = directive.getCollectionPolicy();
        m_parameters = directive.getParameters();
        m_configuration = directive.getConfiguration();
        
        ContextDirective context = directive.getContextDirective();
        Logger logger = getLogger();
        m_context = new DefaultContextModel( this, logger, m_classloader, m_type, context );
        
        final String base = m_path + PARTITION_SEPARATOR;
        processParts( controller, m_classloader, m_type, m_parts, base );
        processParts( controller, m_classloader, m_directive, m_parts, base );
    }
    
    private void processParts(
      ComponentController controller, ClassLoader classloader, Composite composite, Map map, String base )
      throws ControlException, RemoteException
    {
        PartReference[] references = composite.getPartReferences();
        for( int i=0; i < references.length; i++ )
        {
            PartReference ref = references[i];
            String key = ref.getKey();
            Directive part = composite.getDirective( key );
            if( part instanceof ComponentDirective )
            {
                Classpath classpath = new Classpath();
                ComponentDirective component = (ComponentDirective) part;
                ComponentModel model = 
                  controller.createComponentModel( classloader, classpath, base, component );
                map.put( key, model );
            }
            else
            {
                final String error = 
                  "Foreign part [" 
                  + part.getClass() 
                  + "] not supported.";
                throw new UnsupportedOperationException( error );
            }
        }
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
    // Configurable
    // ------------------------------------------------------------------------

    /**
     * Set the component model configuration.
     *
     * @param configuration the model configuration argument.
     * @throws ConfigurationException if an error occurs
     * @throws NullPointerException if  the supplied configuration argument is null
     */
    public void configure( Configuration configuration )
        throws ConfigurationException, NullPointerException
    {
        if( null == configuration )
        {
            throw new NullPointerException( "configuration" );
        }
        
        Configuration old = m_configuration;
        m_configuration = configuration;
        ModelEvent event = new ModelEvent( this, "model.configuration", old, configuration );
        enqueueEvent( event );
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
    * Return the internal part keys.
    * @return the component part keys
    */
    public String[] getPartKeys()
    {
        return (String[]) m_parts.keySet().toArray( new String[0] );
    }
    
   /**
    * Return the component model of an internal part referenced by the supplied key.
    * @return the internal part component model 
    */
    public ComponentModelManager getComponentManager( String key ) throws UnknownKeyException
    {
        ComponentModelManager model = (ComponentModelManager) m_parts.get( key );
        if( null == model )
        {
            throw new UnknownKeyException( key );
        }
        else
        {
            return model;
        }
    }
    
    public Configuration getConfiguration()
    {
        if( null == m_configuration )
        {
            return new DefaultConfiguration( "configuration", null );
        }
        else
        {
            return m_configuration;
        }
    }

    public Parameters getParameters()
    {
        if( null == m_parameters )
        {
            return DefaultParameters.EMPTY_PARAMETERS;
        }
        else
        {
            return m_parameters;
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
    
   /**
    * Return the internal component models.
    * @return the internal component model array
    */
    ComponentModel[] getComponentModels()
    {
        return (ComponentModel[]) m_parts.values().toArray( new ComponentModel[0] );
    }

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
    
    private String[] getPartKeys( Type type )
    {
        PartReference[] references = m_type.getPartReferences();
        String[] keys = new String[ references.length ];
        for( int i=0; i<references.length; i++ )
        {
            keys[i] = references[i].getKey();
        }
        return keys;
    }
}

