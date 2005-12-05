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

package net.dpml.test.params;

import net.dpml.logging.Logger;

import net.dpml.parameters.Parameters;
import net.dpml.parameters.ParameterException;

/**
 * Test component used to validate parameters directives.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@ 
 */
public class ParameterizableComponent
{ 
    //------------------------------------------------------------------
    // static
    //------------------------------------------------------------------
    
    public static int TEST_VALUE = 1024;
    
    //------------------------------------------------------------------
    // state
    //------------------------------------------------------------------

   /**
    * The logging channel.
    */
    private final Logger m_logger;
    
   /**
    * Supplied XML configuration.
    */
    private final Parameters m_params;
    
    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------

   /**
    * Creation of a new parameterized component.
    * @param logger the assingned logging channel
    */
    public ParameterizableComponent( final Logger logger, Parameters params )
    {
        m_logger = logger;
        m_params = params;
    }
    
    //------------------------------------------------------------------
    // test operations
    //------------------------------------------------------------------
    
    public String getName() throws ParameterException
    {
        return m_params.getParameter( "name" );
    }
    
    public int getSize() throws ParameterException
    {
        return m_params.getParameterAsInteger( "size" );
    }
    
}
