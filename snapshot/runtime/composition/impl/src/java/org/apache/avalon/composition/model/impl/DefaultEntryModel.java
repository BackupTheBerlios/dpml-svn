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

package org.apache.avalon.composition.model.impl;

import org.apache.avalon.composition.model.EntryModel;
import org.apache.avalon.composition.model.ModelException;

import org.apache.avalon.meta.info.EntryDescriptor;

/**
 * Abstract implementation of a the context entry model.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultEntryModel.java 30977 2004-07-30 08:57:54Z niclas $
 */
public abstract class DefaultEntryModel implements EntryModel
{
    //==============================================================
    // immmutable state
    //==============================================================

    private EntryDescriptor m_descriptor;

    //==============================================================
    // constructor
    //==============================================================

   /**
    * Creation of a new context entry import model.
    *
    * @param descriptor the context entry descriptor
    */
    public DefaultEntryModel( EntryDescriptor descriptor )
    {
        if( descriptor == null )
        {
            throw new NullPointerException( "descriptor" );
        }
        m_descriptor = descriptor;
    }

    //==============================================================
    // EntryModel
    //==============================================================

   /**
    * Return the context entry key.
    * 
    * @return the key
    */
    public String getKey()
    {
        return m_descriptor.getKey();
    }

   /**
    * Return the context entry value.
    * 
    * @return the context entry value
    */
    public abstract Object getValue() throws ModelException;

}
