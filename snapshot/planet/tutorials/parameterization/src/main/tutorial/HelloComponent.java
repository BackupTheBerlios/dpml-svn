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
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Executable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.parameters.ParameterException;

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
public class HelloComponent extends AbstractLogEnabled
  implements Parameterizable, Initializable, Hello
{
    private String m_color = null;
    private String m_size = null;

    public void sayHello()
    {
        getLogger().info( "HELLO" );
    }

   /**
    * Internal reference to the logging channel supplied to us 
    * by the container. 
    */
    private Logger m_logger;

   /**
    * Component execution trigger by the container following 
    * completion of the initialization stage.
    */
    public void parameterize( Parameters params ) throws ParameterException
    {
        getLogger().info( "execution" );
        m_color = params.getParameter( "color" );
        m_size = params.getParameter( "size" );
    }

   /**
    * Initialization of the component by the container.
    * @exception Exception if an initialization error occurs
    */
    public void initialize() throws Exception
    {
        getLogger().info( "initialization [" + m_color + ", " + m_size + "]" );
    }

}
