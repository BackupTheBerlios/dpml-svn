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
import org.apache.avalon.composition.data.Parameter;
import org.apache.avalon.composition.data.MetaDataException;

/**
 * ParameterTestCase does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ParameterTestCase.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class ParameterTestCase extends TestCase
{
    public ParameterTestCase( String name )
    {
        super( name );
    }

    public void testParameters() throws MetaDataException
    {
        String className = ParameterTestCase.class.getName();
        String value = "val";
        Parameter[] params = new Parameter[] {
            new Parameter( "java.io.File", value ),
            new Parameter( ParameterTestCase.class.getName(), value )
        };

        try
        {
            new Parameter(null);
            // Success!!
        }
        catch (NullPointerException npe)
        {
            fail("Null signifes a null argument.");
        }

        try
        {
            new Parameter( null, value );
            fail( "Null classname must throw a NullPointerException" );
        }
        catch ( NullPointerException npe )
        {
            // Success!!
        }

        try
        {
            new Parameter( className, (String)null );
            // Success!!
        }
        catch ( NullPointerException npe )
        {
            fail( "Null argument is a valid argument." );
        }

        try
        {
            new Parameter( null, params );
            fail( "NullPointerException must be thrown for a null classname." );
        }
        catch ( NullPointerException npe )
        {
            // Success!!
        }

        try
        {
            new Parameter( className, (Parameter[]) null );
            fail( "NullPointerException must be thorwn for null parameters." );
        }
        catch ( NullPointerException npe )
        {
            // Success!!
        }

        Parameter param = new Parameter( value );
        assertEquals( String.class.getName(), param.getClassname());

        param = new Parameter( className, params );
        assertEquals( className, param.getClassname() );
        assertEquals( params.length, param.getParameters().length );
        assertEquals( params.length, param.getParameters().length );
        assertEquals( "java.io.File", param.getParameters()[0].getClassname() );
        assertEquals( ParameterTestCase.class.getName(), param.getParameters()[1].getClassname() );

    }
}