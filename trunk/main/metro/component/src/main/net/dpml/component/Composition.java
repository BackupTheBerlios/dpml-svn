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

package net.dpml.component;

import java.io.IOException;
import java.net.URI;

import net.dpml.lang.Classpath;
import net.dpml.lang.Info;
import net.dpml.lang.Part;

import net.dpml.util.Logger;

/**
 * Component composition.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class Composition extends Part
{
    private final Directive m_directive;
    private final Controller m_controller;
    
    private Model m_model;
    
   /**
    * Creation of a new abstract composition instance.
    * @param logger the assigned logging channel
    * @param info the part info descriptor
    * @param classpath the part classpath definition
    * @param controller the part controller
    * @param directive the part deployment strategy directive
    * @exception IOException if an I/O error occurs
    */
    public Composition( Logger logger, Info info, Classpath classpath, Controller controller, Directive directive )
      throws IOException
    {
        super( logger, info, classpath );
        
        if( null == directive )
        {
            throw new NullPointerException( "directive" );
        }
        m_directive = directive;
        m_controller = controller;
    }

   /**
    * Return the part content or null if the result type is unresolvable 
    * relative to the supplied class argument. Class types recognized over and 
    * above the Part class include Directive, Model, Component and Controller.
    *
    * @param c the content class
    * @return the content
    * @exception IOException if an IO error occurs
    */
    protected Object getContent( Class c ) throws IOException
    {
        if( Directive.class.isAssignableFrom( c ) )
        {
            return m_directive;
        }
        else if( Model.class.isAssignableFrom( c ) )
        {
            return getModel();
        }
        else if( Component.class.isAssignableFrom( c ) )
        {
            return newComponent();
        }
        else if( Controller.class.isAssignableFrom( c ) )
        {
            return m_controller;
        }
        else
        {
            return super.getContent( c );
        }
    }
    
   /**
    * Get the deployment directive.
    * @return the deployment directive
    */
    public Directive getDirective()
    {
        return m_directive;
    }
    
   /**
    * Get the deployment model.
    * @return the deployment model
    */
    public Model getModel()
    {
        if( null == m_model )
        {
            try
            {
                m_model = m_controller.createModel( this );
            }
            catch( Throwable e )
            {
                URI uri = m_controller.getURI();
                final String error = 
                  "Unexpected error while attempting to create a component model."
                  + "\nDirective: " + m_directive;
                throw new ControlRuntimeException( uri, error, e );
            }
        }
        return m_model;
    }
    
   /**
    * Create and return a new component using the deplyment model established by the part.
    * @return the component
    */
    public Component newComponent()
    {
        Model model = getModel();
        try
        {
            return m_controller.createComponent( model );
        }
        catch( Throwable e )
        {
            URI uri = m_controller.getURI();
            final String error = 
              "Unexpected error while attempting to create a component."
              + "\nDirective: " + m_directive;
            throw new ControlRuntimeException( uri, error, e );
        }
    }
    
   /**
    * Instantiate a value.
    * @param args supplimentary arguments
    * @return the resolved instance
    * @exception Exception if a deployment error occurs
    */
    public Object instantiate( Object[] args ) throws Exception
    {
        Component component = newComponent();
        return component.getProvider().getValue( true );
    }
    
   /**
    * Return true if this object is equal to the supplied object.
    * @return the equality status
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof Composition ) )
        {
            Composition composite = (Composition) other;
            if( !m_directive.equals( composite.m_directive ) )
            {
                return false;
            }
            else
            {
                if( null == m_controller )
                {
                    return null == composite.m_controller;
                }
                else
                {
                    return m_controller.equals( composite.m_controller );
                }
            }
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
        if( null != m_controller )
        {
            hash ^= m_controller.hashCode();
        }
        return hash;
    }
}
