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

/**
 * Exception raised when an attempt is made to remove a model 
 * that is referenced by another model.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class ModelReferenceException extends ModelException
{
     private final Model m_consumer;
     private final Model m_reference; 

    /**
     * Construct a new <code>ModelReferenceException</code> instance.
     *
     * @param consumer the model that references the reference model
     * @param reference the model referenced by the consumer
     */
    public ModelReferenceException( final Model consumer, final Model reference )
    {
        super( "Referential integrity error." );
        m_consumer = consumer;
        m_reference = reference;
    }

    public Model getConsumerModel()
    {
        return m_consumer;
    }

    public Model getReferencedModel()
    {
        return m_reference;
    }
}

