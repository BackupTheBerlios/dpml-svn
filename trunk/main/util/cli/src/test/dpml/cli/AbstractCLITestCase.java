/**
 * Copyright 2004 The Apache Software Foundation
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
package dpml.cli;

import junit.framework.TestCase;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision$
  */
public abstract class AbstractCLITestCase extends TestCase
{
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static List list(  )
    {
        return Collections.EMPTY_LIST;
    }

    /**
     * DOCUMENT ME!
     *
     * @param args DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static List list( final Object[] args )
    {
        return new LinkedList( Arrays.asList( args ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @param arg0 DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static List list( final Object arg0 )
    {
        return list( new Object[]{arg0} );
    }

    /**
     * DOCUMENT ME!
     *
     * @param arg0 DOCUMENT ME!
     * @param arg1 DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static List list( final Object arg0, final Object arg1 )
    {
        return list( new Object[]{arg0, arg1} );
    }

    /**
     * DOCUMENT ME!
     *
     * @param arg0 DOCUMENT ME!
     * @param arg1 DOCUMENT ME!
     * @param arg2 DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static List list( final Object arg0, final Object arg1,
        final Object arg2 )
    {
        return list( new Object[]{arg0, arg1, arg2} );
    }

    /**
     * DOCUMENT ME!
     *
     * @param arg0 DOCUMENT ME!
     * @param arg1 DOCUMENT ME!
     * @param arg2 DOCUMENT ME!
     * @param arg3 DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static List list( final Object arg0, final Object arg1,
        final Object arg2, final Object arg3 )
    {
        return list( new Object[]{arg0, arg1, arg2, arg3} );
    }

    /**
     * DOCUMENT ME!
     *
     * @param arg0 DOCUMENT ME!
     * @param arg1 DOCUMENT ME!
     * @param arg2 DOCUMENT ME!
     * @param arg3 DOCUMENT ME!
     * @param arg4 DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static List list( final Object arg0, final Object arg1,
        final Object arg2, final Object arg3, final Object arg4 )
    {
        return list( new Object[]{arg0, arg1, arg2, arg3, arg4} );
    }

    /**
     * DOCUMENT ME!
     *
     * @param arg0 DOCUMENT ME!
     * @param arg1 DOCUMENT ME!
     * @param arg2 DOCUMENT ME!
     * @param arg3 DOCUMENT ME!
     * @param arg4 DOCUMENT ME!
     * @param arg5 DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static List list( final Object arg0, final Object arg1,
        final Object arg2, final Object arg3, final Object arg4,
        final Object arg5 )
    {
        return list( new Object[]{arg0, arg1, arg2, arg3, arg4, arg5} );
    }

    /**
     * DOCUMENT ME!
     *
     * @param expected DOCUMENT ME!
     * @param found DOCUMENT ME!
     */
    public static void assertListContentsEqual( final List expected,
        final List found )
    {
        final Iterator e = expected.iterator(  );
        final Iterator f = found.iterator(  );

        while( e.hasNext(  ) && f.hasNext(  ) )
        {
            assertEquals( e.next(  ), f.next(  ) );
        }

        if( e.hasNext(  ) )
        {
            fail( "Expected more elements" );
        }

        if( f.hasNext(  ) )
        {
            fail( "Found more elements" );
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param expected DOCUMENT ME!
     * @param found DOCUMENT ME!
     */
    public static void assertContentsEqual( final Collection expected,
        final Collection found )
    {
        assertTrue( expected.containsAll( found ) );
        assertTrue( found.containsAll( expected ) );
        assertEquals( expected.size(  ), found.size(  ) );
    }
}
