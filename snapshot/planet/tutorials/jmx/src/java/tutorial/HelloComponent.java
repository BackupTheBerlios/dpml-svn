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

package tutorial;

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Executable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * A sample component.  This component implements a number 
 * of lifecycle interface.  Each lifecycle interface is a stage
 * that is processed by a container during the deployment of 
 * the component.  The lifecycle stages demonstrated here include
 * LogEnabled (association of a logging channel), Initializable
 * (initialization of the component), Executable (component
 * execution), and Disposable (componet disposal).  Please note 
 * that all lifecycle stages are optional.
 *
 * @avalon.component version="1.0" name="hello" lifestyle="singleton"
 * @avalon.service type="tutorial.Hello"
 */
public class HelloComponent 
  implements LogEnabled, Initializable, Configurable, Executable, Disposable, Hello, HelloMBean
{
    private String m_HelloString;
    
    
    public void sayHello()
    {
        getLogger().info( m_HelloString );
    }

   /**
    * Internal reference to the logging channel supplied to us 
    * by the container. 
    */
    private Logger m_logger;

   /**
    * Supply of a logging channel by the container.
    *
    * @param logger the logging channel for this component
    */
    public void enableLogging( final Logger logger )
    {
        m_logger = logger;
        getLogger().info( "logging" );
    }

   /**
    * Initialization of the component by the container.
    * @exception Exception if an initialization error occurs
    */
    public void initialize() throws Exception
    {
        getLogger().info( "initialization" );
    }

    /** Configure the component.
     */
     
    public void configure( Configuration config )
        throws ConfigurationException
    {
        Configuration helloConf = config.getChild( "hello" );
        m_HelloString = helloConf.getValue();
    }
    
   /**
    * Component execution trigger by the container following 
    * completion of the initialization stage.
    */
    public void execute()
    {
        getLogger().info( "execution" );
    } 

   /**
    * Component disposal trigger by the container during which
    * the component will release consumed resources.
    */
    public void dispose()
    {
        getLogger().info( "disposal" );
        m_logger = null;
    }

   /**
    * Return the logging channel assigned to us by the container.
    * @return the logging channel
    */
    private Logger getLogger()
    {
        return m_logger;
    }

    /** Returns the String that will be output throught he sayHello() method.
     */
    public String getOutputString()
    {
        return m_HelloString;
    }
    
    /** Sets the String that will be output throught he sayHello() method.
     */
    public void setOutputString( String text )
    {
        m_HelloString = text;
    }
}
