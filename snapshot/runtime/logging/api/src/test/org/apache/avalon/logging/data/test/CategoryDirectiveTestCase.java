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

import junit.framework.TestCase;

import org.apache.avalon.logging.data.CategoryDirective;

import java.io.*;

/**
 * CategoryTestCase does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: CategoryDirectiveTestCase.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class CategoryDirectiveTestCase extends TestCase
{
    public CategoryDirectiveTestCase( String name )
    {
        super( name );
    }

    public void testCategory()
    {
        String catName = "name";
        CategoryDirective cat = new CategoryDirective(catName);

        testCategory( cat, catName, null, null );
    }

    public void testLogPriority()
    {
        String catName = "name";
        String priority = CategoryDirective.DEBUG;
        CategoryDirective cat = new CategoryDirective( catName, priority);

        testCategory( cat, catName, priority, null );

        priority = CategoryDirective.ERROR;
        cat = new CategoryDirective( catName, priority );

        testCategory( cat, catName, priority, null );

        priority = CategoryDirective.INFO;
        cat = new CategoryDirective( catName, priority );

        testCategory( cat, catName, priority, null );

        priority = CategoryDirective.WARN;
        cat = new CategoryDirective( catName, priority );

        testCategory( cat, catName, priority, null );
    }

    public void testLogTarget()
    {
        String name = "name";
        String priority = CategoryDirective.DEBUG;
        String target = "test";
        CategoryDirective cat = new CategoryDirective( name, priority, target);

        testCategory( cat, name, priority, target );
    }

    protected void testCategory( CategoryDirective cat, String name, String priority, String target )
    {
        assertEquals( name, cat.getName() );
        assertEquals( priority, cat.getPriority() );
        assertEquals( target, cat.getTarget() );
    }

    public void testSerialization() throws IOException, ClassNotFoundException
    {
        File file = new File("name.test");
        String name = "name";
        String priority = CategoryDirective.WARN;
        String target = "test";

        CategoryDirective original = new CategoryDirective( name, priority, target );

        testCategory( original, name, priority, target );

        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
        oos.writeObject(original);
        oos.close();

        ObjectInputStream ois = new ObjectInputStream( new FileInputStream(file));
        CategoryDirective serialized = (CategoryDirective)ois.readObject();
        ois.close();

        file.delete();

        testCategory( serialized, name, priority, target );

        assertEquals( original, serialized );
        assertEquals( original.hashCode(), serialized.hashCode() );
    }
}
