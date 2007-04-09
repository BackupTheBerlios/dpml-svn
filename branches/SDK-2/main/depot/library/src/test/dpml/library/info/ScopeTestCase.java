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

package dpml.library.info;

/**
 * Test the scope enumeration.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ScopeTestCase extends AbstractTestCase
{
    static final InfoDirective INFO = new InfoDirective( "test", "about test" );

   /**
    * Test build scope.
    */
    public void testBuildScope()
    {
        Scope scope = Scope.valueOf( "BUILD" );
        assertEquals( "scope", scope, Scope.BUILD );
    }
    
   /**
    * Test runtime scope.
    */
    public void testRuntimeScope()
    {
        Scope scope = Scope.valueOf( "RUNTIME" );
        assertEquals( "scope", scope, Scope.RUNTIME );
    }
    
   /**
    * Test test scope.
    */
    public void testTestScope()
    {
        Scope scope = Scope.valueOf( "TEST" );
        assertEquals( "scope", scope, Scope.TEST );
    }
    
   /**
    * Test illegal scope.
    */
    public void testIllegalScope()
    {
        try
        {
            Scope scope = Scope.valueOf( "abc" );
            fail( "Did not throw IllegalArgumentException" );
        }
        catch( IllegalArgumentException e )
        {
            // success
        }
    }
    
   /**
    * Test illegal scope.
    */
    public void testEnumerationCount()
    {
        Scope[] scopes = Scope.values();
        assertEquals( "scope count", 3, scopes.length );
    }
}
