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

package net.dpml.composition.engine;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.EventObject;

import net.dpml.component.data.ComponentDirective;
import net.dpml.component.data.ValueDirective;
import net.dpml.component.data.ReferenceDirective;
import net.dpml.component.data.ClassLoaderDirective;
import net.dpml.component.data.ClasspathDirective;
import net.dpml.component.data.ContextDirective;
import net.dpml.component.data.Directive;
import net.dpml.component.info.LifestylePolicy;
import net.dpml.component.info.CollectionPolicy;
import net.dpml.component.info.Type;
import net.dpml.component.info.EntryDescriptor;
import net.dpml.component.info.PartReference;
import net.dpml.component.model.ComponentModel;
import net.dpml.component.model.ContextModel;

import net.dpml.configuration.Configuration;

import net.dpml.logging.Logger;

import net.dpml.parameters.Parameters;

import net.dpml.part.Part;
import net.dpml.part.PartException;
import net.dpml.part.ActivationPolicy;
import net.dpml.part.ControlException;

import net.dpml.state.State;
import net.dpml.state.impl.DefaultState;
import net.dpml.state.impl.DefaultStateMachine;

import net.dpml.transit.Category;
import net.dpml.transit.model.Value;
import net.dpml.transit.model.UnknownKeyException;

/**
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
class DefaultComponentModel extends UnicastEventSource implements ComponentModel
{
    // ------------------------------------------------------------------------
    // state
    // ------------------------------------------------------------------------

    private final ComponentController m_controller;
    
    private final Type m_type;
    private final ComponentDirective m_directive;
    private final ClassLoader m_classloader;
    private final String[] m_partKeys;
    private final HashMap m_parts = new HashMap();
    private final ContextModel m_context;
    private final String m_path;
    
    private String m_classname;
    private ActivationPolicy m_activation;
    private LifestylePolicy m_lifestyle;
    private CollectionPolicy m_collection;
    
    private Parameters m_parameters;  // <------------ remove this (covered by context)
    private Configuration m_configuration; // <----- move to a context entry where the resolved value is a Configuration 
    private Class m_class;
    private State m_graph;

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    public DefaultComponentModel( 
      ClassLoader classloader, ComponentController controller, 
      ComponentDirective directive, String partition ) 
      throws PartException, RemoteException
    {
        super();
        
        m_controller = controller;
        m_path = partition + directive.getName();

        m_directive = directive;
        m_classloader = classloader;
        m_classname = directive.getClassname();
        m_class = m_controller.loadComponentClass( m_classloader, m_classname );
        m_type = m_controller.loadType( m_class );
        m_graph = m_controller.loadStateGraph( m_class );
        
        m_activation = directive.getActivationPolicy();
        m_lifestyle = directive.getLifestylePolicy();
        m_collection = directive.getCollectionPolicy();
        m_parameters = directive.getParameters();
        m_configuration = directive.getConfiguration();
        
        ContextDirective context = directive.getContextDirective();
        m_context = new DefaultContextModel( m_classloader, m_type, context );
        
        final String base = m_path + PARTITION_SEPARATOR;
        m_partKeys = getPartKeys( m_type );
        for( int i=0; i < m_partKeys.length; i++ )
        {
            String key = m_partKeys[i];
            Part part = m_type.getPart( key );
            if( part instanceof ComponentDirective )
            {
                ComponentDirective component = (ComponentDirective) part;
                ComponentModel model = m_controller.createComponentModel( m_classloader, base, component );
                m_parts.put( key, model );
            }
            else
            {
                final String error = 
                  "Support for foreign parts not supported at this time.";
                throw new UnsupportedOperationException( error );
            }
        }
    }

    protected void processEvent( EventObject event )
    {
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
        return m_directive.getName();
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
    * Return the immutable state graph for the component.
    * @return the state graph.
    */
    public State getStateGraph()
    {
        return m_graph;
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
        m_activation = policy;
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
        m_collection = policy;
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
    * Return the set of component model keys.
    * @return the component part keys
    */
    public String[] getPartKeys()
    {
        return m_partKeys;
    }

   /**
    * Return the component model of an internal part referenced by the supplied key.
    * @return the internal part component model 
    */
    public ComponentModel getComponentModel( String key ) throws UnknownKeyException
    {
        ComponentModel model = (ComponentModel) m_parts.get( key );
        if( null == model )
        {
            throw new UnknownKeyException( key );
        }
        else
        {
            return model;
        }
    }
    
    public ClassLoaderDirective getClassLoaderDirective()
    {
        return m_directive.getClassLoaderDirective();
    }
    
    public Configuration getConfiguration()
    {
        return m_configuration;
    }

    public Parameters getParameters()
    {
        return m_parameters;
    }

    // ------------------------------------------------------------------------
    // DefaultComponentModel
    // ------------------------------------------------------------------------
    
    ClassLoader getClassLoader()
    {
        return m_classloader;
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

