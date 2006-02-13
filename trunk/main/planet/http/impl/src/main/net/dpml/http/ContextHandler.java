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
package net.dpml.http;

import net.dpml.transit.util.PropertyResolver;

import net.dpml.logging.Logger;

import org.mortbay.jetty.Handler;

/**
 * Context handler with enhanced support for symbolic property dereferencing. 
 */
public class ContextHandler extends ResolvingContextHandler implements Comparable
{
   /**
    * HTTP Context handler context defintion.
    */
    public interface Context
    {
       /**
        * Get the http context resource base.  The value may contain symbolic
        * property references and should resolve to a local directory.
        *
        * @return the resource base
        */
        String getResourceBase();
        
       /**
        * Get the context path under which the http context instance will 
        * be associated.
        *
        * @return the assigned context path
        */
        String getContextPath();
        
       /**
        * Get the internal context handler's handler.
        *
        * @return the handler
        */
        Handler getHandler();
        
    }
    
    private int m_priority = 0;
    
    public ContextHandler( Server server, Context context ) throws Exception
    {
        String base = context.getResourceBase();
        super.setResourceBase( base );
        String path = context.getContextPath();
        super.setContextPath( path );
        Handler handler = context.getHandler();
        super.setHandler( handler );
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
