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

package dpml.library.info;

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
    private final ImportDirective[] m_imports;
    private final ResourceDirective[] m_resources;
    
   /**
    * Creation of a new library directive.
    * @param imports module imports
    * @param resources the set of declared resources
    * @param properties library properties
    */
    public LibraryDirective(
      ImportDirective[] imports, ResourceDirective[] resources, Properties properties )
    {
        super( properties );
        
        if( null == imports )
        {
            throw new NullPointerException( "imports" );
        }
        for( int i=0; i<imports.length; i++ )
        {
            if( null == imports[i] )
            {
                throw new NullPointerException( "import" );
            } 
        }
        if( null == resources )
        {
            throw new NullPointerException( "resources" );
        }
        for( int i=0; i<resources.length; i++ )
        {
            if( null == resources[i] )
            {
                throw new NullPointerException( "resource" );
            } 
        }

        m_resources = resources;
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
    * Return the set of module directives.
    * @return the resource directive array
    */
    public ResourceDirective[] getResourceDirectives()
    {
        return m_resources;
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
            if( !Arrays.equals( m_resources, object.m_resources ) )
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
    * Compute the hashcode value.
    * @return the hash value
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= super.hashArray( m_resources );
        hash ^= super.hashArray( m_imports );
        return hash;
    }
}
