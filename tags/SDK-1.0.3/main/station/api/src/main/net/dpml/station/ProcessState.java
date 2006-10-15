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

package net.dpml.station;

import net.dpml.lang.ValuedEnum;

/**
 * Lifestyle policy enumeration.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ProcessState extends ValuedEnum implements Comparable
{
    static final long serialVersionUID = 1L;

   /**
    * Idle state.
    */
    public static final ProcessState IDLE = new ProcessState( "idle", 0 );

   /**
    * Starting state.
    */
    public static final ProcessState STARTING = new ProcessState( "starting", 1 );

   /**
    * Started state.
    */
    public static final ProcessState STARTED = new ProcessState( "started", 2 );

   /**
    * Started state.
    */
    public static final ProcessState STOPPING = new ProcessState( "stopping", 3 );

   /**
    * Started state.
    */
    public static final ProcessState STOPPED = new ProcessState( "stopped", 3 );

   /**
    * Array of state enumeration values.
    */
    private static final ProcessState[] ENUM_VALUES = 
      new ProcessState[]{IDLE, STARTING, STARTED, STOPPING, STOPPED};

   /**
    * Returns an array of activation enum values.
    * @return the activation policies array
    */
    public static ProcessState[] values()
    {
        return ENUM_VALUES;
    }
    
   /**
    * Internal constructor.
    * @param label the enumeration label.
    * @param index the enumeration index.
    */
    private ProcessState( String label, int index )
    {
        super( label, index );
    }
    
   /**
    * Return a string representation of the state.
    * @return the string value
    */
    public String toString()
    {
        return getName().toUpperCase();
    }
    
   /**
    * Return a state value matching the supplied value.
    * @param value the state name
    * @return the state
    * @exception IllegalArgumentException if the name if not recognized
    */
    public static ProcessState parse( String value ) throws IllegalArgumentException
    {
        if( value.equalsIgnoreCase( "idle" ) )
        {
            return IDLE;
        }
        else if( value.equalsIgnoreCase( "starting" ) )
        {
            return STARTING;
        }
        else if( value.equalsIgnoreCase( "started" ) )
        {
            return STARTED;
        }
        else if( value.equalsIgnoreCase( "stopping" ) )
        {
            return STOPPING;
        }
        else if( value.equalsIgnoreCase( "stopped" ) )
        {
            return STOPPED;
        }
        else
        {
            final String error =
              "Unrecognized state argument [" + value + "]";
            throw new IllegalArgumentException( error );
        }
    }
}

