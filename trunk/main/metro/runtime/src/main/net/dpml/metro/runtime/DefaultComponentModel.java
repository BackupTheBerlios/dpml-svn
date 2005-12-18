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

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.EventObject;

import net.dpml.metro.data.ComponentDirective;
import net.dpml.metro.data.ClassLoaderDirective;
import net.dpml.metro.data.ContextDirective;
import net.dpml.metro.data.CategoriesDirective;
import net.dpml.metro.data.CategoryDirective;
import net.dpml.part.Directive;
import net.dpml.metro.info.CollectionPolicy;
import net.dpml.metro.info.LifestylePolicy;
import net.dpml.metro.info.Type;
import net.dpml.metro.info.PartReference;
import net.dpml.metro.model.ComponentModel;
import net.dpml.metro.model.MutableComponentModel;
import net.dpml.metro.model.ContextModel;
import net.dpml.part.ActivationPolicy;
import net.dpml.part.ControlException;

import net.dpml.configuration.Configuration;
import net.dpml.configuration.Configurable;
import net.dpml.configuration.ConfigurationException;
import net.dpml.configuration.impl.DefaultConfiguration;

import net.dpml.parameters.Parameters;
import net.dpml.parameters.impl.DefaultParameters;

import net.dpml.transit.model.UnknownKeyException;

/**
 * Default implementation of a mutable component model.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class DefaultComponentModel extends UnicastEventSource implements MutableComponentModel, Configurable
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

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

    public DefaultComponentModel( 
      ClassLoader classloader, ComponentController controller, 
      ComponentDirective directive, String partition ) 
      throws ControlException, RemoteException
    {
        super();
        
        m_controller = controller;
        m_path = partition + directive.getName();

        m_directive = directive;
        m_classloader = classloader;
        m_classname = directive.getClassname();
        m_class = m_controller.loadComponentClass( m_classloader, m_classname );
        m_type = m_controller.loadType( m_class );
        
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
            Directive part = m_type.getDirective( key );
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
        m_configuration = configuration;
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

