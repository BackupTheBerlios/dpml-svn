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

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Properties;
import java.util.EventObject;
import java.util.EventListener;

import net.dpml.station.info.StartupPolicy;
import net.dpml.station.info.ApplicationDescriptor;

import net.dpml.component.Component;
import net.dpml.component.Provider;

import net.dpml.station.Callback;
import net.dpml.station.ProcessState;
import net.dpml.station.Application;
import net.dpml.station.ApplicationException;
import net.dpml.station.ApplicationListener;
import net.dpml.station.ApplicationEvent;

import net.dpml.lang.Logger;
import net.dpml.lang.PID;

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
    * @param input the subprocess input stream
    */
    public OutputStreamReader( Logger logger, InputStream input )
    {
        super( logger, input );
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
                getLogger().debug( line );
            }
        }
        catch( IOException e )
        {
            getLogger().error( "Process read error.", e );
        }
    }
}
