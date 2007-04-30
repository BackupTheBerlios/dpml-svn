/*
 * Copyright 2004-2005 The Apache Software Foundation
 * Copyright 2005-2007 Stephen McConnell
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
package dpml.cli.validation;

import junit.framework.TestCase;

import dpml.cli.resource.ResourceConstants;
import dpml.cli.resource.ResourceHelper;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * EnumValidatorTest.
 *
 * @author $author$
 * @version $Revision$
  */
public class EnumValidatorTest extends TestCase
{
    private static final ResourceHelper RESOURCES = ResourceHelper.getResourceHelper(  );
    private final Set m_enumSet = 
      new TreeSet( 
        Arrays.asList( new Object[]{"red", "green", "blue"} ) );

    /**
     * Test enum validation.
     *
     * @throws InvalidArgumentException if an error occurs
     */
    public void testValidate(  ) throws InvalidArgumentException
    {
        final Object[] array = new Object[]{"red", "green"};

        final List list = Arrays.asList( array );
        final EnumValidator validator = new EnumValidator( m_enumSet );
        assertEquals( 
          "valid values are incorrect", 
          m_enumSet,
          validator.getValidValues() );
        validator.validate( list );

        final Iterator i = list.iterator(  );
        assertEquals( "red", i.next(  ) );
        assertEquals( "green", i.next(  ) );
        assertFalse( i.hasNext(  ) );
    }

    /**
     * Test non-member.
     */
    public void testNonMember(  )
    {
        final Object[] array = new Object[]{"red", "pink"};
        final List list = Arrays.asList( array );
        final EnumValidator validator = new EnumValidator( m_enumSet );

        try
        {
            validator.validate( list );
            fail( "InvalidArgumentException" );
        }
        catch( InvalidArgumentException e )
        {
            assertEquals( RESOURCES.getMessage( 
                    ResourceConstants.ENUM_ILLEGAL_VALUE,
                    new Object[]{"pink", validator.getValuesAsString(  )} ),
                e.getMessage(  ) );
        }
    }
}
