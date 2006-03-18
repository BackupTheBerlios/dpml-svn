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

package net.dpml.part;

import java.io.Serializable;
import java.net.URI;

/**
 * Part deployment strategy description.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class Strategy
{
    private final PartDirective m_controller;
    private final Serializable m_data;
    private final URI m_builder;
    private final boolean m_alias;
    
   /**
    * Creation of a new part instantiation strategy.
    * @param builder the builder uri
    * @param controller the runtime controller that will handle part deployment
    * @param data the data to tbe supplied to the controller
    */ 
    public Strategy( URI builder, PartDirective controller, Serializable data )
    {
        this( builder, controller, data, false );
    }
    
   /**
    * Creation of a new part instantiation strategy.
    * @param builder the builder uri
    * @param controller the runtime controller that will handle part deployment
    * @param data the data to tbe supplied to the controller
    * @param alias the alias flag
    */ 
    public Strategy( URI builder, PartDirective controller, Serializable data, boolean alias )
    {
        if( null == controller )
        {
            throw new NullPointerException( "controller" );
        }
        if( null == data )
        {
            throw new NullPointerException( "data" );
        }
        if( null == builder )
        {
            throw new NullPointerException( "builder" );
        }
        m_builder = builder;
        m_controller = controller;
        m_data = data;
        m_alias = alias;
    }
    
   /**
    * Return the datatype id.
    * @return the constant 'part' type identifier
    */
    public String getID()
    {
        return "part";
    }
    
   /**
    * Return the datatype id.
    * @return the alias flag value
    */
    public boolean getAlias()
    {
        return m_alias;
    }
    
   /**
    * Get the strategy builder uri.
    * @return the builder uri
    */
    public URI getBuilderURI()
    {
        return m_builder;
    }
    
   /**
    * Get the controller deployment directive.
    * @return the deployment controller uri
    */
    public PartDirective getPartDirective()
    {
        return m_controller;
    }
    
   /**
    * Get the deployment data.
    * @return the deployment datastructure
    */
    public Object getDeploymentData()
    {
        return m_data;
    }
    
   /**
    * Test if this instance is equal to the supplied instance.
    * @param other the other instance
    * @return the equality status
    */
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else if( other instanceof Strategy )
        {
            Strategy strategy = (Strategy) other;
            if( !m_builder.equals( strategy.m_builder ) )
            {
                return false;
            }
            else if( !m_controller.equals( strategy.m_controller ) )
            {
                return false;
            }
            else
            {
                return m_data.equals( strategy.m_data );
            }
        }
        else
        {
            return false;
        }
    }
    
   /**
    * Get the hashcode for this instance.
    * @return the hash value
    */
    public int hashCode()
    {
        int hash = m_builder.hashCode();
        hash ^= m_controller.hashCode();
        hash ^= m_data.hashCode();
        return hash;
    }
}
