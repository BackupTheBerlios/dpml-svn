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
import org.apache.avalon.composition.data.MetaDataException;
import org.apache.avalon.composition.data.ImportDirective;

/**
 * ImportDirectiveTestCase does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ImportDirectiveTestCase.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class ImportDirectiveTestCase extends TestCase
{
    public ImportDirectiveTestCase( String name )
    {
        super( name );
    }

    public void testEntry() throws MetaDataException
    {
        String key = "key";
        String imp = "container-scoped-key";

        try
        {
            new ImportDirective( null, null );
            fail( "Did not throw expected NullPointerException" );
        }
        catch ( NullPointerException npe )
        {
            // Success!!
        }

        try
        {
            new ImportDirective( null, imp );
            fail( "Did not throw expected NullPointerException" );
        }
        catch ( NullPointerException npe )
        {
            // Success!!
        }

        try
        {
            new ImportDirective( key, null );
            fail( "Did not throw expected NullPointerException" );
        }
        catch ( NullPointerException npe )
        {
            // Success!!
        }

        ImportDirective entry = new ImportDirective( key, imp );
        assertEquals( key, entry.getKey() );
        assertEquals( imp, entry.getImportKey() );
    }
}