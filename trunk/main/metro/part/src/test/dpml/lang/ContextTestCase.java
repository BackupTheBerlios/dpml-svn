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
import java.net.URI;
import java.util.Hashtable;
import java.util.HashMap;

import junit.framework.TestCase;

import org.acme.Sample.Context;
import org.acme.Sample.AnotherContext;
import org.acme.Sample.Negation;

/**
 * Context test case.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ContextTestCase extends TestCase
{
   /**
    * Test the isaContext implementation for strict evaluation
    * of the org.acme.Sample.Context class.
    * @exception Exception if a test error occurs
    */
    public void testIsaContextUsingStrictAssessment() throws Exception
    {
        boolean foo = ContextInvocationHandler.isaContext( Context.class, true );
        assertEquals( "isa-context", false, foo );
    }

   /**
    * Test the isaContext implementation for non-strict evaluation
    * of the org.acme.Sample.Context class.
    * @exception Exception if a test error occurs
    */
    public void testIsaContextUsingLegacyAssessment() throws Exception
    {
        boolean foo = ContextInvocationHandler.isaContext( Context.class, false );
        assertEquals( "isa-context", true, foo );
    }

   /**
    * Test the isaContext implementation for negation using the 
    * org.acme.Sample.Negation.Context class.
    * @exception Exception if a test error occurs
    */
    public void testIsaContextNegation() throws Exception
    {
        boolean foo = ContextInvocationHandler.isaContext( Negation.Context.class, true );
        assertEquals( "isa-context-negation", false, foo );
    }

   /**
    * Test missing context entry detection.
    * @exception Exception if a test error occurs
    */
    public void testMissingRequiredEntry() throws Exception
    {
        try
        {
            Hashtable<String,Object > map = new Hashtable<String,Object>();
            ContextInvocationHandler.validate( Context.class, map );
            fail( "Did not throw IllegalArgumentException for missing key 'message'." );
        }
        catch( IllegalArgumentException e )
        {
            // success
        }
    }

   /**
    * Test null value for an entry detection.
    * @exception Exception if a test error occurs
    */
    public void testNullEntryValue() throws Exception
    {
        try
        {
            HashMap<String,Object> map = new HashMap< String,Object>();
            map.put( "message", null );
            ContextInvocationHandler.validate( Context.class, map );
            fail( "Did not throw IllegalArgumentException for null value" );
        }
        catch( IllegalArgumentException e )
        {
            // success
        }
    }

   /**
    * Test missing required entry detection.
    * @exception Exception if a test error occurs
    */
    public void testSimpleRequiredEntry() throws Exception
    {
        Hashtable<String,Object> map = new Hashtable<String,Object>();
        map.put( "message", MESSAGE );
        Context context = 
          ContextInvocationHandler.getProxiedInstance( Context.class, map );
        assertEquals( "message", MESSAGE, context.getMessage() );
    }

   /**
    * Test optional entry processing.
    * @exception Exception if a test error occurs
    */
    public void testSimpleOptionalEntry() throws Exception
    {
        Hashtable<String,Object> map = new Hashtable<String,Object>();
        map.put( "message", MESSAGE );
        map.put( "resourceURI", RESOURCE );
        Context context = 
          ContextInvocationHandler.getProxiedInstance( Context.class, map );
        assertEquals( "resourceURI", RESOURCE, context.getResourceURI( null ) );
    }
    
   /**
    * Test compound optional entry processing.
    * @exception Exception if a test error occurs
    */
    public void testCompoundOptionalEntry() throws Exception
    {
        Hashtable<String,Object> external = new Hashtable<String,Object>();
        external.put( "color", Color.RED );
        Hashtable<String,Object> map = new Hashtable<String,Object>();
        map.put( "message", MESSAGE );
        map.put( "external", external );
        Context context = 
          ContextInvocationHandler.getProxiedInstance( Context.class, map );
        AnotherContext ext = context.getExternal( null );
        assertEquals( "color", Color.RED, ext.getColor( null ) );
    }
    
   /**
    * Test instantiation of a class referencing a context parameter.
    * @exception Exception if a test error occurs
    */
    //public void testPluginInstantiation() throws Exception
    //{
    //    Hashtable<String,Object> external = new Hashtable<String,Object>();
    //    external.put( "color", Color.RED );
    //    Hashtable<String,Object> map = new Hashtable<String,Object>();
    //    map.put( "message", MESSAGE );
    //    map.put( "resourceURI", RESOURCE );
    //    map.put( "external", external );
    //    Context context = 
    //      ContextInvocationHandler.getProxiedInstance( Context.class, map );
    //    ServiceRegistry registry = new SimpleServiceRegistry( context );
    //    Plugin plugin = new Plugin( Sample.class );
    //    plugin.initialize( registry );
    //    Sample sample = plugin.getInstance( Sample.class );
    //}
    
    private static final String MESSAGE = "Hello.";
    private static final URI RESOURCE = setupURI();
    
    private static URI setupURI()
    {
        try
        {
            return ContextTestCase.class.getProtectionDomain().getCodeSource().getLocation().toURI();
        }
        catch( Throwable e )
        {
            e.printStackTrace();
            return null;
        }
    }
}
