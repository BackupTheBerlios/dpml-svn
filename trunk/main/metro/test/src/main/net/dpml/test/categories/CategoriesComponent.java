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

package net.dpml.test.categories;

import net.dpml.logging.Logger;

/**
 * Test component used to validate logging category declarations.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@ 
 */
public class CategoriesComponent
{
    //------------------------------------------------------------------
    // state
    //------------------------------------------------------------------

   /**
    * The supplied logging channel.
    */
    private final Logger m_logger;
    
   /**
    * The the alpha child logging channel.
    */
    private final Logger m_alpha;
    
   /**
    * The the beta child logging channel.
    */
    private final Logger m_beta;
    

    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------

   /**
    * Creation of a new startable component instance.
    * @param logger the assingned logging channel
    */
    public CategoriesComponent( final Logger logger )
    {
        m_logger = logger;
        m_alpha = logger.getChildLogger( "alpha" );
        m_beta = logger.getChildLogger( "beta" );
        
        // under the default configuration only info level messages
        // are listed to console however the component configuratuion
        // is overriding this to restrict the alpha and betwa channels
        // to warn and error priority respectively - as such no log 
        // messages appear during a normal test 
        
        m_logger.debug( "instantiation complete" ); 
        m_alpha.info( "an alpha channel message" );
        m_beta.info( "an beta channel message" ); 
    }
}
