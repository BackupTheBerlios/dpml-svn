/*
 * Copyright 2007 Stephen J. McConnell.
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

package net.dpml.lang;

import dpml.util.StandardClassLoader;

/**
 * Exception raised when class cannot be cast to a requested type.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class TypeCastException extends ClassCastException
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;
    
    private final Class m_base;
    private final Class m_type;
    
    /**
     * Construct a new <code>TypeCastException</code> instance.
     *
     * @param message the message
     * @param base the base class against which a failed cast was attempted
     * @param type the class used as the cast criteria
     */
    public TypeCastException( final String message, Class base, Class type )
    {
        super( message );
        m_base = base;
        m_type = type;
    }
    
    public Class getBase()
    {
        return m_base;
    }
    
    public Class getType()
    {
        return m_type;
    }
    
    public String getReport()
    {
        return StandardClassLoader.toString( m_base, m_type );
    }
}

