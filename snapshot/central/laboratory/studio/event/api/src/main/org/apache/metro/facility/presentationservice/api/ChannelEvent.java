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

import java.util.Hashtable;

/**
 *
 * @author  default
 * @version 
 */
public class ChannelEvent extends java.util.EventObject {

    /** Holds value of property newTemperature. */
    private String topic = null;
    /** Holds value of property oldTemperature. */
    private Hashtable data = null;
    
    /** Creates new TemperatureChangeEvent */
    public ChannelEvent(Object source, String topic, Hashtable data){
        super(source);
        this.topic = topic;
        this.data = data;
    }
    
    /** 
     * @return topic of the event
     */
    public String getTopic() {
        return topic;
    }
    
    /** 
     */
    public Hashtable getData(){
        return data;
    }
    
    public void putValue(String key, String value)
    {
        if(value==null)
        {
            value="";
        }
        data.put(key, value);
    }
    
    public void putValueArray(String key, String[] value)
    {
        data.put(key, value);
    }

    /**
     * @param event
     */
    public void setTopic(String topic)
    {
        this.topic = topic;
        
    }

    /**
     * @param string
     * @return
     */
    public String getValue(String string)
    {
        return (String)data.get(string);
    }

    /**
     * @param string
     * @return
     */
    public String[] getValueArray(String string)
    {
        return (String[])data.get(string);
    }
}
