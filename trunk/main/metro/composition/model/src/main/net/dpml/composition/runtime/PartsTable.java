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

package net.dpml.composition.runtime;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Hashtable;

import net.dpml.part.DelegationException;
import net.dpml.part.PartHandlerNotFoundException;
import net.dpml.part.control.DuplicateKeyException;
import net.dpml.part.control.Component;
import net.dpml.part.control.ComponentException;
import net.dpml.part.service.ServiceDescriptor;
import net.dpml.part.Part;

import net.dpml.composition.data.ReferenceDirective;
import net.dpml.composition.control.CompositionController;

/**
 * The parts table contains the subsidiary parts of an enclosing component.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class PartsTable
{
    private final ComponentHandler m_component;

    private final Hashtable m_parts = new Hashtable(); // map of Component instances keyed by part key

   /**
    * Creation of a new parts table.
    * 
    * @param component the enclosing component
    */
    public PartsTable( ComponentHandler component )
    {
        m_component = component;
    }

    public Component addComponent( String key, Part part ) 
      throws ComponentException, DelegationException, PartHandlerNotFoundException
    {
        if( null == key )
        {
            throw new NullPointerException( "key" );
        }
        if( null == part )
        {
            throw new NullPointerException( "part" );
        }
        if( part instanceof ReferenceDirective )
        {
            final String error = 
              "Illegal attempt to add a part reference to a parts collection "
              + "(parts within a component are strongly aggregated and may be associated by reference)."
              + "\nComponent: " + m_component.getURI()
              + "\nKey: " + key;
            throw new ComponentException( error );
        }
        synchronized( m_parts )
        {
            CompositionController controller = m_component.getController();
            Component component = controller.newComponent( m_component, part, key );
            return addComponent( key, component );
        }
    }

    public Component addComponent( String key, Component component ) throws DuplicateKeyException
    {
        synchronized( m_parts )
        {
            if( m_parts.containsKey( key ) )
            {
                throw new DuplicateKeyException( key );
            }
            m_parts.put( key, component );
            return component;
        }
    }

    public boolean containsKey( String key )
    {
        if( null == key )
        {
            throw new NullPointerException( "key" );
        }
        return m_parts.containsKey( key );
    }

    public Component getComponent( String key )
    {
        return (Component) m_parts.get( key );
    }

    Component[] getComponents()
    {
        synchronized( m_parts )
        {
            return (Component[]) m_parts.values().toArray( new Component[0] );
        }
    }

    Component[] getComponents( ServiceDescriptor spec )
    {
        synchronized( m_parts )
        {
            ArrayList list = new ArrayList();
            Component[] components = getComponents();
            for( int i=0; i<components.length; i++ )
            {
                 Component component = components[i];
                 ServiceDescriptor[] services = component.getDescriptors();
                 for( int j=0; j<services.length; j++ )
                 {
                     ServiceDescriptor service = services[j];
                     if( service.matches( spec ) )
                     { 
                         list.add( component );
                         break;
                     }
                 }
             }
             return (Component[]) list.toArray( new Component[0] );
         }
     }
}