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

package org.apache.avalon.tools.tasks;

import org.apache.avalon.tools.model.Context;
import org.apache.avalon.tools.model.Definition;
import org.apache.avalon.tools.model.Info;
import org.apache.avalon.tools.model.Home;
import org.apache.avalon.tools.model.Magic;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import java.io.File;

/**
 * Load a goal. 
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class HomeTask extends ContextualTask
{
    private static final String INDEX_PROPERTIES = "index.properties";

    private String m_path;

    public HomeTask()
    {
    }
    
    public void setIndex( final String path )
    {
        m_path = path;
    }

    public void execute() 
        throws BuildException
    {
        final Project project = getProject();

        if( null != getProject().getReference( Home.KEY ) ) 
            return;

        Magic system = Magic.getSystem( project );
        Home home = system.getHome( project, m_path );
        File root = getHomeDirectory( home );

        project.setProperty( Home.HOME_KEY, getHomePath( home ) );
        project.setProperty( Home.INDEX_KEY, getIndexPath( home ) );

        File homeProperties = new File( root, INDEX_PROPERTIES );
        loadProperties( project, homeProperties );

        File userHomeProperties = new File( root, USER_PROPERTIES );
        loadProperties( project, userHomeProperties );

        project.addReference( Home.KEY, home );

        final String key = getKey();
        if( home.isaResourceKey( key ) )
        {
            final Definition def = home.getDefinition( getKey() );
            verifyBaseDir( def );
            final Info info = def.getInfo();
            final String name = info.getName();
            final String group = info.getGroup();
            final String version = info.getVersion();
            if( null != version )
            {
                project.setProperty( "project.version", version );
            }
            else
            {
                project.setProperty( "project.version", "" );
            }
            project.setProperty( "project.key", def.getKey() );
            project.setProperty( "project.name", name );
            project.setProperty( "project.group", group );
            project.setProperty( "project.basedir", def.getBaseDir().toString() );
            project.setProperty( "project.path", info.getPath() );
            project.setProperty( "project.uri", info.getURI() );
            project.setProperty( "project.spec", info.getSpec() );
            project.setProperty( "project.type", info.getType() );
            project.setProperty( "project.filename", info.getFilename() );
            project.setProperty( "project.short-filename", info.getShortFilename() );
        }
    }

    private File getHomeDirectory( Home home )
    {
        return home.getIndex().getParentFile();
    }

    private String getHomePath( Home home )
    {
        return home.getIndex().getParentFile().toString();
    }

    private String getIndexPath( Home home )
    {
        return home.getIndex().toString();
    }
    
    private void verifyBaseDir( Definition def )
        throws BuildException
    {
        String defBase = Context.getCanonicalPath( def.getBaseDir() );
        String projBase = Context.getCanonicalPath( getProject().getBaseDir() );
        if( defBase.equals( projBase ) )
            return;
        throw new BuildException( 
          "The basedir ["
          + defBase 
          + "] declared in the project index for the key [" 
          + def.getKey() 
          + "] does not correspond with the current working directory ["
          + projBase 
          + "]. Most probably, you have the wrong project name in the build.xml file." );
    }
}
