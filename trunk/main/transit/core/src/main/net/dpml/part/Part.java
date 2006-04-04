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

package net.dpml.part;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import javax.xml.XMLConstants;

import net.dpml.lang.Category;
import net.dpml.lang.Classpath;
import net.dpml.lang.Logger;

import net.dpml.transit.Artifact;

/**
 * Part datastructure.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class Part
{
    protected static final ValueEncoder VALUE_ENCODER = new ValueEncoder();
    protected static final String XML_HEADER = "<?xml version=\"1.0\"?>";
    protected static final String PART_SCHEMA_URN = "@PART-XSD-URI@";
    protected static final String PART_HEADER = 
      "<part xmlns=\"" 
      + PART_SCHEMA_URN 
      + "\""
      + "\n    xmlns:xsi=\"" 
      + XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI
      + "\">";
    protected static final String PART_FOOTER = "</part>";

    private final Info m_info;
    private final Classpath m_classpath;
    private final ClassLoader m_classloader;
    private final Logger m_logger;
    
    public static Part load( URI uri ) throws IOException
    {
        return PartDecoder.getInstance().loadPart( uri );
    }
    
   /**
    * Creation of a new part datastructure.
    * @param logger the logging channel
    * @param info the info descriptor
    * @param strategy the part deployment strategy
    * @param classpath the part classpath definition
    */
    public Part( Logger logger, Info info, Classpath classpath ) throws IOException
    {
        super();
        if( null == info )
        {
            throw new NullPointerException( "info" );
        }
        if( null == classpath )
        {
            throw new NullPointerException( "classpath" );
        }
        m_logger = logger;
        m_info = info;
        m_classpath = classpath;
        m_classloader = buildClassLoader();
    }
    
   /**
    * Get the part info descriptor.
    *
    * @return the part info datastructure
    */
    public Info getInfo()
    {
        return m_info;
    }
    
   /**
    * Get the part classpath definition.
    *
    * @return the classpath definition
    */
    public Classpath getClasspath()
    {
        return m_classpath;
    }

   /**
    * Instantiate a value.
    * @param args supplimentary arguments
    * @return the resolved instance
    * @exception Exception if a deployment error occurs
    */
    public abstract Object instantiate( Object[] args ) throws Exception;
        
    public void encode( OutputStream output ) throws IOException
    {
        final Writer writer = new OutputStreamWriter( output );
        writer.write( XML_HEADER );
        writer.write( "\n" );
        writer.write( "\n" + PART_HEADER );
        writer.write( "\n" );
        encodeInfo( writer, getInfo() );
        writer.write( "\n" );
        encodeStrategy( writer, "  " );
        writer.write( "\n" );
        encodeClasspath( writer, getClasspath() );
        writer.write( "\n" );
        writer.write( "\n" + PART_FOOTER );
        writer.write( "\n" );
        writer.flush();
        output.close();
    }
    
    public boolean equals( Object other )
    {
        if( other instanceof Part )
        {
            Part part = (Part) other;
            if( !m_info.equals( part.getInfo() ) )
            {
                return false;
            }
            else
            {
                return m_classpath.equals( part.getClasspath() );
            }
        }
        else
        {
            return false;
        }
    }
    
    protected abstract void encodeStrategy( Writer writer, String pad ) throws IOException;

   /**
    * Get the implementation classloader.
    * @return the resolved classloader
    * @exception IOException if an IO error occurs during classpath evaluation
    */
    public ClassLoader getClassLoader() throws IOException
    {
        return m_classloader;
    }
    
    protected Logger getLogger()
    {
        return m_logger;
    }
    
    private ClassLoader buildClassLoader() throws IOException
    {
        ClassLoader base = getAnchorClassLoader();
        Classpath classpath = getClasspath();
        return newClassLoader( base, classpath );
    }
    
    private ClassLoader newClassLoader( ClassLoader base, Classpath classpath ) throws IOException
    {
        URI uri = getInfo().getURI();
        URI[] uris = classpath.getDependencies( Category.SYSTEM );
        if( uris.length > 0 )
        {
            updateSystemClassLoader( uris );
        }
        
        URI[] apis = classpath.getDependencies( Category.PUBLIC );
        ClassLoader api = StandardClassLoader.buildClassLoader( uri, Category.PUBLIC, base, apis );
        if( api != base )
        {
            classloaderConstructed( Category.PUBLIC, api );
        }
        URI[] spis = classpath.getDependencies( Category.PROTECTED );
        ClassLoader spi = StandardClassLoader.buildClassLoader( uri, Category.PROTECTED, api, spis );
        if( spi != api )
        {
            classloaderConstructed( Category.PROTECTED, spi );
        }
        URI[] imps = classpath.getDependencies( Category.PRIVATE );
        ClassLoader impl = StandardClassLoader.buildClassLoader( uri, Category.PRIVATE, spi, imps );
        if( impl != spi )
        {
            classloaderConstructed( Category.PRIVATE, impl );
        }
        return impl;
    }
    
    private ClassLoader getAnchorClassLoader()
    {
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        if( null != context )
        {
            return context;
        }
        else
        {
            return Part.class.getClassLoader();
        }
    }

    private void updateSystemClassLoader( URI[] uris ) throws IOException
    {
        ClassLoader parent = ClassLoader.getSystemClassLoader();
        synchronized( parent )
        {
            if( parent instanceof SystemClassLoader )
            {
                SystemClassLoader loader = (SystemClassLoader) parent;
                loader.addDelegates( uris );
                systemExpanded( uris );
            }
            else
            {
                final String message =
                  "Cannot load [" 
                  + uris.length 
                  + "] system artifacts into a foreign system classloader.";
                getLogger().debug( message );
            }
        }
    }


   /**
    * Handle notification of the creation of a new classloader.
    * @param type the type of classloader (api, spi or impl)
    * @param classloader the new classloader 
    */
    public void classloaderConstructed( Category category, ClassLoader classloader )
    {
        if( getLogger().isDebugEnabled() )
        {
            int id = System.identityHashCode( classloader );
            StringBuffer buffer = new StringBuffer();
            buffer.append( "created classloader " );
            buffer.append( m_info.getURI().toString() );
            buffer.append( "#" + category.toString() );
            buffer.append( " (" + id + ")" );
            ClassLoader parent = classloader.getParent();
            if( null != parent )
            {
                buffer.append( " extends (" + System.identityHashCode( parent ) + ")" );
            }
            if( classloader instanceof URLClassLoader )
            {
                URLClassLoader loader = (URLClassLoader) classloader;
                URL[] urls = loader.getURLs();
                for( int i=0; i < urls.length; i++ )
                {
                    URL url = urls[i];
                    buffer.append( "\n  [" + i + "] \t" + url.toString() );
                }
            }
            getLogger().debug( buffer.toString() );
        }
    }

   /**
    * Handle notification of system classloader expansion.
    * @param urls the array of urls added to the system classloader
    */
    public void systemExpanded( URI[] uris )
    {
        if( getLogger().isDebugEnabled() )
        {
            StringBuffer buffer = new StringBuffer();
            buffer.append( "system classloader expansion" );
            for( int i=0; i<uris.length; i++ )
            {
                int n = i+1;
                buffer.append( "\n  [" + n + "] \t" + uris[i] );
            }
            getLogger().debug( buffer.toString() );
        }
    }

    private void encodeInfo( Writer writer, Info info ) throws IOException
    {
        String title = info.getTitle();
        String description = info.getDescription();
        if( null == description )
        {
            writer.write( "\n  <info title=\"" + title + "\"/>" );
        }
        else
        {
            writer.write( "\n  <info title=\"" + title + "\">" );
            writer.write( "\n    <description>" + description + "</description>" );
            writer.write( "\n  </info>" );
        }
    }

    private void encodeClasspath( Writer writer, Classpath classpath ) throws IOException
    {
        writer.write( "\n  <classpath>" );
        encodeClasspathCategory( writer, classpath, Category.SYSTEM );
        encodeClasspathCategory( writer, classpath, Category.PUBLIC );
        encodeClasspathCategory( writer, classpath, Category.PROTECTED );
        encodeClasspathCategory( writer, classpath, Category.PRIVATE );
        writer.write( "\n  </classpath>" );
    }

    private void encodeClasspathCategory( 
      Writer writer, Classpath classpath, Category category ) throws IOException
    {
        URI[] uris = classpath.getDependencies( category );
        if( uris.length > 0 )
        {
            String name = category.getName();
            writer.write( "\n    <" + name + ">" );
            for( int i=0; i<uris.length; i++ )
            {
                URI uri = uris[i];
                writer.write( "\n      <uri>" + uri.toASCIIString() + "</uri>" );
            }
            writer.write( "\n    </" + name + ">" );
        }
    }
}
