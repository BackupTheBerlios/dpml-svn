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

package org.apache.metro.studio.magic;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import javax.xml.transform.dom.DOMSource;

import javax.xml.transform.stream.StreamResult;

import org.apache.avalon.tools.model.Context;
import org.apache.avalon.tools.model.Definition;
import org.apache.avalon.tools.model.Home;
import org.apache.avalon.tools.model.Info;
import org.apache.avalon.tools.model.Policy;
import org.apache.avalon.tools.model.Resource;
import org.apache.avalon.tools.model.ResourceRef;

import org.apache.avalon.tools.tasks.SystemTask;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

import org.apache.tools.ant.types.Path;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.xml.sax.SAXException;

/**
 * Generate a eclipse plugin XML.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class EclipseTask extends SystemTask
{
    /**
     * The file containing the specification for the plugin.xml
     */
    private File m_PluginSpec;

    /**
     * The destination directory for plugin.xml
     */
    private File m_DestDir;


    /**
     * Set the desitation directory to generate output files to.
     *
     * @param destDir The destination directory
     */
    public void setDestDir( final File destDir )
    {
        m_DestDir = destDir;
    }

    /**
     * Return the destination directory in which files are generated.
     *
     * @return the destination directory in which files are generated.
     */
    public File getDestDir()
    {
        return m_DestDir;
    }
    
    /**
     * Set the file containing the specification for the plugin.xml.
     *
     * @param destDir the file containing the specification for 
     *                the plugin.xml.
     */
    public void setPluginSpec( File pluginSpec )
    {
        m_PluginSpec = pluginSpec;
    }

    /**
     * Return the file containing the specification for the plugin.xml.
     *
     * @return the file containing the specification for the plugin.xml.
     */
    public File getPluginSpec()
    {
        return m_PluginSpec;
    }

    /**
     * Execute generator task.
     * @exception BuildException if a build error occurs
     */
    public void execute()
        throws BuildException
    {
        try
        {
            Document doc = parseSpecification();
            addVersionAttribute( doc );
            addRuntimeElement( doc );
            writePluginXml( doc );
        } catch( ParserConfigurationException e )
        {
            String message = "JAXP is not properly setup. Missing Parser.";
            throw new BuildException( message, e );
        } catch( TransformerConfigurationException e )
        {
            String message = "JAXP is not properly setup. Missing Transformer.";
            throw new BuildException( message, e );
        } catch( SAXException e )
        {
            String message = "";
            throw new BuildException( message, e );
        } catch( IOException e )
        {
            String message = "";
            throw new BuildException( message, e );
        } catch( TransformerException e )
        {
            String message = "";
            throw new BuildException( message, e );
        }
    }
    
    private Document parseSpecification()
        throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse( m_PluginSpec );
        return doc;
    }
    
    private void addVersionAttribute( Document doc )
    {
        Element root = doc.getDocumentElement();
        Home home = getHome();
        String name = getProject().getName();
        Definition def = home.getDefinition( name );
        Info info = def.getInfo();
        String version = info.getVersion();
        root.setAttribute( "version", version );
    }
    
    private void addRuntimeElement( Document doc )
    {
        Element root = doc.getDocumentElement();
        Element runtimeElement = doc.createElement( "runtime" );
        Text padding = doc.createTextNode( "\n  " );
        root.appendChild( padding );
        root.appendChild( runtimeElement );
        padding = doc.createTextNode( "\n" );
        root.appendChild( padding );
        
        Project project = getProject();
        String projectName = project.getName();
        Home home = getHome();
        Definition def = home.getDefinition( projectName );
        processDefinition( doc, runtimeElement, def );
    }
    
    private void processDefinition( Document doc, Element runtime, Definition def )
    {
        Element selfNode = appendLibraryChild( doc, runtime, def );
        Element export = doc.createElement( "export" );
        export.setAttribute( "name", "*" );
        Text padding = doc.createTextNode( "\n      " );
        selfNode.appendChild( padding );
        selfNode.appendChild( export );
        padding = doc.createTextNode( "\n    " );
        selfNode.appendChild( padding );
        
        final ResourceRef[] refs = 
            def.getResourceRefs( getProject(), Policy.RUNTIME, ResourceRef.ANY, true );
        Home home = getHome();
        for( int i=0 ; i < refs.length ; i++ )
        {
            Resource resource = home.getResource( refs[i] );
            appendLibraryChild( doc, runtime, resource );
        }
        padding = doc.createTextNode( "\n  " );
        runtime.appendChild( padding );
    }

    private Element appendLibraryChild( Document doc, Node node, Resource resource )
    {
        final String name = resource.getFilename();
        final String group = resource.getInfo().getGroup();
        
        final String filename = "lib/" + group + "/" + name;
        
        final Element library = doc.createElement( "library" );
        library.setAttribute( "name", filename );
        Text padding = doc.createTextNode( "\n    " );
        node.appendChild( padding );
        node.appendChild( library );
        return library;
    }
        
    private void writePluginXml( Document doc )
        throws TransformerConfigurationException, TransformerException
    {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer();
        
        File output = new File( m_DestDir, "plugin.xml" );
        
        DOMSource source = new DOMSource( doc );
        StreamResult result = new StreamResult( output );
        transformer.transform( source, result );
    }
}
