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

package org.apache.avalon.logging.logkit.factory.datagram;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.avalon.logging.logkit.LogTargetException;
import org.apache.avalon.logging.logkit.LogTargetFactory;
import org.apache.avalon.logging.logkit.FormatterFactory;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

import org.apache.log.LogTarget;
import org.apache.log.format.Formatter;
import org.apache.log.output.net.DatagramOutputTarget;

/**
 * This factory creates LogTargets with a wrapped DatagramOutputTarget around it.
 * <p>
 * Configuration syntax:
 * <pre>
 * &lt;datagram-target id="target-id"&gt;
 *   &lt;address hostname="hostname" port="4455" /&gt;
 *   &lt;format type="extended"&gt;
 *      %7.7{priority} %23.23{time:yyyy-MM-dd HH:mm:ss:SSS} [%25.25{category}] : %{message}\n%{throwable}
 *   &lt;/format&gt;
 * &lt;/datagram-target&gt;
 * </pre>
 * </p>
 * <p>
 *  This factory creates a DatagramOutputTarget object which will
 *  sends datagrams to the specified address. The name of the target is specified by the hostname attribute
 *  of the &lt;address&gt; element and the port by the port attribute.The &lt;format&gt; element
 *  wraps the format to output the log.
 * </p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DatagramTargetFactory.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class DatagramTargetFactory implements LogTargetFactory
{
    //--------------------------------------------------------------
    // static
    //--------------------------------------------------------------

    private static final Resources REZ =
      ResourceManager.getPackageResources( DatagramTargetFactory.class );

    //--------------------------------------------------------------
    // immutable state
    //--------------------------------------------------------------

    private final FormatterFactory m_formatter;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

    public DatagramTargetFactory( FormatterFactory formatter )
    {
        m_formatter = formatter;
    }

    //--------------------------------------------------------------
    // LogTargetFactory
    //--------------------------------------------------------------

    /**
     * Create a LogTarget based on a supplied configuration
     * @param conf the target coonfiguration
     * @return the datagram target
     * @exception LogTargetException if a target creation error occurs
     */
    public LogTarget createTarget( final Configuration conf )
        throws LogTargetException
    {
        InetAddress address;

        final Configuration configChild = conf.getChild( "address", false );
        if( null == configChild )
        {
            final String error = 
              REZ.getString( "datagram.error.missing-address" );
            throw new LogTargetException( error );
        }

        try
        {
            address = 
              InetAddress.getByName( 
                configChild.getAttribute( "hostname" ) );
        }
        catch( UnknownHostException uhex )
        {
            final String error = 
              REZ.getString( "datagram.error.unknown-host" );
            throw new LogTargetException( error, uhex );
        }
        catch( ConfigurationException e )
        {
            final String error = 
              REZ.getString( "datagram.error.missing-host" );
            throw new LogTargetException( error, e );
        }

        Configuration formatConfig = conf.getChild( "format" );

        final Formatter formatter = 
          m_formatter.createFormatter( formatConfig );

        try
        {
            int port = configChild.getAttributeAsInteger( "port" );
            return new DatagramOutputTarget( address, port, formatter );
        }
        catch( IOException ioex )
        {
            final String error = 
              REZ.getString( "datagram.error.internal" );
            throw new LogTargetException( error, ioex );
        }
        catch( ConfigurationException e )
        {
            final String error = 
              REZ.getString( "datagram.error.missing-port" );
            throw new LogTargetException( error, e );
        }
    }
}

