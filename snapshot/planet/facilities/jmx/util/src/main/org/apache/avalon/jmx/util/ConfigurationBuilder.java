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
package org.apache.avalon.jmx.util;

import java.io.IOException;
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
 * @version $Revision: 1.2 $
 *
 * @todo update JavaDocs
 */
class ConfigurationBuilder
{
    private static final DTDInfo[] DTD_INFO = new DTDInfo[]
    {
        new DTDInfo
        (
            "-//PHOENIX/Mx Info DTD Version 1.0//EN",
            "http://avalon.apache.org/dtds/meta/mxinfo_1_0.dtd",
            "org/apache/avalon/jmx/util/mxinfo.dtd" 
        ),
        new DTDInfo
        ( 
            "-//PHOENIX/Mx Info DTD Version 1.0//EN",
            "http://jakarta.apache.org/avalon/dtds/phoenix/mxinfo_1_0.dtd",
            "org/apache/avalon/jmx/util/mxinfo.dtd" 
        )
    };

    private static final DTDResolver RESOLVER = new DTDResolver( 
        DTD_INFO, ConfigurationBuilder.class.getClassLoader() );

    /**
     * Private constructor to block instantiation.
     */
    private ConfigurationBuilder()
    {
    }

    /**
     * Utility method to create a new XML reader.
     */
    private static XMLReader createXMLReader() throws SAXException, ParserConfigurationException
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
                                        final SAXConfigurationHandler handler,
                                        final boolean validate ) throws SAXException
    {
        reader.setEntityResolver( RESOLVER );
        reader.setContentHandler( handler );
        reader.setErrorHandler( handler );

        if ( validate )
        {
            // Request validation
            reader.setFeature( "http://xml.org/sax/features/validation", true );
        }
    }

    /**
     * Build a configuration object using an URI
     */
    public static Configuration build( final String uri ) throws SAXException,
        ParserConfigurationException, IOException
    {
        return build( new InputSource( uri ) );
    }

    /**
     * Build a configuration object using an URI, and
     * optionally validate the xml against the DTD.
     */
    public static Configuration build( final String uri, boolean validate ) throws SAXException,
        ParserConfigurationException, IOException
    {
        return build( new InputSource( uri ), validate );
    }

    /**
     * Build a configuration object using an XML InputSource object
     */
    public static Configuration build( final InputSource input ) throws SAXException,
        ParserConfigurationException, IOException
    {
        return build( input, false );
    }

    /**
     * Build a configuration object using an XML InputSource object, and
     * optionally validate the xml against the DTD.
     */
    public static Configuration build( final InputSource input, boolean validate ) throws
        SAXException, ParserConfigurationException, IOException
    {
        final XMLReader reader = createXMLReader();
        final SAXConfigurationHandler handler = new SAXConfigurationHandler();
        setupXMLReader( reader, handler, validate );
        reader.parse( input );
        return handler.getConfiguration();
    }
}
