/*
 * Copyright 2004-2005 The Apache Software Foundation
 * Copyright 2005-2006 Stephen McConnell, The Digital Product Management Laboratory
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

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

/**
 * JUnit test case for the FileValidator.
 *
 * @author Rob Oxspring
 * @author John Keyes
 */
public class FileValidatorTest extends TestCase
{
    /**
     * DOCUMENT ME!
     *
     * @throws InvalidArgumentException DOCUMENT ME!
     */
    public void testValidate(  ) throws InvalidArgumentException
    {
        final Object[] array = new Object[]
            {
                "src", "build.xml", "veryunlikelyfilename"
            };
        final List list = Arrays.asList( array );
        final FileValidator validator = new FileValidator(  );

        validator.validate( list );

        final Iterator i = list.iterator(  );
        assertEquals( "src", new File( "src" ), i.next(  ) );
        assertEquals( "build.xml", new File( "build.xml" ), i.next(  ) );
        assertEquals( "veryunlikelyfilename",
            new File( "veryunlikelyfilename" ), i.next(  ) );
        assertFalse( "next", i.hasNext(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testValidateDirectory(  )
    {
        final Object[] array = new Object[]{"src", "build.xml"};
        final List list = Arrays.asList( array );
        final FileValidator validator = FileValidator.getExistingDirectoryInstance(  );

        assertTrue( "is a directory validator", validator.isDirectory(  ) );
        assertFalse( "is not a file validator", validator.isFile(  ) );
        assertTrue( "is an existing file validator", validator.isExisting(  ) );

        //assertFalse("is not a hidden file validator", validator.isHidden());
        try
        {
            validator.validate( list );
            fail( "InvalidArgumentException" );
        }
        catch( InvalidArgumentException e )
        {
            assertEquals( "build.xml", e.getMessage(  ) );
        }
    }

    //
    /**
     * DOCUMENT ME!
     */
    public void testValidateExisting(  )
    {
        final Object[] array = new Object[]{"build.xml", "veryunlikelyfilename"};
        final List list = Arrays.asList( array );
        final FileValidator validator = FileValidator.getExistingInstance(  );

        assertFalse( "is not a directory validator", validator.isDirectory(  ) );
        assertFalse( "is not a file validator", validator.isFile(  ) );
        assertTrue( "is an existing file validator", validator.isExisting(  ) );

        //assertFalse("is not a hidden file validator", validator.isHidden());
        try
        {
            validator.validate( list );
            fail( "InvalidArgumentException" );
        }
        catch( InvalidArgumentException e )
        {
            assertEquals( "veryunlikelyfilename", e.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testValidateFile(  )
    {
        final Object[] array = new Object[]{"build.xml", "src"};
        final List list = Arrays.asList( array );
        final Validator validator = FileValidator.getExistingFileInstance(  );

        try
        {
            validator.validate( list );
            fail( "InvalidArgumentException" );
        }
        catch( InvalidArgumentException e )
        {
            assertEquals( "src", e.getMessage(  ) );
        }
    }
}
