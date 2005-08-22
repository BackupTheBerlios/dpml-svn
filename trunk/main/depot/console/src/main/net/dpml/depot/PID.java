/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.depot;

import java.io.Serializable;

import java.util.Random;

/**
 * The PID class is a process identifer. 
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Id: Main.java 2480 2005-05-10 04:44:32Z mcconnell@dpml.net $
 */
public class PID implements Serializable
{
    private static final int ID = getInitialValue();

    private static int getInitialValue()
    {
        String id = System.getProperty( "process.id" );
        if( null == id )
        {
            return Math.abs( new Random().nextInt() );
        }
        else
        {
            try
            {
                return Integer.parseInt( id );
            }
            catch( NumberFormatException e )
            {
                return Math.abs( new Random().nextInt() );
            }
        }
    }

    public int getValue()
    {
        return ID;
    }

    public String toString()
    {
        return "[" + ID + "]";
    }

    public boolean equals( Object other )
    {
       if( other instanceof PID )
       {
           PID pid = (PID) other;
           return getValue() == pid.getValue();
       }
       else
       {
           return false;
       }
    }

    public int hashCode()
    {
        return getValue();
    }
}

