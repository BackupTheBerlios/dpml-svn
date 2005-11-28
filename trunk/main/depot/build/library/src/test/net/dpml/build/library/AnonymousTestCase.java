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

package net.dpml.build.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.Properties;

import junit.framework.TestCase;

import net.dpml.build.model.Module;
import net.dpml.build.model.Library;
import net.dpml.build.model.Processor;
import net.dpml.build.model.Resource;
import net.dpml.build.model.Type;
import net.dpml.build.model.ProcessorNotFoundException;
import net.dpml.build.info.LibraryDirective;
import net.dpml.build.info.ResourceDirective;
import net.dpml.build.info.Scope;

import net.dpml.transit.Logger;
import net.dpml.transit.monitor.LoggingAdapter;

/**
 * Test DefaultLibrary implementation.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class AnonymousTestCase extends AbstractTestCase
{
    public void setUp() throws Exception
    {
        setUp( "test.xml" );
    }

   /**
    * Test library properties.
    */

    public void testAnonymousResourceProviders() throws Exception
    {
        Resource resource = m_library.getResource( "demo/example" );
        Resource[] providers = resource.getAggregatedProviders( Scope.RUNTIME, false, false );
        assertEquals( "direct-providers", 1, providers.length );
    }

    public void testAnonymousModuleProviders() throws Exception
    {
        Module module = (Module) m_library.getResource( "demo" );
        Resource[] providers = module.getAggregatedProviders( Scope.RUNTIME, false, false );
        assertEquals( "direct-providers", 1, providers.length );
    }
    
    public void testExpandedAnonymousModuleProviders() throws Exception
    {
        Module module = (Module) m_library.getResource( "demo" );
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
