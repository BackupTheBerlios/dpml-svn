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

package org.apache.avalon.composition.model.test;

import org.apache.avalon.util.exception.ExceptionHelper;

public class AssemblyTestCase extends AbstractTestCase
{      
   //-------------------------------------------------------
   // constructor
   //-------------------------------------------------------

    public AssemblyTestCase()
    {
        super();
    }

    public void setUp() throws Exception
    {
        m_model = super.setUp( "dependency.xml" );
    }

   //-------------------------------------------------------
   // tests
   //-------------------------------------------------------

   /**
    * Validate the composition model.
    */
    public void testAssembly() throws Exception
    {
        try
        {
            m_model.assemble();
            System.out.println( "" );
            printModel( "", m_model );
        }
        catch( Throwable e )
        {
            final String message = "Assembly test failure";
            final String error = ExceptionHelper.packException( message, e, true );
            System.err.println( error );
            fail( error );
        }

        try
        {
            m_model.disassemble();
        }
        catch( Throwable e )
        {
            final String message = "Disassembly test failure";
            final String error = ExceptionHelper.packException( message, e, true );
            System.err.println( error );
            fail( error );
        }

    }
}
