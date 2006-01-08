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

/**
 * Testing the CodeBaseDirective class.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class CacheDirectiveTestCase extends AbstractTestCase
{
    private String m_cache;
    private String m_cacheLayout;
    private String m_local;
    private String m_localLayout;
    private LayoutDirective[] m_layouts;
    private HostDirective[] m_hosts;
    private ContentDirective[] m_content;
    private CacheDirective m_directive;
    
   /**
    * Test case setup.
    * @exception Exception if an error occurs during setup.
    */
    public void setUp() throws Exception
    {
        m_cache = "${dpml.data}/cache";
        m_cacheLayout = "classic";
        m_local = "${dpml.share}/local";
        m_localLayout = "modern";
        m_layouts = new LayoutDirective[0];
        m_hosts = new HostDirective[0];
        m_content = new ContentDirective[0];
        m_directive = 
          new CacheDirective( 
           m_cache, m_cacheLayout, m_local, m_localLayout, m_layouts, m_hosts, m_content );
    }
    
   /**
    * Test cache accessor.
    * @exception Exception if an error occurs during setup.
    */
    public void testCache() throws Exception
    {
        assertEquals( "cache", m_cache, m_directive.getCache() );
    }
    
   /**
    * Test cache layout accessor.
    * @exception Exception if an error occurs during setup.
    */
    public void testCacheLayout() throws Exception
    {
        assertEquals( "layout", m_cacheLayout, m_directive.getCacheLayout() );
    }
    
   /**
    * Test local path accessor.
    * @exception Exception if an error occurs during setup.
    */
    public void testLocal() throws Exception
    {
        assertEquals( "local", m_local, m_directive.getLocal() );
    }
    
   /**
    * Test local layout accessor.
    * @exception Exception if an error occurs during setup.
    */
    public void testLocalLayout() throws Exception
    {
        assertEquals( "layout", m_localLayout, m_directive.getLocalLayout() );
    }
    
   /**
    * Test layouts accessor.
    * @exception Exception if an error occurs during setup.
    */
    public void testLayouts() throws Exception
    {
        assertEquals( "layouts", m_layouts, m_directive.getLayoutDirectives() );
    }
    
   /**
    * Test hosts accessor.
    * @exception Exception if an error occurs during setup.
    */
    public void testHosts() throws Exception
    {
        assertEquals( "hosts", m_hosts, m_directive.getHostDirectives() );
    }
    
   /**
    * Test content handler accessor.
    * @exception Exception if an error occurs during setup.
    */
    public void testContent() throws Exception
    {
        assertEquals( "content", m_content, m_directive.getContentDirectives() );
    }
    
   /**
    * Test directive serialization.
    * @exception Exception if an error occurs during setup.
    */
    public void testSerialization() throws Exception
    {
        doSerializationTest( m_directive );
    }
    
   /**
    * Test directive encoding.
    * @exception Exception if an error occurs during setup.
    */
    public void testEncoding() throws Exception
    {
        CacheDirective result = (CacheDirective) doEncodingTest( m_directive, "cache.xml" );
        assertEquals( "encoded", m_directive, result );
    }
}
