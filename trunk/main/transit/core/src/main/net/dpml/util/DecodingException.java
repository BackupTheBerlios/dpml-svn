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

package net.dpml.util;

import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Exception related to data decoding from a DOM element.
 */
public class DecodingException extends IOException
{
    private final Element m_element;
    
   /**
    * Create a new decoding exception.
    * @param element the element representing the source of the error
    * @param message the exception message
    */
    public DecodingException( Element element, String message )
    {
        this( element, message, null );
    }
    
   /**
    * Create a new decoding exception.
    * @param element the element representing the source of the error
    * @param message the exception message
    * @param cause the causal exception
    */
    public DecodingException( Element element, String message, Throwable cause )
    {
        super( message );
        if( null != cause )
        {
            super.initCause( cause );
        }
        m_element = element;
    }
    
   /**
    * Get the element that is the subject of this exception.
    * @return the subject element
    */
    public Element getElement()
    {
        return m_element;
    }
    
   /**
    * Return a string representation of the exception.
    * @return the string value
    */
    public String getMessage()
    {
        try
        {
            String message = super.getMessage();
            StringBuffer buffer = new StringBuffer( message );
            Element element = getElement();
            String tag = element.getTagName();
            buffer.append( "\nElement: <" );
            buffer.append( tag );
            buffer.append( " ...>" );
            Document document = element.getOwnerDocument();
            String uri = document.getDocumentURI();
            if( null != uri )
            {
                buffer.append( "\nDocument: " + uri );
            }
            return buffer.toString();
        }
        catch( Throwable e )
        {
            return super.getMessage();
        }
    }
}
