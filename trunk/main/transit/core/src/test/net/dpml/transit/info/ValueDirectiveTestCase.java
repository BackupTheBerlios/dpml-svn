/*
 * Copyright 2005 Stephen J. McConnell
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

package net.dpml.transit.info;

import java.io.File;

import net.dpml.transit.Construct;
import net.dpml.transit.ValueException;

/**
 * Testing the ValueDirective class.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ValueDirectiveTestCase extends AbstractTestCase
{
   /**
    * Validate the creation of a value directrive using a single null argument
    * and the resolution of the value by a Construct to a null.
    * @exception Exception if an error occurs
    */
    public void testNullValue() throws Exception
    {
        try
        {
            ValueDirective value = new ValueDirective( null );
            Construct construct = new Construct( value );
            assertEquals( null, construct.resolve() );
        }
        catch( NullPointerException e )
        {
            fail( "Null values allowable." );
        }
    }
    
   /**
    * Validate the creation of a value directrive using a single non-null 
    * String argument and the resolution of the value by a construct to the 
    * same value.
    * @exception Exception if an error occurs
    */
    public void testValue() throws Exception
    {
        String arg = "test";
        ValueDirective value = new ValueDirective( arg );
        assertFalse( "compound", value.isCompound() );
        assertNull( "method", value.getMethodName() );
        assertEquals( "values", 0, value.getValueDirectives().length );
        assertEquals( "base", arg, value.getBaseValue() );
        assertEquals( "target", null, value.getTargetExpression() );
        
        Construct construct = new Construct( value );
        assertEquals( "resolved", arg, construct.resolve() );
    }
    
   /**
    * Validate the creation of a value directrive using a single non-null 
    * String argument that is itself a symbolic reference, and confirm that 
    * and the resolution of the value by a construct returns the expected
    * resolve symbol.
    * @exception Exception if an error occurs
    */
    public void testSymbolicValue() throws Exception
    {
        String name = "user.dir";
        String arg = "${" + name + "}";
        ValueDirective value = new ValueDirective( arg );
        Construct construct = new Construct( value );
        String resolved = System.getProperty( name );
        assertEquals( "resolved", resolved, construct.resolve() );
    }
    
   /**
    * Validate the ValueDirective( String target, String value ) constructor. 
    * @exception Exception if an error occurs
    */
    public void testTargetValueConstructor() throws Exception
    {
        String target = "java.io.File";
        String value = ".";
        ValueDirective directive = new ValueDirective( target, value );
        assertFalse( "compound", directive.isCompound() );
        assertNull( "method", directive.getMethodName() );
        assertEquals( "values", 0, directive.getValueDirectives().length );
        assertEquals( "base", value, directive.getBaseValue() );
        assertEquals( "target", target, directive.getTargetExpression() );
        Construct construct = new Construct( directive );
        assertEquals( "resolved", new File( value ), construct.resolve() );
    }

   /**
    * Validate the ValueDirective( String target, String method, String value ) constructor. 
    * @exception Exception if an error occurs
    */
    public void testTargetMethodValueConstructor() throws Exception
    {
        String target = "java.io.File";
        String value = ".";
        String method = "new";
        ValueDirective directive = new ValueDirective( target, method, value );
        assertFalse( "compound", directive.isCompound() );
        assertEquals( "method", method, directive.getMethodName() );
        assertEquals( "values", 0, directive.getValueDirectives().length );
        assertEquals( "base", value, directive.getBaseValue() );
        assertEquals( "target", target, directive.getTargetExpression() );
        Construct construct = new Construct( directive );
        assertEquals( "resolved", new File( value ), construct.resolve() );
    }
    
   /**
    * Validate the ValueDirective( String target, ValueDirective[] args ) constructor.
    * @exception Exception if an error occurs
    */
    public void testTargetValuesConstructor() throws Exception
    {
        ValueDirective base = new ValueDirective( "java.io.File", "." );
        ValueDirective spec = new ValueDirective( "test" );
        ValueDirective[] args = new ValueDirective[]{base, spec};
        
        String target = "java.io.File";
        ValueDirective directive = new ValueDirective( target, args );
        assertTrue( "compound", directive.isCompound() );
        assertEquals( "target", target, directive.getTargetExpression() );
        assertNull( "method", directive.getMethodName() );
        assertEquals( "values", args, directive.getValueDirectives() );
        assertNull( "base", directive.getBaseValue() );
        Construct construct = new Construct( directive );
        assertEquals( "resolved", new File( new File( "." ), "test" ), construct.resolve() );
    }
    
   /**
    * Validate the ValueDirective( String target, String method, ValueDirective[] args ) constructor.
    * @exception Exception if an error occurs
    */
    public void testTargetMethodValuesConstructor() throws Exception
    {
        ValueDirective base = new ValueDirective( "java.io.File", "." );
        ValueDirective spec = new ValueDirective( "test" );
        ValueDirective[] args = new ValueDirective[]{base, spec};
        
        String target = "java.io.File";
        String method = "new";
        ValueDirective directive = new ValueDirective( target, method, args );
        assertTrue( "compound", directive.isCompound() );
        assertEquals( "target", target, directive.getTargetExpression() );
        assertEquals( "method", method, directive.getMethodName() );
        assertEquals( "values", args, directive.getValueDirectives() );
        assertNull( "base", directive.getBaseValue() );
        Construct construct = new Construct( directive );
        assertEquals( "resolved", new File( new File( "." ), "test" ), construct.resolve() );
    }
    
   /**
    * Validate equality operation.
    */
    public void testEquals()
    {
        ValueDirective base = new ValueDirective( "java.io.File", "." );
        ValueDirective spec1 = new ValueDirective( "test" );
        ValueDirective spec2 = new ValueDirective( "test" );
        if( base.equals( spec1 ) )
        {
            fail( "base and spec1 are not the same" );
        }
        if( base.equals( spec2 ) )
        {
            fail( "base and spec2 are not the same" );
        }
        if( !spec1.equals( spec2 ) )
        {
            fail( "spec1 and spec2 are the same" );
        }
    }
    
   /**
    * Test serialization.
    * @exception Exception if an error occurs
    */
    public void testSerialization() throws Exception
    {
        String target = "java.io.File";
        ValueDirective base = new ValueDirective( "java.io.File", "." );
        ValueDirective spec = new ValueDirective( "test" );
        ValueDirective[] args = new ValueDirective[]{base, spec};
        ValueDirective directive = new ValueDirective( target, args );
        doSerializationTest( directive );
    }
    
   /**
    * Test encoding.
    * @exception Exception if an error occurs
    */
    public void testEncoding() throws Exception
    {
        String target = "java.io.File";
        ValueDirective base = new ValueDirective( "java.io.File", "." );
        ValueDirective spec = new ValueDirective( "test" );
        ValueDirective[] args = new ValueDirective[]{base, spec};
        ValueDirective directive = new ValueDirective( target, args );
        ValueDirective result = (ValueDirective) doEncodingTest( directive, "value-directive.xml" );
        assertEquals( "encoded", directive, result );
    }
}


