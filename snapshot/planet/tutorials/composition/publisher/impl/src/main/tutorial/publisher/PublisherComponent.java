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

package tutorial.publisher;

import java.io.File;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.activity.Disposable;

/**
 * PublisherComponent from Merlin's Composition Tutorial
 *
 * @avalon.component version="1.0" name="publisher"
 * @avalon.service type="tutorial.publisher.PublisherService"
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class PublisherComponent extends AbstractLogEnabled implements PublisherService, Disposable
{
   /**
    * Supply of the logging channel by the container.
    * @param logger the logging channel
    */
    public void enableLogging( Logger logger )
    {
        logger.info( "created" );
        super.enableLogging( logger );
    }

   /**
    * Publish a message.
    * @param message the message to publish
    */
    public void publish( String message )
    {
        getLogger().info( message );
    }

    public void dispose()
    {
        getLogger().info( "disposal" );
    }

}

