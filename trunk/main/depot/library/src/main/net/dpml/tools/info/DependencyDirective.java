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
import java.util.ArrayList;

import net.dpml.transit.Category;

/**
 * The ModuleDirective class describes a module data-structure.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class DependencyDirective extends AbstractDirective
{
   /**
    * BUILD scope.
    */
    public static final Scope BUILD = Scope.BUILD;

   /**
    * RUNTIME scope.
    */
    public static final Scope RUNTIME = Scope.RUNTIME;

   /**
    * TEST scope.
    */
    public static final Scope TEST = Scope.TEST;

    private final Scope m_scope;
    private final IncludeDirective[] m_includes;
    
   /**
    * Creation of a new dependency directive.
    * @param scope the scope
    * @param includes an array of resource includes 
    */
    public DependencyDirective( Scope scope, IncludeDirective[] includes )
    {
        this( scope, includes, null );
    }
    
   /**
    * Creation of a new dependency directive.
    * @param scope the scope
    * @param includes an array of resource includes 
    * @param properties supplimentary properties 
    */
    public DependencyDirective( Scope scope, IncludeDirective[] includes, Properties properties )
    {
        super( properties );
        
        if( null == scope )
        {
            throw new NullPointerException( "scope" );
        }
        if( null == includes )
        {
            throw new NullPointerException( "includes" );
        }
        m_scope = scope;
        m_includes = includes;
    }
    
   /**
    * Return the dependency scope.
    * @return the scope
    */
    public Scope getScope()
    {
        return m_scope;
    }
    
   /**
    * Return the array of resource includes associated with the dependency group.
    * @return the includes array
    */
    public IncludeDirective[] getIncludeDirectives()
    {
        return m_includes;
    }
    
   /**
    * Return the array of resource includes associated with the dependency group
    * filtered relative to a supplied category.
    * @param category a runtime category argument (SYSTEM, PUBLIC, PROTECTED or PRIVATE)
    * @return the filtered includes array
    */
    public IncludeDirective[] getIncludeDirectives( Category category )
    {
        if( null == category )
        {
            return m_includes;
        }
        ArrayList list = new ArrayList();
        for( int i=0; i<m_includes.length; i++ )
        {
            IncludeDirective include = m_includes[i];
            if( category.equals( include.getCategory() ) )
            {
                list.add( include );
            }
        }
        return (IncludeDirective[]) list.toArray( new IncludeDirective[0] );
    }
    
   /**
    * Compare this object with another for equality.
    * @param other the other object
    * @return true if equal
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof DependencyDirective ) )
        {
            DependencyDirective dep = (DependencyDirective) other;
            if( !equals( m_scope, dep.m_scope ) )
            {
                return false;
            }
            else
            {
                return Arrays.equals( m_includes, dep.m_includes );
            }
        }
        else
        {
            return false;
        }
    }
   
   /**
    * Compute the hash value.
    * @return the hascode value
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= super.hashArray( m_includes );
        hash ^= super.hashValue( m_scope );
        return hash;
    }
}
