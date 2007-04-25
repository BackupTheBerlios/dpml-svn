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

import java.awt.Color;

import net.dpml.annotation.Component;
import net.dpml.annotation.Services;

import net.dpml.util.Logger;

/**
 * A sample component declaring a context interface wherein an entry return type is another context interface.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
@Component
@Services( Widget.class )
public class ContextualWidget implements Widget
{
   /**
    * A context interface containing a nested context interface as a return type.
    */
    public interface Context
    {
       /**
        * A simple optional string entry.
        * @param value the default value
        * @return the resolved value
        */
        String getMessage( String value );
        
       /**
        * A non-optional entry with a return type corresponding to a context interface.
        * @return the container resolved implementation of the ColorManager contract
        */
        ColorManager getColors();
        
       /**
        * A non-optional int array entry.
        * @return the array value
        */
        int[] getNumbers();
    }
    
    private final Context m_context;
    
   /**
    * Creation of the componet.
    * @param logger the container supplied logging channel
    * @param context the deployment context
    */
    public ContextualWidget( Logger logger, Context context )
    {
        logger.info( "instantiated" );
        m_context = context;
    }
    
   /**
    * Return the message.
    * @return the message
    */
    public String getMessage()
    {
        return m_context.getMessage( "Hello" );
    }
    
   /**
    * Return the primary color from the nested context.
    * @return the primary color
    */
    public Color getPrimary()
    {
        return m_context.getColors().getPrimary();
    }
    
   /**
    * Return the secondary color from the nested context.
    * @return the secondary color
    */
    public Color getSecondary()
    {
        return m_context.getColors().getSecondary();
    }
    
   /**
    * Return the array of int values.
    * @return the array
    */
    public int[] getNumbers()
    {
        return m_context.getNumbers();
    }
    
   /**
    * The nested context interface defintion.
    */
    @net.dpml.annotation.Context
    public interface ColorManager
    {
       /**
        * Get the primary color.
        * @return the primary color
        */
        Color getPrimary();
        
       /**
        * Get the secondary color.
        * @return the secondary color
        */
        Color getSecondary();
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
