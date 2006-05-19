/*
 * Copyright 2006 Stephen J. McConnell
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

package net.dpml.library.info;

import java.util.Arrays;

import net.dpml.lang.AbstractDirective;
import net.dpml.util.ElementHelper;

import org.w3c.dom.Element;

/**
 * Jar file production criteria.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class JarTypeDirective extends TypeDirective
{
    private final String[] m_rmicIncludes;
    private final String[] m_rmicExcludes;
    
   /**
    * Creation of a new jar type directive.
    * @param element DOM element defining the type 
    * @param alias the alias production policy
    */
    public JarTypeDirective( Element element, boolean alias )
    {
        super( element, "jar", alias );
        
        Element src = ElementHelper.getChild( element, "src" );
        if( null != src )
        {
            throw new UnsupportedOperationException( "src" );
        }
        Element rmic = ElementHelper.getChild( element, "rmic" );
        if( null != rmic )
        {
            Element[] includes = ElementHelper.getChildren( rmic, "include" );
            m_rmicIncludes = new String[ includes.length ];
            for( int i=0; i<includes.length; i++ )
            {
                Element include = includes[i];
                String name = ElementHelper.getAttribute( include, "name" );
                m_rmicIncludes[i] = name;
            }
            Element[] excludes = ElementHelper.getChildren( rmic, "exclude" );
            m_rmicExcludes = new String[ excludes.length ];
            for( int i=0; i<excludes.length; i++ )
            {
                Element exclude = excludes[i];
                String name = ElementHelper.getAttribute( exclude, "name" );
                m_rmicExcludes[i] = name;
            }
        }
        else
        {
            m_rmicIncludes = new String[0];
            m_rmicExcludes = new String[0];
        }
    }
    
   /**
    * Return the rmic include names.
    * @return the include array
    */
    public String[] getRMICIncludes()
    {
        return m_rmicIncludes;
    }
    
   /**
    * Return the rmic include names.
    * @return the include array
    */
    public String[] getRMICExcludes()
    {
        return m_rmicExcludes;
    }
    
   /**
    * Compare this object with another for equality.
    * @param other the other object
    * @return true if equal
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof JarTypeDirective ) )
        {
            JarTypeDirective object = (JarTypeDirective) other;
            if( !Arrays.equals( m_rmicIncludes, object.m_rmicIncludes ) )
            {
                return false;
            }
            else
            {
                return Arrays.equals( m_rmicExcludes, object.m_rmicExcludes );
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
        hash ^= hashArray( m_rmicIncludes );
        hash ^= hashArray( m_rmicExcludes );
        return hash;
    }
}
