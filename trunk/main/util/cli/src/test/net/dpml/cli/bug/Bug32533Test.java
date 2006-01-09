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
package net.dpml.cli.bug;

import junit.framework.TestCase;

import net.dpml.cli.Group;
import net.dpml.cli.Option;
import net.dpml.cli.OptionException;
import net.dpml.cli.builder.DefaultOptionBuilder;
import net.dpml.cli.builder.GroupBuilder;
import net.dpml.cli.commandline.Parser;

/**
 * @author roxspring
 */
public class Bug32533Test extends TestCase
{
    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testBlah(  ) throws OptionException
    {
        Option a1 = new DefaultOptionBuilder(  ).withLongName( "a1" ).create(  );
        Option b1 = new DefaultOptionBuilder(  ).withLongName( "b1" ).create(  );
        Option c1 = new DefaultOptionBuilder(  ).withLongName( "c1" ).create(  );

        Group b = new GroupBuilder(  ).withOption( b1 ).create(  );
        Group c = new GroupBuilder(  ).withOption( c1 ).create(  );
        Group a = new GroupBuilder(  ).withOption( a1 ).withOption( b )
                                      .withOption( c ).create(  );

        Parser parser = new Parser(  );
        parser.setGroup( a );
        parser.parse( new String[]{"--a1", "--b1"} );
    }
}
