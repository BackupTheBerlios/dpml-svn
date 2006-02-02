/*
 * Copyright 2006 Stephen McConnell.
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
package net.dpml.web.server;

import net.dpml.transit.util.PropertyResolver;

/**
 * Context handler with enhanced support for symbolic property dereferencing. 
 */
public class ContextHandler extends org.mortbay.jetty.handler.ContextHandler implements Comparable
{
    private int m_priority = 0;
    
   /**
    * Set the context reosurce base.  The supplied path argument
    * may contain system property references as symbolic references in the 
    * form ${key} which will be expanded prior to value assignment.
    *
    * @param path the base resource as a string
    */
    public void setResourceBase( String path ) 
    {
        String resolved = PropertyResolver.resolve( path );
        super.setResourceBase( resolved );
    }
    
   /**
    * Set the handler priority.
    * @param priority the priority value
    */
    public void setPriority( int priority )
    {
        m_priority = priority;
    }
    
   /**
    * Return the relative ordering of this object relative to the supplied object.
    * If the supplied object is a ContextHandler evaluation will be based on the 
    * associated priority otherwise evaluation will be based on a default 0 priority
    * implied on the supplied object.
    *
    * @param other the other object
    * @return the comparative value
    */
    public int compareTo( Object other )
    {
        if( other instanceof ContextHandler )
        {
            ContextHandler handler = (ContextHandler) other;
            int priority = handler.m_priority;
            return compare( m_priority, priority );
        }
        else
        {
            return compare( m_priority, 0 );
        }
    }
    
    int compare( int a, int b )
    {
        if( a < b )
        {
            return -1;
        }
        else if( a == b )
        {
            return 0;
        }
        else
        {
            return 1;
        }
    }
}
