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
package org.apache.avalon.playground.jmxtest;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.3 $
 *
 * @avalon.component name="JMXTestComponent" lifestyle="singleton"
 * @avalon.service type="org.apache.avalon.playground.jmxtest.JMXTestService"
 */
public class JMXTestComponent extends AbstractLogEnabled implements JMXTestService, Initializable,
    Startable, JMXTestComponentMBean
{
    private int numberOfServiceInvokes = 0;
    
    private int numberOfJmxMethodInvokes = 0;

    private int numberOfJmxAttributeReads = 0;

    private int mutableProperty = 0;

    public void initialize()
    {
        getLogger().info( "JMXTextComponent initialized instance: " + System.identityHashCode( this ) );
    }

    public void start()
    {
        getLogger().info( "JMXTextComponent started instance: " + System.identityHashCode( this ) );
    }

    public void stop()
    {
        getLogger().info( "JMXTextComponent stopped instance: " + System.identityHashCode( this ) );
    }

    public void serviceMethod()
    {
        getLogger().info( "serviceMethod was invoked on instance: " + System.identityHashCode( this ) );
        numberOfServiceInvokes++;
    }

    //JMXTestComponentMBean:
    public int getNumberOfServiceInvokes()
    {
        getLogger().info( "getNumberOfServiceInvokes was invoked on instance: "
                          + System.identityHashCode( this ) );
        numberOfJmxAttributeReads++;
        return numberOfServiceInvokes;
    }

    public int getNumberOfJmxAttributeReads()
    {
        getLogger().info( "getNumberOfJmxAttributeReads was invoked on instance: "
                          + System.identityHashCode( this ) );
        numberOfJmxAttributeReads++;
        return numberOfJmxAttributeReads;
    }

    public int getNumberOfJmxMethodInvokes()
    {
        numberOfJmxAttributeReads++;
        return numberOfJmxMethodInvokes;
    }

    public int getMutableProperty()
    {
        getLogger().info( "getMutableProperty was invoked on instance: "
                          + System.identityHashCode( this ) );
        numberOfJmxAttributeReads++;
        return mutableProperty;
    }

    public void setMutableProperty(int mutableProperty)
    {
        getLogger().info( "setMutableProperty was invoked on instance: "
                          + System.identityHashCode( this ) );
        this.mutableProperty = mutableProperty;
    }

    public String invokeMethodWithReturn()
    {
        final String message = "invokeMethodWithReturn was invoked on instance: "
                               + System.identityHashCode( this )
                               + " at: " + System.currentTimeMillis();
        numberOfJmxMethodInvokes++;
        getLogger().info( message );
        return message;
    }

    public void invokeMethodNoReturn()
    {
        getLogger().info( "invokeMethodNoReturn was invoked on instance: "
                          + System.identityHashCode( this ) );
        numberOfJmxMethodInvokes++;
    }

    public void invokeMethodWithArgs(String arg1, int arg2)
    {
        getLogger().info( "invokeMethodWithArgs was invoked on instance: "
                          + System.identityHashCode( this )
                          + " with arg1: " + arg1
                          + ", arg2: " + arg2 );
        numberOfJmxMethodInvokes++;
    }

    public String invokeMethodWithArgsAndReturn(String arg1, int arg2)
    {
        final String message = "invokeMethodWithArgsAndReturn was invoked on instance: "
                               + System.identityHashCode( this )
                               + " with arg1: " + arg1
                               + ", arg2: " + arg2 
                               + " at: " + System.currentTimeMillis();
        getLogger().info( message );
        numberOfJmxMethodInvokes++;
        return message;
    }
}
