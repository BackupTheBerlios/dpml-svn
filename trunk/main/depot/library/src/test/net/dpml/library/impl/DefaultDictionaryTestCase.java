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

import net.dpml.library.model.Resource;

/**
 * Testing the DefaultDictionary implementation.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DefaultDictionaryTestCase extends AbstractTestCase
{   
   /**
    * Property names exposed by a top-level module such as 'dpml' should
    * include the 7 properties definined in the module plus 3 properties
    * defined in the library, and 4 dynamic basedir property (resulting 
    * in a total of 14 property names).
    * @exception Exception if the test fails
    */
    public void testModuleProperties() throws Exception
    {
        Resource resource = getLibrary().getResource( "dpml" );
        String[] names = resource.getPropertyNames();
        assertEquals( "property-count", 14, names.length );
    }
    
   /**
    * Properties in a reosurce reflect an aggregation of the properties
    * of all parent modules and the library.  In the case of the dpml-transit-main
    * resource there are 3 library properties, 7 module properties, 4 dynamic 
    * property, and 1 resource property - resulting in a total of 15 properties.
    * @exception Exception if the test fails
    */
    public void testResourceProperties() throws Exception
    {
        Resource resource = getLibrary().getResource( "dpml/transit/dpml-transit-main" );
        String[] names = resource.getPropertyNames();
        assertEquals( "property-count", 15, names.length );
    }
}
