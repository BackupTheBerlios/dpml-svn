/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.avalon.activation.impl.test;

import org.apache.avalon.util.exception.ExceptionHelper;

public class PlaygroundTestCase extends AbstractTestCase
{
   //-------------------------------------------------------
   // constructor
   //-------------------------------------------------------

    public PlaygroundTestCase( )
    {
        this( "model" );
    }

    public PlaygroundTestCase( String name )
    {
        super( name );
    }

   //-------------------------------------------------------
   // setup
   //-------------------------------------------------------

   /**
    * Setup the model using a source balock in the conf 
    * directory.
    * @exception Exception if things don't work out
    */
    public void setUp() throws Exception
    {
        super.setUp( "playground.xml" );
    }

   //-------------------------------------------------------
   // test
   //-------------------------------------------------------

   /**
    * Create, assembly, deploy and decommission the block 
    * defined by getPath().
    */
    public void testDeploymentCycle() throws Exception
    {
        try
        {
            executeDeploymentCycle();
        }
        catch( Throwable e )
        {
            final String error = "Playground test failure.";
            final String message = ExceptionHelper.packException( error, e, true );
            getLogger().error( message );
            throw new Exception( message );
        }
    }
}
