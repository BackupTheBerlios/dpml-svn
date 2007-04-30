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
package dpml.cli.commandline;

import dpml.cli.AbstractWriteableCommandLineTestCase;
import dpml.cli.WriteableCommandLine;

import java.util.ArrayList;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision$
  */
public class WriteableCommandLineImplTest
    extends AbstractWriteableCommandLineTestCase
{
    /* (non-Javadoc)
     * @see dpml.cli.WriteableCommandLineTest#createWriteableCommandLine()
     */
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected WriteableCommandLine createWriteableCommandLine(  )
    {
        return new WriteableCommandLineImpl( ROOT, new ArrayList(  ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testToMakeEclipseSpotTheTestCase(  )
    {
        // nothing to test
    }
}
