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
 * Exception raised to veto a disposal request.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class VetoDisposalException extends RuntimeException
{
     private Object m_source;

    /**
     * Construct a new <code>VetoDisposalException</code> instance.
     *
     * @param source the object initiating the veto
     */
    public VetoDisposalException( Object source )
    {
        super( "Disposal veto." );
    }

    /**
     * Construct a new <code>VetoDisposalException</code> instance.
     *
     * @param source the object initiating the veto
     * @param message the reason for the veto
     */
    public VetoDisposalException( Object source, String message )
    {
        super( message );
    }

   /**
    * Return the object rasing the veto.
    * @return the veto source
    */
    public Object getSource()
    {
        return m_source;
    }
}

