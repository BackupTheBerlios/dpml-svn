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

package net.dpml.part;

import java.util.EventObject;

/**
 * Event triggered as a result of a model change.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ModelEvent extends EventObject
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    private final String m_feature;
    private final Object m_from;
    private final Object m_to;

   /**
    * Construct a new <code>ModelEvent</code>.
    *
    * @param source the source component model
    * @param feature the name of the model feature
    * @param from the original value
    * @param to the new value
    */
    public ModelEvent( final Model source, String feature, Object from, Object to )
    {
        super( source );
        
        m_feature = feature;
        m_from = from;
        m_to = to;
    }

   /**
    * Return the feature name.
    * @return the name of the modified feature
    */
    public String getFeature()
    {
        return m_feature;
    }

   /**
    * Return the old value.
    * @return the original value
    */
    public Object getOldValue()
    {
        return m_from;
    }

   /**
    * Return the new value.
    * @return the new current value
    */
    public Object getNewValue()
    {
        return m_to;
    }
    
   /**
    * Return the component model that initiating the event.
    * @return the source model
    */
    public Model getSourceModel()
    {
        return (Model) super.getSource();
    }
}

