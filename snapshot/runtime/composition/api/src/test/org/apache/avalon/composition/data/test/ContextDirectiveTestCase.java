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

package org.apache.avalon.composition.data.test;

import junit.framework.TestCase;
import org.apache.avalon.composition.data.ImportDirective;
import org.apache.avalon.composition.data.EntryDirective;
import org.apache.avalon.composition.data.ContextDirective;

/**
 * ContextDirectiveTestCase does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ContextDirectiveTestCase.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class ContextDirectiveTestCase extends TestCase
{
    public ContextDirectiveTestCase( String name )
    {
        super( name );
    }

    public void testConstructor()
    {
        try
        {
            new ContextDirective( null );
        }
        catch(NullPointerException npe)
        {
            fail("NullPointerException should not be thrown - null indicates default");
        }

        try
        {
            new ContextDirective( null, new EntryDirective[0] );
        }
        catch(NullPointerException npe)
        {
            fail("NullPointerException should not be thrown - null indicates default");
        }

        try
        {
            new ContextDirective( null, null );
        }
        catch ( NullPointerException npe )
        {
            fail("NullPointerException should not be thrown - null indicated default");
        }
    }

    public void testContextDirective()
    {
        EntryDirective[] entries = new EntryDirective[0];
        ContextDirective cd = 
          new ContextDirective( getClass().getName(), entries );

        assertEquals( "classname", getClass().getName(), cd.getClassname());
        assertEquals( "entries", entries, cd.getEntryDirectives());
        assertEquals( "length", entries.length, cd.getEntryDirectives().length);
    }

    public void testGetEntry()
    {
        String key = "key";
        String val = "val";
        ImportDirective imp = new ImportDirective( key, "xxx" );
        EntryDirective[] entries = 
          new EntryDirective[]{ imp };
        ContextDirective cd = new ContextDirective( entries );

        assertNull( cd.getClassname() );
        assertEquals( entries, cd.getEntryDirectives() );
        assertEquals( entries.length, cd.getEntryDirectives().length );
        assertEquals( entries[0], cd.getEntryDirective( key ) );
        assertNull( cd.getEntryDirective( val ) );
    }
}
