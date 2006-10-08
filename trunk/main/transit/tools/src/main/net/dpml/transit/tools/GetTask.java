/*
 * Copyright 2005 Stephen McConnell
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

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

import net.dpml.transit.Artifact;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * The get task handles the retrival of a rresource and the binding of the resource filename
 * to a project property.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class GetTask extends TransitTask
{
   /**
    * The uri of the artifact to load.
    */
    private String m_uri;

   /**
    * The name of a property under which the locally cached artifact filename will be bound.
    */
    private String m_name;

   /**
    * Set the project.
    * @param project the current project
    */
    public void setProject( Project project )
    {
        setTaskName( "get" );
        super.setProject( project );
    }

   /**
    * Set the artifact uri of the plugin from which the task is to be loaded.
    * @param uri an artifact plugin uri
    */
    public void setUri( String uri )
    {
        m_uri = uri;
    }

   /**
    * Set the name of a property into which the local file path will be assigned.
    * @param name the ant property name
    */
    public void setProperty( String name )
    {
        m_name = name;
    }

   /**
    * Return the artifact uri of the plugin.
    * @return the artifact uri
    */
    private URI getURI()
    {
        try
        {
            return new URI( m_uri.toString() );
        }
        catch( Throwable e )
        {
            final String error =
              "Cound not convert the supplied uri spec ["
              + m_uri
              + "] to a formal URI.";
            throw new BuildException( error, e, getLocation() );
        }
    }

   /**
    * Return the ant property name.
    * @return the property name
    */
    private String getPropertyName()
    {
        if( null == m_name )
        {
            final String error =
              "The required 'property' attribute is not declared.";
            throw new BuildException( error, getLocation() );
        }
        else
        {
            return m_name;
        }
    }

   /**
    * Load the resource and assign the locally cached file path to the supplied property name.
    * @exception BuildException if an error occurs during resource resolution
    */
    public void execute() throws BuildException
    {
        if( null == m_uri )
        {
            final String error =
              "Missing uri attribute.";
            throw new BuildException( error, getLocation() );
        }

        String name = getPropertyName();

        try
        {
            URI uri = getURI();
            String spec = getURI().toString();
            log( "artifact: " + spec );
            URL url = Artifact.toURL( uri );
            URLConnection connection = url.openConnection();
            connection.connect();
            File file = (File) connection.getContent( new Class[]{File.class} );
            String path = file.getCanonicalPath();
            getProject().setNewProperty( name, path );
        }
        catch( Throwable e )
        {
            final String error =
              "Unexpected error while attempting to resolve artifact uri ["
              + m_uri
              + "]";
           throw new BuildException( error, e, getLocation() );
        }
    }
}
