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

package org.apache.avalon.test.playground.basic;

/**
 * The <code>BasicService</code> executes a prime objective.
 *
 * @avalon.service version="1.1"
 * @avalon.attribute key="urn:avalon:service.name" value="basic"
 * @avalon.attribute key="urn:avalon:service.description" 
 *   value="A demonstration service used within the scope of the Avalon playground package for educational and unit testing purpose"
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public interface BasicService
{

    /**
     * Execute the prime objective of this services.
     */
    void doPrimeObjective();
}
