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

import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletMapping;

/**
 * A ServletHandler maintains a collection of servlets and a collection
 * servlet name to context mappings.
 */
public class ServletHandler extends org.mortbay.jetty.servlet.ServletHandler
{
    public ServletHandler( ServletHolder[] servlets, ServletMapping[] maps )
    {
        super();
        
        setServlets( servlets );
        setServletMappings( maps );
    }
}
