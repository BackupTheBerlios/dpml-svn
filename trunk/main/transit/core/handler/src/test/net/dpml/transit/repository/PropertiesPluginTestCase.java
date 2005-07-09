/*
 * Copyright 2004 Stephen J. McConnell.
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

package net.dpml.transit.repository;

import java.io.InputStream;

import java.net.URI;

import java.util.Properties;

import junit.framework.TestCase;

import net.dpml.transit.artifact.Artifact;

/**
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Id: PropertiesPluginTestCase.java 2445 2005-04-28 23:44:22Z niclas@hedhman.org $
 */
public class PropertiesPluginTestCase extends TestCase
{
    private PropertiesPlugin m_Plugin;

    protected void setUp() throws Exception
    {
        Properties props = new Properties();
        InputStream stream = getClass().getResourceAsStream( "/plugin.properties" );
        props.load( stream );
        m_Plugin = new PropertiesPlugin( props );
    }

    public void testArtifact()
        throws Exception
    {
        Artifact artifact = Artifact.createArtifact( m_Plugin.getURI() );
        assertEquals( "Artifact name", "dpml-metro-cli", artifact.getName() );

        URI uri = URI.create( "artifact:plugin:dpml/metro/dpml-metro-cli#123" );
        assertEquals( "Artifact URI", uri, artifact.toURI() );
    }

    public void testDomain()
        throws Exception
    {
        assertEquals( "Domain", "net.dpml", m_Plugin.getSpecificationNamespace() );
    }

    public void testVersion()
        throws Exception
    {
        assertEquals( "Version", "1.0", m_Plugin.getSpecificationVersion() );
    }

    public void testClassname()
        throws Exception
    {
        assertEquals( "Classname", "net.dpml.metro.Metro", m_Plugin.getClassname() );
    }

    public void testInterface()
        throws Exception
    {
        // TODO: Fix a better sample
        assertEquals( "Interface", null, m_Plugin.getInterface() );
    }

    public void testApiDependencies()
        throws Exception
    {
        URI[] facts = m_Plugin.getDependencies( Plugin.API_KEY );
        assertEquals( "API deps", 4, facts.length );

        URI uri = URI.create( "artifact:jar:dpml/logging/dpml-logging-api#123" );
        assertEquals( "API Artifact URI", uri, facts[0] );
        uri = URI.create( "artifact:jar:dpml/activity/dpml-activity-api#123" );
        assertEquals( "API Artifact URI", uri, facts[1] );
        uri = URI.create( "artifact:jar:dpml/context/dpml-context-api#123" );
        assertEquals( "API Artifact URI", uri, facts[2] );
        uri = URI.create( "artifact:jar:dpml/transit/dpml-transit-main#123" );
        assertEquals( "API Artifact URI", uri, facts[3] );
    }

    public void testSpiDependencies()
        throws Exception
    {
        URI[] facts = m_Plugin.getDependencies( Plugin.SPI_KEY );
        assertEquals( "SPI deps", 2, facts.length );

        URI uri = URI.create( "artifact:jar:dpml/transit/dpml-transit-spi#123" );
        assertEquals( "SPI Artifact URI", uri, facts[0] );
        uri = URI.create( "artifact:jar:dpml/logging/dpml-logging-spi#123" );
        assertEquals( "SPI Artifact URI", uri, facts[1] );
    }

    public void testImplDependencies()
        throws Exception
    {
        URI[] facts = m_Plugin.getDependencies( Plugin.IMPL_KEY );
        assertEquals( "Impl deps", 5, facts.length );

        URI uri = URI.create( "artifact:jar:dpml/util/dpml-util-i18n#123" );
        assertEquals( "Impl Artifact URI", uri, facts[0] );
        uri = URI.create( "artifact:jar:dpml/util/dpml-util-exception#123" );
        assertEquals( "Impl Artifact URI", uri, facts[1] );
        uri = URI.create( "artifact:jar:dpml/util/dpml-util-cli#123" );
        assertEquals( "Impl Artifact URI", uri, facts[2] );
        uri = URI.create( "artifact:jar:commons-cli/commons-cli#1.0" );
        assertEquals( "Impl Artifact URI", uri, facts[3] );
        uri = URI.create( "artifact:jar:dpml/metro/dpml-metro-cli#123" );
        assertEquals( "Impl Artifact URI", uri, facts[4] );
    }

    public void testAllDependencies()
        throws Exception
    {
        URI[] facts = m_Plugin.getDependencies();
        assertEquals( "All deps", 11, facts.length );

        URI uri = URI.create( "artifact:jar:dpml/logging/dpml-logging-api#123" );
        assertEquals( "API Artifact URI", uri, facts[0] );
        uri = URI.create( "artifact:jar:dpml/activity/dpml-activity-api#123" );
        assertEquals( "API Artifact URI", uri, facts[1] );
        uri = URI.create( "artifact:jar:dpml/context/dpml-context-api#123" );
        assertEquals( "API Artifact URI", uri, facts[2] );
        uri = URI.create( "artifact:jar:dpml/transit/dpml-transit-main#123" );
        assertEquals( "API Artifact URI", uri, facts[3] );

        uri = URI.create( "artifact:jar:dpml/transit/dpml-transit-spi#123" );
        assertEquals( "SPI Artifact URI", uri, facts[4] );
        uri = URI.create( "artifact:jar:dpml/logging/dpml-logging-spi#123" );
        assertEquals( "SPI Artifact URI", uri, facts[5] );

        uri = URI.create( "artifact:jar:dpml/util/dpml-util-i18n#123" );
        assertEquals( "Impl Artifact URI", uri, facts[6] );
        uri = URI.create( "artifact:jar:dpml/util/dpml-util-exception#123" );
        assertEquals( "Impl Artifact URI", uri, facts[7] );
        uri = URI.create( "artifact:jar:dpml/util/dpml-util-cli#123" );
        assertEquals( "Impl Artifact URI", uri, facts[8] );
        uri = URI.create( "artifact:jar:commons-cli/commons-cli#1.0" );
        assertEquals( "Impl Artifact URI", uri, facts[9] );
        uri = URI.create( "artifact:jar:dpml/metro/dpml-metro-cli#123" );
        assertEquals( "Impl Artifact URI", uri, facts[10] );
    }
}

