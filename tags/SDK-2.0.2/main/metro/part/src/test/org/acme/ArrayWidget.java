/*
 * Copyright 2004-2006 Stephen J. McConnell.
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

package org.acme;

import net.dpml.util.Logger;

/**
 * Array test component.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ArrayWidget implements Widget
{
   /**
    * Array component deployment context.
    */
    public interface Context
    {
       /**
        * Get a character array.
        * @return the character array
        */
        char[] getMessage();
    }
    
    private Context m_context;
    
   /**
    * Construct a new array widget instance.
    * @param logger the assigned logging channel
    * @param context the assigned deployment context
    */
    public ArrayWidget( Logger logger, Context context )
    {
        m_context = context;
        logger.info( getMessage() );
    }
    
   /**
    * Return a message constructed from the context character array.
    * @return the message
    */
    public String getMessage()
    {
        return new String( m_context.getMessage() );
    }
    
   /**
    * Return the context value (normally private but exposed here
    * for testcase access).
    * @return the context
    */
    public Context getContext()
    {
        return m_context;
    }
    
   /**
    * Test the supplied object for equality with this object.
    * @param other the supplied object 
    * @return the equality result
    */
    public boolean equals( Object other )
    {
        return ( hashCode() == other.hashCode() );
    }
    
   /**
    * Get the component hashcode.
    * @return the hash value
    */
    public int hashCode()
    {
        return m_context.hashCode();
    }
}
