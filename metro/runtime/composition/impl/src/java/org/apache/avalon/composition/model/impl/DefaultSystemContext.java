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

package org.apache.avalon.composition.model.impl;

import java.io.File;
import java.util.Map;
import java.lang.reflect.Constructor;

import org.apache.avalon.logging.provider.LoggingManager;

import org.apache.avalon.composition.data.SecurityProfile;
import org.apache.avalon.composition.model.DeploymentModel;
import org.apache.avalon.composition.model.impl.DefaultModelFactory;
import org.apache.avalon.composition.provider.ModelFactory;
import org.apache.avalon.composition.provider.SystemContext;
import org.apache.avalon.composition.provider.SystemException;
import org.apache.avalon.composition.provider.Runtime;

import org.apache.avalon.repository.Artifact;
import org.apache.avalon.repository.Repository;
import org.apache.avalon.repository.provider.InitialContext;
import org.apache.avalon.repository.provider.Builder;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.DefaultContext;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

/**
 * Implementation of a system context that exposes a system wide set of parameters.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultSystemContext.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class DefaultSystemContext extends DefaultContext 
  implements SystemContext
{
    //--------------------------------------------------------------
    // static
    //--------------------------------------------------------------

    private static final Resources REZ =
            ResourceManager.getPackageResources( DefaultSystemContext.class );

    //--------------------------------------------------------------
    // immutable state
    //--------------------------------------------------------------

    private final File m_base;

    private final File m_home;

    private final File m_temp;

    private final File m_anchor;

    private final Repository m_repository;

    private final ClassLoader m_system;

    private final ClassLoader m_common;

    private final LoggingManager m_logging;

    private final Logger m_logger;

    private final ModelFactory m_factory;

    private final Runtime m_runtime;

    private final long m_timeout;

    private final boolean m_secure;

    private final boolean m_proxies;

    //--------------------------------------------------------------
    // mutable state
    //--------------------------------------------------------------

    private boolean m_trace;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

   /**
    * Creation of a new system context.
    *
    * @param context the repository intial context
    * @param parent the parent context object (possibly null)
    * @param artifact an artifact reference to a plugin runtime factory
    * @param lifestyle an artifact reference to a plugin lifestyle factory
    * @param runtime the runtime class
    * @param logging the logging manager
    * @param base the base directory from which relative references 
    *   within a classpath or library directive shall be resolved
    * @param home the home directory
    * @param temp the temp directory
    * @param repository the application repository to be used when resolving 
    *   resource directives
    * @param category the kernel logging category name
    * @param trace flag indicating if internal logging is enabled
    * @param proxies flag indicating if proxy creation is enabled
    * @param timeout a system wide default deployment timeout
    * @param security the security profiles
    */
    DefaultSystemContext( 
      InitialContext context, 
      Context parent, 
      Artifact artifact, 
      Artifact lifestyle, 
      Class runtime, 
      LoggingManager logging, 
      File base, 
      File home, 
      File temp, 
      File anchor, 
      Repository repository, 
      String category, 
      boolean trace, 
      boolean proxies, 
      long timeout, 
      boolean secure, 
      SecurityProfile[] security,
      Map grants ) throws SystemException
    {
        super( parent );

        assertNotNull( "context", context );
        assertNotNull( "base", base );
        assertNotNull( "anchor", anchor );
        assertNotNull( "repository", repository );
        assertNotNull( "logging", logging );
        assertNotNull( "category", category );
        assertNotNull( "security", security );
        assertNotNull( "grants", grants );

        if( !base.isDirectory() )
        {
            final String error = 
              REZ.getString( "system.error.base-not-a-directory", base  );
            throw new IllegalArgumentException( error );
        }

        m_base = base;
        m_home = home;
        m_temp = temp;
        m_anchor = anchor;
        m_trace = trace;
        m_proxies = proxies;
        m_repository = repository;
        m_logging = logging;
        m_timeout = timeout;

        m_logger = m_logging.getLoggerForCategory( category );
        m_system = SystemContext.class.getClassLoader();
        m_common = DeploymentModel.class.getClassLoader();

        m_secure = secure;

        if( m_secure )
        {
            if( System.getSecurityManager() == null )
            {
                System.setSecurityManager( new SecurityManager() );
            }
        }

        m_factory = new DefaultModelFactory( this, security, grants );

        //
        // use avalon-repository to load the runtime
        //

        if( null != runtime )
        {
            m_runtime = buildRuntimeInstance( context, runtime, lifestyle );
        }
        else if( null != artifact )
        {
            Class clazz = resolveRuntimeClass( context, artifact );
            m_runtime = buildRuntimeInstance( context, clazz, lifestyle );
        }
        else
        {
            m_runtime = null;
        }
    }

    //--------------------------------------------------------------
    // SystemContext
    //--------------------------------------------------------------

   /**
    * Return the enabled status of the code security policy.
    * @return the code security enabled status
    */
    public boolean isCodeSecurityEnabled()
    {
        return m_secure;
    }

   /**
    * Return the model factory.
    *
    * @return the factory
    */
    public ModelFactory getModelFactory()
    {
        return m_factory;
    }

   /**
    * Return the base directory from which relative classloader 
    * references may be resolved.
    *
    * @return the base directory
    */
    public File getBaseDirectory()
    {
        return m_base;
    }

   /**
    * Return the working directory from which containers may 
    * establish persistent content.
    *
    * @return the working directory
    */
    public File getHomeDirectory()
    {
        return m_home;
    }

   /**
    * Return the temporary directory from which a container 
    * may use to establish a transient content directory. 
    *
    * @return the temporary directory
    */
    public File getTempDirectory()
    {
        return m_temp;
    }

   /**
    * Return the anchor directory from which a container 
    * may use to resolve relative classpath references.
    *
    * @return the anchor directory
    */
    public File getAnchorDirectory()
    {
        return m_anchor;
    }

   /**
    * Return the system wide repository from which resource 
    * directives can be resolved.
    *
    * @return the repository
    */
    public Repository getRepository()
    {
        return m_repository;
    }

   /**
    * Return the API classloader.
    *
    * @return the system classloader
    */
    public ClassLoader getAPIClassLoader()
    {
        return m_common;
    }

   /**
    * Return the SPI classloader. 
    *
    * @return the system classloader
    */
    public ClassLoader getSPIClassLoader()
    {
        return m_system;
    }

   /**
    * Return the system trace flag.
    *
    * @return the trace flag
    */
    public boolean isTraceEnabled()
    {
        return m_trace;
    }

   /**
    * Return the system proxy enablement flag.
    *
    * @return the proxy flag
    */
    public boolean isProxyEnabled()
    {
        return m_proxies;
    }

   /**
    * Set the system trace flag.
    *
    * @param trace the trace flag
    */
    public void setTraceEnabled( boolean trace )
    {
        m_trace = trace;
    }

   /**
    * Return the logging manager.
    *
    * @return the logging manager.
    */
    public LoggingManager getLoggingManager()
    {
        return m_logging;
    }

   /**
    * Return the system logging channel.
    *
    * @return the system logging channel
    */
    public Logger getLogger()
    {
        return m_logger;
    }

   /**
    * Return the default deployment phase timeout value.
    * @return the timeout value
    */
    public long getDefaultDeploymentTimeout()
    {
        return m_timeout;
    }

    //------------------------------------------------------------------
    // runtime operations
    //------------------------------------------------------------------

   /**
    * Request the commissioning of a runtime for a supplied deployment 
    * model.
    * @param model the deployment model 
    * @exception Exception of a commissioning error occurs
    */
    public void commission( DeploymentModel model ) throws Exception
    {
        getRuntime().commission( model );
    }

   /**
    * Request the decommissioning of a runtime for a supplied deployment 
    * model.
    * @param model the deployment model 
    * @exception Exception of a commissioning error occurs
    */
    public void decommission( DeploymentModel model )
    {
        getRuntime().decommission( model );
    }

   /**
    * Request resolution of an object from the runtime.
    * @param model the deployment model
    * @exception Exception if a deployment error occurs
    */
    public Object resolve( DeploymentModel model ) throws Exception
    {
        return getRuntime().resolve( model );
    }

   /**
    * Request resolution of an object from the runtime.
    * @param model the deployment model
    * @param proxy if TRUE the return value will be proxied if the 
    *   underlying component typoe suppports proxy representation 
    * @exception Exception if a deployment error occurs
    */
    public Object resolve( DeploymentModel model, boolean proxy ) throws Exception
    {
        return getRuntime().resolve( model, proxy );
    }

   /**
    * Request the release of an object from the runtime.
    * @param model the deployment model
    * @param instance the object to release
    * @exception Exception if a deployment error occurs
    */
    public void release( DeploymentModel model, Object instance )
    {
        getRuntime().release( model, instance );
    }

   /**
    * Prepare a string representation of an object for presentation.
    * @param object the object to parse
    * @return the presentation string
    */
    public String toString( Object object )
    {
        if( object == null ) return "";

        if( object instanceof String )
        {
            return processString( (String) object );
        }
        else
        {
            return processString( object.toString() );
        }
    }

   /**
    * Prepare a string representation of an object for presentation.
    * @param name the value to parse
    * @return the presentation string
    */
    public String processString( String name )
    {
        if( name == null ) return "";

        String str = name.replace( '\\', '/' );

        String base = getBaseDirectory().toString().replace( '\\', '/' );
        if( str.indexOf( base ) > -1 )
        {
            return getString( str, base, "${merlin.dir}" );
        }

        final String dir = 
          System.getProperty( "user.dir" ).replace( '\\', '/' );
        if( str.indexOf( dir ) > -1 )
        {
            return getString( str, dir, "${user.dir}" );
        }
        
        return name;
    }

   /**
    * Prepare a string representation of an object array for presentation.
    * @param objects the array of objects
    * @return the presentation string
    */
    public String toString( Object[] objects )
    {
        StringBuffer buffer = new StringBuffer();
        for( int i=0; i<objects.length; i++ )
        {
            if( i > 0 ) buffer.append( ";" );
            buffer.append( toString( objects[i] ) );
        }
        return buffer.toString();
    }

    //------------------------------------------------------------------
    // runtime operations
    //------------------------------------------------------------------

    private String getString( String name, String pattern, String replacement )
    {
        final int n = name.indexOf( pattern );
        if( n == -1 ) return name;
        if( name.startsWith( pattern ) )
        {
            return replacement + name.substring( pattern.length() );
        }
        else
        {
            String header = name.substring( 0, n );
            String tail = name.substring( n + pattern.length() );
            return header + replacement + tail; 
        }
    }

   /**
    * Return the runtime factory.
    *
    * @return the factory
    */
    private Runtime getRuntime()
    {
        if( null == m_runtime ) 
        {
            throw new IllegalStateException( "runtime" );
        }
        else
        {
            return m_runtime;
        }
    }

    private Class resolveRuntimeClass( InitialContext context, Artifact artifact )
      throws SystemException
    {
        if( null != artifact )
        {
            return getRuntimeClass( context, artifact );
        }
        else
        {
            return getRuntimeClass( 
              context, getDefaultRuntimeArtifact( context ) );
        }
    }

   /**
    * Get the runtime class referenced by the artifact.
    * @param context the repository initial context
    * @param artifact the factory artifact
    * @return the Runtime class
    */
    private Class getRuntimeClass( InitialContext context, Artifact artifact )
      throws SystemException
    {
        if( null == artifact )
        {
            throw new NullPointerException( "artifact" );
        }

        try
        {
            ClassLoader classloader = 
              DefaultSystemContext.class.getClassLoader();
            Builder builder = 
              context.newBuilder( classloader, artifact );
            return builder.getFactoryClass();
        }
        catch( Throwable e )
        {
            final String error = 
              REZ.getString( "system.error.load", artifact.toString() );
            throw new SystemException( error, e );
        }
    }

    private Artifact getDefaultRuntimeArtifact( InitialContext context )
    {
        Artifact[] artifacts = 
          context.getRepository().getCandidates( Runtime.class );
        if( artifacts.length > 0 ) return artifacts[0];

        final String error =
          "Initial context does not declare an artifact for the class: ["
          + Runtime.class.getName() + "].";
        throw new IllegalStateException( error );
    }

   /**
    * Build a runtime using a supplied class.
    *
    * @param context the initial context from which to bootstrap the runtime
    * @param clazz the runtime factory class
    * @param lifestyle the artifact referencing the lifestyle factory
    * @return a instance of the class
    * @exception SystemException if the class does not expose a public 
    *    constructor, or the constructor requires arguments that the 
    *    builder cannot resolve, or if a unexpected instantiation error 
    *    ooccurs
    */ 
    public Runtime buildRuntimeInstance( 
      InitialContext context, Class clazz, Artifact lifestyle ) 
      throws SystemException
    {
        if( null == clazz ) return null;

        Constructor[] constructors = clazz.getConstructors();
        if( constructors.length < 1 ) 
        {
            final String error = 
              REZ.getString( 
                "system.error.runtime.no-constructor", 
                clazz.getName() );
            throw new SystemException( error );
        }

        //
        // runtime class may has one constructor
        //

        Constructor constructor = constructors[0];
        Class[] classes = constructor.getParameterTypes();
        Object[] args = new Object[ classes.length ];
        for( int i=0; i<classes.length; i++ )
        {
            Class c = classes[i];
            if( SystemContext.class.isAssignableFrom( c ) )
            {
                args[i] = this;
            }
            else if( InitialContext.class.isAssignableFrom( c ) )
            {
                args[i] = context;
            }
            else if( Artifact.class.isAssignableFrom( c ) )
            {
                args[i] = lifestyle;
            }
            else
            {
                final String error = 
                  REZ.getString( 
                    "system.error.unrecognized-runtime-parameter", 
                    c.getName(),
                    clazz.getName() );
                throw new SystemException( error );
            }
        }

        //
        // instantiate the factory
        //

        return instantiateRuntime( constructor, args );
    }

   /**
    * Instantiation of a runtime using a supplied constructor 
    * and arguments.
    * 
    * @param constructor the runtime constructor
    * @param args the constructor arguments
    * @return the runtime instance
    * @exception SystemException if an instantiation error occurs
    */
    private Runtime instantiateRuntime( 
      Constructor constructor, Object[] args ) 
      throws SystemException
    {
        Class clazz = constructor.getDeclaringClass();
        try
        {
            return (Runtime) constructor.newInstance( args );
        }
        catch( Throwable e )
        {
            final String error = 
              REZ.getString( 
                "system.error.runtime-instantiation", 
                clazz.getName() );
            throw new SystemException( error, e );
        }
    }

    private void assertNotNull( String name, Object object )
    {
        if( null == object ) 
        {
            throw new NullPointerException( name );
        } 
    }
}
