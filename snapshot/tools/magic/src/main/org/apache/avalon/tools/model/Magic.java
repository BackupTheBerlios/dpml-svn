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

package org.apache.avalon.tools.model;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Property;
import org.apache.tools.ant.taskdefs.optional.net.SetProxy;
import org.apache.tools.ant.types.DataType;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * The Magic class is the application root of the magic system.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class Magic extends DataType
{
    //-------------------------------------------------------------
    // static
    //-------------------------------------------------------------

   /**
    * Immutable key to the magic home.
    */
    public static final String KEY = "magic.home";

   /**
    * Immutable key to the gump signature identifier used when magic
    * is executed under control of the gump integration plartform. 
    */
    public static final String GUMP_SIGNATURE_KEY = "gump.signature";

   /**
    * Immutable key to a property declaring a set of repository hosts. 
    */
    public static final String HOSTS_KEY = "magic.hosts";

   /**
    * Immutable key to the magic main cache directory. 
    */
    public static final String CACHE_KEY = "magic.cache";

   /**
    * Immutable key to the magic docs cache directory. 
    */
    public static final String DOCS_KEY = "magic.docs";

   /**
    * Immutable key to the magic template directory. 
    */
    public static final String TEMPLATES_KEY = "magic.templates";

   /**
    * Immutable key to the magic proxy hostname value. 
    */
    public static final String PROXY_HOST_KEY = "magic.proxy.host";

   /**
    * Immutable key to the magic proxy port value. 
    */
    public static final String PROXY_PORT_KEY = "magic.proxy.port";

   /**
    * Immutable key to the magic proxy username value. 
    */
    public static final String PROXY_USERNAME_KEY = "magic.proxy.username";

   /**
    * Immutable key to the magic proxy password value. 
    */
    public static final String PROXY_PASSWORD_KEY = "magic.proxy.password";

    private static Magic SYSTEM;
    private static Home HOME;


   /**
    * Utility operation to return the magic system assigned to the project.
    * @param project the ant project
    * @return the assigned magic system 
    */
    public static Magic getSystem( Project project )
    {
        if( null == SYSTEM )
        {
            SYSTEM = new Magic( project );
        }

        File system = SYSTEM.getSystemDirectory();
        project.setProperty( 
          KEY, system.toString() );

        File main = SYSTEM.getRepository().getCacheDirectory();
        project.setProperty( 
          CACHE_KEY, Context.getCanonicalPath( main ) );

        File docs = SYSTEM.getDocsRepository().getCacheDirectory();
        project.setProperty( 
          DOCS_KEY, Context.getCanonicalPath( docs ) );

        project.setProperty( 
          TEMPLATES_KEY, getTemplatePath( system ) );
        project.addReference( KEY, SYSTEM );
        return SYSTEM;
    }

    //-------------------------------------------------------------
    // immutable state
    //-------------------------------------------------------------

    private final String m_signature;
    private final File m_system;
    private final Repository m_main;
    private final Repository m_docs;
    //private final Map m_homes = new Hashtable();

    //-------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------

    private Magic( final Project project )
    {
        setProject( project );

        m_signature = project.getProperty( GUMP_SIGNATURE_KEY );
        m_system = getSystemDirectory( project );
        project.setProperty( KEY, Context.getCanonicalPath( m_system ) );
        File home = new File( System.getProperty( "user.home" ) );
        File user = new File( home, "magic.properties" );
        loadProperties( project, user );
        File properties = new File( m_system, "magic.properties" );
        loadProperties( project, properties );

        //
        // setup the main and docs cache
        //

        final String hostsPath = project.getProperty( HOSTS_KEY );
        final String[] hosts = getHostsSequence( hostsPath );

        project.setNewProperty( CACHE_KEY, "main" );
        final String cachePath = project.getProperty( CACHE_KEY );
        final File main = Context.getFile( m_system, cachePath );
        m_main = new Repository( main, hosts );

        project.setNewProperty( DOCS_KEY, "docs" );
        final String docsPath = project.getProperty( DOCS_KEY );
        final File docs = Context.getFile( m_system, docsPath );
        m_docs = new Repository( docs, hosts );

        setupProxy( project );

        project.log( "Hosts: " + hosts.length, Project.MSG_VERBOSE );
        for( int i=0; i<hosts.length; i++ )
        {
            project.log( "  host: " + hosts[i], Project.MSG_VERBOSE ); 
        }
        project.log( 
          "cache: " + m_main.getCacheDirectory(), Project.MSG_VERBOSE );
        project.log( 
          "docs: " + m_docs.getCacheDirectory(), Project.MSG_VERBOSE );
    }

    //-------------------------------------------------------------
    // public
    //-------------------------------------------------------------

   /**
    * Return the magic system directory.
    * @return the system directory
    */
    public File getSystemDirectory()
    {
        return m_system;
    }

   /**
    * Return the magic artifact repository cache directory.
    * @return the main cache directory
    */
    public Repository getRepository()
    {
        return m_main;
    }

   /**
    * Return the magic doc repository cache directory.
    * @return the docs cache directory
    */
    public Repository getDocsRepository()
    {
        return m_docs;
    }

   /**
    * Return the gump signature.  If not null magic we assume that 
    * magic is running under gump.
    *
    * @return the gump signature
    */
    public String getGumpSignature()
    {
        return m_signature;
    }

   /**
    * Return the magic home.  Current implementation is restricted to a 
    * a single stqtic home.  Future implementations may relax this restriction.
    *
    * @param project the current project
    * @param value a value used to resolve the home index if needed
    * @return the home instance
    */
    public Home getHome( Project project, String value )
    {
        if( null == HOME )
        {
            File index = getIndexFile( project, value );
            project.log( 
              "Creating home in project: " 
              + project.getName() 
              + " using index ["
              + index
              + "].", Project.MSG_VERBOSE );

            HOME = new Home( project, this, index );
        }
        return HOME;

        /*
        File index = getIndexFile( project, value );
        String path = Context.getCanonicalFile( index ).toString();
        Home home = (Home) m_homes.get( path );
        if( null == home ) 
        {
            project.log( 
              "Creating home in project: " 
              + project.getName() 
              + " using index ["
              + index
              + "].", Project.MSG_VERBOSE );

            home = new Home( project, this, index );
        }
        m_homes.put( path, home );
        return home;
        */
    }

    //-------------------------------------------------------------
    // implementation
    //-------------------------------------------------------------

    private File getIndexFile( Project project, String value )
    {
        File basedir = project.getBaseDir();
        if( null != value )
        {
            File index = Context.getFile( basedir, value );
            return resolve( index, false );
        }

        final String path = project.getProperty( Home.KEY );
        if( null != path )
        {
            final File index = Context.getFile( basedir, path ) ;
            return resolve( index, false );
        }
        else
        {
            //
            // seach from here progressively looking in the parent directory
            // until we find an index
            //

            return resolve( basedir, true );
        }
    }

    private File resolve( File index, boolean traverse )
    {
        if( index.isFile() )
            return index;
        if( index.isDirectory() )
        {
            File file = new File( index, "index.xml" );
            if( file.isFile() ) 
                return file;
            if( traverse )
            {
                File resolved = traverse( index );
                if( resolved != null )
                    return resolved;
            }
        }
            
        final FileNotFoundException fnfe =
          new FileNotFoundException( index.toString() );
        throw new BuildException( fnfe );
    }
        
    private File traverse( File dir )
    {
        File file = new File( dir, "index.xml" );
        if( file.isFile() ) 
            return file;
        File parent = dir.getParentFile();
        if( null != parent )
            return traverse( parent );                
        return null;
    }
    
    private static String getTemplatePath( File system )
    {
        File templates = new File( system, "templates" );
        return Context.getCanonicalPath( templates ); 
    }


    private String[] getHostsSequence( final String path )
    {
        if( null == path )
        {
            return new String[0];
        }
        
        final StringTokenizer tokenizer = new StringTokenizer( path, ";" );
        final ArrayList list = new ArrayList();
        while( tokenizer.hasMoreTokens() )
        {
            final String host = tokenizer.nextToken();
            if( host.endsWith( "/" ) )
            {
                list.add( host );
            }
            else
            {
                list.add( host + "/" );
            }
        }
        return (String[]) list.toArray( new String[0] );
    }

    private void setupProxy( final Project project )
    {
        final String host = project.getProperty( PROXY_HOST_KEY );
        if(( null == host ) || "".equals( host ) )
        {
            return;
        }
        else
        {
            final int port = Integer.decode(
              project.getProperty( PROXY_PORT_KEY ) ).intValue();
            final String username =
              project.getProperty( PROXY_USERNAME_KEY );
            final String password =
              project.getProperty( PROXY_PASSWORD_KEY );
            final SetProxy proxy =
              (SetProxy) project.createTask( "setproxy" );
            proxy.init();
            proxy.setProxyHost( host );
            proxy.setProxyPort( port );
            proxy.setProxyUser( username );
            proxy.setProxyPassword( password );
            proxy.execute();
        }
    }

    private File getSystemDirectory( final Project project )
    {
        final String path = project.getProperty( "magic.home" );
        if( null != path )
        {
            File system = new File( path );
            if( system.exists() && system.isFile() )
            {
                final String error = 
                  "Supplied 'magic.home' value is not directory ["
                  + system;
                throw new BuildException( error );
            }
            return Context.getCanonicalFile( system );
        }

        final Property property = (Property) project.createTask( "property" );
        property.setEnvironment( "env" );
        property.init();
        property.execute();

        final String systemPath = project.getProperty( "env.MAGIC_HOME" );
        if( null != systemPath )
        {
            File system = new File( systemPath );
            return Context.getCanonicalFile( system );
        }
        else
        {
            File user = new File( System.getProperty( "user.home" ) );
            File system = new File( user, ".magic" );
            return Context.getCanonicalFile( system );
        }
    }

   /**
    * Utility method to load properties form a file.
    * @param project the project into which properties shall be loaded
    * @param file the file from which properties will be loaded
    */
    protected void loadProperties( 
      final Project project, final File file ) throws BuildException
    {
        final Property props = (Property) project.createTask( "property" );
        props.init();
        props.setFile( file );
        props.execute();
    }
}
