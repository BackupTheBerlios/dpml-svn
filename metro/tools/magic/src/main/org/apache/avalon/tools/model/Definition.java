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

import java.io.File;

/**
 * A definition is an immutable description of a project including its name, 
 * group, version, structrual dependencies, plugin dependecies, and gump 
 * idiosyncrasies. 
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class Definition extends Resource
{
    private ResourceRef[] m_plugins;
    private File m_basedir;
    private String m_path;

   /**
    * Creation of a new definition relative to a supplied home, a unique project key, 
    * project base directory and path, statatory info, gump extras, depednecies and 
    * plugin assumptions.
    *
    * @param home the home into whjich this project is bound
    * @param key a key unique with the home that identifies this project
    * @param basedir the base directory relative to the index file that this project is defined
    * @param path the basedir as a relative path
    * @param info a descriptor of the name, grolup, version and delivery status
    * @param gump addition gump idiosyncracies
    * @param resources the set of resource dependencies
    * @param plugins the set of plugin dependencies
    */
    public Definition( 
      final Home home, final String key, final File basedir, final String path, final Info info,
      final Gump gump, final ResourceRef[] resources, final ResourceRef[] plugins )
    {
        super( home, key, info, gump, resources );

        m_basedir = basedir;
        m_plugins = plugins;
        m_path = path;
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
    * Return the base directory as an absolute file
    * @return the relative base directory
    */
    public File getBaseDir()
    {
        return m_basedir;
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
    * Return the filename of the documentation directory relative to the 
    * common magic dumumentation cache for this project.
    * @return the projects doc destination directory
    */
    public File getDocDirectory()
    {
        final File cache = getHome().getDocsRepository().getCacheDirectory();
        final String spec = getInfo().getSpecification( "/", "/" );
        return new File( cache, spec );
    }

   /**
    * Return the string representation of this defintion.
    * @return the string representation
    */
    public String toString()
    {
        return "[" + getInfo().getGroup() + "/" + getInfo().getName() + "]";
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
            for( int i=0; i<plugins.length; i++ )
            {
                if( !plugins[i].equals( plugins2[i] ) ) return false;
            }
            return true;
        }
        return false;
    }
}
