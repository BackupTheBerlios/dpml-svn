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

package net.dpml.runtime;

import net.dpml.lang.DecodingException;

import dpml.util.ElementHelper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Component related exception.
 */
public class ComponentException extends DecodingException
{    
   /**
    * Create a new decoding exception.
    * @param message the exception message
    */
    public ComponentException( String message )
    {
        this( message, null );
    }
    
   /**
    * Create a new decoding exception.
    * @param message the exception message
    * @param cause the causal exception
    */
    public ComponentException( String message, Throwable cause )
    {
        this( message, cause, null );
    }
    
   /**
    * Create a new decoding exception.
    * @param message the exception message
    * @param cause the causal exception
    * @param element the element representing the source definition
    */
    public ComponentException( String message, Throwable cause, Element element )
    {
        super( message, cause, element );
    }
}
