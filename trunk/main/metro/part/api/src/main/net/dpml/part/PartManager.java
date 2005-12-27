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

package net.dpml.part;

import java.net.URI;
import java.lang.reflect.Constructor;
import java.util.Date;

import net.dpml.transit.Transit;
import net.dpml.transit.Logger;
import net.dpml.transit.monitor.LoggingAdapter;
import net.dpml.transit.Repository;

/**
 * Utility class through which the default controller is established. The 
 * default controller is loaded using the System property "dpml.part.controller.uri".
 * If undefined the default controller implementation plugin "@PART-HANDLER-URI@" 
 * will be selected.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PartManager // extends ContentHandler
{
   /**
    * Static reference to the default controller.
    */
    public static final Controller CONTROLLER = newController( new LoggingAdapter( "" ) );
    
   /**
    * Construct a controller.
    * @param logger the assigned logging channel
    * @return the controller
    */
    public static Controller newController( final Logger logger )
    {
        if( null == logger )
        {
            throw new NullPointerException( "logger" );
        }
        try
        {
            long now = new Date().getTime();
            ClassLoader classloader = PartManager.class.getClassLoader();
            URI uri = getControllerURI();
            Repository repository = Transit.getInstance().getRepository();
            Class c = repository.getPluginClass( classloader, uri );
            Constructor constructor = c.getConstructor( new Class[]{Logger.class} );
            Controller handler = (Controller) constructor.newInstance( new Object[]{logger} );
            return handler;
        }
        catch( Throwable e )
        {
            final String error =
              "Internal error while attempting to establish the default part handler.";
            throw new RuntimeException( error, e );
        }
    }
    
    private final Logger m_logger;
    private final Controller m_handler;

   /**
    * Creation of a new <tt>PartManager</tt>.
    * @param logger the assigned logging channel
    */
    public PartManager( Logger logger )
    {
        m_logger = logger;
        m_handler = newController( logger );
    }
    
   /**
    * Return the default part controller.
    * @return the part controller
    */
    public Controller getController()
    {
        return m_handler;
    }
    
    private static URI getControllerURI() throws Exception
    {
        String spec = System.getProperty( "dpml.part.controller.uri", "@PART-HANDLER-URI@" );
        return new URI( spec );
    }
}
