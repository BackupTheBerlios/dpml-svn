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

package dpml.tools.tasks;

import dpml.tools.BuilderError;
import dpml.tools.Context;

import dpml.library.Library;
import dpml.library.Resource;
import dpml.library.Type;
import dpml.library.ResourceNotFoundException;

import org.apache.tools.ant.BuildException;

/**
 * The plugin task handles the establishment of ant tasks, listeners, and antlibs derived
 * from a classloader established by the tools sub-system.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PluginTask extends dpml.tools.transit.PluginTask
{
    private String m_ref;
    
   /**
    * Set the ref address of a plugin resource.
    * @param ref a resource ref
    */
    public void setRef( String ref )
    {
        m_ref = ref;
    }
    
   /**
    * Task initialization.
    */
    public void init()
    {
        super.init();
        Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
    }
    
   /**
    * Task execution.
    */
    public void execute()
    {   
        if( null != m_ref )
        {
            Context context = (Context) getProject().getReference( "project.context" );
            if( null == context )
            {
                final String error = 
                  "Missing 'project.context' reference.";
                throw new IllegalStateException( error );
            }
            Library library = context.getLibrary();
            try
            {
                Resource resource = library.getResource( m_ref );
                Type type = resource.getType( "part" );
                String uri = type.getResolvedArtifact().toURI().toASCIIString();
                setUri( uri );
            }
            catch( ResourceNotFoundException e )
            {
                String message = e.getMessage();
                throw new BuilderError( message, e, getLocation() );
            }
            catch( Exception e )
            {
                final String error = 
                  "Unexpected error while attempting to initiaze plugin task uri.";
                throw new BuilderError( error, e );
            }
        }
        
        super.execute();
    }
}
