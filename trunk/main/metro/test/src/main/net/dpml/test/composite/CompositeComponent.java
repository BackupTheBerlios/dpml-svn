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

package net.dpml.test.composite;

import java.awt.Color;
import java.util.Map;

import net.dpml.logging.Logger;

import net.dpml.test.ColorManager;

/**
 * This composinent declares an inner Parts interface through which 
 * it accesses a map of the context infomation of a subsidiary component.
 * The subsidiary component is defined and established within the component
 * types definition.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@ 
 */
public class CompositeComponent implements ColorManager
{
    //------------------------------------------------------------------
    // concerns
    //------------------------------------------------------------------

   /**
    * The componnet defined construction criteria.
    */
    public interface Context
    {
       /**
        * Return the assigned color.
        * @return the color value
        */
        Color getColor();
    }

   /**
    * The componnet defined construction criteria.
    */
    public interface Parts
    {
       /**
        * Get the context map of the child component.
        * @return the context map
        */
        Map getChildMap();
        ColorManager getChild();
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
    * Creation of a new singleton component instance.
    * @param logger the assinged logging channel
    * @param context a context implementation fullfil,ling the context criteria
    */
    public CompositeComponent( final Logger logger, final Context context, final Parts parts )
    {
        logger.info( "instantiation" );
        
        m_context = context;
        m_logger = logger;
        
        
        getLogger().info( "color: " + context.getColor() );
        getLogger().info( "accessing map" );
        
        Map map = parts.getChildMap();
        map.put( "color", context.getColor() );
        getLogger().info( "# MAP: " + map );
        
        getLogger().info( "# CHILD: " + parts.getChild() );
        
    }

    //------------------------------------------------------------------
    // Example
    //------------------------------------------------------------------
   
   /**
    * Return the color value assigned to the component context.
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
