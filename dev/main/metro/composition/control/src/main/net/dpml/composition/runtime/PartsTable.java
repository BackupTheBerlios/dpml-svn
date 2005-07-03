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
import java.util.Map;
import java.util.Hashtable;

import net.dpml.part.control.DelegationException;
import net.dpml.part.control.HandlerNotFoundException;
import net.dpml.part.manager.DuplicateKeyException;
import net.dpml.part.manager.Component;
import net.dpml.part.manager.ComponentException;
import net.dpml.part.part.Part;

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

    public void addPart( String key, Part part ) 
      throws ComponentException, DelegationException, HandlerNotFoundException
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
              "Illegal attempt to add a part reference to a parts collection."
              + "\nComponent: " + m_component.getURI()
              + "\nKey: " + key;
            throw new ComponentException( error );
        }
        synchronized( m_parts )
        {
            CompositionController controller = m_component.getController();
            Component component = controller.newComponent( m_component, part, key );
            addComponent( key, component );
        }
    }

    public void addComponent( String key, Component component ) throws DuplicateKeyException
    {
        synchronized( m_parts )
        {
            if( m_parts.containsKey( key ) )
            {
                throw new DuplicateKeyException( key );
            }
            m_parts.put( key, component );
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
}
