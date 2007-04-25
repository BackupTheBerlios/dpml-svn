/*
 * Copyright 2004-2006 Stephen J. McConnell.
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

package dpml.lang;

import java.awt.Color;
import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.Hashtable;
import java.util.Arrays;

/**
 * Construct testcase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ConstructTestCase extends AbstractTestCase
{
   /**
    * Testcase setup.
    */
    public void setUp()
    {
        ClassLoader loader = ConstructTestCase.class.getClassLoader();
        Thread.currentThread().setContextClassLoader( loader );
    }
    
   /**
    * Test creation of a simple construct.
    * @exception Exception if an unexpected error occurs.
    */
    public void testSimpleConstruct() throws Exception
    {
        Construct construct = new Construct( "fred" );
        Object value = construct.resolve();
        if( null == value )
        {
            throw new NullPointerException( "value" );
        }
        assertEquals( "isa-string", value.getClass(), String.class );
        assertEquals( "simple construct value", value, "fred" );
    }

   /**
    * Test creation of a construct using a null value.
    * @exception Exception if an unexpected error occurs.
    */
    public void testNullArgConstruct() throws Exception
    {
        Construct construct = new Construct( Date.class.getName(), (String) null );
        Object value = construct.resolve();
        assertEquals( "isa-data", value.getClass(), Date.class );
    }

   /**
    * Test creation of a construct using a primitive type.
    * @exception Exception if an unexpected error occurs.
    */
    public void testPrimitiveConstruct() throws Exception
    {
        Construct construct = new Construct( "int", "10" );
        ClassLoader cl = ConstructTestCase.class.getClassLoader();
        Object value = construct.resolve();
        assertEquals( "isa-Integer", Integer.class, value.getClass() );
    }

   /**
    * Test creation of a construct using a single typed argument.
    * @exception Exception if an unexpected error occurs.
    */
    public void testSingleArgConstruct() throws Exception
    {
        Construct construct = new Construct( File.class.getName(), "abc" );
        Object value = construct.resolve();
        assertEquals( "isa-file", value.getClass(), File.class );
        assertEquals( "file", value, new File( "abc" ) );
    }

   /**
    * Test creation of a construct using a multiple primitate arguments.
    * @exception Exception if an unexpected error occurs.
    */
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
            fail( "context2 logical return value is not 'true'" );
        }
    }
    
   /**
    * Test creation of a construct using a static method operator.
    * @exception Exception if an unexpected error occurs.
    */
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
            fail( "context2 logical return value is not 'true'" );
        }
    }
    
   /**
    * Test creation of a construct using a static field operator.
    * @exception Exception if an unexpected error occurs.
    */
    public void testStaticField() throws Exception
    {
        Value v = new Construct( Color.class.getName(), "RED", (String) null );
        Object value = v.resolve();
        assertEquals( "color", Color.RED, value );
    }

   /**
    * Test creation of a composite construct using a static field operator.
    * @exception Exception if an unexpected error occurs.
    */
    public void testStaticFieldInComposite() throws Exception
    {
        Value v = new Construct( Color.class.getName(), "RED", new Value[0] );
        Object value = v.resolve();
        assertEquals( "color", Color.RED, value );
    }

   /**
    * Test creation of a construct using a symbolic reference.
    * @exception Exception if an unexpected error occurs.
    */
    public void testSymbolicReference() throws Exception
    {
        Map<String, Object> map = new Hashtable<String, Object>();
        File file = new File( "somewhere" );
        map.put( "test", file );
        Value construct = new Construct( "${test}" );
        Object value = construct.resolve( map );
        assertEquals( "value", file, value );
    }
    
   /**
    * Test creation of a construct using a typed symbolic reference.
    * @exception Exception if an unexpected error occurs.
    */
    public void testTypedSymbolicReference() throws Exception
    {
        Map<String, Object> map = new Hashtable<String, Object>();
        map.put( "number", new Integer( 100 ) );
        map.put( "logical", new Boolean( true ) );
        
        Value number = new Construct( "int", "${number}" );
        Value logical = new Construct( "boolean", "${logical}" );
        Value construct = new Construct( Context2.class.getName(), new Value[]{number, logical} );
        Object value = construct.resolve( map );
        
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
    
   /**
    * Test array return type.
    * @exception Exception if an unexpected error occurs.
    */
    public void testSimpleArray() throws Exception
    {
        String[] args = new String[]{"aaa", "bbb"};
        Value s1 = new Construct( "aaa" );
        Value s2 = new Construct( "bbb" );
        Value construct = new Construct( args.getClass().getName(), new Value[]{s1, s2} );
        String[] result = (String[]) construct.resolve();
        boolean equal = Arrays.equals( args, result );
        assertTrue( "simple-array", equal );
    }
    
   /**
    * Test array with zero entries.
    * @exception Exception if an unexpected error occurs.
    */
    public void testZeroLengthArray() throws Exception
    {
        String[] args = new String[0];
        Value construct = new Construct( args.getClass().getName(), new Value[0] );
        String[] result = (String[]) construct.resolve();
        boolean equal = Arrays.equals( args, result );
        assertTrue( "zero-length-array", equal );
    }
    
   /**
    * Test creation of a simple construct.
    * @exception Exception if an unexpected error occurs.
    */
    public void testArrayAsCompositeArgument() throws Exception
    {
        Construct a = new Construct( "Hello " );
        Construct b = new Construct( "World!" );
        Construct array = new Construct( String[].class.getName(), new Value[]{a, b} );
        Construct construct = new Construct( Demo.class.getName(), new Value[]{array} );
        Demo demo = (Demo) construct.resolve();
        assertNotNull( "demo", demo );
    }
    
   /**
    * Test creation of a simple construct.
    * @exception Exception if an unexpected error occurs.
    */
    public void testPrimitiveArray() throws Exception
    {
        int[] args = new int[]{1, 2, 3};
        Value p1 = new Construct( "int", "1" );
        Value p2 = new Construct( "int", "2" );
        Value p3 = new Construct( "int", "3" );
        Value construct = new Construct( "int[]", new Value[]{p1, p2, p3} );
        Object result = construct.resolve();
        int[] array = (int[]) construct.resolve();
        boolean equal = Arrays.equals( args, array );
        assertTrue( "primitive-array", equal );
    }

   /**
    * Test construct encoding.
    * @exception Exception if an unexpected error occurs.
    */
    public void testEncoding() throws Exception
    {
        Value number = new Construct( "int", "${number}" );
        Value logical = new Construct( "boolean", "${logical}" );
        Value construct = new Construct( Context2.class.getName(), new Value[]{number, logical} );
        Value result = (Value) executeEncodingTest( construct );
        assertEquals( "encoding", construct, result );
    }

   /**
    * Test class.
    */
    public static class Demo
    {
       /**
        * Create a new demo class.
        * @param args the args
        */
        public Demo( String[] args )
        {
            for( int i=0; i<args.length; i++ )
            {
                System.out.print( args[i] );
            }
            System.out.println( "" );
        }
    }
    
   /**
    * Mock class.
    */
    public static class Context 
    {
        private File m_a;
        private File m_b;

       /**
        * Creation of a mock composite argument object.
        * @param a the primary argument
        * @param b the secondary argument
        */
        public Context( File a, File b )
        {
            m_a = a;
            m_b = b;
        }

       /**
        * Return the primary argument.
        * @return the primary argument value
        */
        public File getA()
        {
            return m_a;
        }

       /**
        * Return the secondary argument.
        * @return the secondary argument value
        */
        public File getB()
        {
            return m_b;
        }
    }

   /**
    * Another mock class.
    */
    public static class Context2 
    {
        private int m_number;
        private boolean m_logical;
        
       /**
        * Static constructor method.
        * @return an instance
        */
        public static Context2 create()
        {
            return new Context2( 100, true );
        }

       /**
        * Creation of a mock object using primiative arguments.
        * @param number a primitive number
        * @param logical a boolean value
        */
        public Context2( int number, boolean logical )
        {
            m_number = number;
            m_logical = logical;
        }

       /**
        * Return the number.
        * @return the number
        */
        public int getNumber()
        {
            return m_number;
        }

       /**
        * Return the boolean.
        * @return the boolean
        */
        public boolean getLogical()
        {
            return m_logical;
        }
    }
}
