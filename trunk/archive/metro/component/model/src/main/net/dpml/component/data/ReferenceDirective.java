/*
 * Copyright 2004 Stephen J. McConnell.
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

package net.dpml.component.data;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

import net.dpml.part.Part;

/**
 * A reference directive is a reference to a part within the enclosing part's
 * context or parts.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: ImportDirective.java 2230 2005-04-06 18:50:19Z mcconnell@dpml.net $
 */
public final class ReferenceDirective extends AbstractDirective
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    /**
     * The container scoped key.
     */
    private final URI m_uri;

    /**
     * Creation of a new entry directive.
     * @param uri a uri referencing a a part in an enclosing container
     * @exception NullPointerException if the uri argument is null.
     */
    public ReferenceDirective( final URI uri )
        throws NullPointerException
    {
        if( null == uri )
        {
            throw new NullPointerException( "uri" );
        }
        m_uri = uri;
    }

    //--------------------------------------------------------------------------
    // ImportDirective
    //--------------------------------------------------------------------------

    /**
     * Return the uri reference.
     *
     * @return the uri
     */
    public URI getURI()
    {
        return m_uri;
    }

   /**
    * Test if the supplied object is equal to this object.
    * @param other the object to compare with this instance
    * @return TRUE if the supplied object is equal to this object
    */
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else
        {
            if( other instanceof ReferenceDirective )
            {
                ReferenceDirective directive = (ReferenceDirective) other;
                return m_uri.equals( directive.getURI() );
            }
            else
            {
                return false;
            }
        }
    }

   /**
    * Return the hashcode for the instance.
    * @return the instance hashcode
    */
    public int hashCode()
    {
        int hash = m_uri.hashCode();
        return hash;
    }
}
