/*
 * Copyright 2004 Stephen J. McConnell.
 * Copyright 1999-2004 The Apache Software Foundation
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
package net.dpml.i18n.test;

import java.util.MissingResourceException;

import junit.framework.TestCase;

import net.dpml.i18n.ResourceManager;
import net.dpml.i18n.Resources;

/**
 * TestCase for ResourceManager.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class ResourceManagerTestCase
    extends TestCase
{
   /**
    * Test class resources.
    */
    public void testClassResources()
    {
        try
        {
            final Resources resources =
                ResourceManager.getClassResources( getClass() );

            resources.getBundle();
        }
        catch( final MissingResourceException mre )
        {
            fail( "Unable to find class resource for class " + getClass() );
        }
    }

   /**
    * Test package resources.
    */
    public void testPackageResources()
    {
        try
        {
            final Resources resources =
                ResourceManager.getPackageResources( getClass() );

            resources.getBundle();
        }
        catch( final MissingResourceException mre )
        {
            fail( "Unable to find package resources for class " + getClass() );
        }
    }
}
