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

package org.apache.avalon.composition.info;

/**
 * Descriptor used to mark a lifecycle artifact as non-deliverable.  This 
 * is used in the special case of a component that is not itself context 
 * dependent, but is dependent on a lifecycle stage extension handler which 
 * is dependent on the component context.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: NullDeliveryDescriptor.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class NullDeliveryDescriptor extends DeliveryDescriptor
{
    public NullDeliveryDescriptor( Class casting )
    {
        super( casting );
    }
}
