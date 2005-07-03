/*
 * Copyright 2004 Niclas Hedman.
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

import org.mortbay.http.HttpHandler;

/**
 * Interface implemented by servlet handlers.
 * @metro.service type="net.dpml.http.ServletHandler" version="1.0"
 */
public interface ServletHandler extends HttpHandler
{
    /** 
     * Adds the contextObject into the ServletContext object.
     * @param entryName the name of the entry
     * @param contextObject the object to add to the servlet context under
     *   the entry name
     */
    void addServletContextEntry( String entryName, Object contextObject );
}
