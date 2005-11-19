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

package net.dpml.test;

import java.awt.Color;

import net.dpml.logging.Logger;

/**
 * Component implementation that demonstrates the use of a context inner-class. 
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ExampleComponent implements ColorManager
{
    //------------------------------------------------------------------
    // concerns
    //------------------------------------------------------------------

   /**
    * Component driven context criteria specification.
    */
    public interface Context
    {
       /**
        * Return a non-optional color value.
        * @return the color
        */
        Color getColor();
    }

    //------------------------------------------------------------------
    // state
    //------------------------------------------------------------------

   /**
    * The logging channel.
    */
    private final Logger m_logger;

   /**
    * The assigned context instance.
    */
    private final Context m_context;

    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------

   /**
    * Creation of a new <tt>ExampleComponent</tt> using a supplied 
    * logging channel and context.
    * 
    * @param logger the assigned logging channel
    * @param context the assigned context
    */
    public ExampleComponent( final Logger logger, final Context context )
    {
        m_context = context;
        m_logger = logger;
        
        getLogger().debug( "example component created" );
    }

    //------------------------------------------------------------------
    // Example
    //------------------------------------------------------------------
    
   /**
    * Return the color value from the supplied context.
    * @return the color value
    */
    public Color getColor()
    {
        return m_context.getColor();
    }
    
    //------------------------------------------------------------------
    // internal
    //------------------------------------------------------------------

   /**
    * Return the assigned logging channel.
    * @return the logging channel
    */
    private Logger getLogger()
    {
        return m_logger;
    }
}
