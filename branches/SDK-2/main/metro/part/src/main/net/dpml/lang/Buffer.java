/*
 * Copyright 2005 Stephen J. McConnell.
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

import java.io.Writer;
import java.io.IOException;

/**
 * Utility class used as a destination during generalized object encoding.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class Buffer
{
    private final String m_namespace;
    private final String m_pad;
    private final Writer m_writer;
    
   /**
    * Construct a new encoding buffer.
    *
    * @param writer the output stream writer
    * @param namespace the current namespace
    * @param pad the current offset
    * @exception NullPointerException of the writer or namespace arguments are null
    */
    public Buffer( final Writer writer, final String namespace, final String pad )
    {
        if( null == writer )
        {
            throw new NullPointerException( "writer" );
        }
        if( null == namespace )
        {
            throw new NullPointerException( "namespace" );
        }
        if( null == pad )
        {
            m_pad = "";
        }
        else
        {
            m_pad = pad;
        }
        m_namespace = namespace;
        m_writer = writer;
    }
    
    public String getEnclosingNamespace()
    {
        return m_namespace;
    }
    
    public String getOffset()
    {
        return m_pad;
    }
    
    public void write( String value ) throws IOException
    {
        m_writer.write( value );
    }
    
    public void write( int n ) throws IOException
    {
        m_writer.write( n );
    }
    
    public void write( char[] array ) throws IOException
    {
        m_writer.write( array );
    }
    
    public void nl( String value ) throws IOException
    {
        m_writer.write( "\n" + m_pad + value );
    }
    
    public void nl( int n ) throws IOException
    {
        m_writer.write( "\n" + m_pad + n );
    }
    
    public void nl( char[] array ) throws IOException
    {
        m_writer.write( "\n" + m_pad );
        m_writer.write( array );
    }
    
    public Buffer indent()
    {
        return indent( "  " );
    }
    
    public Buffer indent( String indent )
    {
        return new Buffer( m_writer, m_namespace, m_pad + indent );
    }
    
    public boolean isNamespace( String namespace )
    {
        return m_namespace.equals( namespace );
    }
    
    public Buffer namespace( String namespace )
    {
        if( m_namespace.equals( namespace ) )
        {
            return this;
        }
        else
        {
            return new Buffer( m_writer, namespace, m_pad );
        }
    }
}
