/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.avalon.activation.impl;

import java.util.Map;
import java.util.Hashtable;
import java.lang.reflect.Constructor;

import org.apache.avalon.activation.RuntimeFactory;
import org.apache.avalon.activation.RuntimeFactoryException;
import org.apache.avalon.activation.Appliance;
import org.apache.avalon.activation.ApplianceRuntimeException;

import org.apache.avalon.composition.model.ModelRuntimeException;
import org.apache.avalon.composition.model.DeploymentModel;
import org.apache.avalon.composition.model.ComponentModel;
import org.apache.avalon.composition.model.ContainmentModel;
import org.apache.avalon.composition.provider.SystemContext;
import org.apache.avalon.composition.provider.LifestyleFactory;
import org.apache.avalon.composition.provider.LifestyleManager;

import org.apache.avalon.repository.Artifact;
import org.apache.avalon.repository.provider.InitialContext;
import org.apache.avalon.repository.provider.Builder;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

/**
 * A factory enabling the establishment of runtime handlers.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultRuntimeFactory.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class DefaultRuntimeFactory implements RuntimeFactory
{
    //-------------------------------------------------------------------
    // static
    //-------------------------------------------------------------------

    private static final Resources REZ =
      ResourceManager.getPackageResources( 
        DefaultRuntimeFactory.class );

    //-------------------------------------------------------------------
    // immutable state
    //-------------------------------------------------------------------

    private final SystemContext m_system;

    private final Map m_map = new Hashtable();

    private final LifestyleFactory m_factory;

    private final boolean m_secure;

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

    public DefaultRuntimeFactory( SystemContext system )
    {
        m_system = system;
        m_factory = new DefaultLifestyleFactory( m_system );
        m_secure = m_system.isCodeSecurityEnabled();
    }

   /**
    * Get the runtime class referenced by the artifact.
    * @param context the repository initial context
    * @param artifact the factory artifact
    * @return the Runtime class
    */
    private LifestyleFactory getLifestyleFactory( 
      SystemContext system, InitialContext context, Artifact artifact )
      throws RuntimeFactoryException
    {
        if( null == artifact )
        {
            return new DefaultLifestyleFactory( system );
        }

        try
        {
            ClassLoader classloader = 
              DefaultRuntimeFactory.class.getClassLoader();
            Builder builder = 
              context.newBuilder( classloader, artifact );
            Class candidate = builder.getFactoryClass();
            return buildLifestyleFactory( context, candidate );
        }
        catch( Throwable e )
        {
            final String error = 
              REZ.getString( "system.error.load", artifact.toString() );
            throw new RuntimeFactoryException( error, e );
        }
    }

   /**
    * Build a lifestyle factory using a supplied class.
    *
    * @param clazz the log target factory class
    * @return a instance of the class
    * @exception RuntimeFactoryException if the class does not expose a public 
    *    constructor, or the constructor requires arguments that the 
    *    builder cannot resolve, or if a unexpected instantiation error 
    *    ooccurs
    */ 
    public LifestyleFactory buildLifestyleFactory( InitialContext context, Class clazz ) 
      throws RuntimeFactoryException
    {
        if( null == clazz )
        {
            throw new NullPointerException( "clazz" );
        }

        Constructor[] constructors = clazz.getConstructors();
        if( constructors.length < 1 ) 
        {
            final String error = 
              REZ.getString( 
                "runtime.error.lifestyle.no-constructor", 
                clazz.getName() );
            throw new RuntimeFactoryException( error );
        }

        //
        // lifestyle factories may declare constructor arguments
        // including the SystemContext, InitialContext, and/or 
        // RuntimeFactory
        //

        Constructor constructor = constructors[0];
        Class[] classes = constructor.getParameterTypes();
        Object[] args = new Object[ classes.length ];
        for( int i=0; i<classes.length; i++ )
        {
            Class c = classes[i];
            if( SystemContext.class.isAssignableFrom( c ) )
            {
                args[i] = m_system;
            }
            else if( InitialContext.class.isAssignableFrom( c ) )
            {
                args[i] = context;
            }
            else if( RuntimeFactory.class.isAssignableFrom( c ) )
            {
                args[i] = this;
            }
            else
            {
                final String error = 
                  REZ.getString( 
                    "runtime.error.unrecognized-runtime-parameter", 
                    c.getName(),
                    clazz.getName() );
                throw new RuntimeFactoryException( error );
            }
        }

        //
        // instantiate the factory
        //

        return instantiateLifestyleFactory( constructor, args );
    }

   /**
    * Instantiation of a lifestyle factory using a supplied constructor 
    * and arguments.
    * 
    * @param constructor the runtime constructor
    * @param args the constructor arguments
    * @return the runtime instance
    * @exception RuntimeFactoryException if an instantiation error occurs
    */
    private LifestyleFactory instantiateLifestyleFactory( 
      Constructor constructor, Object[] args ) 
      throws RuntimeFactoryException
    {
        Class clazz = constructor.getDeclaringClass();
        try
        {
            return (LifestyleFactory) constructor.newInstance( args );
        }
        catch( Throwable e )
        {
            final String error = 
              REZ.getString( 
                "runtime.error.lifestyle-instantiation", 
                clazz.getName() );
            throw new RuntimeFactoryException( error, e );
        }
    }


    //-------------------------------------------------------------------
    // RuntimeFactory
    //-------------------------------------------------------------------

   /**
    * Resolve a runtime handler for a model.
    * @param model the deployment model
    * @return the runtime appliance
    */
    public Appliance getRuntime( DeploymentModel model )
    {
        synchronized( m_map )
        {

            Appliance runtime = getRegisteredRuntime( model );
            if( null != runtime ) 
                return runtime;

            //
            // create the runtime
            // check the model for an overriding runtime using the 
            // standard runtime as the default (not implemented
            // yet)
            //

            if( model instanceof ComponentModel )
            {
                ComponentModel component = (ComponentModel) model;
                LifestyleManager manager = 
                  m_factory.createLifestyleManager( component );
                runtime = newComponentRuntime( component, manager );
            }
            else if( model instanceof ContainmentModel )
            {
                ContainmentModel containment = (ContainmentModel) model;
                runtime = newContainmentRuntime( containment );
            }
            else
            {
                final String error = 
                  REZ.getString( 
                    "runtime.error.unknown-model", 
                    model.toString(),
                    model.getClass().getName() );
                  throw new ModelRuntimeException( error );
            }

            registerRuntime( model, runtime );
            return runtime;
        }
    }

    //-------------------------------------------------------------------
    // private implementation
    //-------------------------------------------------------------------

   /**
    * Resolve a runtime handler for a component model.
    * @param model the containment model
    * @return the runtime handler
    */
    protected Appliance newComponentRuntime( ComponentModel model, LifestyleManager manager )
    {
        return new DefaultAppliance( model, manager, m_secure );
    }

   /**
    * Resolve a runtime handler for a containment model.
    * @param model the containment model
    * @return the runtime handler
    */
    protected Appliance newContainmentRuntime( ContainmentModel model )
      throws ApplianceRuntimeException
    {
        return new DefaultBlock( m_system, model );
    }

   /**
    * Lookup a runtime relative to the model name.
    * @param model the deployment model
    * @return the matching runtime (possibly null)
    */
    private Appliance getRegisteredRuntime( DeploymentModel model )
    {
        String name = model.getQualifiedName();
        return (Appliance) m_map.get( name );
    }

    private void registerRuntime( DeploymentModel model, Appliance runtime )
    {
        String name = model.getQualifiedName();
        m_map.put( name, runtime );
    }
}
