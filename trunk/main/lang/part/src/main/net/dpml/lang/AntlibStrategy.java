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
import java.io.Writer;
import java.net.URL;

import net.dpml.state.StateListener;

import net.dpml.util.Logger;
import dpml.lang.Disposable;

/**
 * Simple resource deployment strategy.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class AntlibStrategy extends Strategy
{
    private final String m_urn;
    private final String m_path;
    
   /**
    * Creation of resource datatype.
    * @param classloader the classloader
    * @param urn the resource urn
    * @param path the resource path
    * @exception IOException if an I/O error occurs
    */
    public AntlibStrategy( ClassLoader classloader, String urn, String path )
      throws IOException
    {
        super( classloader );
        if( null == urn )
        {
            throw new NullPointerException( "urn" );
        }
        if( null == path )
        {
            throw new NullPointerException( "path" );
        }
        m_urn = urn;
        m_path = path;
    }
    
    public String getName()
    {
        return m_urn;
    }
    
    public int getPriority()
    {
        return 0;
    }

   /**
    * Return the part content or null if the result type is unresolvable 
    * relative to the supplied classes argument. Class arguments recognized
    * over an above plugin include the URL and String classes.  If the URL
    * class is supplied a URL referencing the resource identified by path 
    * is returned.  If a String is requested the urn value is returned.
    *
    * @param c the content class
    * @return the content
    * @exception IOException if an IO error occurs
    */
    public <T>T getContentForClass( Class<T> c ) throws IOException
    {
        if( Strategy.class.equals( c ) )
        {
            return c.cast( this );
        }
        else if( URL.class.equals( c ) )
        {
            URL url = getClassLoader().getResource( m_path );
            return c.cast( url );
        }
        else if( String.class.equals( c ) )
        {
            String urn = getURN();
            return c.cast( urn );
        }
        else
        {
            return null;
        }
    }
    
   /**
    * Get the resource urn.
    * @return the urn
    */
    public String getURN()
    {
        return m_urn;
    }
    
   /**
    * Get the resource path.
    * @return the path
    */ 
    public String getPath()
    {
        return m_path;
    }
    
    public boolean isaCandidate( Class<?> type )
    {
        throw new UnsupportedOperationException();
    }

    public void initialize( ServiceRegistry registry )
    {
        // not required
    }

   /**
    * Instantiate a value.
    * @param type the return type
    * @return the resolved instance
    */
    public <T>T getInstance( Class<T> type )
    {
        try
        {
            ClassLoader classloader = getClassLoader();
            Object resource = classloader.getResource( m_path );
            return type.cast( resource );
        }
        catch( Exception e )
        {
            throw new PartError( e.getMessage(), e.getCause() );
        }
    }
    
   /**
    * Encode the resource strategy to XML.
    * @param buffer the output buffer
    * @param key the key
    * @exception IOException if an I/O error occurs
    */
    public void encode( Buffer buffer, String key ) throws IOException
    {
        String urn = getURN();
        String path = getPath();
        buffer.nl( "<resource xmlns=\"" + AntlibStrategyHandler.NAMESPACE + "\"" );
        if( null != key )
        {
            buffer.write( " key=\"" + key + "\"" );
        }
        buffer.write( " urn=\"" + urn + "\" " );
        buffer.write( " path=\"" + path + "\"" );
        buffer.write( "/>" );
    }
    
   /**
    * Test if this instance is equal to the supplied instance.
    * @param other the supplied instance
    * @return the equality status
    */
    public boolean equals( Object other )
    {
        if( other instanceof AntlibStrategy )
        {
            AntlibStrategy resource = (AntlibStrategy) other;
            if( !m_path.equals( resource.m_path ) )
            {
                return false;
            }
            else
            {
                return m_urn.equals( resource.m_urn );
            }
        }
        else
        {
            return false;
        }
    }
    
   /**
    * Get the hashcode for this instance.
    * @return the hash value
    */
    public int hashCode()
    {
        int hash = 99875845;
        hash ^= m_path.hashCode();
        hash ^= m_urn.hashCode();
        return hash;
    }
}
