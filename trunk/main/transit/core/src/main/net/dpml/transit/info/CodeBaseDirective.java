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

package net.dpml.transit.info;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import net.dpml.lang.AbstractDirective;
import net.dpml.lang.ValueDirective;

/**
 * The CodeBaseDirective is immutable datastructure used to 
 * describe a codebase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class CodeBaseDirective extends AbstractDirective
{
    private final URI m_codebase;
    private final ValueDirective[] m_parameters;
    
   /**
    * Creation of a new codebase descriptor.
    * @param codebase the codebase uri 
    * @param parameters an array of plugin parameter descriptors
    * @exception URISyntaxException if the codebase URI is invalid
    */
    public CodeBaseDirective( URI codebase, ValueDirective[] parameters )
    {
        if( null == codebase )
        {
            throw new NullPointerException( "codebase" );
        }
        m_codebase = codebase;
        if( null == parameters )
        {
            throw new NullPointerException( "parameters" );
        }
        m_parameters = parameters;
    }
    
   /**
    * Return the codebase URI.
    *
    * @return the codebase uri
    */
    public URI getCodeBaseURI()
    {
        return m_codebase;
    }
    
   /**
    * Return the codebase URI as a string.
    *
    * @return the codebase uri specification
    */
    public String getCodeBaseURISpec()
    {
        return m_codebase.toASCIIString();
    }
    
   /**
    * Return the array of codebase parameter values.
    *
    * @return the parameter value array
    */
    public ValueDirective[] getValueDirectives()
    {
        return m_parameters;
    }
    
   /**
    * Test if the supplied object is equal to this object.
    * @param other the object to evaluate
    * @return true if this object is equal to the supplied object
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof CodeBaseDirective ) )
        {
            CodeBaseDirective directive = (CodeBaseDirective) other;
            if( !Arrays.equals( m_parameters, directive.m_parameters ) )
            {
                return false;
            }
            else
            {
                return equals( m_codebase, directive.m_codebase );
            }
        }
        else
        {
            return false;
        }
    }

   /**
    * Compute the instance hashcode value.
    * @return the hashcode
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= hashValue( m_codebase );
        hash ^= hashArray( m_parameters );
        return hash;
    }

}
