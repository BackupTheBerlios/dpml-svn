/**
 * Copyright 2003-2004 The Apache Software Foundation
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
package dpml.cli.util;

import dpml.cli.AbstractCLITestCase;
import dpml.cli.Option;
import dpml.cli.option.CommandTest;
import dpml.cli.option.DefaultOptionTest;
import dpml.cli.option.GroupTest;
import dpml.cli.option.ParentTest;
import dpml.cli.option.SwitchTest;

import java.util.Collections;
import java.util.List;

/**
 * @author Rob Oxspring
 */
public class ComparatorsTest extends AbstractCLITestCase
{
    /**
     * DOCUMENT ME!
     */
    public void testGroupFirst(  )
    {
        final Option o1 = GroupTest.buildAntGroup(  );
        final Option o2 = ParentTest.buildLibParent(  );
        final List list = AbstractCLITestCase.list( o1, o2 );

        Collections.sort( list, Comparators.groupFirst(  ) );

        AbstractCLITestCase.assertListContentsEqual( AbstractCLITestCase.list( 
                o1, o2 ), list );
    }

    /**
     * DOCUMENT ME!
     */
    public void testGroupLast(  )
    {
        final Option o1 = GroupTest.buildAntGroup(  );
        final Option o2 = ParentTest.buildLibParent(  );
        final List list = AbstractCLITestCase.list( o1, o2 );

        Collections.sort( list, Comparators.groupLast(  ) );

        AbstractCLITestCase.assertListContentsEqual( AbstractCLITestCase.list( 
                o2, o1 ), list );
    }

    /**
     * DOCUMENT ME!
     */
    public void testSwitchFirst(  )
    {
        final Option o1 = SwitchTest.buildDisplaySwitch(  );
        final Option o2 = ParentTest.buildLibParent(  );
        final List list = AbstractCLITestCase.list( o1, o2 );

        Collections.sort( list, Comparators.switchFirst(  ) );

        AbstractCLITestCase.assertListContentsEqual( AbstractCLITestCase.list( 
                o1, o2 ), list );
    }

    /**
     * DOCUMENT ME!
     */
    public void testSwitchLast(  )
    {
        final Option o1 = SwitchTest.buildDisplaySwitch(  );
        final Option o2 = ParentTest.buildLibParent(  );

        //final Option o3 = new SwitchBuilder().withName("hidden").create();
        final List list = AbstractCLITestCase.list( o1, o2 );

        Collections.sort( list, Comparators.switchLast(  ) );

        AbstractCLITestCase.assertListContentsEqual( AbstractCLITestCase.list( 
                o2, o1 ), list );
    }

    /**
     * DOCUMENT ME!
     */
    public void testCommandFirst(  )
    {
        final Option o1 = CommandTest.buildCommitCommand(  );
        final Option o2 = ParentTest.buildLibParent(  );
        final List list = AbstractCLITestCase.list( o1, o2 );

        Collections.sort( list, Comparators.commandFirst(  ) );

        AbstractCLITestCase.assertListContentsEqual( AbstractCLITestCase.list( 
                o1, o2 ), list );
    }

    /**
     * DOCUMENT ME!
     */
    public void testCommandLast(  )
    {
        final Option o1 = CommandTest.buildCommitCommand(  );
        final Option o2 = ParentTest.buildLibParent(  );
        final List list = AbstractCLITestCase.list( o1, o2 );

        Collections.sort( list, Comparators.commandLast(  ) );

        AbstractCLITestCase.assertListContentsEqual( AbstractCLITestCase.list( 
                o2, o1 ), list );
    }

    /**
     * DOCUMENT ME!
     */
    public void testDefaultOptionFirst(  )
    {
        final Option o1 = DefaultOptionTest.buildHelpOption(  );
        final Option o2 = CommandTest.buildCommitCommand(  );
        final List list = AbstractCLITestCase.list( o1, o2 );

        Collections.sort( list, Comparators.defaultOptionFirst(  ) );

        AbstractCLITestCase.assertListContentsEqual( AbstractCLITestCase.list( 
                o1, o2 ), list );
    }

    /**
     * DOCUMENT ME!
     */
    public void testDefaultOptionLast(  )
    {
        final Option o1 = DefaultOptionTest.buildHelpOption(  );
        final Option o2 = CommandTest.buildCommitCommand(  );
        final List list = AbstractCLITestCase.list( o1, o2 );

        Collections.sort( list, Comparators.defaultOptionLast(  ) );

        AbstractCLITestCase.assertListContentsEqual( AbstractCLITestCase.list( 
                o2, o1 ), list );
    }

    /**
     * DOCUMENT ME!
     */
    public void testNamedFirst(  )
    {
        final Option o1 = DefaultOptionTest.buildHelpOption(  );
        final Option o2 = ParentTest.buildLibParent(  );
        final List list = AbstractCLITestCase.list( o1, o2 );

        Collections.sort( list, Comparators.namedFirst( "--help" ) );

        AbstractCLITestCase.assertListContentsEqual( AbstractCLITestCase.list( 
                o1, o2 ), list );
    }

    /**
     * DOCUMENT ME!
     */
    public void testNamedLast(  )
    {
        final Option o1 = DefaultOptionTest.buildHelpOption(  );
        final Option o2 = ParentTest.buildLibParent(  );
        final List list = AbstractCLITestCase.list( o1, o2 );

        Collections.sort( list, Comparators.namedLast( "--help" ) );

        AbstractCLITestCase.assertListContentsEqual( AbstractCLITestCase.list( 
                o2, o1 ), list );
    }

    /**
     * DOCUMENT ME!
     */
    public void testPreferredNameFirst(  )
    {
        final Option o1 = DefaultOptionTest.buildHelpOption(  );
        final Option o2 = ParentTest.buildLibParent(  );
        final List list = AbstractCLITestCase.list( o1, o2 );

        Collections.sort( list, Comparators.preferredNameFirst(  ) );

        AbstractCLITestCase.assertListContentsEqual( AbstractCLITestCase.list( 
                o1, o2 ), list );
    }

    /**
     * DOCUMENT ME!
     */
    public void testPreferredNameLast(  )
    {
        final Option o1 = DefaultOptionTest.buildHelpOption(  );
        final Option o2 = ParentTest.buildLibParent(  );
        final List list = AbstractCLITestCase.list( o1, o2 );

        Collections.sort( list, Comparators.preferredNameLast(  ) );

        AbstractCLITestCase.assertListContentsEqual( AbstractCLITestCase.list( 
                o2, o1 ), list );
    }

    /**
     * DOCUMENT ME!
     */
    public void testRequiredFirst(  )
    {
        final Option o1 = DefaultOptionTest.buildHelpOption(  );
        final Option o2 = DefaultOptionTest.buildXOption(  );
        final List list = AbstractCLITestCase.list( o1, o2 );

        Collections.sort( list, Comparators.requiredFirst(  ) );

        AbstractCLITestCase.assertListContentsEqual( AbstractCLITestCase.list( 
                o2, o1 ), list );
    }

    /**
     * DOCUMENT ME!
     */
    public void testRequiredLast(  )
    {
        final Option o1 = DefaultOptionTest.buildHelpOption(  );
        final Option o2 = DefaultOptionTest.buildXOption(  );
        final List list = AbstractCLITestCase.list( o1, o2 );

        Collections.sort( list, Comparators.requiredLast(  ) );

        AbstractCLITestCase.assertListContentsEqual( AbstractCLITestCase.list( 
                o1, o2 ), list );
    }

    /**
     * DOCUMENT ME!
     */
    public void testChained(  )
    {
        final Option o1 = CommandTest.buildCommitCommand(  );
        final Option o2 = SwitchTest.buildDisplaySwitch(  );
        final Option o3 = DefaultOptionTest.buildHelpOption(  );
        final List list = AbstractCLITestCase.list( o1, o2, o3 );

        Collections.sort( list,
            Comparators.chain( Comparators.namedFirst( "--help" ),
                Comparators.commandFirst(  ) ) );

        AbstractCLITestCase.assertListContentsEqual( AbstractCLITestCase.list( 
                o3, o1, o2 ), list );
    }
}
