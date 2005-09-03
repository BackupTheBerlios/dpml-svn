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

package net.dpml.transit;

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
    private final int m_id;

   /**
    * Creation of a new process identifier.
    */
    public PID()
    {
        m_id = setupInitialValue();
    }

   /**
    * Creation of a new process identifier.
    * @param pid the process identity
    */
    public PID( int pid )
    {
        m_id = pid;
    }

    private static int setupInitialValue()
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

   /**
    * Return the process identifier int value.
    * @return the process id value
    */
    public int getValue()
    {
        return m_id;
    }

   /**
    * Return the string representation of this process identifier.
    * @return the process identifier as a string
    */
    public String toString()
    {
        return "[" + m_id + "]";
    }

   /**
    * Test if a supplied object is equal to this process identifier.
    * @param other the object to compare with this object
    * @return TRUE if the objects are equivalent
    */
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

   /**
    * Return the hashcode for this PID instance.
    * @return the hashcode value
    */
    public int hashCode()
    {
        return getValue();
    }
}

