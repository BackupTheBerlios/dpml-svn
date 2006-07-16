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

import net.dpml.library.ResourceNotFoundException;
import net.dpml.library.Resource;
import net.dpml.library.Feature;
import net.dpml.library.Type;

import net.dpml.tools.Context;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * Locate a named feature of the a project or resource.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class ResourceTask extends GenericTask
{
    private String m_key;
    private String m_ref;

   /**
    * Set the key of the target project or resource description from which features will be 
    * resolved from.  If not declared the key defaults to the current defintion.
    *
    * @param key the resource key
    */
    public void setKey( final String key )
    {
        m_key = key;
    }

   /**
    * Set the ref of the target project or resource description from which features will be 
    * resolved from.
    *
    * @param ref the resource reference
    */
    public void setRef( final String ref )
    {
        m_ref = ref;
    }

   /**
    * Get the project definition.
    * @return the resource
    */
    protected Resource getResource()
    {
        if( null != m_ref )
        {
            return getResource( m_ref );
        }
        else if( null != m_key )
        {
            Context context = getContext();
            Resource resource = context.getResource();
            Resource parent = resource.getParent();
            String ref = parent.getResourcePath() + "/" + m_key;
            return getResource( ref );
        }
        else
        {
            return getContext().getResource();
        }
    }

    private Resource getResource( String ref )
    {
        try
        {
            return getContext().getLibrary().getResource( ref );
        }
        catch( ResourceNotFoundException e )
        {
            final String error = 
              "Feature reference ["
              + ref
              + "] in the project [" 
              + getResource()
              + "] is unknown.";
            throw new BuildException( error, e );
        }
    }
}
