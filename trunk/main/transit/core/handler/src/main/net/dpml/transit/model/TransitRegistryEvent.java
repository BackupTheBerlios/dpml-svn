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

package net.dpml.transit.model;

import java.util.EventObject;

/**
 * An event issued by a Tranist registry signalling addition or removal
 * of Transit profiles from or to a Transit model registry.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class TransitRegistryEvent extends EventObject
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private final TransitModel m_model;

   /**
    * Creation of a new TransitRegistryEvent signalling the addition
    * or removal of a transit model.
    * 
    * @param registry the Transit model registry
    * @param model the model that was added or removed
    */   
    public TransitRegistryEvent( TransitRegistryModel registry, TransitModel model )
    {
        super( registry );
        m_model = model;
    }
    
   /**
    * Return the model initiating the event.
    * @return the transit registry model
    */
    public TransitRegistryModel getTransitRegistryModel()
    {
        return (TransitRegistryModel) getSource();
    }

   /**
    * Return the resolver model that was added or removed.
    * @return the transit model
    */
    public TransitModel getTransitModel()
    {
        return m_model;
    }
}

