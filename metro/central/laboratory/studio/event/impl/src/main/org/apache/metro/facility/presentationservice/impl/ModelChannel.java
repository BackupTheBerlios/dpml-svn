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
package org.apache.metro.facility.presentationservice.impl;

import java.util.Hashtable;

import org.apache.metro.facility.presentationservice.api.ChannelEvent;
import org.apache.metro.facility.presentationservice.api.ChannelException;
import org.apache.metro.facility.presentationservice.api.IModelChannel;
import org.apache.metro.facility.presentationservice.api.ViewEventService;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Metro Development Team</a>
 * 21.08.2004
 * last change:
 * 
 */
public class ModelChannel implements IModelChannel
{

    private String name;
    private ViewEventService service;
    private ChannelEvent channelEvent;
    /**
     * 
     */
    public ModelChannel(String name)
    {
        super();
		try
        {
	        this.name = name;
            Hashtable values = new Hashtable();	        
            channelEvent = new ChannelEvent(this, null, values);
            service = PresentationServiceFactory.getViewEventService();
            
        } catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public void windowCreated(String event) throws ChannelException
    {
        channelEvent.setTopic(event);
        channelEvent = service.windowCreated(channelEvent);
    }

    public void controlClicked(String event) throws ChannelException
    {
        channelEvent.setTopic(event);
        channelEvent = service.controlClicked(channelEvent);
    }

    public String getValue(String event)
    {
        String value =(String)channelEvent.getData().get(event); 
        if(null==value)
        {
            return "";
        }
        return value;
    }

    public String[] getValueArray(String event)
    {
        String[] value =(String[])channelEvent.getData().get(event); 
        if(null==value)
        {
            return new String[1];
        }
        return value;
    }

    public void putValue(String key, String value)
    {
        if(value == null)
        {
            value = "";
        }
        channelEvent.getData().put(key, value);
    }

    public void putValueArray(String key, String[] value)
    {
        channelEvent.getData().put(key, value);
    }

}
