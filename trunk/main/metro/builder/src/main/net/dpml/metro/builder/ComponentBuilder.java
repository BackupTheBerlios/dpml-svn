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

package net.dpml.metro.builder;

import java.io.IOException;
import java.io.Writer;

import net.dpml.lang.Decoder;
import net.dpml.lang.DecodingException;
import net.dpml.lang.Encoder;

import net.dpml.part.DecoderFactory;

import org.w3c.dom.Element;

/**
 * Component strategy builder.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ComponentBuilder extends ComponentConstants implements Decoder, Encoder
{
    private final ComponentStrategyDecoder m_strategyDecoder;
    private final ComponentStrategyEncoder m_strategyEncoder;
    
   /**
    * Creation of a new component builder.
    * @param factory the decoder factory
    */
    public ComponentBuilder( DecoderFactory factory )
    {
        m_strategyDecoder = new ComponentStrategyDecoder( factory );
        m_strategyEncoder = new ComponentStrategyEncoder();
    }
    
   /**
    * Constructs a component deployment strategy.
    * @param classloader the base classloader
    * @param element the DOM element
    * @return the deployment strategy
    * @exception DecodingException if an error occurs during element evaluation
    */
    public Object decode( ClassLoader classloader, Element element ) throws DecodingException
    {
        return m_strategyDecoder.decode( classloader, element );
    }
    
   /** 
    * Export a component directive to an output stream as XML.
    * @param writer the print writer
    * @param object the object to encode
    * @param pad character offset
    * @exception IOException if an IO error occurs
    */
    public void encode( Writer writer, Object object, String pad ) throws IOException
    {
        m_strategyEncoder.encode( writer, object, pad );
    }
}
