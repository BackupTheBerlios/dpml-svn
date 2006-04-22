/*
 * Copyright (c) 2005 Stephen J. McConnell
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

package net.dpml.metro.data;

import java.io.IOException;

import net.dpml.lang.Classpath;
import net.dpml.lang.Value;
import net.dpml.lang.Info;
import net.dpml.lang.Plugin;

import net.dpml.util.Logger;

/**
 * A component based plugin.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class Composite extends Plugin 
{
    private final ComponentDirective m_directive;
    
   /**
    * Creation of a new composite instance.
    * @param logger the assigned logging channel
    * @param info the info descriptor
    * @param classpath the classpath descriptor
    * @param directive the deployment directive
    * @exception IOException if an I/O error occurs
    */
    public Composite( 
      Logger logger, Info info, Classpath classpath, ComponentDirective directive )
      throws IOException
    {
        super( logger, info, classpath, directive.getClassname(), new Value[0] );
        
        m_directive = directive;
    }
    
   /**
    * Get the deplyment directive.
    * @return the deployment directive
    */
    public ComponentDirective getComponentDirective()
    {
        return m_directive;
    }
    
   /**
    * Return true if this object is equal to the supplied object.
    * @return the equality status
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof Composite ) )
        {
            Composite composite = (Composite) other;
            return m_directive.equals( composite.m_directive );
        }
        else
        {
            return false;
        }
    }
    
   /**
    * Return the hashcode for the instance.
    * @return the instance hashcode
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= m_directive.hashCode();
        return hash;
    }
}

