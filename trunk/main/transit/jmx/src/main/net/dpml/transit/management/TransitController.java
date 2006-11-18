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

import java.lang.management.ManagementFactory;
import java.net.PasswordAuthentication; 
import java.util.ArrayList;
import java.util.List;
import java.util.Hashtable;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanNotificationInfo;
import javax.management.NotificationBroadcasterSupport;
import javax.management.MBeanException;

import net.dpml.transit.Transit;
import net.dpml.transit.DefaultTransitModel;
import net.dpml.transit.model.TransitModel;
import net.dpml.transit.model.HostModel;
import net.dpml.transit.model.LayoutModel;
import net.dpml.transit.model.ProxyModel;
import net.dpml.transit.monitor.LoggingAdapter;

import net.dpml.util.Logger;

/** 
 * Transit MBean.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class TransitController extends NotificationBroadcasterSupport implements TransitControllerMXBean 
{
   /**
    * The Transit system version.
    */
    public static final String VERSION = "@PROJECT-VERSION@";

    private TransitModel m_model;
    private Logger m_logger;
    private List m_layouts = new ArrayList();
    private List m_hosts = new ArrayList();
    
    public TransitController( final Logger logger, final TransitModel model ) throws Exception
    {
        this( ManagementFactory.getPlatformMBeanServer(), logger, model );
    }
    
    public TransitController( final MBeanServer server, final Logger logger, final TransitModel model ) throws Exception
    {
        m_model = model;
        m_logger = logger;
        
        Hashtable table = new Hashtable();
        table.put( "type", "Transit" );
        ObjectName name =
          ObjectName.getInstance( "net.dpml", table );
        server.registerMBean( this, name );
        
        HostModel[] hosts = m_model.getCacheModel().getHostModels();
        for( int i=0; i < hosts.length; i++ )
        {
            HostModel host = hosts[i];
            addHostModel( server, m_hosts, host );
        }
        
        LayoutModel[] layouts = m_model.getCacheModel().getLayoutRegistryModel().getLayoutModels();
        for( int i=0; i < layouts.length; i++ )
        {
            LayoutModel layout = layouts[i];
            addLayoutModel( server, m_layouts, layout );
        }
    }
    
    private void addLayoutModel( final MBeanServer server, final List list, final LayoutModel layout ) throws MBeanException
    {
        try
        {
            String id = layout.getID();
            Hashtable table = new Hashtable();
            table.put( "type", "TransitLayouts" );
            table.put( "name", id );
            ObjectName name =
              ObjectName.getInstance( "net.dpml", table );
            LayoutController controller = new LayoutController( layout );
            server.registerMBean( controller, name );
            list.add( controller );
        }
        catch( Exception e )
        {
            throw new MBeanException( e );
        }
    }
    
    private void addHostModel( final MBeanServer server, final List list, final HostModel host ) throws MBeanException
    {
        try
        {
            String id = host.getID();
            Hashtable table = new Hashtable();
            table.put( "type", "TransitHosts" );
            table.put( "name", id );
            ObjectName name =
              ObjectName.getInstance( "net.dpml", table );
            HostController controller = new HostController( host );
            list.add( controller );
            server.registerMBean( controller, name );
        }
        catch( Exception e )
        {
            throw new MBeanException( e );
        }
    }
    
    public String getProxyHost() throws MBeanException
    {
        try
        {
            return m_model.getProxyModel().getHost().toString();
        }
        catch( Exception e )
        {
            throw new MBeanException( e );
        }
    }
    
    public String getProxyUsername() throws MBeanException
    {
        try
        {
            PasswordAuthentication auth = m_model.getProxyModel().getAuthentication();
            if( null != auth )
            {
                return auth.getUserName();
            }
            else
            {
                return null;
            }
        }
        catch( Exception e )
        {
            throw new MBeanException( e );
        }
    }
    
    public String getProxyPassword() throws MBeanException
    {
        try
        {
            PasswordAuthentication auth = m_model.getProxyModel().getAuthentication();
            if( null != auth )
            {
                return "*******";
            }
            else
            {
                return null;
            }
        }
        catch( Exception e )
        {
            throw new MBeanException( e );
        }
    }    
    public String getCacheDirectoryPath() throws MBeanException
    {
        try
        {
            return m_model.getCacheModel().getCacheDirectoryPath();
        }
        catch( Exception e )
        {
            throw new MBeanException( e );
        }
    }
    
    public String getCacheDirectory() throws MBeanException
    {
        try
        {
            return m_model.getCacheModel().getCacheDirectory().getCanonicalPath();
        }
        catch( Exception e )
        {
            throw new MBeanException( e );
        }
    }
    
    public String getCacheLayoutID() throws MBeanException
    {
        try
        {
            return m_model.getCacheModel().getLayoutModel().getID();
        }
        catch( Exception e )
        {
            throw new MBeanException( e );
        }
    }
    
    public String getCacheLayoutTitle() throws MBeanException
    {
        try
        {
            return m_model.getCacheModel().getLayoutModel().getTitle();
        }
        catch( Exception e )
        {
            throw new MBeanException( e );
        }
    }
    
    public String getHome() throws MBeanException
    {
        try
        {
            return Transit.DPML_HOME.getCanonicalPath();
        }
        catch( Exception e )
        {
            throw new MBeanException( e );
        }
    }
    
    public String getData() throws MBeanException
    {
        try
        {
            return Transit.DPML_DATA.getCanonicalPath();
        }
        catch( Exception e )
        {
            throw new MBeanException( e );
        }
    }
    
    public String getPrefs() throws MBeanException
    {
        try
        {
            return Transit.DPML_PREFS.getCanonicalPath();
        }
        catch( Exception e )
        {
            throw new MBeanException( e );
        }
    }
    
    public String getShare() throws MBeanException
    {
        try
        {
            return Transit.DPML_SYSTEM.getCanonicalPath();
        }
        catch( Exception e )
        {
            throw new MBeanException( e );
        }
    }
    
    public String getVersion() throws MBeanException
    {
        try
        {
            return Transit.VERSION;
        }
        catch( Exception e )
        {
            throw new MBeanException( e );
        }
    }

    public LayoutControllerMXBean[] getLayouts()
    {
        return (LayoutControllerMXBean[]) m_layouts.toArray( new LayoutControllerMXBean[0] );
    }
    
    public HostControllerMXBean[] getHosts()
    {
        return (HostControllerMXBean[]) m_hosts.toArray( new HostControllerMXBean[0] );
    }
}
