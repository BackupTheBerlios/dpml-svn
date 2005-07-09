/*
 * Copyright 2005 Stephen J. McConnell.
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

import java.net.URI;
import java.net.URL;

import org.apache.tools.ant.BuildException;

import net.dpml.transit.artifact.Artifact;
import net.dpml.transit.link.Link;

/**
 * Ant task that provides support for the import of build file templates
 * via an artifact url.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class LinkTask extends TransitTask
{
    private URI m_uri;
    private URI m_target;

    public void init()
    {
        setTaskName( "link" );
    }

    public void setURI( URI uri )
    {
        m_uri = uri;
    }

    public void setTarget( URI uri )
    {
        m_target = uri;
    }

    public void execute() throws BuildException
    {
        if( null == m_uri )
        {
            final String error = 
              "Required 'uri' attribute is missing.";
            throw new BuildException( error );
        }
        if( null == m_target )
        {
            final String error = 
              "Required 'target' attribute is missing.";
            throw new BuildException( error );
        }
        try
        {
            Artifact main = Artifact.createArtifact( m_uri );
            Artifact target = Artifact.createArtifact( m_target );
            URL url = main.toURL();
            Class[] args = new Class[] { Link.class };
            Link link = (Link) url.getContent( args );
            link.setTargetURI( m_target );
            log( "urn " + main );
            log( "uri " + target );
        }
        catch( Throwable e )
        {
            final String error = 
              "Link creation failure."
              + "\nLink URI: " + m_uri
              + "\nTarget URI: " + m_target;
            throw new BuildException( error, e );
        }
    }
}

