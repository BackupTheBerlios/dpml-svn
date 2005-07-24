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
 * An event signalling a model disposal.
 */
public class DisposalEvent extends EventObject 
{
   /**
    * Creation of a new disposal event.
    * @param model the model initiating the event
    */
    public DisposalEvent( Model model )
    {
        super( model );
    }

   /**
    * Return the source of the disposalo event.
    * @return the source model
    */
    public Model getModel()
    {
        return (Model) getSource();
    }
}
