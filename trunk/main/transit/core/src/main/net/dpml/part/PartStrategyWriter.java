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
import java.util.Map;

import net.dpml.lang.Value;

/**
 * Utility used to build a plugin strategy from a DOM element.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PartStrategyWriter extends ValueBuilder
{
   /**
    * Creation of a new part strategy writer.
    * @param map the map of namespace to builder uris
    */
    public PartStrategyWriter( Map map )
    {
        super( map );
    }
    
   /**
    * Externalize a strategy.
    * @param writer the output stream writer
    * @param strategy the strategy
    * @exception IOException if an IO error occurs
    */
    public void write( Writer writer, Strategy strategy ) throws IOException
    {
        writeStrategy( writer, strategy, "" );
    }
    
   /**
    * Externalize a strategy.
    * @param writer the output stream writer
    * @param strategy the strategy
    * @param pad the character offset
    * @exception IOException if an IO error occurs
    */
    public void writeStrategy( Writer writer, Strategy strategy, String pad ) throws IOException
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
                writeValues( writer, values, "    " );
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
