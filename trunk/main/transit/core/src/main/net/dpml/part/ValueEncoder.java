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

package net.dpml.part;

import java.io.Writer;
import java.io.IOException;

import net.dpml.lang.Value;
import net.dpml.lang.Construct;

/**
 * Utility used to write value instances to an output stream as XML.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ValueEncoder
{
   /**
    * Write an array of values to XML.
    * @param writer the output stream writer
    * @param values the value array
    * @param pad the character offset
    * @exception IOException if an IO error occurs
    */
    public void encodeValues( Writer writer, Value[] values, String pad ) throws IOException
    {
        for( int i=0; i<values.length; i++ )
        {
            Value value = values[i];
            encodeValue( writer, value, pad );
        }
    }
    
    
   /**
    * Write a value to XML.
    * @param writer the output stream writer
    * @param value the value
    * @param pad the character offset
    * @exception IOException if an IO error occurs
    */
    public void encodeValue( Writer writer, Value value, String pad ) throws IOException
    {
        if( value instanceof Construct )
        {
            Construct construct = (Construct) value;
            String method = construct.getMethodName();
            String target = construct.getTargetExpression();
            
            writer.write( "\n" + pad + "<param" );
            if( null != target )
            {
                writer.write( " class=\"" + target + "\"" );
            }
            if( null != method )
            {
                writer.write( " method=\"" + method  + "\"" );
            }
            if( construct.isCompound() )
            {
                writer.write( ">" );
                Value[] values = construct.getValues();
                encodeValues( writer, values, pad + "  " );
                writer.write( "\n" + pad + "</param>" );
            }
            else
            {
                String v = construct.getBaseValue();
                if( null != v )
                {
                    writer.write( " value=\"" + v  + "\"" );
                }
                writer.write( "/>" );
            }
        }
        else
        {
            final String error = 
              "Value class [" 
              + value.getClass().getName()
              + "] not supported.";
            throw new IOException( error );
        }
    }
}
