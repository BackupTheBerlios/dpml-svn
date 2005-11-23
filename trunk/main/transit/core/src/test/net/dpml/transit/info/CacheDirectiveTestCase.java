/*
 * Copyright 2005 Stephen J. McConnell
 *
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

package net.dpml.transit.info;

import java.util.Arrays;

/**
 * Testing the CodeBaseDirective class.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class CacheDirectiveTestCase extends AbstractTestCase
{
    private String m_cache;
    private String m_layout;
    private String m_local;
    private LayoutDirective[] m_layouts;
    private HostDirective[] m_hosts;
    private ContentDirective[] m_content;
    private CacheDirective m_directive;
    
    public void setUp() throws Exception
    {
        m_cache = "${dpml.data}/cache";
        m_layout = "classic";
        m_local = "${dpml.share}/local";
        m_layouts = new LayoutDirective[0];
        m_hosts = new HostDirective[0];
        m_content = new ContentDirective[0];
        m_directive = 
          new CacheDirective( 
           m_cache, m_local, m_layout, m_layouts, m_hosts, m_content );
    }
    
    public void testCache() throws Exception
    {
        assertEquals( "cache", m_cache, m_directive.getCache() );
    }
    
    public void testLayout() throws Exception
    {
        assertEquals( "layout", m_layout, m_directive.getLayout() );
    }
    
    public void testLocal() throws Exception
    {
        assertEquals( "local", m_local, m_directive.getLocal() );
    }
    
    public void testLayouts() throws Exception
    {
        assertEquals( "layouts", m_layouts, m_directive.getLayoutDirectives() );
    }
    
    public void testHosts() throws Exception
    {
        assertEquals( "hosts", m_hosts, m_directive.getHostDirectives() );
    }
    
    public void testContent() throws Exception
    {
        assertEquals( "content", m_content, m_directive.getContentDirectives() );
    }
    
    public void testSerialization() throws Exception
    {
        doSerializationTest( m_directive );
    }
    
    public void testEncoding() throws Exception
    {
        CacheDirective result = (CacheDirective) doEncodingTest( m_directive, "cache.xml" );
        assertEquals( "encoded", m_directive, result );
    }
}
