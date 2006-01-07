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

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.Properties;

import junit.framework.TestCase;

import net.dpml.library.model.Module;
import net.dpml.library.model.Library;
import net.dpml.library.model.Resource;
import net.dpml.library.model.Type;
import net.dpml.library.info.LibraryDirective;
import net.dpml.library.info.ResourceDirective;
import net.dpml.library.info.Scope;

import net.dpml.transit.Logger;
import net.dpml.transit.monitor.LoggingAdapter;

/**
 * Test DefaultLibrary implementation.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class AnonymousTestCase extends AbstractTestCase
{
    public void setUp() throws Exception
    {
        setUp( "test.xml" );
    }

   /**
    * Test anonymous resource providers.
    * @exception if a test error occurs
    */
    public void testAnonymousResourceProviders() throws Exception
    {
        Resource resource = getLibrary().getResource( "demo/example" );
        Resource[] providers = resource.getAggregatedProviders( Scope.RUNTIME, false, false );
        assertEquals( "direct-providers", 1, providers.length );
    }

   /**
    * Test anonymous module providers.
    * @exception if a test error occurs
    */
    public void testAnonymousModuleProviders() throws Exception
    {
        Module module = (Module) getLibrary().getResource( "demo" );
        Resource[] providers = module.getAggregatedProviders( Scope.RUNTIME, false, false );
        assertEquals( "direct-providers", 1, providers.length );
    }
    
   /**
    * Test expanded anonymous module providers.
    * @exception if a test error occurs
    */
    public void testExpandedAnonymousModuleProviders() throws Exception
    {
        Module module = (Module) getLibrary().getResource( "demo" );
        Resource[] providers = module.getAggregatedProviders( Scope.RUNTIME, true, false );
        assertEquals( "expanded-providers", 2, providers.length );
        boolean test = false;
        for( int i=0; i<providers.length; i++ )
        {
            Resource provider = providers[i];
            if( provider.getResourcePath().equals( "commons-collections/commons-collections" ) )
            {
                test = true;
            }
        }
        if( !test )
        {
            fail( "Missing commons-collection dynamic dependency." );
        }
    }
}
