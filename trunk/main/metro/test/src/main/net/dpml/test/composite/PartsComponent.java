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
import java.rmi.RemoteException;

import net.dpml.logging.Logger;

import net.dpml.test.ColorManager;

import net.dpml.metro.PartsManager;
import net.dpml.metro.ComponentHandler;

import net.dpml.lang.UnknownKeyException;

/**
 * This component declares an inner Parts interface through which 
 * it accesses a map of the context infomation of a subsidiary component
 * named 'child'. The subsidiary component is defined and established 
 * within the component types definition (see build file for details).
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@ 
 */
public class PartsComponent implements ColorManager
{
    //------------------------------------------------------------------
    // concerns
    //------------------------------------------------------------------

   /**
    * The construction criteria.
    */
    public interface Context
    {
       /**
        * Return the assigned color.
        * @return the color value
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

   /**
    * The assigned part manager.
    */
    private final PartsManager m_parts;

    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------

   /**
    * Creation of a new composite component instance.
    * @param logger the assingned logging channel
    * @param context a context implementation
    * @param parts the parts manager
    * @exception UnknownKeyException if the implementation uses a bad reference
    */
    public PartsComponent( 
      final Logger logger, final Context context, final PartsManager parts ) 
      throws UnknownKeyException, RemoteException
    {
        logger.debug( "instantiation" );
        
        m_context = context;
        m_logger = logger;
        m_parts = parts;
        
        ComponentHandler handler = parts.getComponentHandler( "child" );
        handler.getContextMap().put( "color", context.getColor() );
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
    // Validation
    //------------------------------------------------------------------
    
   /**
    * Return the child component for evaluation by the testcase.
    * @return the child
    * @exception Exception if an error occurs 
    */
    public ChildComponent getChild() throws Exception
    {
        ComponentHandler handler = m_parts.getComponentHandler( "child" );
        return (ChildComponent) handler.getProvider().getValue( false );
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
