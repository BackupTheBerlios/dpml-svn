/*
 * Copyright 2005 Stephen J. McConnell
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

package net.dpml.lang.process;

import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.ArrayList;
import java.util.Hashtable;

import net.dpml.util.DOM3DocumentBuilder;
import net.dpml.util.ElementHelper;
import net.dpml.util.DecodingException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.TypeInfo;

import junit.framework.TestCase;

/**
 * The Process XSD class validates the prodicess XSD schema.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class ProcessXsdTestCase extends TestCase
{
    private static final String XSD_URI = "@PROJECT-XSD-URI@";
    
    private URI m_uri;
    private Map m_products;
    private Map m_processes;
    
    public void setUp() throws Exception
    {
        // get the sample xml file to parse
        
        File testdir = new File( System.getProperty( "project.test.dir" ) );
        File doc = new File( testdir, "sample.xml" );
        m_uri = doc.toURI();
        
        // define a system property declaring the location of the source xsd 
        
        File basedir = new File( System.getProperty( "project.basedir" ) );
        File target = new File( basedir, "target" );
        File deliverables = new File( target, "deliverables" );
        File xsds = new File( deliverables, "xsds" );
        String version = System.getProperty( "project.version" );
        File xsd = new File( xsds, "dpml-process-" + version + ".xsd" );
        System.setProperty( XSD_URI, xsd.toURI().toString() );

        // parse the file
        
        DOM3DocumentBuilder builder = new DOM3DocumentBuilder();
        Document document = builder.parse( m_uri );
        Element element = document.getDocumentElement();
        
        // setup the product and process maps
        
        Element[] children = ElementHelper.getChildren( element );
        m_products = new Hashtable();
        m_processes = new Hashtable();
        
        for( int i=0; i<children.length; i++ )
        {
            Element child = children[i];
            TypeInfo info = child.getSchemaTypeInfo();
            if( info.isDerivedFrom( XSD_URI, "ProductType", TypeInfo.DERIVATION_EXTENSION ) )
            {
                String name = ElementHelper.getAttribute( child, "name" );
                m_products.put( name, child );
            }
            else if( 
              XSD_URI.equals( info.getTypeNamespace() ) && "ProcessType".equals( info.getTypeName() ) 
              || info.isDerivedFrom( XSD_URI, "ProcessType", TypeInfo.DERIVATION_EXTENSION ) )
            {
                String name = ElementHelper.getAttribute( child, "name" );
                m_processes.put( name, child );
            }
            else
            {
                final String error = 
                  "Element is not derived from product or process."
                  + "\nNamespace: " 
                  + info.getTypeNamespace() 
                  + " (" 
                  + XSD_URI.equals( info.getTypeNamespace() ) 
                  + ")"
                  + "\nType: " 
                  + info.getTypeName()
                  + "\n" 
                  + child;
                fail( error );
            }
        }
    }
    
    public void testSampleParse() throws Exception
    {
        System.out.println( "products: " + m_products.size() );
        System.out.println( "processes: " + m_processes.size() );
        
        String[] keys = (String[]) m_processes.keySet().toArray( new String[0] );
        for( int i=0; i<keys.length; i++ )
        {
            String key = keys[i];
            Element elem = (Element) m_processes.get( key );
            evaluationProcess( elem );
        }
    }
    
   /**
    * List the processes that are declared as implicit.
    * @exception Exception if an error occurs
    */
    public void testImplicitTargets() throws Exception
    {
        Element[] implicits = getImplicitProcesses();
        for( int i=0; i<implicits.length; i++ )
        {
            Element implicit = implicits[i];
            listProcess( implicit );
        }
    }
    
    private Element[] getImplicitProcesses()
    {
        ArrayList list = new ArrayList();
        String[] keys = (String[]) m_processes.keySet().toArray( new String[0] );
        for( int i=(keys.length-1); i>-1; i-- )
        {
            String key = keys[i];
            Element elem = (Element) m_processes.get( key );
            if( ElementHelper.getBooleanAttribute( elem, "implicit", false ) )
            {
                list.add( elem );
            }
        }
        return (Element[]) list.toArray( new Element[0] );
    }
    
    private void listProcess( Element process )
    {
        Element[] inputs = getInputElements( process );
        for( int i=0; i<inputs.length; i++ )
        {
            Element e = inputs[i];
            String id = ElementHelper.getAttribute( e, "id" );
            Element product = (Element) m_products.get( id );
            //System.out.println( "#   input: " + DecodingException.list( product ) );
            Element[] processes = getInputProcesses( product );
            for( int j=0; j<processes.length; j++ )
            {
                listProcess( processes[i] );
                //System.out.println( "#   processes: " + DecodingException.list( processes[i] ) );
            }
        }
        String name = ElementHelper.getAttribute( process, "name" );
        System.out.println( "# process: " + name );
    }
    
    private Element[] getInputProcesses( Element product )
    {
        String name = ElementHelper.getAttribute( product, "name" );
        return getProducers( name );
    }
    
    private Element[] getProducers( String id )
    {
        ArrayList list = new ArrayList();
        String[] keys = (String[]) m_processes.keySet().toArray( new String[0] );
        for( int i=(keys.length-1); i>-1; i-- )
        {
            String key = keys[i];
            Element process = (Element) m_processes.get( key );
            String[] ids = getProcessProductionIDs( process );
            for( int j=0; j<ids.length; j++ )
            {
                String productionId = ids[j];
                if( id.equals( productionId ) )
                {
                    list.add( process );
                }
            }
        }
        return (Element[]) list.toArray( new Element[0] );
    }
    
    private String[] getProcessProductionIDs( Element process )
    {
        Element[] outputs = getOutputElements( process );
        String[] result = new String[ outputs.length ];
        for( int i=0; i<outputs.length; i++ )
        {
            Element output = outputs[i];
            String out = ElementHelper.getAttribute( output, "id" );
            result[i] = out;
        }
        return result;
    }
    
    private Element[] getOutputElements( Element process )
    {
        Element outputs = ElementHelper.getChild( process, "produces" );
        return ElementHelper.getChildren( outputs );
    }
    
    private Element[] getInputElements( Element process )
    {
        Element consumes = ElementHelper.getChild( process, "consumes" );
        return ElementHelper.getChildren( consumes );
    }
    
    private void evaluationProcess( Element process )
    {
        Element[] inputs = getInputElements( process );
        for( int i=0; i<inputs.length; i++ )
        {
            Element input = inputs[i];
            String id = ElementHelper.getAttribute( input, "id" );
            Element product = (Element) m_products.get( id );
            if( null == product )
            {
                final String error =
                  "Input element:\n"
                  + DecodingException.list( input )
                  + "\n references an unknown product: "
                  + id;
                fail( error );
            }
        }
        Element produces = ElementHelper.getChild( process, "produces" );
        Element[] outputs = ElementHelper.getChildren( produces );
        for( int i=0; i<outputs.length; i++ )
        {
            Element output = outputs[i];
            String id = ElementHelper.getAttribute( output, "id" );
            Element product = (Element) m_products.get( id );
            if( null == product )
            {
                final String error =
                  "Output element:\n"
                  + DecodingException.list( output )
                  + "\n references an unknown product: "
                  + id;
                fail( error );
            }
        }
    }
}
