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

package org.apache.avalon.meta;

import java.io.IOException;
import java.net.MalformedURLException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.SAXConfigurationHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Utility class used to load Configuration trees from XML files.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ConfigurationBuilder.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class ConfigurationBuilder
{
    private static final DTDInfo[] DTD_INFO = new DTDInfo[]
    {
        new DTDInfo( "-//AVALON/Component Type DTD Version 1.0//EN",
                     "http://avalon.apache.org/dtds/meta/type_1_0.dtd",
                     "org/apache/avalon/meta/type.dtd" ),
        new DTDInfo( "-//AVALON/Component Type DTD Version 1.1//EN",
                     "http://avalon.apache.org/dtds/meta/type_1_1.dtd",
                     "org/apache/avalon/meta/type.dtd" ),
        new DTDInfo( "-//AVALON/Service DTD Version 1.0//EN",
                     "http://avalon.apache.org/dtds/meta/service_1_0.dtd",
                     "org/apache/avalon/meta/service.dtd" ),
        new DTDInfo( "-//PHOENIX/Block Info DTD Version 1.0//EN",
                     "http://avalon.apache.org/dtds/phoenix/blockinfo_1.0.dtd",
                     "org/apache/avalon/meta/blockinfo.dtd" ),
    };

    private static final DTDResolver DTD_RESOLVER =
        new DTDResolver( DTD_INFO, ConfigurationBuilder.class.getClassLoader() );

    /**
     * Private constructor to block instantiation.
     */
    private ConfigurationBuilder()
    {
    }

    /**
     * Utility method to create a new XML reader.
     */
    private static XMLReader createXMLReader()
        throws SAXException, ParserConfigurationException
    {
        final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        saxParserFactory.setNamespaceAware( false );
        final SAXParser saxParser = saxParserFactory.newSAXParser();
        return saxParser.getXMLReader();
    }

    /**
     * Internally sets up the XMLReader
     */
    private static void setupXMLReader( final XMLReader reader,
                                        final SAXConfigurationHandler handler )
    {
        reader.setEntityResolver( DTD_RESOLVER );
        reader.setContentHandler( handler );
        reader.setErrorHandler( handler );
    }

    /**
     * Build a configuration object using an URI
     * @param uri an input source system identifier
     * @return the contfiguration instance
     * @exception SAXException is a parser exception is encountered
     * @exception ParserConfigurationException if a parser configuration failure occurs
     * @exception IOException if an IO exception occurs while attempting to read the
     *    resource identified by the system identifier
     */
    public static Configuration build( final String uri )
        throws SAXException, ParserConfigurationException, IOException
    {
        try
        {
            return build( new InputSource( uri ) );
        }
        catch( MalformedURLException mue )
        {
            final String error = 
              "Invalid input source uri: " + uri;
            throw new IOException( error );
        }
    }

    /**
     * Build a configuration object using an XML InputSource object
     * @param input an input source
     * @return the contfiguration instance
     * @exception SAXException is a parser exception is encountered
     * @exception ParserConfigurationException if a parser configuration failure occurs
     * @exception IOException if an IO exception occurs while attempting to read the
     *    resource associated with the input source
     */
    public static Configuration build( final InputSource input )
        throws SAXException, ParserConfigurationException, IOException
    {
        if( input == null )
        {
            throw new NullPointerException( "input" );
        }

        final XMLReader reader = createXMLReader();
        final SAXConfigurationHandler handler = new SAXConfigurationHandler();
        setupXMLReader( reader, handler );
        reader.parse( input );
        return handler.getConfiguration();
    }
}
