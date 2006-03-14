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
public final class TransitDirectiveTestCase extends AbstractTestCase
{
    private CacheDirective m_cache;
    private ProxyDirective m_proxy;
    private TransitDirective m_directive;
    
   /**
    * Test case setup.
    * @exception Exception if an error occurs
    */
    public void setUp() throws Exception
    {
        m_proxy = 
          new ProxyDirective( 
            "http://localhost:1234", new String[]{"http://www.gotham-city.net"}, 
            "batman", new char[]{'r', 'o', 'b', 'i', 'n'} );
        m_cache = 
          new CacheDirective( 
            "${dpml.data}/cache", "classic", "${dpml.share}/local", "modern", 
            new LayoutDirective[0], new HostDirective[0], new ContentDirective[0] );
        m_directive = 
          new TransitDirective( 
            m_proxy, m_cache );
    }
    
   /**
    * Test proxy directive accessor.
    * @exception Exception if an error occurs
    */
    public void testProxyDirective() throws Exception
    {
        assertEquals( "proxy", m_proxy, m_directive.getProxyDirective() );
    }
    
   /**
    * Test cache directive accessor.
    * @exception Exception if an error occurs
    */
    public void testCacheDirective() throws Exception
    {
        assertEquals( "cache", m_cache, m_directive.getCacheDirective() );
    }
        
   /**
    * Test directive serialization.
    * @exception Exception if an error occurs
    */
    public void testSerialization() throws Exception
    {
        doSerializationTest( m_directive );
    }
}
