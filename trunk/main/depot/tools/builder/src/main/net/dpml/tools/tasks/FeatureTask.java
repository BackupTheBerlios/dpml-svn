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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

/**
 * Locate a named feature of the a project or resource.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class FeatureTask extends GenericTask
{
    private String m_key;
    private String m_ref;
    private String m_feature;
    private String m_type; // optional - used to select type when resolving uris
    private boolean m_alias = false; // used when resolving uris

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
    * Set filename resolution switch. If true the filename feature will
    * return an alias path.
    *
    * @param flag the alias switch
    */
    public void setAlias( final boolean flag )
    {
        m_alias = flag;
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
    * Set the name of the feature.
    * @param feature the feature name
    */
    public void setFeature( final String feature )
    {
        m_feature = feature;
    }

   /**
    * Optionaly set the resource type that the feature is related to.
    * @param type the resource type
    */
    public void setType( final String type )
    {
        m_type = type;
    }

   /**
    * Return the assigned feature name.
    * @return the feature name
    */
    protected String getFeature()
    {
        return m_feature;
    }

   /**
    * Resolve the feature value.
    * @return the feature value
    */
    protected String resolve()
    {
        if( null == m_feature )
        {
            final String error = "Missing 'feature' attribute.";
            throw new BuildException( error );
        }
        else
        {
            log( "Processing feature: " + m_feature, Project.MSG_VERBOSE );
        }
        
        String ref = getRef();
        Resource resource = getResource( ref );
        Feature feature = Feature.parse( m_feature );
        if( null == m_type )
        {
            return Feature.resolve( resource, feature );
        }
        else
        {
            Type type = resource.getType( m_type );
            return Feature.resolve( resource, feature, type, m_alias );
        }
    }
    
    private String getRef()
    {
        if( null != m_ref )
        {
            return m_ref;
        }
        else if( null != m_key )
        {
            return getResource().getParent().getResourcePath() + "/" + m_key;
        }
        else
        {
            return getResource().getResourcePath();
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
