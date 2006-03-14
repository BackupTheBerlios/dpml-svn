/* 
 * Copyright 2005 Stephen McConnell.
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

package net.dpml.depot;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Properties;
import java.util.logging.LogManager;

/**
 * Utility class used to establish the logging configuration for managed subprocesses.
 * The handler redirects logging records to a remote LoggingService via RMI that 
 * aggregates logging messages from multiple JVM within a local domain.  This 
 * configuration handler is declared as the default logging configuration for 
 * suprocesses launched by the DPML Station.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DepotLoggingConfiguration
{
   /**
    * Creation of the logging controller.
    */
    public DepotLoggingConfiguration()
    {
        String group = System.getProperty( "dpml.system.group", "root" );
        String level = System.getProperty( "dpml.logging.level", "INFO" ).toUpperCase();

        Properties properties = new Properties();

        properties.setProperty( 
          "handlers", 
          System.getProperty( 
            "handlers", 
            "net.dpml.depot.DepotHandler" ) );

        //
        // set the default level by setting the root logger level
        //

        properties.setProperty( ".level", level );

        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            properties.store( out, "DPML Logging properties" );
            byte[] bytes = out.toByteArray();
            ByteArrayInputStream input = new ByteArrayInputStream( bytes );
            LogManager manager = LogManager.getLogManager();
            manager.readConfiguration( input );
        }
        catch( Throwable e )
        {
            e.printStackTrace();
        }
    }
}

