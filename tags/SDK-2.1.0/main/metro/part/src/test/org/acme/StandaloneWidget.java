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
 * An example of a component with bundled defaults (refer StandaloneWidget.xprofile)
 * including defaults for nested context definitions.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
@Component
@Services( Widget.class )
public class StandaloneWidget implements Widget
{
   /**
    * Deployment contrace.
    */
    public interface Context
    {
       /**
        * Get the message value.
        * @param value the defaul value supplied by the component
        * @return the resolved value
        */
        String getMessage( String value );
        
       /**
        * Get a color manager (itself a context interface).
        * @return the colour manager
        */
        ColorManager getColors();
        
       /**
        * Get an array of numbers
        * @return the numbers array
        */
        int[] getNumbers();
    }
    
    private final Context m_context;
    
   /**
    * Creation of standalone widget.
    * @param logger the assigned logging channel
    * @param context the assigned deployment context
    */
    public StandaloneWidget( Logger logger, Context context )
    {
        logger.info( "instantiated" );
        logger.info( "message: " + context.getMessage( null ) );
        logger.info( "colors: " + context.getColors() );
        for( int n : context.getNumbers() )
        {
            logger.info( "  " + n );
        }
        m_context = context;
    }
    
   /**
    * Get the message.
    * @return the message
    */
    public String getMessage()
    {
        return m_context.getMessage( "Hello" );
    }
    
   /**
    * Get the primary color.
    * @return the color
    */
    public Color getPrimary()
    {
        return m_context.getColors().getPrimary();
    }
    
   /**
    * Get the seconary color.
    * @return the color
    */
    public Color getSecondary()
    {
        return m_context.getColors().getSecondary();
    }
    
   /**
    * Get the array of numbers
    * @return the array
    */
    public int[] getNumbers()
    {
        return m_context.getNumbers();
    }
    
   /**
    * A nested context definition.
    */
    @net.dpml.annotation.Context
    public interface ColorManager
    {
       /**
        * Get the color declared under the primary key.
        * @return the color
        */
        Color getPrimary();
        
       /**
        * Get the color declared under the secondary key.
        * @return the color
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
