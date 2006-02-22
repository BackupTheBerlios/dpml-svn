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

package net.dpml.metro.builder;

import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.IOException;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.dpml.library.util.PluginFactory;
import net.dpml.library.model.Resource;

import net.dpml.lang.DefaultPluginHelper;
import net.dpml.lang.Version;
import net.dpml.lang.Plugin;

import net.dpml.transit.util.ElementHelper;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

/**
 * Utility class supporting the reading and writing of standard plugins definitions.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ComponentPluginHelper extends DefaultPluginHelper implements PluginFactory
{
    private static final PluginFactory FACTORY = new ComponentPluginFactory();

    //------------------------------------------------------------------------------
    // PluginFactory
    //------------------------------------------------------------------------------
    
   /**
    * Build the plugin definition.
    * @exception exception if a build related error occurs
    */
    public Plugin build( File dir, Resource resource ) throws Exception
    {
        return FACTORY.build( dir, resource );
    }
    
    //------------------------------------------------------------------------------
    // PluginHelper
    //------------------------------------------------------------------------------
    
    public Plugin resolve( URI uri, Element element ) throws Exception
    {
        return super.resolve( uri, element );
    }
}
