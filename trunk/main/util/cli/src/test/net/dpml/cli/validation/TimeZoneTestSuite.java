/**
 * Copyright 2005 The Apache Software Foundation
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

import junit.extensions.TestDecorator;

import junit.framework.Test;
import junit.framework.TestResult;

import java.util.TimeZone;

/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision$
  */
public class TimeZoneTestSuite extends TestDecorator
{
    private final TimeZone m_timeZone;
    private final TimeZone m_originalTimeZone;

    /**
     * Creates a new TimeZoneTestSuite object.
     *
     * @param timeZone DOCUMENT ME!
     * @param test DOCUMENT ME!
     */
    public TimeZoneTestSuite( String timeZone, Test test )
    {
        super( test );
        m_timeZone = TimeZone.getTimeZone( timeZone );
        m_originalTimeZone = TimeZone.getDefault();
    }

    /**
     * DOCUMENT ME!
     *
     * @param testResult DOCUMENT ME!
     */
    public void run( TestResult testResult )
    {
        try
        {
            TimeZone.setDefault( m_timeZone );
            super.run( testResult );
        }
        finally
        {
            TimeZone.setDefault( m_originalTimeZone ); // cleanup
        }
    }
}
