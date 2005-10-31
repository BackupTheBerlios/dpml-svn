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

package net.dpml.tools.info;

import java.util.Arrays;
import java.util.Properties;

/**
 * The ModuleDirective class describes a module data-structure.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ModuleDirective extends ResourceDirective
{
    private final ResourceDirective[] m_resources;
    
    public ModuleDirective(
      ResourceDirective resource, ResourceDirective[] resources )
    {
        this( 
          resource.getName(), resource.getVersion(), resource.getClassifier(), resource.getBasedir(), 
          resource.getTypeDirectives(), resource.getDependencyDirectives(), resources, resource.getProperties() );
    }
    
    public ModuleDirective(
      String name, String version, Classifier classifier, String basedir, TypeDirective[] types,
      DependencyDirective[] dependencies, ResourceDirective[] resources, Properties properties )
    {
        super( name, version, classifier, basedir, types, dependencies, properties );
        if( null == resources )
        {
            throw new NullPointerException( "resources" );
        }
        m_resources = resources;
    }
    
    public ResourceDirective export( String version )
    {
        ResourceDirective resource = super.export( version );
        ResourceDirective[] resources = new ResourceDirective[ m_resources.length ];
        for( int i=0; i<m_resources.length; i++ )
        {
            resources[i] = m_resources[i].export( version );
        }
        return new ModuleDirective( resource, resources );
    }
    
    public ResourceDirective[] getResourceDirectives()
    {
        return m_resources;
    }
    
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof ModuleDirective ) )
        {
            ModuleDirective object = (ModuleDirective) other;
            if( !Arrays.equals( m_resources, object.m_resources ) )
            {
                return false;
            }
            else
            {
                return true;
            }
        }
        else
        {
            return false;
        }
    }
    
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= super.hashArray( m_resources );
        return hash;
    }
}
