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
import net.dpml.lang.Encoder;

/**
 * Utility used to build a plugin strategy from a DOM element.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PartStrategyEncoder implements Encoder
{
    private static final ValueEncoder VALUE_ENCODER = new ValueEncoder();
    
   /**
    * Externalize a object to XML.
    * @param writer the output stream writer
    * @param object the object to externalize
    * @param pad the character offset
    * @exception IOException if an IO error occurs
    */
    public void encode( Writer writer, Object object, String pad ) throws IOException
    {
        if( object instanceof Strategy )
        {
            encodeStrategy( writer, (Strategy) object, pad );
        }
        else
        {
            final String error = 
              "Supplied object argument ["
              + object.getClass().getName() 
              + "] is not recognized.";
            throw new IllegalArgumentException( error );
        }
    }
    
   /**
    * Externalize a strategy.
    * @param writer the output stream writer
    * @param strategy the strategy
    * @exception IOException if an IO error occurs
    */
    //public void write( Writer writer, Strategy strategy ) throws IOException
    //{
    //    encodeStrategy( writer, strategy, "" );
    //}
    
   /**
    * Externalize a strategy.
    * @param writer the output stream writer
    * @param strategy the strategy
    * @param pad the character offset
    * @exception IOException if an IO error occurs
    */
    public void encodeStrategy( Writer writer, Strategy strategy, String pad ) throws IOException
    {
        Object data = strategy.getDeploymentData();
        if( data instanceof Plugin )
        {
            Plugin plugin = (Plugin) data;
            String classname = plugin.getClassname();
            writer.write( "\n  <strategy xsi:type=\"plugin\" class=\"" );
            writer.write( classname );
            writer.write( "\"" );
            if( plugin.getValues().length > 0 )
            {
                Value[] values = plugin.getValues();
                VALUE_ENCODER.encodeValues( writer, values, "    " );
            }
            else
            {
                writer.write( "/>" );
            }
        }
        else if( data instanceof Resource )
        {
            Resource resource = (Resource) data;
            String urn = resource.getURN();
            String path = resource.getPath();
            writer.write( "\n  <strategy xsi:type=\"resource\"" );
            writer.write( " urn=\"" + urn );
            writer.write( "\" path=\"" + path );
            writer.write( "\"/>" );
        }
        else
        {
            final String error = 
              "Unsupported strategy datatype: " 
              + data.getClass().getName();
            throw new IOException( error );
        }
    }
}
