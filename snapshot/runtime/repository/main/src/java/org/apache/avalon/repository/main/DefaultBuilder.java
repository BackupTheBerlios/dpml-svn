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

package org.apache.avalon.repository.main;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import javax.naming.directory.Attributes;

import org.apache.avalon.repository.Artifact;
import org.apache.avalon.repository.Repository;
import org.apache.avalon.repository.RepositoryException;
import org.apache.avalon.repository.RepositoryRuntimeException;
import org.apache.avalon.repository.meta.FactoryDescriptor;
import org.apache.avalon.repository.provider.Builder;
import org.apache.avalon.repository.provider.InitialContext;
import org.apache.avalon.repository.provider.Factory;


/**
 * Application and component bootstrapper used to instantiate, and or invoke
 * Classes and their methods within newly constructed Repository ClassLoaders.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultBuilder.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class DefaultBuilder extends AbstractBuilder implements Builder
{
    //-----------------------------------------------------------
    // public static
    //-----------------------------------------------------------

   /**
    * Resolve the default implementation taking into account 
    * local and home properties, and application defaults.
    * @param classloader the embedding classloader
    * @param base the base directory
    * @param resource a properties filename
    * @param key a property key containing an artifact specification
    * @return the artifact reference
    */
    public static Artifact createImplementationArtifact( 
      ClassLoader classloader, File base, 
      String resource, String key ) throws Exception
    {
        return createImplementationArtifact( 
          classloader, null, base, resource, key );
    }

   /**
    * Resolve the default implementation taking into account 
    * local and home properties, and application defaults.
    * @param classloader the embedding classloader
    * @param system the application system home directory
    * @param base the base directory
    * @param resource a properties filename
    * @param key a property key containing an artifact specification
    * @return the artifact reference
    */
    public static Artifact createImplementationArtifact( 
      ClassLoader classloader, File system, File base, 
      String resource, String key ) throws Exception
    {
        //
        // check for the implementation property in the 
        // working directory
        //

        String spec = 
          getLocalProperties( base, resource ).getProperty( key );
       
        //
        // check for the implementation property in the 
        // user's home directory
        //

        if( null == spec )
        {
            spec = 
              getLocalProperties( USER, resource ).getProperty( key );
        }

        //
        // check for the implementation property in the 
        // applications home directory
        //

        if( null == spec )
        {
            spec = 
              getLocalProperties( system, resource ).getProperty( key );
        }

        //
        // check for the implementation property in the 
        // classloader
        //

        if( null == spec )
        {
            Properties properties = new Properties();
            InputStream input = classloader.getResourceAsStream( resource );
            if( input == null ) 
            {
                final String error = 
                  "Missing resource: [" + resource + "]";
                throw new IllegalStateException( error );
            }
            properties.load( input );
            spec = properties.getProperty( key );
            if( spec == null ) 
            {
                final String error = 
                  "Missing property: [" + key + "] in resource: [" + resource + "]";
                throw new IllegalStateException( error );
            }
        }

        //
        // return the artifact referencing the implementation to be loaded
        //

        return Artifact.createArtifact( spec );
    }

    //-----------------------------------------------------------
    // private static
    //-----------------------------------------------------------

    private static Properties getLocalProperties( 
      File dir, String filename ) throws IOException
    {
        Properties properties = new Properties();
        if( null == dir ) return properties;
        File file = new File( dir, filename );
        if( !file.exists() ) return properties;
        properties.load( new FileInputStream( file ) );
        return properties;
    }

    private static final File USER = 
      new File( System.getProperty( "user.home" ) );

    //-----------------------------------------------------------
    // immutable state
    //-----------------------------------------------------------

   /**
    * The repository established by the loader that will be used 
    * to cache the resources needed to establish the application 
    * classloader.
    */
    private final Repository m_repository;
    
   /**
    * The classloader established by the loader used to load the 
    * the application factory.
    */
    private final ClassLoader m_classloader;
    
   /**
    * The initial repository factory that may be supplied to 
    * application factories as a constructor argument for applications
    * that need to create their own repositories.
    */
    private final InitialContext m_context;
    
   /**
    * The factory class established by the builder.
    */
    private final Class m_class;

   /**
    * The factory instance.
    */
    private final Factory m_delegate;

    //-----------------------------------------------------------
    // constructors
    //-----------------------------------------------------------

   /**
    * Creates a DefaultBuilder for a specific target application.
    * 
    * @param context the initial repository context
    * @param artifact the reference to the application
    * @exception RepositoryException if a app factory creation error occurs
    */
    public DefaultBuilder( InitialContext context, Artifact artifact )
        throws Exception
    {
        this( context, null, artifact );
    }
    
   /**
    * Creates a DefaultBuilder for a specific target application.
    * 
    * @param context the initial repository context
    * @param classloader the parent classloader
    * @param artifact the reference to the application
    * @exception Exception if a app factory creation error occurs
    */
    public DefaultBuilder( 
      InitialContext context, ClassLoader classloader, Artifact artifact )
      throws Exception
    {
        if( null == context ) throw new NullPointerException( "context" );
        if( null == artifact ) throw new NullPointerException( "artifact" );

        m_context = context;

        ClassLoader parent = getClassLoader( classloader );

        m_repository = m_context.getRepository();

        Attributes attributes = m_repository.getAttributes( artifact );
        FactoryDescriptor descriptor = new FactoryDescriptor( attributes );
        String classname = descriptor.getFactory();
        if( null == classname ) 
        {
            final String error = 
              "Required property 'avalon.artifact.factory' not present in artifact: ["
              + artifact + "] under the active repository: [" + m_repository + "].";
            throw new IllegalArgumentException( error );
        }

        m_classloader = m_repository.getClassLoader( parent, artifact );
        m_class = loadFactoryClass( m_classloader, classname );

        if( Factory.class.isAssignableFrom( m_class ) )
        {
            try
            {
                m_delegate = createDelegate( m_classloader, m_class, m_context );
            }
            catch( Throwable e )
            {
                final String error = 
                  "Unable to establish a factory for the supplied artifact:";
                StringBuffer buffer = new StringBuffer( error );
                buffer.append( "\n artifact: " + artifact );
                buffer.append( "\n build: " + descriptor.getBuild() );
                buffer.append( "\n factory: " + descriptor.getFactory() );
                buffer.append( "\n source: " 
                  + m_class.getProtectionDomain().getCodeSource().getLocation() );
                buffer.append( "\n repository: " + m_repository );
                throw new RepositoryException( buffer.toString(), e );
            }
        }
        else
        {
            m_delegate = null;
        }
    }

    //-----------------------------------------------------------
    // Builder
    //-----------------------------------------------------------

   /**
    * Return the primary class established by the builder.
    * @return the main class
    */
    public Class getFactoryClass()
    {
        return m_class;
    }

   /**
    * Return the factory established by the loader.
    * @return the delegate factory
    * @exception RepositoryRuntimeException if the declared class does 
    *    not implement the factory interface
    * @see #getFactoryClass
    */
    public Factory getFactory()
    {
        if( null != m_delegate )
        {
            return m_delegate;
        }
        else
        {
            final String error = 
              "Supplied class [" + m_class.getName() 
              + "] does not implement the Factory interface.";
            throw new RepositoryRuntimeException( error );
        }
    }

    /**
     * Gets the ClassLoader used by this Bootstrapper.
     * 
     * @return the ClassLoader built by the Repository
     */
    public ClassLoader getClassLoader()
    {
        return m_classloader;
    }
    
    //-----------------------------------------------------------
    // internal
    //-----------------------------------------------------------

    private ClassLoader getClassLoader( ClassLoader classloader )
    {
        if( null != classloader ) return classloader;
        return DefaultBuilder.class.getClassLoader();
    }
}
