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

package org.apache.avalon.util.exception;

import junit.framework.TestCase ;

/**
 * ExceptionHelper tests.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ExceptionHelperTest.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class ExceptionHelperTest extends TestCase
{    
    /**
     * Constructor for ExceptionHelperTest.
     * @param name
     */
    public ExceptionHelperTest( String name )
    {
        super( name );
    }

    final public void testCompositeExceptionReport()
        throws Exception
    {
        Throwable e1 = null;
        Throwable e2 = null;
        try
        {
            doSomething();
        }
        catch( Throwable e )
        {
            e1 = e;
        }
        try
        {
            doSomething();
        }
        catch( Throwable e )
        {
            e2 = e;
        }
        final String message = 
           ExceptionHelper.packException( 
             "Composite exception report", 
             new Throwable[]{ e1, e2 }, false );
        assertNotNull( message );
        //System.out.println( message );
    }

    final public void testExceptionWithMessageReport()
        throws Exception
    {
        try
        {
            doSomething();
        }
        catch( Throwable e )
        {
            final String message = 
              ExceptionHelper.packException( "An error occured.", e );
            //System.out.println( message );
            assertNotNull( message );
        }
    }

    final public void testExceptionWithStackTrace()
        throws Exception
    {
        try
        {
            doSomething();
        }
        catch( Throwable e )
        {
            final String message = 
              ExceptionHelper.packException( e, true );
            assertNotNull( message );
        }
    }

    final public void testExceptionWithMessageAndStackTrace()
        throws Exception
    {
        try
        {
            doSomething();
        }
        catch( Throwable e )
        {
            final String message = 
              ExceptionHelper.packException( "An error occured.", e, true );
            assertNotNull( message );
        }
    }

    private void doSomething() throws StandardException
    {
        try
        {
            doSomethingElse();
        }
        catch( Throwable e )
        {
            final String error =
              "Unable to do something due to a error condition.";
            throw new StandardException( error, e );
        }
    }

    private void doSomethingElse()
    {
        try
        {
            causeSomeError();
        }
        catch( Throwable e )
        {
            final String error =
              "Unable to do something else due to a error condition.";
            throw new StandardRuntimeException( error, e );
        }
    }

    private void causeSomeError()
    {
        final String error = 
          "Raising exception because that's what I'm programmed to do.";
        throw new StandardError( error );
    }    
}
