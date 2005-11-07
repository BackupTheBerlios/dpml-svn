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
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ProcessorDescriptor  extends AbstractDirective
{
    private final String m_name;
    private final String[] m_dependencies;
    private final String m_urn;

   /**
    * Creation of a new processor descriptor.
    * @param name the processor name
    */
    public ProcessorDescriptor( String name )
    {
        this( name, null );
    }
    
   /**
    * Creation of a new processor descriptor.
    * @param name the processor name
    * @param urn the processor codebase
    */
    public ProcessorDescriptor( String name, String urn )
    {
        this( name, urn, new String[0] );
    }
    
   /**
    * Creation of a new processor descriptor.
    * @param name the processor name
    * @param urn the processor codebase
    * @param dependencies array of processor names that this processor depends upon
    */
    public ProcessorDescriptor( String name, String urn, String[] dependencies )
    {
        this( name, urn, dependencies, null );
    }
    
   /**
    * Creation of a new processor descriptor.
    * @param name the processor name
    * @param urn the processor codebase
    * @param dependencies array of processor names that this processor depends upon
    * @param properties supplimentary properties
    */
    public ProcessorDescriptor( String name, String urn, String[] dependencies, Properties properties )
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
    
   /**
    * Return the processor type name.
    * @return the name
    */
    public String getName()
    {
        return m_name;
    }
    
   /**
    * Return the processor codebase urn.
    * @return the urn
    */
    public String getURN()
    {
        return m_urn;
    }
    
   /**
    * Return the processor dependencies.
    * @return the array of dependency names
    */
    public String[] getDependencies()
    {
        return m_dependencies;
    }
    
   /**
    * Compare this object with another for equality.
    * @param other the other object
    * @return true if equal
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof ProcessorDescriptor ) )
        {
            ProcessorDescriptor object = (ProcessorDescriptor) other;
            if( !equals( m_name, object.m_name ) )
            {
                return false;
            }
            else if( !equals( m_urn, object.m_urn ) )
            {
                return false;
            }
            else
            {
                return Arrays.equals( m_dependencies, object.m_dependencies );
            }
        }
        else
        {
            return false;
        }
    }
    
   /**
    * Compute the hash value.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= super.hashValue( m_name );
        hash ^= super.hashArray( m_dependencies );
        hash ^= super.hashValue( m_urn );
        return hash;
    }
}
