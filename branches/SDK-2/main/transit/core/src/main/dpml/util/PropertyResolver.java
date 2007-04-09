/*
 * Copyright 2006-2007 Stephen McConnell
 * Copyright 2004 Niclas Hedhman
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dpml.util;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Stack;
import java.util.StringTokenizer;

import net.dpml.transit.Transit;

/**
 * Utility class that handles substitution of property names in the string
 * for ${value} relative to a supplied set of properties.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class PropertyResolver
{
    // ------------------------------------------------------------------------
    // static
    // ------------------------------------------------------------------------
    
    static
    {
        Object s = Transit.DATA;
    }

   /**
    * System property symbol substitution from properties.
    * Replace any occurances of ${[key]} with the value of the property
    * assigned to the [key] in the system properties or supplied properties.
    * @param base base property defintion
    * @param properties an arbitary properties file containing unresolved properties
    * @return the property file with expended properties
    */
    public static Properties resolve( Properties base, Properties properties )
    {
        Properties props = new Properties();
        Enumeration names = properties.propertyNames();
        while( names.hasMoreElements() )
        {
            String name = (String) names.nextElement();
            String old = properties.getProperty( name );
            String value = resolve( base, old );
            String v2 = resolve( properties, value );
            props.setProperty( name, v2 );
        }
        return props;
    }

   /**
    * Symbol substitution from properties.
    * Replace any occurances of ${[key]} with the value of the property
    * assigned to the [key] in the supplied properties argument.
    * @param props the source properties from which substitution is resolved
    * @param value a string containing possibly multiple ${[value]} sequences
    * @return the expanded string
    */
    public static String resolve( Properties props, String value )
    {
        if( value == null )
        {
            return null;
        }
        
        // optimization for common case.
        if( value.indexOf( '$' ) < 0 )
        {
            return value;
        }
        int pos1 = value.indexOf( "${" );
        if( pos1 < 0 )
        {
            return value;
        }
        
        Stack<String> stack = new Stack<String>();
        StringTokenizer st = new StringTokenizer( value, "${}", true );

        while ( st.hasMoreTokens() )
        {
            String token = st.nextToken();
            if( token.equals( "}" ) )
            {
                String name = (String) stack.pop();
                String open = (String) stack.pop();
                if( open.equals( "$${" ) )
                {
                    push( stack, "${" + name + "}" );
                }
                else if( open.equals( "${" ) )
                {
                    String propValue = System.getProperty( name );
                    if( ( propValue == null ) && ( null != props ) )
                    {
                        propValue = props.getProperty( name );
                    }
                    if( propValue == null )
                    {
                        push( stack, "${" + name + "}" );
                    }
                    else
                    {
                        push( stack, propValue );
                    }
                }
                else
                {
                    push( stack, "${" + name + "}" );
                }
            }
            else
            {
                if( token.equals( "$" ) )
                {
                    String peek = peek( stack );
                    if( "$".equals( peek ) )
                    {
                        stack.push( "$$" );
                    }
                    else
                    {
                        stack.push( "$" );
                    }
                }
                else
                {
                    push( stack, token );
                }
            }
        }
        String result = "";
        while ( stack.size() > 0 )
        {
            result = stack.pop() + result;
        }
        return result;
    }
    
    private static String peek( Stack<String> stack )
    {
        try
        {
            return stack.peek();
        }
        catch( Exception e )
        {
            return null;
        }
    }
    
   /**
    * Pushes a value on a stack
    * @param stack the stack
    * @param value the value
    */
    private static void push( Stack<String> stack , String value )
    {
        if( stack.size() > 0 )
        {
            String data = (String) stack.pop();
            if( data.equals( "$" ) && !"{".equals( value ) )
            {
                stack.push( value );
            }
            else if( data.equals( "${" ) )
            {
                stack.push( data );
                stack.push( value );
            }
            else if( data.equals( "$${" ) )
            {
                stack.push( value );
            }
            else
            {
                stack.push( data + value );
            }
        }
        else
        {
            stack.push( value );
        }
    }

    // ------------------------------------------------------------------------
    // constructor
    // ------------------------------------------------------------------------

   /**
    * Null constructor.
    */
    private PropertyResolver()
    {
    }
}

