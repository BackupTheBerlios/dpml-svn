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

package org.apache.avalon.util.defaults ;


import java.util.Properties ;


/**
 * Attempts to discover defaults using an array of Properties as value sources.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: SimpleDefaultsFinder.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class SimpleDefaultsFinder implements DefaultsFinder
{
    /** Properties array to use for discovery */
    private Properties [] m_sources ;
    /** halt on first finding flag */
    private boolean m_haltOnDiscovery = true ;
    
    
    /**
     * Creates a simple defaults finder that searches a single source Properties
     * instance for default values.
     * 
     * @param source single source Properties to discover values in
     */
    public SimpleDefaultsFinder( Properties source )
    {
        m_sources = new Properties [] { source } ;
        m_haltOnDiscovery = false ;
    }
    
    
    /**
     * Creates a simple defaults filder that searches a set of source Properties
     * for default values.
     * 
     * @param sources the source Properties to discover values in
     * @param haltOnDiscovery true to halt search when first value is 
     * discovered, false to continue search overriding values until the last 
     * value is discovered.
     */
    public SimpleDefaultsFinder( Properties [] sources, 
                                   boolean haltOnDiscovery )
    {
        m_sources = sources ;
        m_haltOnDiscovery = haltOnDiscovery ;
    }
    
    
    /**
     * Applies default discovery using properties in array of properties.
     * 
     * @see org.apache.avalon.util.defaults.DefaultsFinder#find(
     * org.apache.avalon.util.defaults.Defaults)
     */
    public void find( Defaults a_defaults )
    {
        Defaults.discover( a_defaults, m_sources, m_haltOnDiscovery ) ;
    }
}
