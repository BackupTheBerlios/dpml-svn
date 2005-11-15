/*
 * Copyright 2004 Stephen J. McConnell.
 * Copyright 2004 Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dpml.configuration;

/**
 * This interface should be implemented by classes that need to be
 * configured with custom parameters before initialization.
 * <br/>
 *
 * The contract surrounding a <code>Configurable</code> is that the
 * instantiating entity must call the <code>configure</code>
 * method before it is valid. 
 * <br/>
 *
 * Note that this interface is incompatible with Parameterizable.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: Configurable.java 259 2004-10-30 07:24:40Z mcconnell $
 */
public interface Configurable
{
    /**
     * Pass the <code>Configuration</code> to the <code>Configurable</code>
     * class. 
     *
     * @param configuration the class configurations. Must not be <code>null</code>.
     * @throws ConfigurationException if an error occurs
     */
    void configure( Configuration configuration )
        throws ConfigurationException;
}
