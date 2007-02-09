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

package dpml.lang;

import dpml.util.ObjectUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.net.URI;
import java.util.Arrays;
import java.util.Hashtable;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MBeanException;
import javax.management.InstanceAlreadyExistsException;

import javax.xml.XMLConstants;

import dpml.util.Category;
import net.dpml.lang.Buffer;
import net.dpml.lang.Strategy;
import net.dpml.lang.PartContentHandler;
import net.dpml.lang.ServiceRegistry;
import net.dpml.lang.SimpleServiceRegistry;
import net.dpml.lang.PartManager;

import net.dpml.util.Logger;

/**
 * Part datastructure.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class Part implements PartManager
{
    private final Info m_info;
    private final Strategy m_strategy;
    private final Classpath m_classpath;
    
   /**
    * Creation of a new part datastructure.
    * @param info the info descriptor
    * @param classpath the part classpath definition
    * @param strategy the part deployment strategy
    * @exception IOException if an I/O error occurs
    */
    public Part( Info info, Classpath classpath, Strategy strategy ) throws IOException
    {
        if( null == info )
        {
            throw new NullPointerException( "info" );
        }
        if( null == strategy )
        {
            throw new NullPointerException( "strategy" );
        }
        
        m_info = info;
        m_strategy = strategy;
        m_classpath = classpath;

        String flag = System.getProperty( "dpml.jmx.enabled", "false" );
        if( "true".equals( flag ) )
        {
            try
            {
                MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                Hashtable<String,String> table = new Hashtable<String,String>();
                table.put( "type", "Parts" );
                table.put( "name", "" + System.identityHashCode( this ) );
                ObjectName name =
                  ObjectName.getInstance( "net.dpml.transit", table );
                server.registerMBean( this, name );
            }
            catch( Exception e )
            {
                e.printStackTrace();
            }
        }
    }
    
   /**
    * Service instantiation.
    * @return the instantiated instance
    * @exception IOException if an error occurs
    * @deprecated Replaced by getInstance( Class<T> )
    */
    public Object getContent() throws IOException
    {
        return m_strategy.getContentForClass( Object.class );
    }
    
   /**
    * Return the part content or null if the result type is unresolvable 
    * relative to the supplied classes argument. 
    * @param classes the content type selection classes
    * @return the content
    * @exception IOException if an IO error occurs
    * @deprecated Replaced by getContentForClass(Class) method
    */
    public Object getContent( Class[] classes ) throws IOException
    {
        if( classes.length == 0 )
        {
            return this;
        }
        else
        {
            for( int i=0; i<classes.length; i++ )
            {
                Class<?> c = classes[i];
                Object content = m_strategy.getContentForClass( c );
                if( null != content )
                {
                    return content;
                }
            }
            return null;
        }
    }
    
    //-----------------------------------------------------------------------
    // PartManager
    //-----------------------------------------------------------------------
    
   /**
    * Returns the part URI as a string.
    * @return the codebase uri
    */
    public String getCodebaseURI()
    {
        return m_info.getURI().toASCIIString();
    }
    
   /**
    * Returns the part title.
    * @return the title
    */
    public String getTitle()
    {
        return m_info.getTitle();
    }
    
   /**
    * Returns the part description.
    * @return the description
    */
    public String getDescription()
    {
        return m_info.getDescription();
    }
    
    //-----------------------------------------------------------------------
    // Part
    //-----------------------------------------------------------------------
    
   /**
    * Get the part strategy.
    *
    * @return the strategy definition.
    */
    public Strategy getStrategy()
    {
        return m_strategy;
    }
    
   /**
    * Get the part classpath.
    *
    * @return the classpath definition.
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
    * @deprecated Replaced by getInstance( Class<T>, Object[] )
    */
    public Object instantiate( Object[] args ) throws Exception
    {
        Strategy strategy = getStrategy();
        ServiceRegistry registry = new SimpleServiceRegistry( args );
        strategy.initialize( registry );
        return strategy.getInstance( Object.class );
    }
    
    public void save( OutputStream output ) throws IOException
    {
        final Writer writer = new OutputStreamWriter( output );
        Buffer b = new Buffer( writer, PartContentHandler.NAMESPACE, "" );
        try
        {
            b.write( XML_HEADER );
            b.nl( "\n" + PART_HEADER );
            Buffer b2 = b.indent( "  " );
            encodeInfo( b2, m_info );
            Strategy strategy = getStrategy();
            strategy.encode( b2, null );
            encodeClasspath( b2, getClasspath() );
            b.nl( PART_FOOTER );
            b.nl( "" );
        }
        finally
        {
            writer.flush();
            output.close();
        }
    }
    
   /**
    * Test if this part is equivalent to the supplied part.
    *
    * @param other the other object
    * @return true if the parts are equivalent
    */
    public boolean equals( Object other )
    {
        if( other instanceof Part )
        {
            Part part = (Part) other;
            if( !m_info.equals( part.m_info ) )
            {
                return false;
            }
            else if( !m_classpath.equals( part.m_classpath ) )
            {
                return false;
            }
            else
            {
                return m_strategy.equals( part.m_strategy );
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
        hash ^= m_strategy.hashCode();
        hash ^= m_classpath.hashCode();
        return hash;
    }
    
    private static void encodeInfo( Buffer b, Info info ) throws IOException
    {
        String title = info.getTitle();
        String description = info.getDescription();
        if( null == description )
        {
            if( null == title )
            {
                b.nl( "<info/>" );
            }
            else
            {
                b.nl( "<info title=\"" + title + "\"/>" );
            }
        }
        else
        {
            if( null == title )
            {
                b.nl( "<info>" );
            }
            else
            {
                b.nl( "<info title=\"" + title + "\">" );
            }
            Buffer b2 = b.indent();
            b2.nl( "<description>" + description + "</description>" );
            b.nl( "</info>" );
        }
    }

    private static void encodeClasspath( Buffer b, Classpath classpath ) throws IOException
    {
        b.nl( "<classpath>" );
        if( null != classpath ) 
        {
            Buffer b2 = b.indent();
            encodeClasspathCategory( b2, classpath, Category.SYSTEM );
            encodeClasspathCategory( b2, classpath, Category.PUBLIC );
            encodeClasspathCategory( b2, classpath, Category.PROTECTED );
            encodeClasspathCategory( b2, classpath, Category.PRIVATE );
        }
        b.nl( "</classpath>" );
    }

    private static void encodeClasspathCategory( 
      Buffer b, Classpath classpath, Category category ) throws IOException
    {
        URI[] uris = classpath.getDependencies( category );
        if( uris.length > 0 )
        {
            String name = category.name().toLowerCase();
            b.nl( "<" + name + ">" );
            Buffer b2 = b.indent();
            for( int i=0; i<uris.length; i++ )
            {
                URI uri = uris[i];
                b2.nl( "<uri>" + uri.toASCIIString() + "</uri>" );
            }
            b.nl( "</" + name + ">" );
        }
    }
    
   /**
    * Default XML header.
    */
    private static final String XML_HEADER = "<?xml version=\"1.0\"?>";

   /**
    * Part header.
    */
    private static final String PART_HEADER = 
      "<part xmlns=\"" 
      + PartContentHandler.NAMESPACE 
      + "\""
      + "\n    xmlns:xsi=\"" 
      + XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI
      + "\">";
      
   /**
    * Part footer.
    */
    private static final String PART_FOOTER = "</part>";
}
