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

package org.apache.avalon.logging.logkit.factory.syslog;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.avalon.logging.logkit.LogTargetFactory;
import org.apache.avalon.logging.logkit.LogTargetException;
import org.apache.avalon.logging.logkit.FormatterFactory;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

import org.apache.log.LogTarget;
import org.apache.log.format.Formatter;

/**
 * SyslogTargetFactory
 *
 * This factory creates LogTargets with a wrapped SyslogTarget around it:
 *
 * <pre>
 *
 * &lt;target id="syslog"
 *       artifact="avalon-logging/avalon-logkit-syslog#1.0-SNAPSHOT"&gt;
 *   &lt;address hostname="hostname" port="514" facility="USER"/&gt;
 *   &lt;format type="extended"&gt;
 *       %7.7{priority} %23.23{time:yyyy-MM-dd HH:mm:ss:SSS}   [%25.25{category}] : %{message}\n%{throwable}
 *   &lt;/format&gt;
 * &lt;/syslog&gt;
 * </pre>
 *
 * <p>
 *  This factory creates a SyslogTarget object which will sends syslog style messages to the
 *  specified address. The name of the target is specified by the hostname attribute
 *  of the &lt;address&gt; element and the port by the port attribute.The &lt;address&gt; element
 *  wraps the format to output the log.
 * </p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: SyslogTargetFactory.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class SyslogTargetFactory implements LogTargetFactory
{
    //--------------------------------------------------------------
    // static
    //--------------------------------------------------------------

    private static final Resources REZ =
      ResourceManager.getPackageResources( SyslogTargetFactory .class );

    //--------------------------------------------------------------
    // immutable state
    //--------------------------------------------------------------

    private final FormatterFactory m_formatter;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

    public SyslogTargetFactory( FormatterFactory formatter )
    {
        m_formatter = formatter;
    }

    //--------------------------------------------------------------
    // LogTargetFactory
    //--------------------------------------------------------------

    /**
     * Creates a log target based on Configuration
     *
     * @param conf Configuration requied for creating the log target
     * @throws ConfigurationException if something goes wrong while reading from
     *          configuration
     */
    public LogTarget createTarget( final Configuration conf )
        throws LogTargetException
    {
        Configuration formatConfig = conf.getChild( "format" );
        final Formatter formatter = 
          m_formatter.createFormatter( formatConfig );

        final Configuration configChild = 
          conf.getChild( "address", false );
        if( null == configChild )
        {
            final String error = 
              REZ.getString( "syslog.error.missing-address" );
            throw new LogTargetException( error );
        }

        final InetAddress address = getAddress( configChild );
        String name = getFacilityName( configChild );
        int facility = SyslogTarget.getFacilityValue( name );
        int port = getPort( configChild );

        try
        {
            return new SyslogTarget( address, port, formatter, facility );
        }
        catch( Throwable e )
        {
            final String error = 
              REZ.getString( "syslog.error.internal" );
            throw new LogTargetException( error, e );
        }
    }

    private InetAddress getAddress( Configuration config ) throws LogTargetException
    {
        try
        {
            return InetAddress.getByName( 
                config.getAttribute( "hostname" ) );
        }
        catch( UnknownHostException uhex )
        {
            final String error = 
              REZ.getString( "syslog.error.unknown-host" );
            throw new LogTargetException( error, uhex );
        }
        catch( ConfigurationException e )
        {
            final String error = 
              REZ.getString( "syslog.error.missing-host" );
            throw new LogTargetException( error, e );
        }
    }

    private String getFacilityName( Configuration config ) throws LogTargetException
    {
        try
        {
            return config.getAttribute( "facility" );
        }
        catch( ConfigurationException e )
        {
            final String error = 
              REZ.getString( "syslog.error.missing-facility" );
            throw new LogTargetException( error, e );
        }
    }

    private int getPort( Configuration config ) throws LogTargetException
    {
        try
        {
            return config.getAttributeAsInteger( "port" );
        }
        catch( ConfigurationException e )
        {
            final String error = 
              REZ.getString( "syslog.error.missing-port" );
            throw new LogTargetException( error, e );
        }
    }

}
