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
 * The LibraryDirective class describes a collection of modules together
 * with information about type defintions.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class LibraryDirective extends AbstractDirective
{
    private final ProcessDescriptor[] m_processes;
    private final ImportDirective[] m_imports;
    
   /**
    * Creation of a new library directive.
    * @param processes an array of processor directives
    * @param imports module imports
    * @param properties library properties
    */
    public LibraryDirective(
      ProcessDescriptor[] processes, ImportDirective[] imports, Properties properties )
    {
        super( properties );
        
        if( null == processes )
        {
            throw new NullPointerException( "processes" );
        }
        if( null == imports )
        {
            throw new NullPointerException( "imports" );
        }

        m_processes = processes;
        m_imports = imports;
    }
    
   /**
    * Return the set of module imports.
    * @return the module import array
    */
    public ImportDirective[] getImportDirectives()
    {
        return m_imports;
    }
    
   /**
    * Return the set of processor declarations.
    * @return the processor descriptor array
    */
    public ProcessDescriptor[] getProcessDescriptors()
    {
        return m_processes;
    }
    
   /**
    * Compare this object with another for equality.
    * @param other the other object
    * @return true if equal
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof LibraryDirective ) )
        {
            LibraryDirective object = (LibraryDirective) other;
            if( !Arrays.equals( m_processes, object.m_processes ) )
            {
                return false;
            }
            else
            {
                return Arrays.equals( m_imports, object.m_imports );
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
        hash ^= super.hashArray( m_processes );
        hash ^= super.hashArray( m_imports );
        return hash;
    }
}
