/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.avalon.logging.data.test;

import org.apache.avalon.logging.data.CategoryDirective;
import org.apache.avalon.logging.data.CategoriesDirective;

import java.io.*;

/**
 * CategoriesDirectiveTestCase.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: CategoriesDirectiveTestCase.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class CategoriesDirectiveTestCase extends CategoryDirectiveTestCase
{
    public CategoriesDirectiveTestCase( String name )
    {
        super( name );
    }

    public void testCategories()
    {
        String catName = "name";
        CategoriesDirective cat = 
          new CategoriesDirective( 
            catName, null, null, new CategoryDirective[0] );
        testCategory( cat, catName, null, null );
    }

    public void testSerialization() throws IOException, ClassNotFoundException
    {
        File file = new File("name.test");
        String name = "name";
        String priority = CategoryDirective.WARN;
        String target = "test";

        CategoriesDirective original = 
          new CategoriesDirective( name, priority, target, new CategoryDirective[0] );

        testCategory( original, name, priority, target );

        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
        oos.writeObject(original);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream( new FileInputStream(file));
        CategoriesDirective serialized = (CategoriesDirective)ois.readObject();
        ois.close();

        file.delete();

        testCategory( serialized, name, priority, target );

        assertEquals( original, serialized );
        assertEquals( original.hashCode(), serialized.hashCode() );
    }
}
