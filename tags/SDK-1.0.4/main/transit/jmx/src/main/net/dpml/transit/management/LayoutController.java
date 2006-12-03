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

import net.dpml.transit.model.LayoutModel;

/** 
 * Transit MBean.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class LayoutController implements LayoutControllerMXBean 
{
    private LayoutModel m_model;
    
    public LayoutController( final LayoutModel model )
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
    
    public String getTitle() throws MBeanException
    {
        try
        {
            return m_model.getTitle();
        }
        catch( Exception e )
        {
            throw new MBeanException( e );
        }
    }
    
    public String getClassname() throws MBeanException
    {
        try
        {
            return m_model.getClassname();
        }
        catch( Exception e )
        {
            throw new MBeanException( e );
        }
    }
}