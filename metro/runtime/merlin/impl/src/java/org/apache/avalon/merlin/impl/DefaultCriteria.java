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

package org.apache.avalon.merlin.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ArrayList;

import org.apache.avalon.merlin.KernelCriteria;
import org.apache.avalon.merlin.KernelRuntimeException;

import org.apache.avalon.repository.Artifact;
import org.apache.avalon.repository.ArtifactHandler;
import org.apache.avalon.repository.BlockHandler;
import org.apache.avalon.repository.provider.InitialContext;

import org.apache.avalon.util.defaults.Defaults;
import org.apache.avalon.util.defaults.DefaultsBuilder;
import org.apache.avalon.util.env.Env;
import org.apache.avalon.util.exception.ExceptionHelper;
import org.apache.avalon.util.criteria.Criteria;
import org.apache.avalon.util.criteria.Parameter;
import org.apache.avalon.util.criteria.PackedParameter;


/**
 * A Criteria is a class holding the values supplied by a user 
 * for application to a factory.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultCriteria.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class DefaultCriteria extends Criteria implements KernelCriteria
{
    //--------------------------------------------------------------
    // static
    //--------------------------------------------------------------

    private static final String AVALON_PROPERTIES = "avalon.properties";
    private static final String MERLIN_PROPERTIES = "merlin.properties";

    private static final File USER_DIR = getBaseDirectory();

    private static final File USER_HOME = 
      new File( System.getProperty( "user.home" ) );

    private static final File TEMP_DIR = 
      new File( System.getProperty( "java.io.tmpdir" ) );

    private static final File AVALON_HOME_DIR = 
      getAvalonHomeDirectory();

    private static final File MERLIN_HOME_DIR = 
      getMerlinHomeDirectory();

   /**
    * Return the avalon home directory using the ${avalon.home}, AVALON_HOME,
    * and fallback ${user.home}/.avalon as the search order.
    * @return the avalon home directory
    */
    private static File getAvalonHomeDirectory()
    {
        return getEnvironment( "AVALON_HOME", "avalon.home", ".avalon" );
    }

   /**
    * Return the merlin home directory using the ${merlin.home}, MERLIN_HOME,
    * and fallback ${user.home}/.merlin as the search order.
    * @return the merlin home directory
    */
    private static File getMerlinHomeDirectory()
    {
        return getEnvironment( "MERLIN_HOME", "merlin.home", ".merlin" );
    }

   /**
    * Return a directory taking into account a supplied env symbol, 
    * a property key and a property filename.  Use the supplied key
    * to locate a system property with falback to the supplied 
    * sysbol, with fallback to the supplied path relative to ${user.home}.
    * 
    * @return the derived directory
    */
    private static File getEnvironment( String symbol, String key, String path )
    {
        try
        {
            String home = 
              System.getProperty( key, Env.getEnvVariable( symbol ) );

            if( null != home ) return new File( home ).getCanonicalFile();

            return new File(
              System.getProperty( "user.home" ) 
              + File.separator 
              + path ).getCanonicalFile();

        }
        catch( Throwable e )
        {
            final String error = 
              "Internal error while attempting to access symbol [" 
              + symbol 
              + "] environment variable.";
            final String message = 
              ExceptionHelper.packException( error, e, true );
            throw new RuntimeException( message );
        }
    }

   /**
    * The factory parameters template.
    * @return the set of parameters constraining the criteria
    */
    private static Parameter[] buildParameters( InitialContext context )
    { 
        return new Parameter[]{
            new Parameter( 
              MERLIN_REPOSITORY,
              File.class, new File( AVALON_HOME_DIR, "repository" ) ),
            new Parameter( 
              MERLIN_HOME,
              File.class, MERLIN_HOME_DIR ),
            new Parameter( 
              MERLIN_SYSTEM,
              File.class, new File( MERLIN_HOME_DIR, "system" ) ),
            new Parameter( 
              MERLIN_CONFIG,
              File.class, new File( MERLIN_HOME_DIR, "config" ) ),
            new PackedParameter( 
              MERLIN_INSTALL, ",", new String[0] ),
            new PackedParameter( 
              MERLIN_DEPLOYMENT, ",", new String[0] ),
            new Parameter( 
              MERLIN_KERNEL, String.class, null ),
            new Parameter( 
              MERLIN_LOGGING_CONFIG, 
              String.class, null ),
            new Parameter( 
              MERLIN_LOGGING_IMPLEMENTATION, 
              String.class, null ),
            new Parameter( 
              MERLIN_RUNTIME, 
              String.class, null ),
            new Parameter( 
              MERLIN_RUNTIME_IMPLEMENTATION, 
              String.class, null ),
            new Parameter( 
              MERLIN_OVERRIDE, String.class, null ),
            new Parameter( 
              MERLIN_DIR, File.class, context.getInitialWorkingDirectory() ),
            new Parameter( 
              MERLIN_TEMP, File.class, TEMP_DIR ),
            new Parameter( 
              MERLIN_CONTEXT, File.class, null ),
            new Parameter( 
              MERLIN_ANCHOR, File.class, null ),
            new Parameter( 
              MERLIN_INFO, Boolean.class, new Boolean( false ) ),
            new Parameter( 
              MERLIN_DEBUG, Boolean.class, new Boolean( false ) ),
            new Parameter( 
              MERLIN_AUDIT, Boolean.class, new Boolean( false ) ),
            new Parameter( 
              MERLIN_PROXY, Boolean.class, new Boolean( true ) ),
            new Parameter( 
              MERLIN_SERVER, Boolean.class, new Boolean( true ) ),
            new Parameter( 
              MERLIN_AUTOSTART, Boolean.class, new Boolean( true ) ),
            new Parameter( 
              MERLIN_LANG, String.class, null ),
            new Parameter( 
              MERLIN_DEPLOYMENT_TIMEOUT, 
              Long.class, new Long( 1000 ) ),
            new Parameter( 
              MERLIN_CODE_SECURITY_ENABLED, 
              Boolean.class, new Boolean( false ) )
         };
    }

    //--------------------------------------------------------------
    // immutable state
    //--------------------------------------------------------------

    private final InitialContext m_context;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

   /**
    * Creation of a new default kernel criteria.
    * @param context the initial repository context
    */
    public DefaultCriteria( InitialContext context )
    {
        super( buildParameters( context ) );

        m_context = context;

        try
        {
            final String key = context.getApplicationKey();
            final File work = context.getInitialWorkingDirectory();
            DefaultsBuilder builder = new DefaultsBuilder( key, work );
            Properties defaults = 
              Defaults.getStaticProperties( 
                DefaultCriteria.class, "/merlin.properties" );

            //
            // set the ${merlin.dir} value 
            //

            defaults.setProperty( 
              "merlin.dir", getWorkingDirectory().toString() );

            //
            // get the consolidated properties
            //

            final String[] keys = super.getKeys();
            Properties properties = 
              builder.getConsolidatedProperties( defaults, keys );

            //
            // expand the properties
            //

            Defaults.macroExpand( properties, new Properties[0] );
            Defaults.macroExpand( properties, new Properties[0] );

            //
            // apply any non-null properties to the criteria
            //

            for( int i=0; i<keys.length; i++ )
            {
                final String propertyKey = keys[i];
                final String value = 
                  properties.getProperty( propertyKey );
                if( null != value )
                {
                    put( propertyKey, value );
                }
            }
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected error while constructing criteria defaults.";
            throw new KernelRuntimeException( error, e );
        }
    }

    //--------------------------------------------------------------
    // Criteria
    //--------------------------------------------------------------

   /**
    * Return a string representation of the kernel criteria.
    * @return the criteria as a string
    */
    public String toString()
    {
        return super.toString();
    }

    //--------------------------------------------------------------
    // KernelCriteria
    //--------------------------------------------------------------

   /**
    * Return the root directory to the shared repository.
    * @return the avalon home root repository directory
    */
    public File getRepositoryDirectory()
    {
        return (File) get( MERLIN_REPOSITORY );
    }

   /**
    * Return the lang code.  A null value indicates that the 
    * default language applies.
    * @return the language code
    */
    public String getLanguageCode()
    {
        return (String) get( MERLIN_LANG );
    }

   /**
    * Return the root directory to the merlin installation
    * @return the merlin home directory
    */
    public File getHomeDirectory()
    {
        return (File) get( MERLIN_HOME );
    }

   /**
    * Return the root directory to the merlin system repository
    * @return the merlin system repository directory
    */
    public File getSystemDirectory()
    {
        return (File) get( MERLIN_SYSTEM );
    }

   /**
    * Return the sequence of deployment urls
    * @return the block urls
    */
    public URL[] getDeploymentURLs()
    {
        String[] blocks = (String[]) get( MERLIN_DEPLOYMENT );

        ArrayList list = new ArrayList();
        File base = getWorkingDirectory();
        for( int i=0; i<blocks.length; i++ )
        {
            String path = blocks[i];
            try
            {
                URL url = resolveURL( base, path );
                list.add( url );
            }
            catch( Throwable e )
            {
                final String error = 
                 "Unable to transform the token: ["
                 + path
                 + "] due to an unexpected error.";
                throw new KernelRuntimeException( error, e );
            }
        }
        return (URL[]) list.toArray( new URL[0] ); 
    }

   /**
    * Return the root directory to the merlin configurations
    * @return the merlin configuration directory
    */
    public File getConfigDirectory()
    {
        return (File) get( MERLIN_CONFIG );
    }

   /**
    * Return the url to the kernel configuration
    * @return the kernel configuration url
    */
    public URL getKernelURL()
    {
        String uri = (String) get( MERLIN_KERNEL );
        if( null == uri )
        {
            File conf = getConfigDirectory();
            if( null == conf ) return null;
            File kernel = new File( conf, "kernel.xml" );
            if( kernel.exists() )
            {
                return toURL( kernel );
            }
            else
            {
                return null;
            }
        }
        else if( uri.indexOf( ":" ) < 0 )
        {
            File target = new File( uri );
            if( target.exists() )
            {
                return toURL( target );
            }
            else
            {
                File base = getWorkingDirectory();
                target = new File( base, uri );
                if( target.exists() )
                {
                    return toURL( target );
                }
                else
                {
                    final String error = 
                      "Kernel file not found [" + target + "].";
                    throw new KernelRuntimeException( error );
                }
            }
        }
        else
        {
            return toURL( uri );
        }
    }

   /**
    * Return an external logging system configuration source
    * @return the configuration url (possibly null)
    */
    public URL getLoggingConfiguration()
    {
        String path = (String) get( MERLIN_LOGGING_CONFIG );
        if( null == path ) return null;
        File base = getWorkingDirectory();
        return resolveURL( base, path );
    }

   /**
    * Return the artifact reference to the logging implementation factory .
    * @return the logging implementation factory artifact
    */
    public Artifact getLoggingImplementation()
    {
        String value = (String) get( MERLIN_LOGGING_IMPLEMENTATION );
        return Artifact.createArtifact( value );
    }

   /**
    * Return the artifact reference to the runtime implementation factory .
    * @return the runtime implementation factory artifact
    */
    public Artifact getRuntimeImplementation()
    {
        String value = (String) get( MERLIN_RUNTIME );
        if( null != value )
        {
            return Artifact.createArtifact( value );
        }
        else
        {
            return getStandardRuntimeImplementation();
        }
    }

   /**
    * Return the artifact reference to the runtime implementation factory .
    * @return the runtime implementation factory artifact
    */
    public Artifact getStandardRuntimeImplementation()
    {
        String value = (String) get( MERLIN_RUNTIME_IMPLEMENTATION );
        return Artifact.createArtifact( value );
    }

   /**
    * Return the url to the configuration override targets.
    * @return the override url
    */
    public String getOverridePath()
    {
        return (String) get( MERLIN_OVERRIDE );
    }

   /**
    * Return the working client directory.
    * @return the working directory
    */
    public File getWorkingDirectory()
    {
        return m_context.getInitialWorkingDirectory();
    }

   /**
    * Return the temporary directory.
    * @return the temp directory
    */
    public File getTempDirectory()
    {
        return (File) get( MERLIN_TEMP );
    }

   /**
    * Return the context directory from which relative 
    * runtime home directories will be established for 
    * components referencing urn:avalon:home
    *
    * @return the context directory
    */
    public File getContextDirectory()
    {
        File context = (File) get( MERLIN_CONTEXT );
        if( null == context )
        {
            return new File( getWorkingDirectory(), "home" );
        }
        else
        {
            return resolveWorkingFile( context );
        }
    }

   /**
    * Return the anchor directory to be used when resolving 
    * library declarations in classload specifications.
    *
    * @return the anchor directory
    */
    public File getAnchorDirectory()
    {
        File anchor = (File) get( MERLIN_ANCHOR );
        return resolveWorkingFile( anchor );
    }

   /**
    * Return info generation policy.  If TRUE the parameters 
    * related to deployment will be listed on startup. 
    *
    * @return the info policy
    */
    public boolean isInfoEnabled()
    {
        Boolean value = (Boolean) get( MERLIN_INFO );
        if( null != value ) return value.booleanValue();
        return false;
    }

   /**
    * Return debug policy.  If TRUE all logging channels will be 
    * set to debug level (useful for debugging).
    *
    * @return the debug policy
    */
    public boolean isDebugEnabled()
    {
        Boolean value = (Boolean) get( MERLIN_DEBUG );
        if( null != value ) return value.booleanValue();
        return false;
    }

   /**
    * Return audit policy.  If TRUE a model listing will be generated.
    *
    * @return the audit policy
    */
    public boolean isAuditEnabled()
    {
        Boolean value = (Boolean) get( MERLIN_AUDIT );
        if( null != value ) return value.booleanValue();
        return false;
    }

   /**
    * Return proxy policy. 
    *
    * @return the proxy policy
    */
    public boolean isProxyEnabled()
    {
        Boolean value = (Boolean) get( MERLIN_PROXY );
        if( null != value ) return value.booleanValue();
        return true;
    }

   /**
    * Return the autostart policy.  If TRUE (the default) the 
    * deployment of the application container will be initiated
    * following kernel initialization.
    *
    * @return the autostart policy
    */
    public boolean isAutostartEnabled()
    {
        Boolean value = (Boolean) get( MERLIN_AUTOSTART );
        if( null != value ) return value.booleanValue();
        return true;
    }

   /**
    * Return server execution policy.  If TRUE the kernel will 
    * continue until explicitly terminated.  If FALSE the kernel
    * will initiate decommissioning on completion of deployment.
    *
    * @return the server execution mode
    */
    public boolean isServerEnabled()
    {
        Boolean value = (Boolean) get( MERLIN_SERVER );
        if( null != value ) return value.booleanValue();
        return false;
    }

   /**
    * Return the default deployment timeout value.
    *
    * @return the default timeout for the component deployment sequence
    */
    public long getDeploymentTimeout()
    {
        Long value = (Long) get( MERLIN_DEPLOYMENT_TIMEOUT );
        if( null == value ) return 1000;
        return value.longValue();
    }

   /**
    * Return the code security enabled status.
    *
    * @return TRUE if code security is enabled - default is false
    */
    public boolean isSecurityEnabled()
    {
        Boolean value = (Boolean) get( MERLIN_CODE_SECURITY_ENABLED );
        if( null != value ) return value.booleanValue();
        return false;
    }

    //--------------------------------------------------------------
    // internal
    //--------------------------------------------------------------

    private File resolveWorkingFile( File file )
    {
        if( null == file ) return getWorkingDirectory();
        if( file.isAbsolute() ) return getCanonicalForm( file );
        
        File relative = new File( getWorkingDirectory(), file.toString() );
        return getCanonicalForm( relative );
    }

    private void printProperties( Properties properties, String label )
    {
        System.out.print( "\n------------ " 
          + label.toUpperCase()
          + "--------------------------------".substring( label.length() ) 
          + "\n\n" );
        if( null == properties ) return;
        Enumeration names = properties.propertyNames();
        while( names.hasMoreElements() )
        {
            String name = (String) names.nextElement();
            System.out.println( "   ${" + name + "} == " 
            + properties.getProperty( name ) );
        }
    }

    private Properties getEnvinronment()
    {
        try
        {
            Properties properties = new Properties();
            setProperty( 
              properties, "avalon.home", 
              Env.getEnvVariable( "AVALON_HOME" ) );
            setProperty( 
              properties, "merlin.home", 
              Env.getEnvVariable( "MERLIN_HOME" ) );
            setProperty( 
              properties, "maven.home.local", 
              Env.getEnvVariable( "MAVEN_HOME_LOCAL" ) );
            return properties;
        }
        catch( Throwable e )
        {
            final String error =
              "Internal error occured while attempting to access system environment.";
            throw new KernelRuntimeException( error, e );
        }
    }

    private void setProperty( 
      Properties properties, String key, String value )
    {
        if( null != value ) properties.setProperty( key, value );
    }

    private Properties getLocalProperties( 
      File dir, String filename ) 
    {
        Properties properties = new Properties();
        if( null == dir ) return properties;
        File file = new File( dir, filename );
        if( !file.exists() ) return properties;
        try
        {
            properties.load( new FileInputStream( file ) );
            return properties;
        }
        catch( Throwable e )
        {
            final String error = 
              "Unexpected exception while attempting to read properties from: " 
              + file;
            throw new KernelRuntimeException( error, e );
        }
    }

   /**
    * Read in the static defined properties that contribute to 
    * the default context value establishment.
    *
    * @return the static default properties
    * @exception KernelRuntimeException if a error occurs while attempt to 
    *    load the property resource
    */
    private Properties getStaticProperties( String path ) 
      throws KernelRuntimeException
    {
        try
        {
            final String resource = "/" + path;
            return Defaults.getStaticProperties( 
              DefaultCriteria.class, resource );
        }
        catch ( IOException e )
        {
            throw new KernelRuntimeException( 
             "Failed to load implementation defaults resource: /"
             + path, e );
        }
    }

    private URL resolveURL( File base, String value )
    {
        if( value.startsWith( "block:" ) )
        {
            return blockSpecToURL( value );
        }
        else if( value.startsWith( "artifact:" ) )
        {
            return artifactSpecToURL( value );
        }

        try
        {
            return new URL( value );
        }
        catch( Exception e )
        {
            File target = new File( value );
            if( target.exists() )
            {
                return toURL( target );
            }
            else
            {
                target = new File( base, value );
                if( target.exists() )
                {
                    return toURL( target );
                }
                else
                {
                    if( e instanceof MalformedURLException )
                    {
                        FileNotFoundException fnfe = new FileNotFoundException( value );
                        final String error = 
                          "Unable to resolve the block path [" + value + "].";
                        throw new KernelRuntimeException( error, fnfe );
                    }
                    else
                    {
                        final String error = 
                          "Unable to resolve the block path [" + value + "].";
                        throw new KernelRuntimeException( error, e );
                    }
                }
            }
        }
    }

    private URL toURL( File file )
    {
        if( null == file ) throw new NullPointerException( "file" );
        try
        {
            return file.getCanonicalFile().toURL();
        }
        catch( Throwable e )
        {
            final String error = 
              "Unable to transform the file [" 
                  + file.toString()
                  + "] to a URL.";
            throw new KernelRuntimeException( error, e );
        }
    }

    private URL blockSpecToURL( String spec )
    {
        if( null == spec ) throw new NullPointerException( "spec" );
        try
        {
            return new URL( null, spec, new BlockHandler() );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unable to transform the block specification [" 
                + spec
                + "] to a URL.";
            throw new KernelRuntimeException( error, e );
        }
    }

    private URL artifactSpecToURL( String spec )
    {
        if( null == spec ) throw new NullPointerException( "spec" );
        try
        {
            return new URL( null, spec, new ArtifactHandler() );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unable to transform the artifact specification [" 
                + spec
                + "] to a URL.";
            throw new KernelRuntimeException( error, e );
        }
    }

    private URL toURL( String spec )
    {
        if( null == spec ) throw new NullPointerException( "spec" );

        try
        {
            return new URL( spec );
        }
        catch( Throwable e )
        {
            final String error = 
              "Unable to construct url from spec [" 
                + spec
                + "].";
            throw new KernelRuntimeException( error, e );
        }
    }

    private static File getBaseDirectory()
    {
        String base = System.getProperty( "basedir" );
        if( null != base )
        {
            return getCanonicalForm( new File( base ) );
        }
        return getCanonicalForm( 
          new File( System.getProperty( "user.dir" ) ) );
    }

    private static File getCanonicalForm( File file )
    {
        try
        {
            return file.getCanonicalFile();
        }
        catch( Throwable e )
        {
            final String error =
              "Unable to resolve cononical representation of: "
              + file; 
            throw new KernelRuntimeException( error, e );
        }
    }
}
