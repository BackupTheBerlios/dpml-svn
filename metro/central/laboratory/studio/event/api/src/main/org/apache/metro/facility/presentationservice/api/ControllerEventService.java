/*

   Copyright 2004. The Apache Software Foundation.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License. 

*/
package org.apache.metro.facility.presentationservice.api;

/**
 *
 * @author  default
 * @version 
 */
public interface ControllerEventService extends EventService {            

    /** Notifies all registered listeners about the event.
     *
     * @param e The event to be fired
     */
    public void modelChanged(ChannelEvent event) 
        throws ChannelException;  
    
    public void addControlClickedListener(String topic, 
            ChannelListener listener)
    	throws ChannelException;
    
    public void addWindowCreatedListener(String topic, 
            ChannelListener listener)
    	throws ChannelException;

}
