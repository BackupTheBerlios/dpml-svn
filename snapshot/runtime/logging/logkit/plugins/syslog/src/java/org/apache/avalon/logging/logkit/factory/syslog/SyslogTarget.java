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

import java.io.IOException;
import java.net.InetAddress;

import org.apache.log.LogEvent;
import org.apache.log.format.Formatter;
import org.apache.log.output.net.DatagramOutputTarget;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: SyslogTarget.java 30977 2004-07-30 08:57:54Z niclas $
 */

public class SyslogTarget extends DatagramOutputTarget 
{
    // The following constants are extracted from a syslog.h file
    // copyrighted by the Regents of the University of California
    // I hope nobody at Berkley gets offended.

    /** Kernel messages */
    final static public int LOG_KERN     = 0;
    /** Random user-level messages */
    final static public int LOG_USER     = 1<<3;
    /** Mail system */
    final static public int LOG_MAIL     = 2<<3;
    /** System daemons */
    final static public int LOG_DAEMON   = 3<<3;
    /** security/authorization messages */
    final static public int LOG_AUTH     = 4<<3;
    /** messages generated internally by syslogd */
    final static public int LOG_SYSLOG   = 5<<3;

    /** line printer subsystem */
    final static public int LOG_LPR      = 6<<3;
    /** network news subsystem */
    final static public int LOG_NEWS     = 7<<3;
    /** UUCP subsystem */
    final static public int LOG_UUCP     = 8<<3;
    /** clock daemon */
    final static public int LOG_CRON     = 9<<3;
    /** security/authorization  messages (private) */
    final static public int LOG_AUTHPRIV = 10<<3;
    /** ftp daemon */
    final static public int LOG_FTP      = 11<<3;

    // other codes through 15 reserved for system use
    /** reserved for local use */
    final static public int LOG_LOCAL0 = 16<<3;
    /** reserved for local use */
    final static public int LOG_LOCAL1 = 17<<3;
    /** reserved for local use */
    final static public int LOG_LOCAL2 = 18<<3;
    /** reserved for local use */
    final static public int LOG_LOCAL3 = 19<<3;
    /** reserved for local use */
    final static public int LOG_LOCAL4 = 20<<3;
    /** reserved for local use */
    final static public int LOG_LOCAL5 = 21<<3;
    /** reserved for local use */
    final static public int LOG_LOCAL6 = 22<<3;
    /** reserved for local use*/
    final static public int LOG_LOCAL7 = 23<<3;

    final static public int SYSLOG_FATAL = 0;
    final static public int SYSLOG_ERROR = 3;
    final static public int SYSLOG_WARN  = 4;
    final static public int SYSLOG_INFO  = 6;
    final static public int SYSLOG_DEBUG = 7;

    private int facility = LOG_USER;

    /**
     * Create a output target with end point specified by address and port.
     *
     * @param address the address endpoint
     * @param port the address port
     * @exception IOException if an error occurs
     */
    public SyslogTarget( final InetAddress address,
                                 final int port,
                                 final Formatter formatter,
                                 final int facility )
        throws IOException
    {
        super(address, port, formatter);
        this.facility = facility;
    }

    /**
     * Create a output target with end point specified by address and port.
     *
     * @param address the address endpoint
     * @param port the address port
     * @exception IOException if an error occurs
     */
    public SyslogTarget( final InetAddress address,
                                 final int port,
                                 final Formatter formatter )
        throws IOException
    {
        super(address, port, formatter);
    }

    /**
     * Create a output target with end point specified by address and port.
     *
     * @param address the address endpoint
     * @param port the address port
     * @exception IOException if an error occurs
     */
    public SyslogTarget( final InetAddress address, final int port )
        throws IOException
    {
        super( address, port );
    }

    protected void doProcessEvent( LogEvent event )
    {
        int priority = event.getPriority().getValue();
        int syslogPriority = SYSLOG_INFO;

        switch (priority) {
            case 5 : {
                syslogPriority = SYSLOG_DEBUG;
                break;
            }
            case 10 : {
                syslogPriority = SYSLOG_INFO;
                break;
            }
            case 15 : {
                syslogPriority = SYSLOG_WARN;
                break;
            }
            case 20 : {
                syslogPriority = SYSLOG_ERROR;
                break;
            }
            case 25 : {
                syslogPriority = SYSLOG_FATAL;
                break;
            }
        }

        //final String data = format( event );
        if( null != getFormatter() )
        {
            write ("<"+(facility | syslogPriority)+">"+getFormatter().format( event ));
        }
        else
        {
            write ("<"+(facility | syslogPriority)+">"+event.toString());
        }
    }

    public static int getFacilityValue (String name) 
    {
        if( name.equalsIgnoreCase( "kern" ) ) 
        {
            return LOG_KERN;
        } 
        else if( name.equalsIgnoreCase( "user" ) )  
        {
            return LOG_USER;
        } 
        else if( name.equalsIgnoreCase("mail") ) 
        {
            return LOG_MAIL;
        } 
        else if (name.equalsIgnoreCase("daemon")) 
        {
            return LOG_DAEMON;
        }
        else if (name.equalsIgnoreCase("auth")) 
        {
            return LOG_AUTH;
        }
        else if (name.equalsIgnoreCase("syslog")) 
        {
            return LOG_SYSLOG;
        }
        else if (name.equalsIgnoreCase("lpr")) 
        {
            return LOG_LPR;
        }
        else if (name.equalsIgnoreCase("news"))
        {
            return LOG_NEWS;
        }
        else if (name.equalsIgnoreCase("uucp")) 
        {
            return LOG_UUCP;
        }
        else if (name.equalsIgnoreCase("cron")) 
        {
            return LOG_CRON;
        }
        else if (name.equalsIgnoreCase("authpriv")) 
        {
            return LOG_AUTHPRIV;
        }
        else if (name.equalsIgnoreCase("ftp")) 
        {
            return LOG_FTP;
        }
        else if (name.equalsIgnoreCase("local0")) 
        {
            return LOG_LOCAL0;
        }
        else if (name.equalsIgnoreCase("local1")) 
        {
            return LOG_LOCAL1;
        } 
        else if (name.equalsIgnoreCase("local2")) 
        {
            return LOG_LOCAL2;
        } 
        else if (name.equalsIgnoreCase("local3")) 
        {
            return LOG_LOCAL3;
        } 
        else if (name.equalsIgnoreCase("local4")) 
        {
            return LOG_LOCAL4;
        } 
        else if (name.equalsIgnoreCase("local5")) 
        {
            return LOG_LOCAL5;
        } 
        else if (name.equalsIgnoreCase("local6")) 
        {
            return LOG_LOCAL6;
        } 
        else if (name.equalsIgnoreCase("local7")) 
        {
            return LOG_LOCAL7;
        }
        return LOG_USER;
    }

    public void setFacility(String name)
    {
        facility = getFacilityValue(name);
    }

    public void setFacility(int value)
    {
        facility = value;
    }

    public int getFacility()
    {
        return facility;
    }
}
