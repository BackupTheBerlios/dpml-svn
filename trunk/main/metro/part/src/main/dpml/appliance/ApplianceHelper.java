/*
 * Copyright 2006-2007 Stephen J. McConnell.
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

package dpml.appliance;

import dpml.util.PropertyResolver;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import net.dpml.appliance.Appliance;
import net.dpml.appliance.ApplianceException;
import net.dpml.appliance.ApplianceFactory;

import net.dpml.transit.Artifact;
import net.dpml.transit.Transit;
import net.dpml.transit.ContentHandler;


/**
 * Remote adapter to a component.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ApplianceHelper
{
    private ApplianceHelper()
    {
        // disabled
    }
    
   /**
    * Creation of a new appliance instance.
    * @param uri the appliance uri
    * @return the new appliance
    * @exception IOException if an error occurs during appliance creation
    */
    public static Appliance newAppliance( URI uri ) throws IOException
    {
        String partition = getPartition();
        return newAppliance( partition, uri );
    }
    
   /**
    * Creation of a new appliance instance.
    * @param partition the partition name under which the appliance will be created
    * @param uri the appliance uri
    * @return the new appliance
    * @exception IOException if an error occurs during appliance creation
    */
    public static Appliance newAppliance( String partition, URI uri ) throws IOException
    {
        if( null == partition )
        {   
            throw new NullPointerException( "partition" );
        }
        Artifact artifact = Artifact.createArtifact( uri );
        String type = artifact.getType();
        ContentHandler handler = Transit.getInstance().getContentHandler( type );
        if( null == handler )
        {
            final String error = 
              "No content handler found for the type ["
              + type
              + "] referenced by the uri ["
              + uri 
              + "]";
            throw new ApplianceException( error, null );
        }
        if( handler instanceof ApplianceFactory )
        {
            URL url = artifact.toURL();
            URLConnection connection = url.openConnection();
            ApplianceFactory factory = (ApplianceFactory) handler;
            return factory.newAppliance( connection, partition );
        }
        else
        {
            final String error = 
              "Content type ["
              + type
              + "] referenced by the uri ["
              + uri 
              + "] does not support appliance production.";
            throw new ApplianceException( error, null );
        }
    }
    
    private static String getPartition()
    {
        String partition = System.getProperty( "dpml.station.partition", "main" );
        Properties system = System.getProperties();
        return PropertyResolver.resolve( system, partition );
    }
}

