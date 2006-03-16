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

package net.dpml.transit;

import java.net.URL;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import net.dpml.lang.Logger;

/**
 * Utility class supporting the reading and writing of standard plugins definitions.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
final class SaxMonitor implements ErrorHandler
{
    private Logger m_logger;
    
    public SaxMonitor( Logger logger )
    {
        if( null == logger )
        {
            throw new NullPointerException( "logger" );
        }
        m_logger = logger;
    }
    
    public void error( SAXParseException e ) throws SAXException
    {
        final String message = getMessage( e ); 
        m_logger.warn( message );
    }
    
    public void fatalError( SAXParseException e ) throws SAXException
    {
        final String message = getMessage( e ); 
        m_logger.error( message );
    }
    
    public void warning( SAXParseException e ) throws SAXException
    {
        final String message = getMessage( e ); 
        m_logger.warn( message );
    }
    
    private String getMessage( SAXParseException e )
    {
        return e.getMessage() 
          + "(" + e.getLineNumber() 
          + "," + e.getColumnNumber() 
          + ")";
    }
}
