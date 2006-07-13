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

package net.dpml.library.impl;

import net.dpml.library.Module;
import net.dpml.library.Resource;
import net.dpml.library.info.Scope;

/**
 * Test DefaultLibrary implementation.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class AnonymousTestCase extends AbstractTestCase
{
   /**
    * Testcase setup.
    * @exception Exception if a setup error occurs
    */
    public void setUp() throws Exception
    {
        setUp( "samples/anonymous.xml" );
    }

   /**
    * Test anonymous resource providers.
    * @exception Exception if a test error occurs
    */
    public void testAnonymousResourceProviders() throws Exception
    {
        Resource resource = getLibrary().getResource( "demo/example" );
        Resource[] providers = resource.getAggregatedProviders( Scope.RUNTIME, false, false );
        assertEquals( "direct-providers", 1, providers.length );
    }

   /**
    * Test anonymous module providers.
    * @exception Exception if a test error occurs
    */
    public void testAnonymousModuleProviders() throws Exception
    {
        Module module = (Module) getLibrary().getResource( "demo" );
        Resource[] providers = module.getAggregatedProviders( Scope.RUNTIME, false, false );
        assertEquals( "direct-providers", 1, providers.length );
    }
    
   /**
    * Test expanded anonymous module providers.
    * @exception Exception if a test error occurs
    */
    public void testExpandedAnonymousModuleProviders() throws Exception
    {
        Module module = (Module) getLibrary().getResource( "demo" );
        boolean expanded = true;
        boolean sorted = false;
        Resource[] providers = module.getAggregatedProviders( Scope.RUNTIME, expanded, sorted );
        assertEquals( "expanded-providers", 2, providers.length );
    }
}
