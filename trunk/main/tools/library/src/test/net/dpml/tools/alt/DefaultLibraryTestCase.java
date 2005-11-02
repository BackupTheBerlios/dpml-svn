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

package net.dpml.tools.alt;

import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.util.Properties;

import junit.framework.TestCase;

import net.dpml.tools.model.Module;
import net.dpml.tools.model.Library;
import net.dpml.tools.model.Processor;
import net.dpml.tools.model.Resource;
import net.dpml.tools.model.Type;
import net.dpml.tools.model.ProcessorNotFoundException;
import net.dpml.tools.info.LibraryDirective;
import net.dpml.tools.info.ResourceDirective;
import net.dpml.tools.info.Scope;

import net.dpml.transit.Logger;
import net.dpml.transit.monitor.LoggingAdapter;

/**
 * Test DefaultLibrary implementation.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class DefaultLibraryTestCase extends AbstractTestCase
{       
   /**
    * Test library properties.
    */
    public void testProperties() throws Exception
    {
        Resource resource = m_library.getResource( "demo/demo" );
        Resource[] providers = resource.getProviders( Scope.RUNTIME, true, true );
        for( int i=0; i<providers.length; i++ )
        {
            System.out.println( "# " + providers[i] );
        }
    }
    
}
