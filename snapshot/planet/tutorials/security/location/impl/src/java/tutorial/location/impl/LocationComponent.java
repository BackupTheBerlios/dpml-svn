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

package tutorial.location.impl;

import java.io.File;
import java.security.AccessController;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.activity.Disposable;

import tutorial.location.LocationService;
import tutorial.location.LocationPermission;

/**
 * HelloComponent from Merlin's Tutorial
 *
 * @avalon.component version="1.0" name="location-provider"
 * @avalon.service type="tutorial.location.LocationService"
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class LocationComponent extends AbstractLogEnabled implements
Configurable, Disposable, LocationService
{
    private String m_location = "unknown";

   /**
    * Configuration of the component by the container.  The
    * implementation gets a child element named 'source' and
    * assigns log the value.
    *
    * @param config the component configuration
    * @exception ConfigurationException if a configuration error occurs
    */
    public void configure( Configuration config ) 
        throws ConfigurationException    
    {
        m_location = config.getChild( "place" ).getValue( "unknown" );
        getLogger().info( "location: " + m_location );
    }

   /**
    * Return a location.
    * @return a location
    * @avalon.security.permission class="tutorial.location.LocationPermission"
    */
    public String getLocation()
    {
        LocationPermission perm = new LocationPermission( m_location );
        AccessController.checkPermission( perm );
        return m_location;
    }

    public void dispose()
    {
        getLogger().info( "locator disposal" );
    }
}

