/**
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dpml.cli.validation;

import junit.framework.TestCase;

import net.dpml.cli.resource.ResourceConstants;
import net.dpml.cli.resource.ResourceHelper;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision$
  */
public class URLValidatorTest extends TestCase
{
    private static final ResourceHelper RESOURCES = ResourceHelper.getResourceHelper(  );

    /**
     * DOCUMENT ME!
     *
     * @throws InvalidArgumentException DOCUMENT ME!
     * @throws MalformedURLException DOCUMENT ME!
     */
    public void testValidate(  )
        throws InvalidArgumentException, MalformedURLException
    {
        final Object[] array = new Object[]
            {
                "http://www.apache.org/", "file:///etc"
            };
        final List list = Arrays.asList( array );
        final Validator validator = new URLValidator(  );

        validator.validate( list );

        final Iterator i = list.iterator(  );
        assertEquals( new URL( "http://www.apache.org/" ), i.next(  ) );
        assertEquals( new URL( "file:///etc" ), i.next(  ) );
        assertFalse( i.hasNext(  ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws InvalidArgumentException DOCUMENT ME!
     * @throws MalformedURLException DOCUMENT ME!
     */
    public void testMalformedURL(  )
        throws InvalidArgumentException, MalformedURLException
    {
        final Object[] array = new Object[]{"www.apache.org"};
        final List list = Arrays.asList( array );
        final Validator validator = new URLValidator(  );

        try
        {
            validator.validate( list );
        }
        catch( InvalidArgumentException e )
        {
            assertEquals( RESOURCES.getMessage( 
                    ResourceConstants.URLVALIDATOR_MALFORMED_URL,
                    new Object[]{"www.apache.org"} ), e.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testBadProtocol(  )
    {
        final Object[] array = new Object[]
        {
            "http://www.apache.org/", "file:///etc"
        };
        final List list = Arrays.asList( array );
        final URLValidator validator = new URLValidator(  );
        validator.setProtocol( "http" );

        assertEquals( "incorrect protocol", "http",
        validator.getProtocol(  ) );

        try
        {
            validator.validate( list );
            fail( "Expected InvalidArgumentException" );
        }
        catch( InvalidArgumentException e )
        {
            assertEquals( "file:///etc", e.getMessage(  ) );
        }
    }
    
     /**
     * DOCUMENT ME!
     */
    public void testBadProtocol2(  )
    {
        final Object[] array = new Object[]
        {
            "http://www.apache.org/", "file:///etc"
        };
        final List list = Arrays.asList( array );
        final URLValidator validator = new URLValidator( "http" );

        try
        {
            validator.validate( list );
            fail( "Expected InvalidArgumentException" );
        }
        catch( InvalidArgumentException e )
        {
            assertEquals( "file:///etc", e.getMessage(  ) );
        }
    }
}
