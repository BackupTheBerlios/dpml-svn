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

import java.util.Properties;
import java.util.Arrays;

/**
 * The ProcessDescriptor class describes a datatype creation processor.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class ProcessDescriptor  extends AbstractDirective
{
    private final String m_name;
    private final String[] m_dependencies;
    private final String m_urn;

    public ProcessDescriptor( String name )
    {
        this( name, null );
    }
    
    public ProcessDescriptor( String name, String urn )
    {
        this( name, urn, new String[0] );
    }
    
    public ProcessDescriptor( String name, String urn, String[] dependencies )
    {
        this( name, urn, dependencies, null );
    }
    
    public ProcessDescriptor( String name, String urn, String[] dependencies, Properties properties )
    {
        super( properties );
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }
        if( null == dependencies )
        {
            throw new NullPointerException( "dependencies" );
        }
        m_dependencies = dependencies;
        m_name = name;
        m_urn = urn;
    }
    
    public String getName()
    {
        return m_name;
    }
    
    public String getURN()
    {
        return m_urn;
    }
    
    public String[] getDependencies()
    {
        return m_dependencies;
    }
    
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof ProcessDescriptor ) )
        {
            ProcessDescriptor object = (ProcessDescriptor) other;
            if( !equals( m_name, object.m_name ) )
            {
                return false;
            }
            else if( !equals( m_urn, object.m_urn ) )
            {
                return false;
            }
            else if( !Arrays.equals( m_dependencies, object.m_dependencies ) )
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
        hash ^= super.hashValue( m_name );
        hash ^= super.hashArray( m_dependencies );
        hash ^= super.hashValue( m_urn );
        return hash;
    }
}
