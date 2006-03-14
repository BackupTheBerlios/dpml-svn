/*
 * Copyright 2004 Stephen J. McConnell.
 * Copyright 2004 Apache Software Foundation
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
package net.dpml.configuration.test;

import junit.framework.TestCase;

import net.dpml.configuration.impl.DefaultConfiguration;
import net.dpml.configuration.impl.DefaultConfigurationSerializer;

import java.io.File;

/**
 * Test the basic public methods of DefaultConfigurationSerializer.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public final class DefaultConfigurationSerializerTestCase extends TestCase
{
    private File m_testDirectory;
    private File m_testDirectory2;

    /**
     * Creates a new DefaultConfigurationSerializerTestCase object.
     */
    public DefaultConfigurationSerializerTestCase(  )
    {
        this( "DefaultConfigurationSerializer Test Case " );
    }

    /**
     * Creates a new DefaultConfigurationSerializerTestCase object.
     *
     * @param name DOCUMENT ME!
     */
    public DefaultConfigurationSerializerTestCase( final String name )
    {
        super( name );
    }

    /**
     * Testcase setup.
     */
    public void setUp()
    {
        File basedir = getWorkDir(  );
        m_testDirectory = ( new File( basedir, "io" ) ).getAbsoluteFile(  );
        m_testDirectory2 = new File( basedir,
                "DefaultConfigurationSerializerTestCase" ).getAbsoluteFile(  );

        if( !m_testDirectory.exists(  ) )
        {
            m_testDirectory.mkdirs(  );
        }

        assertTrue( !m_testDirectory2.exists(  ) );
    }

    private File getWorkDir()
    {
        String path = System.getProperty( "project.test.dir" );

        if( null != path )
        {
            return new File( path );
        }
        else
        {
            path = System.getProperty( "basedir" );

            File root = new File( path );

            return new File( root, "target/test" );
        }
    }

    /**
     * Checks that the <code>serializeToFile</code> method closes the output stream
     * when it is done.
     * @throws Exception if an error occurs
     */
    public void testSerializeToFile() throws Exception
    {
        DefaultConfiguration config = new DefaultConfiguration( "root", "" );
        config.setAttribute( "attribute", "value" );

        File file = new File( m_testDirectory,
                "DefaultConfigurationSerializerTestCase.xml" );

        DefaultConfigurationSerializer serializer = new DefaultConfigurationSerializer(  );
        serializer.serializeToFile( file, config );

        //
        // This will not work if the serializeToFile method keeps the stream open.
        //
        assertTrue( m_testDirectory.renameTo( m_testDirectory2 ) );
        assertTrue( m_testDirectory2.renameTo( m_testDirectory ) );

        file.delete(  );
    }
}
