/*
 * Copyright 2005-2007 Stephen J. McConnell.
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

package dpml.util;

import java.util.ArrayList;

/**
 * CLI hander for the depot package.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class CLIHelper
{
    private CLIHelper()
    {
        // static utility
    }
    
   /**
    * Utility operation to consolidate a supplied array of command line arguments 
    * by removal of the supplied argument.
    * 
    * @param args the command line arguments to consolidate
    * @param argument the argument to remove
    * @return the consolidated argument array
    */
    public static String[] consolidate( String [] args, String argument )
    {
        return consolidate( args, argument, 0 );
    }

   /**
    * Utility operation to consolidate a supplied array of command line arguments 
    * by removal of the supplied argument and the subsequent parameter values. 
    * 
    * @param args the command line arguments to consolidate
    * @param argument the argument to remove
    * @param n the number of parameters to consolidate
    * @return the consolidated argument array
    */
    public static String[] consolidate( String [] args, String argument, int n )
    {
        boolean flag = false;
        ArrayList<String> list = new ArrayList<String>();
        for( int i=0; i < args.length; i++ )
        {
            String arg = args[i];
            if( flag )
            {
                list.add( arg );
            }
            else
            {
                if( arg.equals( argument ) )
                {
                    flag = true;
                    i = i+n;
                }
                else
                {
                    list.add( arg );
                }
            }
        }
        return list.toArray( new String[0] );
    }

   /**
    * Test is the supplied option is present in the set of supplied command line 
    * arguments.
    *
    * @param args the set of command line arguments to test against
    * @param flag the command line option to test for
    * @return TRUE if one of the command line options matching the supplied falg argument
    */
    public static boolean isOptionPresent( String[] args, String flag )
    {
        for( int i=0; i < args.length; i++ )
        {
            String arg = args[i];
            if( arg.equals( flag ) )
            {
                return true;
            }
        }
        return false;
    }

   /**
    * Return a command line argument immediately following an option.
    * @param args an array of command line arguments
    * @param option the command line option used as the key to locate the option value
    * @return the option argument value
    */
    public static String getOption( String[] args, String option )
    {
        for( int i=0; i < args.length; i++ )
        {
            String arg = args[i];
            if( arg.equals( option ) )
            {
                try
                {
                    return args[i+1];
                }
                catch( IndexOutOfBoundsException e )
                {
                    final String error = 
                      "Requestion option ["
                      + option
                      + "] is not followed by an argument value.";
                    throw new IllegalArgumentException( error );
                }
            }
        }
        final String error = 
          "Option does not exist within the supplied commandline.";
        throw new IllegalArgumentException( error );
    }
}

