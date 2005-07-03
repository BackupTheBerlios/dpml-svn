/*
 * Copyright 2004 Niclas Hedhman.
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

package net.dpml.transit.tools;

import net.dpml.transit.Transit;
import net.dpml.transit.artifact.Artifact;
import net.dpml.transit.repository.Repository;
import net.dpml.transit.repository.StandardLoader;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ComponentHelper;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.SubBuildListener;
import org.apache.tools.ant.Target;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Properties;

public class PluginManager
    implements SubBuildListener
{
    private File m_MagicDir;

    public PluginManager()
    {
        m_MagicDir = new File( Transit.DPML_HOME, "processes" );
    }

    public void buildStarted( BuildEvent event )
        throws BuildException
    {
        processStart( event );

    }

    public void subBuildStarted( BuildEvent event )
    {
        processStart( event );
    }

    public void subBuildFinished( BuildEvent event )
    {
    }

    public void buildFinished( BuildEvent event )
    {
    }

    public void targetStarted( BuildEvent event )
    {
    }

    public void targetFinished( BuildEvent event )
    {
    }

    public void taskStarted( BuildEvent event )
    {
    }

    public void taskFinished( BuildEvent event )
    {
    }

    public void messageLogged( BuildEvent event )
    {
    }

    private void processStart( BuildEvent event )
    {
        Project project = event.getProject();

        //
        // handle target addition
        //

        String type = readProjectType( project );
        if( type == null )
            return; // this is not a Magic project.

        ArrayList pluginNames = readPluginNames( project, type );
        if( pluginNames == null )
            return; // build process file is not readable.

        project.log( "Magic Extension enabled."  );
        for( int i = 0; i < pluginNames.size(); i++ )
        {
            String pluginName = (String) pluginNames.get( i );
            try
            {
                Artifact artifact = Artifact.createArtifact( pluginName );
                URI uri = artifact.toURI();
                Repository repo = new StandardLoader();

                //TODO: is it correct to use this class' classloader
                ClassLoader cl = getClass().getClassLoader();
                TargetFactory factory = (TargetFactory) repo.getPlugin( cl, uri, new Object[] { project, uri } );
                Target[] plugins = factory.createTargets();
                for( int j = 0; j < plugins.length; j++ )
                {
                    Target target = plugins[ j ];
                    project.addTarget( target );
                }
            } catch( Exception e )
            {
                project.log( "Warning!! Ant target in plugin '" + pluginName + "' could not be loaded: " + e );
            }
        }
    }

    private ArrayList readPluginNames( Project project, String type )
    {
        File pluginFile = new File( m_MagicDir, type + ".type" );
        FileInputStream in = null;
        try
        {
            ArrayList result = new ArrayList();
            in = new FileInputStream( pluginFile );
            InputStreamReader isr  = new InputStreamReader( in );
            BufferedReader reader = new BufferedReader( isr );
            String line = reader.readLine();
            while( line != null )
            {
                line = line.trim();
                if( ! "".equals( line ) && ! line.startsWith( "#" ) )
                    result.add( line.trim() );
                line = reader.readLine();
            }
            return result;
        } catch( IOException e )
        {
            project.log( "Warning!! No plugins found in " + pluginFile );
            return null;
        } finally
        {
            if( in != null )
            {
                try
                {
                    in.close();
                } catch( IOException e )
                {} // ignore.
            }
        }
    }

    private String readProjectType( Project project )
    {
        FileInputStream in = null;
        try
        {
            File basedir = project.getBaseDir();
            File f = new File( basedir, "build.properties" );
            in = new FileInputStream( f );
            Properties p = new Properties();
            p.load( in );
            String value = p.getProperty( "dpml.magic.build.process" );
            if( value != null )
                value = value.trim();
            return value;
        } catch( IOException e )
        {
            return null;
        } finally
        {
            if( in != null )
            {
                try
                {
                    in.close();
                } catch( IOException e )
                {}
            }
        }
    }

}
