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

package net.dpml.library.info;

/**
 * The ModuleDirective class describes a module data-structure.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class DependencyDirectiveTestCase extends AbstractTestCase
{
    static final IncludeDirective[] INCLUDES = IncludeDirectiveTestCase.INCLUDES;
    static final DependencyDirective[] DEPENDENCIES = new DependencyDirective[3];
    static
    {
        DEPENDENCIES[0] = new DependencyDirective( Scope.BUILD, INCLUDES, PROPERTIES );
        DEPENDENCIES[1] = new DependencyDirective( Scope.RUNTIME, INCLUDES, PROPERTIES );
        DEPENDENCIES[2] = new DependencyDirective( Scope.TEST, INCLUDES, PROPERTIES );
    }
      
   /**
    * Test null scope.
    */
    public void testNullScope()
    {
        try
        {
            DependencyDirective dep = 
              new DependencyDirective( null, INCLUDES, PROPERTIES );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
   /**
    * Test null includes.
    */
    public void testNullIncludes()
    {
        try
        {
            DependencyDirective dep = 
              new DependencyDirective( Scope.RUNTIME, null, PROPERTIES );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
   /**
    * Test runtime scope.
    */
    public void testScopeRuntime()
    {
        DependencyDirective dep = 
          new DependencyDirective( Scope.RUNTIME, INCLUDES, PROPERTIES );
        assertEquals( "scope", Scope.RUNTIME, dep.getScope() );
    }
    
   /**
    * Test test scope.
    */
    public void testScopeTest()
    {
        DependencyDirective dep = 
          new DependencyDirective( Scope.TEST, INCLUDES, PROPERTIES );
        assertEquals( "scope", Scope.TEST, dep.getScope() );
    }
    
   /**
    * Test includes.
    */
    public void testIncludes()
    {
        DependencyDirective dep = new DependencyDirective( Scope.RUNTIME, INCLUDES, PROPERTIES );
        assertEquals( "includes", 3, dep.getIncludeDirectives().length );
    }
    
   /**
    * Test directive serialization.
    * @exception Exception if the serailization test raises an error
    */
    public void testSerialization() throws Exception
    {
        DependencyDirective dep = new DependencyDirective( Scope.RUNTIME, INCLUDES, PROPERTIES );
        doSerializationTest( dep );
    }
}
