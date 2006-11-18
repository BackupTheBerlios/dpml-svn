/*
 * Copyright 2006 Stephen J. McConnell.
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

package net.dpml.transit.management;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanNotificationInfo;
import javax.management.NotificationBroadcasterSupport;
import javax.management.MBeanException;

import net.dpml.transit.model.HostModel;

/** 
 * Transit MBean.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class HostController implements HostControllerMXBean 
{
    private HostModel m_model;
    
    public HostController( final HostModel model ) throws Exception
    {
        m_model = model;
    }
    
    public String getID() throws MBeanException
    {
        try
        {
            return m_model.getID();
        }
        catch( Exception e )
        {
            throw new MBeanException( e );
        }
    }
    
    public int getPriority() throws MBeanException
    {
        try
        {
            return m_model.getPriority();
        }
        catch( Exception e )
        {
            throw new MBeanException( e );
        }
    }
    
    public String getBase() throws MBeanException
    {
        try
        {
            return m_model.getBasePath();
        }
        catch( Exception e )
        {
            throw new MBeanException( e );
        }
    }
    
    public String getLayout() throws MBeanException
    {
        try
        {
            return m_model.getLayoutModel().getID();
        }
        catch( Exception e )
        {
            throw new MBeanException( e );
        }
    }
    
    public boolean getEnabled() throws MBeanException
    {
        try
        {
            return m_model.getEnabled();
        }
        catch( Exception e )
        {
            throw new MBeanException( e );
        }
    }
    
    public boolean getTrusted() throws MBeanException
    {
        try
        {
            return m_model.getTrusted();
        }
        catch( Exception e )
        {
            throw new MBeanException( e );
        }
    }
}