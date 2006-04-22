/*
 * Copyright 2006 Stephen J. McConnell.
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

package net.dpml.http;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Stream reader utility class.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class OutputStreamReader extends StreamReader
{
   /**
    * Creation of a process output reader.
    * @param logger the assigned logging channel
    * @param input the subprocess input stream
    */
    public OutputStreamReader( InputStream input )
    {
        super( input );
    }

   /**
    * Start the stream reader.
    */
    public void run()
    {
        try
        {
            InputStreamReader isr = new InputStreamReader( getInputStream() );
            BufferedReader reader = new BufferedReader( isr );
            String line = null;
            while( ( line = reader.readLine() ) != null )
            {
                System.out.println( "$ " + line );
            }
        }
        catch( IOException e )
        {
            e.printStackTrace();
        }
    }
}
