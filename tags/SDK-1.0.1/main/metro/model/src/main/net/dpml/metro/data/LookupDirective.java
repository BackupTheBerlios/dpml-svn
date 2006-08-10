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

package net.dpml.metro.data;

import net.dpml.component.Directive;

/**
 * A reference directive is a reference to a part within the enclosing part's
 * context or parts.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class LookupDirective extends AbstractDirective implements Directive
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    /**
     * The container scoped key.
     */
    private final String m_classname;

    /**
     * Creation of a new lookup directive.
     * @param classname the service classname
     * @exception NullPointerException if the classname argument is null.
     */
    public LookupDirective( final String classname )
        throws NullPointerException
    {
        if( null == classname )
        {
            throw new NullPointerException( "classname" );
        }
        m_classname = classname;
    }

    /**
     * Return the service classname.
     *
     * @return the classname
     */
    public String getServiceClassname()
    {
        return m_classname;
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
            if( other instanceof LookupDirective )
            {
                LookupDirective directive = (LookupDirective) other;
                return m_classname.equals( directive.m_classname );
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
        return m_classname.hashCode();
    }
}
