/*
 * Copyright 2004-2005 Stephen J. McConnell.
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

package net.dpml.composition.data;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import net.dpml.part.Part;
import net.dpml.part.Directive;

import net.dpml.transit.model.Value;
import net.dpml.transit.model.Construct;

/**
 * A <code>ValueDirective</code> represents a single constructed argument value. The directive
 * holds a classname (default value of <code>java.lang.String</code>) and possible sub-directives.
 * The directives value is established by creating a new instance using the classname
 * together with the values directived from the sub-sidiary directives as constructor arguments.
 *
 * <p><b>XML</b></p>
 * <p>A value is a nested structure containing a string value or contructor parameter arguments.</p>
 * <pre>
 *    <font color="gray">&lt;-- Simple string param declaration --&gt;</font>
 *
 *    &lt;value&gt;<font color="darkred">London</font>&lt;/value&gt;
 *
 *    <font color="gray">&lt;-- Typed value declaration --&gt;</font>
 *
 *    &lt;value class="<font color="darkred">java.io.File</font>"&gt;<font color="darkred">./home</font>&lt;/value&gt;
 *
 *    <font color="gray">&lt;-- Multi-argument parameter declaration --&gt;</font>
 *
 *    &lt;value class="<font color="darkred">MyClass</font>"&gt;
 *       &lt;value class="<font color="darkred">java.io.File</font>"><font color="darkred">./home</font>&lt;/value&gt;
 *       &lt;value&gt;<font color="darkred">London</font>&lt;/value&gt;
 *    &lt;/value&gt;
 * </pre>
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: Parameter.java 2119 2005-03-23 02:04:46Z mcconnell@dpml.net $
 */
public class ValueDirective extends Construct implements Directive
{
    //--------------------------------------------------------------------------
    // static
    //--------------------------------------------------------------------------

   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;
    
    //--------------------------------------------------------------------------
    // constructors
    //--------------------------------------------------------------------------

   /**
    * Create a new construct using the default java.lang.String class as the base type.
    * @param value the construct value
    */
    public ValueDirective( String value )
    {
        super( value );
    }

   /**
    * Create a new construct using a supplied target defintion.  The target argument 
    * may be either a classname or a symbolic reference in the form ${[key]}.  If the 
    * argument is symbolic it resolved relative to a context map supplied by the 
    * application resolving construct values.
    *
    * @param target a classname or symbolic reference
    * @param value the construct value
    */
    public ValueDirective( String target, String value )
    {
        super( target, value );
    }

   /**
    * Create a new construct using a supplied target defintion.  The target argument 
    * may be either a classname or a symbolic reference in the form ${[key]}.  If the 
    * argument is symbolic it is resolved relative to a context map supplied by the 
    * application resolving construct values.  If the construct value is symbolic
    * the implementation will attempt to expand the reference relative to a context
    * map (if supplied) otherwise the implementation will attempt to expand the value 
    * using system properties.
    *
    * @param target a classname or symbolic reference
    * @param method the method to invoke on the target
    * @param value the construct value
    */
    public ValueDirective( String target, String method, String value )
    {
        super( target, method, value );
    }

   /**
    * Create a new construct using a supplied target defintion.  The target argument 
    * may be either a classname or a symbolic reference in the form ${[key]}.  If the 
    * argument is symbolic it is resolved relative to a context map supplied by the 
    * application resolving construct values. Instance values resolved from the 
    * supplied Value[] will be used as constructor arguments when resolving the target.
    *
    * @param target the construct classname
    * @param args an array of unresolved parameter values
    */
    public ValueDirective( String target, Value[] args )
    {
        super( target, args );
    }
    
   /**
    * Create a new construct using a supplied target defintion.  The target argument 
    * may be either a classname or a symbolic reference in the form ${[key]}.  If the 
    * argument is symbolic it is resolved relative to a context map supplied by the 
    * application resolving construct values. Instance values resolved from the 
    * supplied Value[] will be used as method arguments when resolving the target.
    *
    * @param target the construct classname
    * @param method the method to invoke on the target
    * @param args an array of unresolved parameter values
    */
    public ValueDirective( String target, String method, Value[] args )
    {
        super( target, method, args );
    }
    
    //--------------------------------------------------------------------------
    // ValueDirective
    //--------------------------------------------------------------------------

    public boolean equals( Object other )
    {
        boolean equals = super.equals( other );
        if( null == other )
        {
            return false;
        }
        if( ! ( other instanceof ValueDirective ) )
        {
            return false;
        }
        else
        {
            ValueDirective directive = (ValueDirective) other;
            if( !getPartHandlerURI().equals( directive.getPartHandlerURI() ) )
            {
                return false;
            }
            return super.equals( other );
        }
    }
    
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= getPartHandlerURI().hashCode();
        return hash;
    }
    
    //--------------------------------------------------------------------------
    // Part
    //--------------------------------------------------------------------------

   /**
    * Return the part handler uri.
    * @return the uri of the part handler
    */
    public URI getPartHandlerURI()
    {
        return PART_HANDLER_URI;
    }

    private static URI PART_HANDLER_URI = setupURI( "@PART-HANDLER-URI@" );

    protected static URI setupURI( String spec )
    {
        try
        {
            return new URI( spec );
        }
        catch( URISyntaxException ioe )
        {
            return null;
        }
    }
}
