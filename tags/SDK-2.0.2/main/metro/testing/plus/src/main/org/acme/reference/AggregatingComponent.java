/*
 * Copyright 2005 Stephen McConnell
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

package org.acme.reference;

import net.dpml.annotation.Component;

import static net.dpml.annotation.LifestylePolicy.SINGLETON;

import org.acme.Hello;

/**
 * Sample component used in testing aggregation by reference.  Specifically, the 
 * component direxctive contains a reference to a hello component using a uri.  The
 * container has to reach out and build a part under this component (involving the 
 * creation of a classloader sub-hierachy) and bind the result component to the 
 * internal parts of this component.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
@Component( lifestyle=SINGLETON )
public class AggregatingComponent
{
    private Parts m_parts;
    
   /**
    * Internal parts contact.
    */
    public interface Parts
    {
       /**
        * Get named component.
        * @return a service
        */
        Hello getHello();
    }
    
   /**
    * Component constructor.
    * @param parts the parts solution
    */
    public AggregatingComponent( Parts parts )
    {
        m_parts = parts;
    }
    
   /**
    * Get a message.
    * @return a message
    */
    public String getMessage()
    {
        Hello hello = m_parts.getHello();
        return hello.getMessage();
    }
}
