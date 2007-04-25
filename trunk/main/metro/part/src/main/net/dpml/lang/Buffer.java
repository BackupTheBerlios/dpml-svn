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
    public Buffer( final Writer writer, final String namespace, final String pad ) throws NullPointerException
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
    
   /**
    * Get the current namespace.
    * @return the namespace
    */
    public String getEnclosingNamespace()
    {
        return m_namespace;
    }
    
   /**
    * Get the space indent offset.
    * @return the offset value
    */
    public String getOffset()
    {
        return m_pad;
    }
    
   /**
    * Write a value to the buffer.
    * @param value the value to write to the buffer
    * @exception IOException if an IO error occurs
    */
    public void write( String value ) throws IOException
    {
        m_writer.write( value );
    }
    
   /**
    * Write an int value to the buffer.
    * @param n the int value to write to the buffer
    * @exception IOException if an IO error occurs
    */
    public void write( int n ) throws IOException
    {
        m_writer.write( n );
    }
    
   /**
    * Write an character array value to the buffer.
    * @param array the array value to write to the buffer
    * @exception IOException if an IO error occurs
    */
    public void write( char[] array ) throws IOException
    {
        m_writer.write( array );
    }
    
   /**
    * Write a value to the buffer following a nl character.
    * @param value the value to write to the buffer
    * @exception IOException if an IO error occurs
    */
    public void nl( String value ) throws IOException
    {
        m_writer.write( "\n" + m_pad + value );
    }
    
   /**
    * Write an int value to the buffer following a nl character.
    * @param n the int value to write to the buffer
    * @exception IOException if an IO error occurs
    */
    public void nl( int n ) throws IOException
    {
        m_writer.write( "\n" + m_pad + n );
    }
    
   /**
    * Write an character array value to the buffer following a nl character.
    * @param array the array value to write to the buffer
    * @exception IOException if an IO error occurs
    */
    public void nl( char[] array ) throws IOException
    {
        m_writer.write( "\n" + m_pad );
        m_writer.write( array );
    }
    
   /**
    * Indent the current offset value by 2 space characters.
    * @return a buffer
    */
    public Buffer indent()
    {
        return indent( "  " );
    }
    
   /**
    * Indent the current offset value by a supplied value.
    * @param indent the indent value
    * @return a new buffer
    */
    public Buffer indent( String indent )
    {
        return new Buffer( m_writer, m_namespace, m_pad + indent );
    }
    
   /**
    * Test id the supplied namespace is the current namesapce.
    * @param namespace to namespace to compare with the current namespace
    * @return true if the namesapce is current
    */
    public boolean isNamespace( String namespace )
    {
        return m_namespace.equals( namespace );
    }
    
   /**
    * Creation of a new buffer mapped to the supplied namespace.
    * @param namespace the namespace
    * @return a buffer
    */
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
