/*
 * Copyright 2004-2006 Stephen J. McConnell.
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

package org.acme;

import java.awt.Color;
import java.util.Map;
import java.util.SortedMap;

import net.dpml.util.Logger;

/**
 * A component containing variations on a theme related to the 
 * usage of Map return types in a context definition.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class MapWidget implements Widget
{
   /**
    * A context interface with examples of map dreturn type declarations.
    */
    public interface Context
    {
       /**
        * Return a map with string keys and values using the 'primary' key.
        * @return the map
        */
        Map<String,String> getPrimary();
        
       /**
        * Return a map with string keys and values using the 'secondary' key
        * with an explicit map specialization as a return type. 
        * @return the map
        */
        SortedMap<String,String> getSecondary();
        
       /**
        * Return a map with string keys and complex object values using the 'colors' key. 
        * @return the map
        */
        Map<String,Color> getColors();
    }
    
    private Context m_context;
    
   /**
    * Creation of the test component.
    * @param logger the assigned logging channel
    * @param context the deployment context
    */
    public MapWidget( Logger logger, Context context )
    {
        m_context = context;
        logger.info( "" + getMessage() );
    }
    
   /**
    * Get a message.
    * @return a message
    */
    public String getMessage()
    {
        return m_context.getPrimary().get( "message" );
    }
    
   /**
    * Get the supplied context so that testcase can do its stuff.
    * @return the constructor supplied context.
    */
    public Context getContext()
    {
        return m_context;
    }
    
   /**
    * Test the supplied object for equality with this object.
    * @param other the supplied object 
    * @return the equality result
    */
    public boolean equals( Object other )
    {
        return ( hashCode() == other.hashCode() );
    }

   /**
    * Get the component hashcode.
    * @return the hash value
    */
    public int hashCode()
    {
        return m_context.hashCode();
    }
}
