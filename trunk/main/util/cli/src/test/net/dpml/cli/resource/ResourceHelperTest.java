/*
 * Copyright 2003-2005 The Apache Software Foundation
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
package net.dpml.cli.resource;

import junit.framework.TestCase;

import java.util.ResourceBundle;

/**
 * A utility class used to provide internationalisation support.
 *
 * @author John Keyes
 */
public class ResourceHelperTest extends TestCase
{
    /** system property */
    private static final String PROP_LOCALE = "net.dpml.cli.resource.bundle";
    private static ResourceHelper m_HELPER;

    /** resource m_bundle */
    private ResourceBundle m_bundle;

    /**
     * Create a new ResourceHelperTest.
     */
    public ResourceHelperTest(  )
    {
        super( "ResourceHelperTest" );
    }

    /**
     * DOCUMENT ME!
     */
    public void setUp(  )
    {
        System.setProperty( PROP_LOCALE, "net.dpml.cli.resource.TestBundle" );
        m_HELPER = ResourceHelper.getResourceHelper(  );
    }

    /**
     * DOCUMENT ME!
     */
    public void tearDown(  )
    {
        System.setProperty( PROP_LOCALE,
            "net.dpml.cli.resource.CLIMessageBundle_en_US.properties" );
    }

    /**
     * DOCUMENT ME!
     */
    public void testOverridden(  )
    {
        assertEquals( "wrong message",
            "The class name \"ResourceHelper\" is invalid.",
            m_HELPER.getMessage( "ClassValidator.bad.classname", "ResourceHelper" ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testNewMessage1Param(  )
    {
        assertEquals( "wrong message",
            "Some might say we will find a brighter day.",
            m_HELPER.getMessage( "test.message" ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testNewMessage2Params(  )
    {
        assertEquals( "wrong message",
            "Some might say we will find a brighter day.",
            m_HELPER.getMessage( "test.message", "Some" ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testNewMessage3Params(  )
    {
        assertEquals( "wrong message",
            "Some might say we will find a brighter day.",
            m_HELPER.getMessage( "test.message", "Some", "might" ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testNewMessage4Params(  )
    {
        assertEquals( "wrong message",
            "Some might say we will find a brighter day.",
            m_HELPER.getMessage( "test.message", "Some", "might", "say" ) );
    }

    /**
     * DOCUMENT ME!
     */
    public void testDefaultBundle(  )
    {
        System.setProperty( PROP_LOCALE, "madeupname.properties" );
        m_HELPER = ResourceHelper.getResourceHelper(  );
        assertEquals( "wrong message",
            "The class name \"ResourceHelper\" is invalid.",
            m_HELPER.getMessage( "ClassValidator.bad.classname", "ResourceHelper" ) );
    }
}
