/*
 * Copyright 1997-2003 The Apache Software Foundation
 * Copyright 2004 Stephen J. McConnell.
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
package net.dpml.activity;

/**
 * The Startable interface is used when components need to
 * be "running" to be active. It provides a method through
 * which components can be "started" and "stopped" without
 * requiring a thread.
 * Note that these methods should start the component but return
 * imediately.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: Startable.java 2103 2005-03-21 16:44:54Z mcconnell@dpml.net $
 */
public interface Startable
{
    /**
     * Starts the component.
     *
     * @throws Exception if Component can not be started
     */
    void start()
        throws Exception;

    /**
     * Stops the component.
     *
     * @throws Exception if the Component can not be Stopped.
     */
    void stop()
        throws Exception;
}
