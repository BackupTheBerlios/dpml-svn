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

package net.dpml.metro.data;

import java.io.IOException;
import java.io.Writer;

import net.dpml.component.Controller;
import net.dpml.component.Composition;

import net.dpml.lang.Classpath;
import net.dpml.lang.Info;

import net.dpml.metro.builder.ComponentEncoder;

import net.dpml.util.Logger;


/**
 * Component composition.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class DefaultComposition extends Composition
{   
    private ComponentDirective m_directive;
    
    private final Classpath m_classpath;
    
   /**
    * Creation of a new composition diefinition.
    * @param logger the assigned logging channel
    * @param info the part info definition
    * @param classpath the part classpath definition
    * @param controller the deployment controller
    * @param directive the deployment directive
    * @exception IOException if an I/O exception occurs
    */
    public DefaultComposition( 
      Logger logger, Info info, Classpath classpath, Controller controller, ComponentDirective directive )
      throws IOException
    {
        super( logger, info, classpath, controller, directive, directive.getName() );
        
        m_directive = directive;
        
        if( null != m_directive.getBaseDirective() )
        {
            // override the classpath defintion with a extended definition
            
            DefaultComposition composition = m_directive.getBasePart();
            Classpath base = composition.getClasspath();
            m_classpath = new Classpath( base, classpath );
        }
        else
        {
            m_classpath = null;
        }
    }
    
   /**
    * Get the deployment directive.
    * @return the deployment directive
    */
    public ComponentDirective getComponentDirective()
    {
        return m_directive;
    }
    
   /**
    * Get the part classpath definition.
    *
    * @return the classpath definition
    */
    public Classpath getClasspath()
    {
        if( null != m_classpath )
        {
            return m_classpath;
        }
        else
        {
            return super.getClasspath();
        }
    }

   /**
    * Encode the deployment directive to XML.
    * @param writer the output stream writer
    * @param pad the outoput offset
    * @exception IOException if an I/O exception occurs
    */
    protected void encodeStrategy( Writer writer, String pad ) throws IOException
    {
        ComponentEncoder encoder = new ComponentEncoder();
        encoder.writeComponent( writer, m_directive, pad );
    }
    
   /**
    * Return true if this object is equal to the supplied object.
    * @param other the object to evaluate
    * @return the equality status
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof DefaultComposition ) )
        {
            DefaultComposition composite = (DefaultComposition) other;
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
