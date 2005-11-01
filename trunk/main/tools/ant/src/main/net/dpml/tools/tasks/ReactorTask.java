/*
 * Copyright 2005 Stephen J. McConnell
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

package net.dpml.tools.tasks;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import net.dpml.tools.model.Library;
import net.dpml.tools.model.Resource;
import net.dpml.transit.Artifact;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Ant;

/**
 * Resolves a sorted sequence of projects with a basedir equal to or 
 * derived from the current ${basedir}.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ReactorTask extends GenericTask
{
    private String m_target;
    
   /**
    * Set the target task name.
    * @param target the target task
    */
    public void setTarget( String target )
    {
        m_target = target;
    }
    
    private String[] getTargets()
    {
        if( null != m_target )
        {
            return new String[]{m_target};
        }
        else
        {
            return new String[0];
        }
    }
    
   /**
    * Execute the task.
    */
    public void execute()
    {
        try
        {
            Library library = getContext().getLibrary();
            File basedir = getProject().getBaseDir().getCanonicalFile();
            Resource[] resources = library.select( basedir );
            log( "Reactive selection: " + resources.length );
            for( int i=0; i<resources.length; i++ )
            {
                Resource resource = resources[i];
                executeTarget( resource );
            }
        }
        catch( BuildException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            final String error = 
              "Unexpected error while resolving reactive project list.";
            throw new BuildException( error, e );
        }
    }
    
    private void executeTarget( final Resource resource ) throws BuildException
    {
        final Ant ant = (Ant) getProject().createTask( "ant" );
        ant.setDir( resource.getBaseDir() );
        ant.setInheritRefs( false );
        ant.setInheritAll( false );

        String templateSpec = resource.getProperty( "project.template" );
        File template = getTemplateFile( templateSpec );
        ant.setAntfile( template.toString() );
        if( null != m_target )
        {
            if( !"default".equals( m_target ) )
            {
                log( "building " + resource + " with target: " + m_target );
                ant.setTarget( m_target );
            }
            else
            {
                log( "building " + resource + " with default target" );
            }
        }
        else
        {
            log( "building " + resource + " with default target" );
        }
        ant.init();
        ant.execute();
    }

    private File getTemplateFile( String spec )
    {
        try
        {
            URI uri = new URI( spec );
            if( Artifact.isRecognized( uri ) )
            {
                URL url = Artifact.createArtifact( uri ).toURL();
                return (File) url.getContent( new Class[]{File.class} );
            }
            else
            {
                final String error = 
                  "Unrecognized template uri [" + spec + "].";
                throw new BuildException( error, getLocation() );
            }
        }
        catch( IOException e )
        {
            final String error = 
              "Unexpected IO error while attempting to load template [" + spec + "].";
            throw new BuildException( error, getLocation() );
        }
        catch( URISyntaxException e )
        {
            return new File( spec );
        }
    }
}
