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

package net.dpml.magic.model;

import java.io.IOException;
import java.io.File;

import net.dpml.magic.AntFileIndex;

import net.dpml.transit.NullArgumentException;

import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;



/**
 * A definition is an immutable description of a project including its name,
 * group, version, structrual dependencies and plugin dependencies.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class Definition extends Resource
{
    private final ResourceRef[] m_plugins;
    private final File m_basedir;
    private final String m_path;
    private final String m_build;

   /**
    * Creation of a new definition relative to a supplied home, a unique project key,
    * project base directory and path, statatory info, dependencies and
    * plugin assumptions.
    *
    * @param index the index into which this project is bound
    * @param key a key unique with the home that identifies this project
    * @param basedir the base directory relative to the index file that this project is defined
    * @param path the basedir as a relative path
    * @param info a descriptor of the name, grolup, version and delivery status
    * @param resources the set of resource dependencies
    * @param plugins the set of plugin dependencies
    * @param uri containing module uri
    */
    public Definition(
      final AntFileIndex index, final String key, final File basedir, String build,
      final String path, final Info info,
      final ResourceRef[] resources, final ResourceRef[] plugins, String uri )
    {
        super( index, key, info, resources, uri );

        m_basedir = basedir;
        m_plugins = plugins;
        m_path = path;
        m_build = build;
    }

   /**
    * Return the base directory relative to main index file that this defintion is
    * established within.
    * @return the relative base directory path
    */
    public String getBasePath()
    {
        return m_path;
    }

   /**
    * Return the build file to use to construct the project.
    * @return the project build file (possibly null)
    */
    public String getBuildFile()
    {
        return m_build;
    }

   /**
    * Return the build file location pointer.
    * @return the location
    */
    public Location getLocation()
    {
        if( null == getBuildFile() )
        {
            File build = new File( getBaseDir(), "build.xml" );
            try
            {
                return new Location( build.getCanonicalPath() );
            }
            catch( IOException e )
            {
                return new Location( build.toString() );
            }
        }
        else
        {
            File build = new File( getBaseDir(), getBuildFile() );
            try
            {
                return new Location( build.getCanonicalPath() );
            }
            catch( IOException e )
            {
                return new Location( build.toString() );
            }
        }
    }

   /**
    * Return the base directory as an absolute file
    * @return the absolute base directory
    */
    public File getBaseDir()
    {
        return m_basedir;
    }

   /**
    * Returns the classpath.  If the defintion type is a module
    * the path returned is the aggregation of project classpaths
    * from all projects with the same or deeper group, otherwise
    * the path corresponds to external resource reference path entries.
    *
    * @return the classpath
    * @exception NullArgumentException if the supplied project argument is null.
    */
    public Path getClassPath( Project project )
        throws NullArgumentException
    {
        if( null == project )
        {
            throw new NullArgumentException( "project" );
        }

        Path path = getPath( project, Policy.BUILD );

        if( getInfo().isa( "module" ) )
        {
            Definition[] defs = getIndex().getSubsidiaryDefinitions( this );
            for( int i=0; i<defs.length; i++ )
            {
                Definition d = defs[i];
                path.add( d.getClassPath( project ) );
            }
        }

        return path;
    }

   /**
    * Return the set of plugin references that this defintion declares
    * @return the set of plugin references referencing plugins needed
    *   as part of the project build
    */
    public ResourceRef[] getPluginRefs()
    {
        return m_plugins;
    }

   /**
    * Return TRUE is this defintionj is equal to a supplied defintion
    * @return the equality status
    */
    public boolean equals( final Object other )
    {
        if( super.equals( other ) && ( other instanceof Definition ))
        {
            final Definition def = (Definition) other;

            final ResourceRef[] plugins = getPluginRefs();
            final ResourceRef[] plugins2 = def.getPluginRefs();

            if( plugins.length != plugins2.length )
            {
                return false;
            }
            for( int i=0; i<plugins.length; i++ )
            {
                if( !plugins[i].equals( plugins2[i] ) ) return false;
            }

            return true;
        }
        return false;
    }
}
