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

import java.io.File;
import java.util.Date;

import org.apache.avalon.composition.model.ComponentModel;
import org.apache.avalon.composition.model.ContextModel;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;

public class ContextTestCase extends AbstractTestCase
{
   private static final String FACADE_CLASSNAME =
     "org.apache.avalon.test.testa.DefaultFacade";
      
   private Context context;

   //-------------------------------------------------------
   // constructor
   //-------------------------------------------------------

    public ContextTestCase( )
    {
        super();
    }

    public void setUp() throws Exception
    {
        m_model = super.setUp( "context.xml" );
        ComponentModel model = (ComponentModel) m_model.getModel( "test-a" );
        if( null == model )
        {
            throw new IllegalStateException( "null deployment model" );
        }

        ContextModel contextModel = model.getContextModel();
        if( null == contextModel )
        {
            throw new IllegalStateException( "null context model" );
        }

        context = (Context) contextModel.getContext();
        if( null == context )
        {
            throw new IllegalStateException( "null context" );
        }
    }

   //-------------------------------------------------------
   // tests
   //-------------------------------------------------------

    public void testContext() throws Exception
    {
        executeClassLoader();
        executeHomeDirectory();
        executeTempDirectory();
        executePartition();
        executeAlias();
        executeVolatile();
        executeImport();
        executeContextCasting();
    }

    private void executeClassLoader() throws Exception
    {
        try
        {
            ClassLoader loader = (ClassLoader) context.get( "urn:avalon:classloader" );
        }
        catch( ContextException e )
        {
            fail( "urn:avalon:classloader" );
        }
    }

    private void executeHomeDirectory() throws Exception
    {   
        try
        {
            File home = (File) context.get( "urn:avalon:home" );
        }
        catch( ContextException e )
        {
            assertTrue( "urn:avalon:home", false );
        }
    }

    private void executeTempDirectory() throws Exception
    {
        try
        {
            File temp = (File) context.get( "urn:avalon:temp" );
        }
        catch( ContextException e )
        {
            assertTrue( "urn:avalon:temp", false );
        }
    }

    private void executePartition() throws Exception
    {
        try
        {
            String partition = (String) context.get( "urn:avalon:partition" );
        }
        catch( ContextException e )
        {
            fail( "urn:avalon:partition" );
        }
    }

    private void executeAlias() throws Exception
    {
        //
        // validate context entry lookup using an alias
        //

        try
        {
            String name = (String) context.get( "name" );
        }
        catch( ContextException e )
        {
            fail( "alias based lookup of the component name" );
        }
    }

    private void executeVolatile() throws Exception
    {

        //
        // validate volatile entries
        //

        Date date = null;
        try
        {
            date = (Date) context.get( "time" );
            try
            {
                Thread.sleep( 1200 );
            }
            catch( Throwable e )
            {
                // continue
            }
        }
        catch( ContextException e )
        {
            assertTrue( "date", false );
        }

        try
        {
            Date now = (Date) context.get( "time" );
            assertTrue( "volatile", now.after( date ) );
        }
        catch( ContextException e )
        {
            assertTrue( "now", false );
        }
    }

    private void executeImport() throws Exception
    {
        //
        // validate an imported context entry
        //

        try
        {
            String path = (String) context.get( "path" );
        }
        catch( ContextException e )
        {
            fail( "path import" );
        }
    }

    private void executeContextCasting() throws Exception
    {
        //
        // validate context safe-casting
        // (e.g. ((MyContext)m_context).myMethod() type of thing)
        //

        final String className =  context.getClass().getName();
        boolean classNameMatches = className.equals( FACADE_CLASSNAME );
        assertTrue( "custom context", classNameMatches );
    }
}
