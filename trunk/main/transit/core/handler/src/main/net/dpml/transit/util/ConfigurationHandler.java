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

package net.dpml.transit.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Properties;
import java.util.logging.LogManager;

/**
 * Utility class used to establish the logging configuration.  The contents of 
 * this class are subject to radical change but that's largely academic because 
 * this class does not expose any operations.
 */
public class ConfigurationHandler
{
   /**
    * Creation of the logging controller.
    */
    public ConfigurationHandler()
    {
        String group = System.getProperty( "dpml.logging.category", "root" );
        String level = System.getProperty( "dpml.logging.level", "INFO" ).toUpperCase();

        Properties properties = new Properties();

        setProperty( properties, "handlers", "java.util.logging.ConsoleHandler" );
        setProperty( properties, "java.util.logging.ConsoleHandler.formatter", "net.dpml.transit.util.StandardFormatter" );

        /*
        setProperty( properties, "handlers", "java.util.logging.FileHandler, java.util.logging.ConsoleHandler" );
        setProperty( properties, "java.util.logging.ConsoleHandler.formatter", "net.dpml.transit.util.StandardFormatter" );
        setProperty( properties, "java.util.logging.FileHandler.pattern", "%h/" + group + "%u.log" );
        setProperty( properties, "java.util.logging.FileHandler.limit", "50000" );
        setProperty( properties, "java.util.logging.FileHandler.count", "1" );
        setProperty( properties, "java.util.logging.FileHandler.formatter", "net.dpml.transit.util.StandardFormatter" );
        */

        //
        // set the default level by setting the root logger level
        //

        properties.setProperty( ".level", level );

        //
        // set the level that the console handler will handle
        //

        setProperty( properties, "java.util.logging.ConsoleHandler.level", "FINEST" );

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

    private void setProperty( Properties properties, String key, String value )
    {
        properties.setProperty( key, System.getProperty( key, value ) );
    }
}
