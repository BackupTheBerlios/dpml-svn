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

package tutorial.application;

import java.io.File;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.activity.Disposable;

import tutorial.location.LocationService;
import tutorial.publisher.PublisherService;

/**
 * PublisherComponent from Merlin's Composition Tutorial
 *
 * @avalon.component version="1.0" name="test"
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class Application extends AbstractLogEnabled 
    implements Serviceable, Disposable
{

   /**
    * Servicing of the component by the container during
    * which service dependencies declared under the component
    * can be resolved using the supplied service manager.  In this 
    * example the component uses a LocatorService service to log a 
    * message exposing a location.
    *
    * @param manager the service manager
    * @avalon.dependency key="locator" type="tutorial.location.LocationService"
    * @avalon.dependency key="publisher" type="tutorial.publisher.PublisherService"
    */
    public void service( ServiceManager manager ) throws ServiceException
    {
        getLogger().info( "servicing application" );

        LocationService locator = (LocationService) manager.lookup( "locator" );
        PublisherService publisher = (PublisherService) manager.lookup( "publisher" );

        //
        // get the location from the locator and publish
        // it using the publisher
        //

        publisher.publish( 
            "\n******************"
          + "\n* " + locator.getLocation() 
          + "\n******************");

        getLogger().info( "done" );
    }

    public void dispose()
    {
        getLogger().info( "disposal" );
    }
}

