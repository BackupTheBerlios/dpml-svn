/*
Copyright 2004 The Apache Software Foundation
Licensed  under the  Apache License,  Version 2.0  (the "License");
you may not use  this file  except in  compliance with the License.
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed  under the  License is distributed on an "AS IS" BASIS,
WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
implied.

See the License for the specific language governing permissions and
limitations under the License.
*/

package org.apache.avalon.tools.model;

import java.util.Properties;
import java.util.Stack;
import java.util.StringTokenizer;


public class PropertyResolver
{
    static public String resolve( Properties props, String value )
    {
        if( value == null )
            return null;
            
        // optimization for common case.
        if( value.indexOf( '$' ) < 0 )
            return value;
        int pos1 = value.indexOf( "${" );
        if( pos1 < 0 )
            return value;
        
        Stack stack = new Stack();
        StringTokenizer st = new StringTokenizer( value, "${}", true );
        
        while( st.hasMoreTokens() )
        {
            String token = st.nextToken();
            if( token.equals( "}" ) )
            {
                String name = (String) stack.pop();
                String open = (String) stack.pop();
                if( open.equals( "${" ) )
                {
                    String propValue = props.getProperty( name );
                    if( propValue == null )
                        push( stack, "${" + name + "}" );
                    else
                        push( stack, propValue );
                }
                else
                {
                    push( stack, "${" + name + "}" );
                }
            }
            else
            {
                if( token.equals( "$" ) )
                    stack.push( "$" );
                else
                {
                    push( stack, token );
                }
            }
        }
        String result = "";
        while( stack.size() > 0 )
        {
            result = (String) stack.pop() + result;
        }
        return result;
    }
    
    static private void push( Stack stack , String value )
    {
        if( stack.size() > 0 )
        {
            String data = (String) stack.pop();
            if( data.equals( "${" ) )
            {
                stack.push( data );
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
}
 
