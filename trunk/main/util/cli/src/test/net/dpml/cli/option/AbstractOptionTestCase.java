/**
 * Copyright 2003-2004 The Apache Software Foundation
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
package net.dpml.cli.option;

import java.util.List;

import net.dpml.cli.AbstractCLITestCase;
import net.dpml.cli.Option;
import net.dpml.cli.OptionException;
import net.dpml.cli.WriteableCommandLine;
import net.dpml.cli.commandline.WriteableCommandLineImpl;

/**
 * @author Rob Oxspring
 */
public abstract class AbstractOptionTestCase extends AbstractCLITestCase {

    public static WriteableCommandLine commandLine(
        final Option option,
        final List args) {
        return new WriteableCommandLineImpl(option, args);
    }

    public abstract void testTriggers();

    public abstract void testPrefixes();

    public abstract void testCanProcess();

    public abstract void testProcess() throws OptionException;

    public abstract void testValidate() throws OptionException;

    public abstract void testAppendUsage() throws OptionException;

    public abstract void testGetPreferredName();

    public abstract void testGetDescription();

    public abstract void testHelpLines();
}
