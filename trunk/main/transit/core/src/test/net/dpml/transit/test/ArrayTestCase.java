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

package net.dpml.transit.test;

import net.dpml.transit.Value;
import net.dpml.transit.Construct;
import net.dpml.transit.Construct.Array;

/**
 * Construct testcase.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ArrayTestCase extends AbstractEncodingTestCase
{
   /**
    * Test creation of a simple construct.
    * @exception Exception if an unexpected error occurs.
    */
    public void testSimpleArray() throws Exception
    {
        Construct a = new Construct( "a" );
        Construct b = new Construct( "b" );
        Value[] args = new Value[]{a, b};
        Array array = new Array( String.class.getName(), args );
        Object value = array.resolve();
        if( null == value )
        {
            throw new NullPointerException( "value" );
        }
        if( value instanceof String[] )
        {
            String[] strings = (String[]) value;
            assertEquals( "length", 2, strings.length );
        }
        else
        {
            throw new IllegalStateException( "result is not an array" );
        }
    }
    
   /**
    * Test creation of a simple construct.
    * @exception Exception if an unexpected error occurs.
    */
    public void testArrayAsCompositeArgument() throws Exception
    {
        Construct a = new Construct( "Hello " );
        Construct b = new Construct( "World!" );
        Value[] args = new Value[]{a, b};
        Array array = new Array( String.class.getName(), args );
        
        Value[] params = new Value[]{array};
        Construct construct = new Construct( Demo.class.getName(), params );
        Demo demo = (Demo) construct.resolve();
        assertNotNull( "demo", demo );
    }
    
    public static class Demo
    {
        public Demo( String[] args )
        {
            for( int i=0; i<args.length; i++ )
            {
                System.out.print( args[i] );
            }
            System.out.println("");
        }
    }
}
