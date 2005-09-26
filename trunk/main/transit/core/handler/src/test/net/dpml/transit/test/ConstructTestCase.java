/*
 * Copyright 2004 Niclas Hedhman.
 * Copyright 2004 Stephen J. McConnell.
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

package net.dpml.transit.test;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.Hashtable;

import net.dpml.transit.model.Value;
import net.dpml.transit.model.Construct;

import junit.framework.TestCase;

/**
 * Construct testcase.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Id: OnlineTestCase.java 2926 2005-06-27 10:53:17Z mcconnell@dpml.net $
 */
public class ConstructTestCase extends TestCase
{
    public void testSimpleConstruct() throws Exception
    {
        Construct construct = new Construct( "fred" );
        Object value = construct.resolve();
        assertEquals( "isa-string", value.getClass(), String.class );
        assertEquals( "simple construct value", value, "fred" );
    }

    public void testNullArgConstruct() throws Exception
    {
        Construct construct = new Construct( Date.class.getName(), (String) null );
        Object value = construct.resolve();
        assertEquals( "isa-data", value.getClass(), Date.class );
    }

    public void testPrimitiveConstruct() throws Exception
    {
        Construct construct = new Construct( "int", "10" );
        ClassLoader cl = ConstructTestCase.class.getClassLoader();
        Object value = construct.resolve();
        assertEquals( "isa-Integer", Integer.class, value.getClass() );
    }

    public void testSingleArgConstruct() throws Exception
    {
        Construct construct = new Construct( File.class.getName(), "abc" );
        Object value = construct.resolve();
        assertEquals( "isa-file", value.getClass(), File.class );
        assertEquals( "file", value, new File( "abc" ) );
    }

    public void testMultiArgConstruct() throws Exception
    {
        Value a = new Construct( File.class.getName(), "aaa" );
        Value b = new Construct( File.class.getName(), "${java.io.tmpdir}" );
        Value c = new Construct( Context.class.getName(), new Value[]{a, b} );

        Object value = c.resolve();
        assertEquals( "isa-context", value.getClass(), Context.class );
        Context context = (Context) value;
        assertEquals( "file-a", context.getA(), new File( "aaa" ) );
        assertEquals( "file-b", context.getB(), new File( System.getProperty( "java.io.tmpdir" ) ) );
    }

    public void testPrimitiveMultiArgConstruct() throws Exception
    {
        Value a = new Construct( "int", "100" );
        Value b = new Construct( "boolean", "true" );
        Value c = new Construct( Context2.class.getName(), new Value[]{a, b} );
        Object value = c.resolve();
        assertEquals( "isa-context", value.getClass(), Context2.class );
        Context2 context = (Context2) value;
        if( !( context.getNumber() == 100 ) )
        {
            System.out.println( "# number: " + context.getNumber() );
            fail( "context2 number return value is not 100" );
        }
        if( !context.getLogical() )
        {
            System.out.println( "# logical: " + context.getLogical() );
            fail( "context2 logical return value is not true" );
        }
    }
    
    public void testStaticMethod() throws Exception
    {
        Value v = new Construct( Context2.class.getName(), "create", new Value[0] );
        Object value = v.resolve();
        assertEquals( "isa-context", value.getClass(), Context2.class );
        Context2 context = (Context2) value;
        if( !( context.getNumber() == 100 ) )
        {
            System.out.println( "# number: " + context.getNumber() );
            fail( "context2 number return value is not 100" );
        }
        if( !context.getLogical() )
        {
            System.out.println( "# logical: " + context.getLogical() );
            fail( "context2 logical return value is not true" );
        }
    }
    
    public void testSymbolicReference() throws Exception
    {
        Map map = new Hashtable();
        map.put( "number", new Integer( 100 ) );
        map.put( "logical", new Boolean( true ) );
        
        Value number = new Construct( "int", "${number}" );
        Value logical = new Construct( "boolean", "${logical}" );
        Value construct = new Construct( Context2.class.getName(), new Value[]{ number, logical } );
        Object value = construct.resolve( map, null );
        
        assertEquals( "isa-context", value.getClass(), Context2.class );
        Context2 context = (Context2) value;
        if( !( context.getNumber() == 100 ) )
        {
            System.out.println( "# number: " + context.getNumber() );
            fail( "context2 number return value is not 100" );
        }
        if( !context.getLogical() )
        {
            System.out.println( "# logical: " + context.getLogical() );
            fail( "context2 logical return value is not true" );
        }
    }

    public static class Context 
    {
        private File m_a;
        private File m_b;

        public Context( File a, File b )
        {
            m_a = a;
            m_b = b;
        }

        public File getA()
        {
            return m_a;
        }

        public File getB()
        {
            return m_b;
        }
    }

    public static class Context2 
    {
        private int m_number;
        private boolean m_logical;
        
        public static Context2 create()
        {
            return new Context2( 100, true );
        }

        public Context2( int number, boolean logical )
        {
            m_number = number;
            m_logical = logical;
        }

        public int getNumber()
        {
            return m_number;
        }

        public boolean getLogical()
        {
            return m_logical;
        }
    }

}
