/*
 * Copyright 2003-2005 The Apache Software Foundation
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

import net.dpml.cli.resource.ResourceHelper;

import java.net.URL;
import java.net.URLClassLoader;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision$
  */
public class ClassValidatorTest extends TestCase
{
    private final static ResourceHelper resources = ResourceHelper.getResourceHelper(  );
    private ClassValidator validator;

    /**
     * DOCUMENT ME!
     */
    protected void setUp(  )
    {
        validator = new ClassValidator(  );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws InvalidArgumentException DOCUMENT ME!
     */
    public void testValidName(  ) throws InvalidArgumentException
    {
        final Object[] array = new Object[]{"MyApp", "org.apache.ant.Main"};
        final List list = Arrays.asList( array );

        validator.validate( list );

        assertEquals( "Name is incorrect", "MyApp", list.get( 0 ) );
        assertEquals( "Name is incorrect", "org.apache.ant.Main", list.get( 1 ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testNameBadStart(  )
    {
        final String className = "1stClass";
        final Object[] array = new Object[]{className};
        final List list = Arrays.asList( array );

        try
        {
            validator.validate( list );
            fail( "Class name cannot start with a number." );
        }
        catch( InvalidArgumentException ive )
        {
            assertEquals( resources.getMessage( 
                    "ClassValidator.bad.classname", className ),
                ive.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testNameBadEnd(  )
    {
        final String className = "My.Class.";

        final Object[] array = new Object[]{className};
        final List list = Arrays.asList( array );

        try
        {
            validator.validate( list );
            fail( "Trailing period not permitted." );
        }
        catch( InvalidArgumentException ive )
        {
            assertEquals( resources.getMessage( 
                    "ClassValidator.bad.classname", className ),
                ive.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testNameBadMiddle(  )
    {
        final String className = "My..Class";

        final Object[] array = new Object[]{className};
        final List list = Arrays.asList( array );

        try
        {
            validator.validate( list );
            fail( "Two consecutive periods is not permitted." );
        }
        catch( InvalidArgumentException ive )
        {
            assertEquals( resources.getMessage( 
                    "ClassValidator.bad.classname", className ),
                ive.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testIllegalNameChar(  )
    {
        final String className = "My?Class";

        final Object[] array = new Object[]{className};
        final List list = Arrays.asList( array );

        try
        {
            validator.validate( list );
            fail( "Illegal character not allowed in Class name." );
        }
        catch( InvalidArgumentException ive )
        {
            assertEquals( resources.getMessage( 
                    "ClassValidator.bad.classname", className ),
                ive.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testLoadable(  )
    {
        assertFalse( "Validator is loadable", validator.isLoadable(  ) );
        validator.setLoadable( true );
        assertTrue( "Validator is NOT loadable", validator.isLoadable(  ) );
        validator.setLoadable( false );
        assertFalse( "Validator is loadable", validator.isLoadable(  ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws InvalidArgumentException DOCUMENT ME!
     */
    public void testLoadValid(  ) throws InvalidArgumentException
    {
        final Object[] array = new Object[]
            {
                "net.dpml.cli.Option", "java.util.Vector"
            };
        final List list = Arrays.asList( array );

        validator.setLoadable( true );
        validator.validate( list );

        final Iterator i = list.iterator(  );
        assertEquals( "net.dpml.cli.Option", ( (Class) i.next(  ) ).getName(  ) );
        assertEquals( "java.util.Vector", ( (Class) i.next(  ) ).getName(  ) );
        assertFalse( i.hasNext(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testLoadInvalid(  )
    {
        final String className = "net.dpml.cli.NonOption";

        final Object[] array = new Object[]{className, "java.util.Vectors"};
        final List list = Arrays.asList( array );

        validator.setLoadable( true );

        try
        {
            validator.validate( list );
            fail( "Class Not Found" );
        }
        catch( InvalidArgumentException ive )
        {
            assertEquals( resources.getMessage( 
                    "ClassValidator.class.notfound", className ),
                ive.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testInstantiate(  )
    {
        assertFalse( "Validator creates instances", validator.isInstance(  ) );
        validator.setInstance( true );
        assertTrue( "Validator does NOT create instances",
            validator.isInstance(  ) );
        validator.setInstance( false );
        assertFalse( "Validator creates instances", validator.isInstance(  ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws InvalidArgumentException DOCUMENT ME!
     */
    public void testCreateClassInstance(  ) throws InvalidArgumentException
    {
        final Object[] array = new Object[]{"java.util.Vector"};
        final List list = Arrays.asList( array );

        validator.setInstance( true );

        validator.validate( list );
        assertTrue( "Vector instance NOT found",
            list.get( 0 ) instanceof java.util.Vector );
    }

    /**
     * DOCUMENT ME!
     */
    public void testCreateInterfaceInstance(  )
    {
        final String className = "java.util.Map";
        final Object[] array = new Object[]{className};
        final List list = Arrays.asList( array );

        validator.setInstance( true );

        try
        {
            validator.validate( list );
            fail( "It's not possible to create a '" + className + "'" );
        }
        catch( final InvalidArgumentException ive )
        {
            assertEquals( resources.getMessage( "ClassValidator.class.create",
                    className ), ive.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testCreateProtectedInstance(  )
    {
        final String className = "net.dpml.cli.validation.protect.ProtectedClass";
        final Object[] array = new Object[]{className};
        final List list = Arrays.asList( array );

        validator.setInstance( true );

        try
        {
            validator.validate( list );
            fail( "It's not possible to create a '" + className + "'" );
        }
        catch( final InvalidArgumentException ive )
        {
            assertEquals( resources.getMessage( "ClassValidator.class.access",
                    className,
                    "Class net.dpml.cli.validation.ClassValidator " +
                    "can not access a member of class " +
                    "net.dpml.cli.validation.protect.ProtectedClass " +
                    "with modifiers \"protected\"" ), ive.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     */
    public void testClassloader(  )
    {
        assertEquals( "Wrong classloader found",
            validator.getClass(  ).getClassLoader(  ),
            validator.getClassLoader(  ) );

        URLClassLoader classloader = new URLClassLoader( new URL[]{} );
        validator.setClassLoader( classloader );

        assertEquals( "Wrong classloader found", classloader,
            validator.getClassLoader(  ) );
    }
}
