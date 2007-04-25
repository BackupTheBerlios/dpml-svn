/* 
 * Copyright 2005-2007 Stephen McConnell.
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

package dpml.util;

import java.net.URL;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.util.logging.LogManager;

import static net.dpml.transit.Transit.DATA;

/**
 * Utility class used to establish the logging configuration.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ConfigurationHandler
{
    private static final File TRANSIT_DATA = DATA;
    
   /**
    * Creation of the logging controller.
    */
    public ConfigurationHandler()
    {
        
        //
        // customize the configuration based on a properties file declared under 
        // the 'dpml.logging.config' property
        //
        
        Properties properties = new Properties();
        String config = System.getProperty( "dpml.logging.config" );
        if( null != config )
        {
            Properties sys = System.getProperties();
            String spec = PropertyResolver.resolve( sys, config );
            try
            {
                URL url = new URL( spec );
                InputStream stream = url.openStream();
                properties.load( stream );
                properties = PropertyResolver.resolve( sys, properties );
            }
            catch( FileNotFoundException e )
            {
                final String error = 
                  "Logging configuration does not exist."
                  + "\nURI: " + spec;
                System.err.println( error );
            }
            catch( Exception e )
            {
                System.out.println( "Error loading user properties: " + config );
                e.printStackTrace();
            }
        }
        
        //
        // ensure that sensible defaults exist
        //
        
        if( null == properties.getProperty( ".level" ) )
        {
            String level = getDefaultLevel();
            properties.setProperty( ".level", level );
        }
        
        if( null == properties.getProperty( "handlers" ) )
        {
            setProperty( properties, 
              "handlers", 
              "java.util.logging.ConsoleHandler" );
            setProperty( properties, 
              "java.util.logging.ConsoleHandler.formatter", 
              "dpml.util.StandardFormatter" );
            setProperty( properties, "java.util.logging.ConsoleHandler.level", "ALL" );
        }

        File base = new File( System.getProperty( "user.dir" ) );
        File file = new File( base, "logging.properties" );
        if( file.exists() )
        {
            try
            {
                InputStream input = new FileInputStream( file );
                properties.load( input );
            }
            catch( Throwable e )
            {
                e.printStackTrace();
            }
        }
        
        //
        // convert the resolved properties instance to an input stream
        // and supply this to the log manager
        //
        
        try
        {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            properties.store( out, "DPML Logging Properties" );
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
    
    private String getDefaultLevel()
    {
        //if( "true".equals( System.getProperty( "dpml.debug" ) ) )
        //{
        //    return "FINE";
        //}
        //else
        //{
            return System.getProperty( "dpml.logging.level", "INFO" ).toUpperCase();
        //}
    }    
}
