/* 
 * Copyright 2004 Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 * 
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.avalon.logging.logkit.factory;

import java.io.OutputStream;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.avalon.logging.logkit.LogTargetFactory;
import org.apache.avalon.logging.logkit.FormatterFactory;

import org.apache.log.LogTarget;
import org.apache.log.format.Formatter;
import org.apache.log.output.io.StreamTarget;

/**
 * TargetFactory for {@link org.apache.log.output.io.StreamTarget}.
 *
 * This factory is able to create different StreamTargets according to the following
 * configuration syntax:
 * <pre>
 * &lt;stream id="foo"&gt;
 *  &lt;name&gt;<i>stream-context-name</i>&lt;/name&gt;
 *  &lt;format type="<i>raw|pattern|extended</i>"&gt;<i>pattern to be used if needed</i>&lt;/format&gt;
 * &lt;/stream&gt;
 * </pre>
 *
 * <p>The "stream-context-name" is the name of an <code>java.io.OutputStream</code>.
 * Two stream names are supported:
 * <li>"<code>System.out</code>" for the system output stream,</li>
 * <li>"<code>System.err</code>" for the system error stream.</li>
 * </p>
 *
 * <p>The syntax of "format" is the same as in <code>FileTargetFactory</code>.</p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: StreamTargetFactory.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class StreamTargetFactory implements LogTargetFactory
{
    //--------------------------------------------------------------
    // immutable state
    //--------------------------------------------------------------

    private final FormatterFactory m_formatter;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

    public StreamTargetFactory( FormatterFactory formatter )
    {
        m_formatter = formatter;
    }

    //--------------------------------------------------------------
    // LogTargetFactory
    //--------------------------------------------------------------

    /**
     * Create a LogTarget based on a Configuration
     */
    public LogTarget createTarget( final Configuration configuration )
    {
        OutputStream stream;

        final Configuration streamConfig = 
          configuration.getChild( "name", false );
        if( null == streamConfig )
        {
            stream = System.out;
        }
        else
        {
            final String streamName = streamConfig.getValue( "" );
            if( streamName.equals( "System.out" ) )
            {
                stream = System.out;
            }
            else
            {
                stream = System.err;
            }
        }

        Configuration formatConfig = configuration.getChild( "format", false );
        final Formatter formatter = 
          m_formatter.createFormatter( formatConfig );

        return new StreamTarget( stream, formatter );
    }
}

