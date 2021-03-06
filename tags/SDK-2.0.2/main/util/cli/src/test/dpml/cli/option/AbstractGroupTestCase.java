/*
 * Copyright 2003-2005 The Apache Software Foundation
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
package dpml.cli.option;

import dpml.cli.OptionException;

/**
 * @author Rob Oxspring
 */
public abstract class AbstractGroupTestCase extends AbstractOptionTestCase
{
    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public abstract void testProcessOptions(  ) throws OptionException;

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public abstract void testProcessAnonymousArguments(  )
        throws OptionException;
}
