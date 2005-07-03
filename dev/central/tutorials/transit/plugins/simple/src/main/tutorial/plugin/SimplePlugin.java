/*
 * Copyright 2004 Stephen J. McConnell.
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

package tutorial.plugin;

/**
 * A really simple plugin.
 *
 * @author <a href="mailto:dev@dpmlnet">Stephen J. McConnell</a>
 */
public class SimplePlugin
{
    // ------------------------------------------------------------------------
    // constructors
    // ------------------------------------------------------------------------

   /**
    * Creation of a new SimplePlugin instance.
    *
    * @param message the message to output to System.out
    */
    public SimplePlugin( String[] messages )
    {
        if( messages.length == 0 )
        {
            System.out.println( "Nothing to say." );
        }
        for( int i=0; i<messages.length; i++ )
        {
            System.out.println( messages[i] );
        }
    }
}

