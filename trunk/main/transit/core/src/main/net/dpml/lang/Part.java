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

package net.dpml.lang;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;

import javax.xml.XMLConstants;

import net.dpml.util.Logger;

/**
 * Part datastructure.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class Part
{
   /**
    * A value encoder.
    */
    protected static final ValueEncoder VALUE_ENCODER = new ValueEncoder();
    
   /**
    * Default XML header.
    */
    protected static final String XML_HEADER = "<?xml version=\"1.0\"?>";

   /**
    * Part schema URN.
    */
    protected static final String PART_SCHEMA_URN = "@PART-XSD-SPEC-URI@";

   /**
    * Part header.
    */
    protected static final String PART_HEADER = 
      "<part xmlns=\"" 
      + PART_SCHEMA_URN 
      + "\""
      + "\n    xmlns:xsi=\"" 
      + XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI
      + "\">";
      
   /**
    * Part footer.
    */
    protected static final String PART_FOOTER = "</part>";

    private final Info m_info;
    private final Classpath m_classpath;
    private final ClassLoader m_classloader;
    private final Logger m_logger;
    
   /**
    * Load a part from an external XML source with part caching.
    * @param uri the external part source
    * @return the resolved part
    * @exception IOException of an I/O error occurs
    */
    public static Part load( URI uri ) throws IOException
    {
        return load( uri, true );
    }
    
   /**
    * Load a part from an external XML source.
    * @param uri the external part source
    * @param cache the cache policy
    * @return the resolved part
    * @exception IOException of an I/O error occurs
    */
    public static Part load( URI uri, boolean cache ) throws IOException
    {
        return PartDecoder.getInstance().loadPart( uri, cache );
    }
    
   /**
    * Creation of a new part datastructure.
    * @param logger the logging channel
    * @param info the info descriptor
    * @param classpath the part classpath definition
    * @param label debug label
    * @exception IOException if an I/O error occurs
    */
    public Part( Logger logger, Info info, Classpath classpath ) throws IOException
    {
        this( logger, info, classpath, null );
    }
    
   /**
    * Creation of a new part datastructure.
    * @param logger the logging channel
    * @param info the info descriptor
    * @param classpath the part classpath definition
    * @param label debug label
    * @exception IOException if an I/O error occurs
    */
    public Part( Logger logger, Info info, Classpath classpath, String label ) throws IOException
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
        m_classloader = buildClassLoader( label );
    }
    
   /**
    * Return the default part content. 
    * @return the result of part instantiation
    * @exception IOException if an IO error occurs
    */
    public Object getContent() throws IOException
    {
        try
        {
            return instantiate( new Object[0] );
        }
        catch( IOException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            final String error = 
              "Part instantiation error.";
            throw new PartException( error, e );
        }
    }
    
   /**
    * Return the part content or null if the result type is unresolvable 
    * relative to the supplied classes argument. 
    * @param classes the content type selection classes
    * @return the content
    * @exception IOException if an IO error occurs
    */
    public Object getContent( Class[] classes ) throws IOException
    {
        if( classes.length == 0 )
        {
            return getContent();
        }
        else
        {
            for( int i=0; i<classes.length; i++ )
            {
                Class c = classes[i];
                Object content = getContent( c );
                if( null != content )
                {
                    return content;
                }
            }
            return null;
        }
    }
    
   /**
    * Return the part content or null if the result type is unresolvable 
    * relative to the supplied classes argument. Recognized class arguments
    * include Info, Classpath, Part, ClassLoader, and Object.
    *
    * @param c the content type class
    * @return the content
    * @exception IOException if an IO error occurs
    */
    protected Object getContent( Class c ) throws IOException
    {
        if( Info.class.isAssignableFrom( c ) )
        {
            return getInfo();
        }
        else if( Classpath.class.isAssignableFrom( c ) )
        {
            return getClasspath();
        }
        else if( Part.class.isAssignableFrom( c ) )
        {
            return this;
        }
        else if( ClassLoader.class.isAssignableFrom( c ) )
        {
            return getClassLoader();
        }
        else if( Object.class == c )
        {
            try
            {
                return instantiate( new Object[0] );
            }
            catch( IOException e )
            {
                throw e;
            }
            catch( Exception e )
            {
                final String error = 
                  "Part instantiation error.";
                throw new PartException( error, e );
            }
        }
        else
        {
            return null;
        }
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
   
   /**
    * Externalize the part to XML.
    * @param output the output stream
    * @exception IOException if an I/O error occurs
    */
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
    
   /**
    * Test is this part is equiovalent to the supplied part.
    *
    * @param other the other object
    * @return true if the parts are equivalent
    */
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
    
   /**
    * Get the part hashcode.
    *
    * @return the hash value
    */
    public int hashCode()
    {
        int hash = m_info.hashCode();
        hash ^= m_classpath.hashCode();
        return hash;
    }
    
   /**
    * Encode this part strategy to XML.
    *
    * @param writer the output stream writer 
    * @param pad the character offset 
    * @exception IOException if an I/O error occurs during part externalization
    */
    protected abstract void encodeStrategy( Writer writer, String pad ) throws IOException;

   /**
    * Get the implementation classloader.
    * @return the resolved classloader
    */
    public ClassLoader getClassLoader()
    {
        return m_classloader;
    }

   /**
    * Get the assigned logging channel.
    * @return the logging channel
    */
    protected Logger getLogger()
    {
        return m_logger;
    }
    
    private ClassLoader buildClassLoader( String label ) throws IOException
    {
        ClassLoader base = getAnchorClassLoader();
        Classpath classpath = getClasspath();
        String tag = getLabel( label );
        return newClassLoader( base, classpath, tag );
    }
    
    private String getLabel( String label )
    {
        if( null != label )
        {
            return label;
        }
        if( null != getInfo().getTitle() )
        {
            return getInfo().getTitle();
        }
        else
        {
            return PartDecoder.getPartSpec( getInfo().getURI() );
        }
    }
    
    private ClassLoader newClassLoader( ClassLoader base, Classpath classpath, String label ) throws IOException
    {
        Logger logger = getLogger();
        URI[] uris = classpath.getDependencies( Category.SYSTEM );
        if( uris.length > 0 )
        {
            updateSystemClassLoader( uris );
        }
        
        URI[] apis = classpath.getDependencies( Category.PUBLIC );
        ClassLoader api = StandardClassLoader.buildClassLoader( logger, label, Category.PUBLIC, base, apis );
        URI[] spis = classpath.getDependencies( Category.PROTECTED );
        ClassLoader spi = StandardClassLoader.buildClassLoader( logger, label, Category.PROTECTED, api, spis );
        URI[] imps = classpath.getDependencies( Category.PRIVATE );
        return StandardClassLoader.buildClassLoader( logger, label, Category.PRIVATE, spi, imps );
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
    * @param label the classloader label
    * @param category the classloader category
    * @param classloader the new classloader to report
    */
    /*
    private void classloaderConstructed( String label, Category category, ClassLoader classloader )
    {
        if( getLogger().isDebugEnabled() )
        {
            int id = System.identityHashCode( classloader );
            StringBuffer buffer = new StringBuffer();
            buffer.append( "created new " );
            buffer.append( category.toString() );
            buffer.append( " classloader" );
            buffer.append( "\n  ID: " + id );
            buffer.append( "\n  Label: " + label + " " + category );
            ClassLoader parent = classloader.getParent();
            if( null != parent )
            {
                buffer.append( "\n  Extends: " + System.identityHashCode( parent ) );
            }
            if( classloader instanceof URLClassLoader )
            {
                URLClassLoader loader = (URLClassLoader) classloader;
                URL[] urls = loader.getURLs();
                for( int i=0; i < urls.length; i++ )
                {
                    URL url = urls[i];
                    buffer.append( "\n  [" + i + "] " + url.toString() );
                }
            }
            getLogger().debug( buffer.toString() );
        }
    }
    */
    
   /**
    * Handle notification of system classloader expansion.
    * @param uris the array of uris added to the system classloader
    */
    private void systemExpanded( URI[] uris )
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
            if( null == title )
            {
                writer.write( "\n  <info/>" );
            }
            else
            {
                writer.write( "\n  <info title=\"" + title + "\"/>" );
            }
        }
        else
        {
            if( null == title )
            {
                writer.write( "\n  <info>" );
            }
            else
            {
                writer.write( "\n  <info title=\"" + title + "\">" );
            }
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
