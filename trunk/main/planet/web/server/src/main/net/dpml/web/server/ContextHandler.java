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
 * HTTP server implementation.
 */
public class ContextHandler extends org.mortbay.jetty.handler.ContextHandler
{
    /**
     * @param resourceBase The base resource as a string.
     */
    public void setResourceBase( String resourceBase ) 
    {
        String resolved = PropertyResolver.resolve( resourceBase );
        System.out.println( "## RESOURCE BASE: " + resolved );
        super.setResourceBase( resolved );
    }

}
