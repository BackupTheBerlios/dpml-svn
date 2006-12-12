/*
 * Copyright 2005 Stephen J. McConnell.
 *
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

package net.dpml.station.server; 

import java.io.InputStream;

import net.dpml.util.Logger;

/**
 * Stream reader utility class.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
abstract class StreamReader extends Thread
{
    private final Logger m_logger;
    private final InputStream m_input;
    
   /**
    * Creation of a new reader.
    * @param logger the assigned logging channel
    * @param input the subprocess input stream
    */
    public StreamReader( Logger logger, InputStream input )
    {
        m_input = input;
        m_logger = logger;
    }

   /**
    * Return the input stream.
    * @return the subprocess input stream
    */
    protected InputStream getInputStream()
    {
        return m_input;
    }
    
   /**
    * Return the assigned logging channel.
    * @return the logging channel
    */
    protected Logger getLogger()
    {
        return m_logger;
    }
}
