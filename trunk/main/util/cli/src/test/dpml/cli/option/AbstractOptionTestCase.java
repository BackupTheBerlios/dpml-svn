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
package dpml.cli.option;

import dpml.cli.AbstractCLITestCase;
import dpml.cli.Option;
import dpml.cli.OptionException;
import dpml.cli.WriteableCommandLine;
import dpml.cli.commandline.WriteableCommandLineImpl;

import java.util.List;

/**
 * @author Rob Oxspring
 */
public abstract class AbstractOptionTestCase extends AbstractCLITestCase
{
    /**
     * DOCUMENT ME!
     *
     * @param option DOCUMENT ME!
     * @param args DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static WriteableCommandLine commandLine( final Option option,
        final List args )
    {
        return new WriteableCommandLineImpl( option, args );
    }

    /**
     * DOCUMENT ME!
     */
    public abstract void testTriggers(  );

    /**
     * DOCUMENT ME!
     */
    public abstract void testPrefixes(  );

    /**
     * DOCUMENT ME!
     */
    public abstract void testCanProcess(  );

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public abstract void testProcess(  ) throws OptionException;

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public abstract void testValidate(  ) throws OptionException;

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public abstract void testAppendUsage(  ) throws OptionException;

    /**
     * DOCUMENT ME!
     */
    public abstract void testGetPreferredName(  );

    /**
     * DOCUMENT ME!
     */
    public abstract void testGetDescription(  );

    /**
     * DOCUMENT ME!
     */
    public abstract void testHelpLines(  );
}
