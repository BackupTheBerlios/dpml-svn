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

import java.io.Reader;
import java.io.InputStream;

import org.w3c.dom.ls.LSInput;

/**
 * This Class <code>DOMInput</code> represents a single input source for an XML entity.
 * <p> This Class allows an application to encapsulate information about
 * an input source in a single object, which may include a public
 * identifier, a system identifier, a byte stream (possibly with a specified
 * encoding), and/or a character stream.
 * <p> The exact definitions of a byte stream and a character stream are
 * binding dependent.
 * <p> There are two places that the application will deliver this input
 * source to the parser: as the argument to the <code>parse</code> method,
 * or as the return value of the <code>DOMResourceResolver.resolveEntity</code>
 *  method.
 * <p> The <code>DOMParser</code> will use the <code>LSInput</code>
 * object to determine how to read XML input. If there is a character stream
 * available, the parser will read that stream directly; if not, the parser
 * will use a byte stream, if available; if neither a character stream nor a
 * byte stream is available, the parser will attempt to open a URI
 * connection to the resource identified by the system identifier.
 * <p> An <code>LSInput</code> object belongs to the application: the
 * parser shall never modify it in any way (it may modify a copy if
 * necessary).  Eventhough all attributes in this interface are writable the
 * DOM implementation is expected to never mutate a LSInput.
 * <p>See also the <a href='http://www.w3.org/TR/2001/WD-DOM-Level-3-ASLS-20011025'>Document Object Model (DOM) Level 3 Abstract Schemas and Load
and Save Specification</a>.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DOMInput implements LSInput 
{
    private String m_publicId = null;
    private String m_systemId = null;
    private String m_base = null;
    private InputStream m_byteStream = null;
    private Reader m_reader    = null;
    private String m_data = null;
    private String m_encoding = null;
    private boolean m_certified = false;

  /**
    * Default constructor.
    */
    public DOMInput()
    {
    }

   /**
    * An attribute of a language-binding dependent type that represents a
    * stream of bytes.
    * <br>The parser will ignore this if there is also a character stream
    * specified, but it will use a byte stream in preference to opening a
    * URI connection itself.
    * <br>If the application knows the character encoding of the byte stream,
    * it should set the encoding property. Setting the encoding in this way
    * will override any encoding specified in the XML declaration itself.
    */
    public InputStream getByteStream()
    {
        return m_byteStream;
    }

    /**
    * An attribute of a language-binding dependent type that represents a
    * stream of bytes.
    * <br>The parser will ignore this if there is also a character stream
    * specified, but it will use a byte stream in preference to opening a
    * URI connection itself.
    * <br>If the application knows the character encoding of the byte stream,
    * it should set the encoding property. Setting the encoding in this way
    * will override any encoding specified in the XML declaration itself.
    */
    public void setByteStream( InputStream byteStream )
    {
        m_byteStream = byteStream;
    }

    /**
    *  An attribute of a language-binding dependent type that represents a
    * stream of 16-bit units. Application must encode the stream using
    * UTF-16 (defined in  and Amendment 1 of ).
    * <br>If a character stream is specified, the parser will ignore any byte
    * stream and will not attempt to open a URI connection to the system
    * identifier.
    */
    public Reader getCharacterStream()
    {
        return m_reader;
    }
    /**
    *  An attribute of a language-binding dependent type that represents a
    * stream of 16-bit units. Application must encode the stream using
    * UTF-16 (defined in  and Amendment 1 of ).
    * <br>If a character stream is specified, the parser will ignore any byte
    * stream and will not attempt to open a URI connection to the system
    * identifier.
    */
    public void setCharacterStream( Reader characterStream )
    {
        m_reader = characterStream;
    }

    /**
    * A string attribute that represents a sequence of 16 bit units (utf-16
    * encoded characters).
    * <br>If string data is available in the input source, the parser will
    * ignore the character stream and the byte stream and will not attempt
    * to open a URI connection to the system identifier.
    */
    public String getStringData()
    {
        return m_data;
    }

   /**
    * A string attribute that represents a sequence of 16 bit units (utf-16
    * encoded characters).
    * <br>If string data is available in the input source, the parser will
    * ignore the character stream and the byte stream and will not attempt
    * to open a URI connection to the system identifier.
    */
    public void setStringData( String stringData )
    {
        m_data = stringData;
    }

    /**
    *  The character encoding, if known. The encoding must be a string
    * acceptable for an XML encoding declaration ( section 4.3.3 "Character
    * Encoding in Entities").
    * <br>This attribute has no effect when the application provides a
    * character stream. For other sources of input, an encoding specified
    * by means of this attribute will override any encoding specified in
    * the XML claration or the Text Declaration, or an encoding obtained
    * from a higher level protocol, such as HTTP .
    */
    public String getEncoding()
    {
        return m_encoding;
    }

    /**
    *  The character encoding, if known. The encoding must be a string
    * acceptable for an XML encoding declaration ( section 4.3.3 "Character
    * Encoding in Entities").
    * <br>This attribute has no effect when the application provides a
    * character stream. For other sources of input, an encoding specified
    * by means of this attribute will override any encoding specified in
    * the XML claration or the Text Declaration, or an encoding obtained
    * from a higher level protocol, such as HTTP .
    */
    public void setEncoding( String encoding )
    {
        m_encoding = encoding;
    }

    /**
    * The public identifier for this input source. The public identifier is
    * always optional: if the application writer includes one, it will be
    * provided as part of the location information.
    */
    public String getPublicId()
    {
        return m_publicId;
    }
    /**
    * The public identifier for this input source. The public identifier is
    * always optional: if the application writer includes one, it will be
    * provided as part of the location information.
    */
    public void setPublicId( String publicId )
    {
        m_publicId = publicId;
    }

    /**
    * The system identifier, a URI reference , for this input source. The
    * system identifier is optional if there is a byte stream or a
    * character stream, but it is still useful to provide one, since the
    * application can use it to resolve relative URIs and can include it in
    * error messages and warnings (the parser will attempt to fetch the
    * ressource identifier by the URI reference only if there is no byte
    * stream or character stream specified).
    * <br>If the application knows the character encoding of the object
    * pointed to by the system identifier, it can register the encoding by
    * setting the encoding attribute.
    * <br>If the system ID is a relative URI reference (see section 5 in ),
    * the behavior is implementation dependent.
    */
    public String getSystemId()
    {
        return m_systemId;
    }
    /**
    * The system identifier, a URI reference , for this input source. The
    * system identifier is optional if there is a byte stream or a
    * character stream, but it is still useful to provide one, since the
    * application can use it to resolve relative URIs and can include it in
    * error messages and warnings (the parser will attempt to fetch the
    * ressource identifier by the URI reference only if there is no byte
    * stream or character stream specified).
    * <br>If the application knows the character encoding of the object
    * pointed to by the system identifier, it can register the encoding by
    * setting the encoding attribute.
    * <br>If the system ID is a relative URI reference (see section 5 in ),
    * the behavior is implementation dependent.
    */
    public void setSystemId( String systemId )
    {
        m_systemId = systemId;
    }

   /**
    *  The base URI to be used (see section 5.1.4 in ) for resolving relative
    * URIs to absolute URIs. If the baseURI is itself a relative URI, the
    * behavior is implementation dependent.
    */
    public String getBaseURI()
    {
        return m_base;
    }
    
   /**
    *  The base URI to be used (see section 5.1.4 in ) for resolving relative
    * URIs to absolute URIs. If the baseURI is itself a relative URI, the
    * behavior is implementation dependent.
    */
    public void setBaseURI( String baseURI )
    {
        m_base = baseURI;
    }

   /**
    *  If set to true, assume that the input is certified (see section 2.13
    * in [<a href='http://www.w3.org/TR/2002/CR-xml11-20021015/'>XML 1.1</a>]) when
    * parsing [<a href='http://www.w3.org/TR/2002/CR-xml11-20021015/'>XML 1.1</a>].
    */
    public boolean getCertifiedText()
    {
        return m_certified;
    }

   /**
    *  If set to true, assume that the input is certified (see section 2.13
    * in [<a href='http://www.w3.org/TR/2002/CR-xml11-20021015/'>XML 1.1</a>]) when
    * parsing [<a href='http://www.w3.org/TR/2002/CR-xml11-20021015/'>XML 1.1</a>].
    */
    public void setCertifiedText( boolean certifiedText )
    {
        m_certified = certifiedText;
    }
}
