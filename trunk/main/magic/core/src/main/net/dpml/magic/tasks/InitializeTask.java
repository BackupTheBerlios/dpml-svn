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

package net.dpml.magic.tasks;

import java.io.File;
import java.net.URI;

import net.dpml.magic.model.Definition;
import net.dpml.magic.model.Resource;
import net.dpml.magic.model.ResourceRef;
import net.dpml.magic.model.Policy;

import net.dpml.transit.NullArgumentException;
import net.dpml.transit.tools.PluginTask;
import net.dpml.transit.Plugin;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.types.Path;

/**
 * The initialize task loads and plugins that a project
 * has declared under the &lt;plugins&gt; element.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class InitializeTask extends ProjectTask
{
    public void execute() throws BuildException
    {
        //
        // if the project declares plugin dependencies then install
        // these now
        //
        final Definition def = getDefinition();
        final ResourceRef[] refs = def.getPluginRefs();
        for( int i=0; i<refs.length; i++ )
        {
            final ResourceRef ref = refs[i];
            final Resource plugin = getIndex().getResource( ref );
            final String uri = plugin.getInfo().getURI( "plugin" );
            loadPlugin( uri );
        }
    }

    private void loadPlugin( String spec )
        throws BuildException, NullArgumentException
    {
        if( null == spec )
        {
            throw new NullArgumentException( "spec" );
        }
        log( "loading: " + spec );
        try
        {
            URI uri = new URI( spec );
            Plugin plugin = getRepository().getPluginDescriptor( uri );
            String resource = plugin.getResource();
            if( null != resource )
            {
                String urn = plugin.getURN();
                Object task = getProject().createTask( "antlib:net.dpml.transit:plugin" );
//                System.out.println( "-------------   Loaded PluginTask   --------------" );
//                printClassLoaderURLs( task.getClass().getClassLoader() );
//                System.out.println( "-------------  Resident PluginTask  --------------" );
//                printClassLoaderURLs( PluginTask.class.getClassLoader() );
//                System.out.println( "--------------------------------------------------" );
                PluginTask p =(PluginTask) task;
                p.init();
                p.setUri( spec );
                PluginTask.Antlib antlib = p.createAntlib();
                antlib.setResource( resource );
                antlib.setUrn( urn );
                p.execute();
            }
            else
            {
                //
                // load and check for listener
                //

                ClassLoader classloader = this.getClass().getClassLoader();
                Object[] params = new Object[]{ getProject() };
                Object object = getRepository().getPlugin( classloader, uri, params );
                if( object instanceof BuildListener )
                {
                    BuildListener listener = (BuildListener) object;
                    getProject().addBuildListener( listener );
                    log( "registered listener: " + listener.getClass().getName() );
                }
            }
        }
        catch( BuildException e )
        {
            throw e;
        }
        catch( Throwable e )
        {
            final String error = "Unabled to load the plugin ["
              + spec
              + "] declared as a build time dependency under the project "
              + getDefinition()
              + " due to "
              + e.toString();
            throw new BuildException( error, e );
        }
    }

/*
    private void printClassLoaderURLs( ClassLoader cl )
    {
        if( cl == null )
            return;
        if( cl instanceof URLClassLoader )
        {
            URLClassLoader ucl = (URLClassLoader) cl;
            URL[] urls = ucl.getURLs();
            System.out.print( " * " + ucl + " : " );
            for( int i=0 ; i < urls.length ; i++ )
            {
                if( i != 0 )
                    System.out.print( ", " );
                System.out.print( urls[i] );
            }
            System.out.println();
        }
        else
        {
            log( "Unable to print the URL for classloader " + cl.getClass().getName() );
        }
        printClassLoaderURLs( cl.getParent() );
    }
*/
}
