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

package org.apache.avalon.composition.event;

import org.apache.avalon.composition.model.DeploymentModel;
import org.apache.avalon.composition.model.ContainmentModel;


/**
 * A event raised by a containment model as a result of the 
 * addition or removal of a subsidiary model.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: CompositionEvent.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class CompositionEvent extends java.util.EventObject
{
    /**
     * The model added or removed from the containment model.
     */
    private final DeploymentModel m_child;

    /**
     * Create a CompositionEvent event.
     *
     * @param source the comtainment model raising the event
     * @param child the model that is the subject of composition
     */
    public CompositionEvent( final ContainmentModel source, DeploymentModel child )
    {
        super( source );
        m_child = child;
    }

    /**
     * Return the child that was added or removed from the containment 
     * model.
     *
     * @return the source containment model
     */
    public DeploymentModel getChild()
    {
        return m_child;
    }

    /**
     * Return the the containment model that generated the event.
     *
     * @return the source containment model
     */
    public ContainmentModel getContainmentModel()
    {
        return (ContainmentModel) getSource();
    }
    
    public String toString()
    {
        return "composition-event: [source: " 
          + getContainmentModel() 
          + ", child: " 
          + getChild() 
          + "]";
    } 
}
