/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.avalon.composition.data;

import java.io.Serializable;

/**
 * The <code>Mode</code> class declares the EXPLICIT, PACKAGED or IMPLICIT mode of creation of a profile.
 *
 * @see DeploymentProfile
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: Mode.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class Mode implements Serializable
{
    /**
     * Constant indicating that the profile was implicitly created.
     */
    public static final int IMPLICIT_VALUE = 0;

    /**
     * Constant indicating that the profile was created based on a profile packaged with the type.
     */
    public static final int PACKAGED_VALUE = 1;

    /**
     * Constant indicating that the profile was explicitly declared under an assembly directive.
     */
    public static final int EXPLICIT_VALUE = 2;

    /**
     * Constant indicating that the profile was implicitly created.
     */
    public static final Mode IMPLICIT = new Mode( IMPLICIT_VALUE );

    /**
     * Constant indicating that the profile was created based on a profile packaged with the type.
     */
    public static final Mode PACKAGED = new Mode( PACKAGED_VALUE );

    /**
     * Constant indicating that the profile was explicitly declared under an assembly directive.
     */
    public static final Mode EXPLICIT = new Mode( EXPLICIT_VALUE );

   /**
    * Returns a string representation of a mode value.
    * @param mode the mode value
    * @return the string representation
    */
    public static String modeToString( int mode )
    {
        if( mode == IMPLICIT_VALUE )
        {
            return "IMPLICIT";
        }
        else if( mode == PACKAGED_VALUE )
        {
            return "PACKAGED";
        }
        else if( mode == EXPLICIT_VALUE )
        {
            return "EXPLICIT";
        }
        else
        {
            return "?";
        }
    }

    /**
     * The creation mode.
     */
    private final int m_mode;

   /**
    * Creation of a new mode value.
    * @param mode the int value of the mode.
    */
    public Mode( int mode )
    {
        m_mode = mode;
    }

    /**
     * The supplied argument.
     * @return the mode value
     */
    public int getValue()
    {
        return m_mode;
    }

   /**
    * Return a string representatio of the mode.
    * @return String the string value
    */
    public String toString()
    {
        return modeToString( getValue() );
    }

   /**
    * Compare a supplied object for equality.
    * @param object the other object
    * @return TRUE if the supplied mode is equivalent to this mode.
    */
    public boolean equals( Object object )
    {
        if( object instanceof Mode )
        {
            return ((Mode)object).getValue() == getValue();
        }
        return false;
    }
}
