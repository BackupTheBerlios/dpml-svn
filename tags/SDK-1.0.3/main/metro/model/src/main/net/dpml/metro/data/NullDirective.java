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
 * A <code>NullDirective</code> is used to mark a solution as resolved
 * on the basis that the solution will be managed by an enclosing component.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class NullDirective extends AbstractDirective implements Directive
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    //--------------------------------------------------------------------------
    // constructors
    //--------------------------------------------------------------------------

   /**
    * Create a new null directive.
    */
    public NullDirective()
    {
    }

    //--------------------------------------------------------------------------
    // NullDirective
    //--------------------------------------------------------------------------

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
            return ( other instanceof NullDirective );
        }
    }

   /**
    * Return the hashcode for the instance.
    * @return the instance hashcode
    */
    public int hashCode()
    {
        return getClass().hashCode();
    }
}
