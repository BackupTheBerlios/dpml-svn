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

package org.apache.avalon.meta;

import java.io.File;

import junit.framework.TestCase;
import org.apache.avalon.meta.info.Service;
import org.apache.avalon.meta.info.Type;
import org.apache.avalon.meta.info.ContextDescriptor;
import org.apache.avalon.meta.info.EntryDescriptor;
import org.apache.avalon.meta.info.CategoryDescriptor;
import org.apache.avalon.meta.info.DependencyDescriptor;
import org.apache.avalon.meta.info.ServiceDescriptor;
import org.apache.avalon.meta.info.builder.tags.TypeTag;
import org.apache.avalon.meta.info.builder.tags.ServiceTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaSource;
import com.thoughtworks.qdox.JavaDocBuilder;

/**
 * A testcase for the meta tools package.  The testcase verifies the number
 * and integrity of service and type defintions created as a result of a scan
 * of the working test directory for source files containing the avalon.version
 * javadoc tag.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class MetaTestCase extends TestCase
{
    private static final String PRIMARY = "org.apache.avalon.playground.Primary";
    private static final String SECONDARY = "org.apache.avalon.playground.Secondary";
    private static final String PRIMARY_S = "org.apache.avalon.playground.PrimaryService";
    private static final String SECONDARY_S = "org.apache.avalon.playground.SecondaryService";

   /**
    * The qdox builder that constructs the JavaClass instances
    * that hold the javadoc comment structures.
    */
    private JavaDocBuilder m_qdox = null;

    private Service m_primaryService;
    private Service m_secondaryService;
    private Type m_primary;
    private Type m_secondary;

    public MetaTestCase()
    {
        this( "MetaTestCase" );
    }

    public MetaTestCase( String name )
    {
        super( name );
    }

   /**
    * Setup the qdox javadoc builder and the type and
    * service builders, and scan the test directory for
    * sources, resulting in the population of a type and
    * service list that this test case will verify for
    * integrity.
    *
    * @exception Exception if a setup error occurs
    */
    protected void setUp() throws Exception
    {
        m_qdox = new JavaDocBuilder();
        buildMeta();
    }

   /**
    * Scan the test directory for source files and build up the
    * qdox JavaSource defintions from which JavaClass defintions
    * are resolved.  If the class contains a avalon.version tag
    * then we build a Type or Service instance (class or interface
    * respectively, and add them to the type and service lists.
    *
    * @exception Exception if a build error occurs
    */
    public void buildMeta() throws Exception
    {
        File basedir = getWorkDir();
        m_qdox.addSourceTree( basedir );
        JavaSource[] sources = m_qdox.getSources();
        for( int i=0; i<sources.length; i++ )
        {
            JavaSource source = sources[i];
            JavaClass[] classes = source.getClasses();
            for( int j=0; j<classes.length; j++ )
            {
                JavaClass c = classes[j];
                if( c.isInterface() )
                {
                    Service service = new ServiceTag( c ).getService();
                    if( service == null )
                    {
                        fail( "encounter null service: " + c );
                    }
                    if( service.getReference().getClassname().equals( PRIMARY_S ) )
                    {
                        m_primaryService = service;
                    }
                    else if( service.getReference().getClassname().equals( SECONDARY_S ) )
                    {
                        m_secondaryService = service;
                    }
                    else
                    {
                        fail( "Unexpected (invalid) service reference: " + service );
                    }
                }
                else
                {
                    Type type = new TypeTag( c ).getType();
                    if( type == null )
                    {
                        fail( "encounter null type: " + c );
                    }
                    if( type.getInfo().getClassname().equals( PRIMARY ) )
                    {
                        m_primary = type;
                    }
                    else if( type.getInfo().getClassname().equals( SECONDARY ) )
                    {
                        m_secondary = type;
                    }
                    else
                    {
                        fail( "Unexpected (invalid) type reference: " + type );
                    }
                }
            }
        }
    }

    private File getWorkDir()
    {
        String path = System.getProperty( "project.dir" );
        if( null != path )
        {
            return new File( path );
        }
        else
        {
            path = System.getProperty( "basedir" );
            File root = new File( path );
            return new File( root, "target/test-classes" );
        }
    }

   /**
    * Verify the the build process generated two Type defintions.
    */
    public void testTypeCreation()
    {
        assertTrue( "primary != null", m_primary != null );
        assertTrue( "secondary != null", m_secondary != null );
    }

   /**
    * Verify the the build process generated two Service defintions.
    */
    public void testServiceCreation()
    {
        assertTrue( "primary service != null", m_primaryService != null );
        assertTrue( "secondary service != null", m_secondaryService != null );
    }

   /**
    * Verify the integrity of the primary Service definitions for integrity
    * relative to the javadoc tag statements.
    *
    * @exception Exception if a verification error occurs
    */
    public void testPrimaryService() throws Exception
    {
        Service service = m_primaryService;
        assertTrue( "version", service.getReference().getVersion().toString().equals( "9.8.0" ) );
        assertTrue( "classname", service.getClassname().equals( PRIMARY_S ) );
        assertTrue( "attribute", service.getAttribute("status").equals( "test" ) );
    }

   /**
    * Verify the integrity of the secondary Service definitions for integrity
    * relative to the javadoc tag statements.
    *
    * @exception Exception if a verification error occurs
    */
    public void testSecondaryService() throws Exception
    {
        Service service = m_secondaryService;
        assertTrue(
          "version", service.getReference().getVersion().toString().equals( "0.1.0" ) );
        assertTrue( "classname", service.getClassname().equals( SECONDARY_S ) );
    }

    public void testPrimaryType() throws Exception
    {
        Type type = m_primary;
        assertTrue( "version", type.getInfo().getVersion().toString().equals( "1.3.0" ) );
        assertTrue( "name", type.getInfo().getName().equals( "primary-component" ) );
        assertTrue(
          "lifestyle", type.getInfo().getLifestyle().equals( "singleton" ) );

        ContextDescriptor context = m_primary.getContext();
        EntryDescriptor entry = context.getEntry( "home" );
        if( entry == null )
        {
            assertTrue( "no context entries", false );
            throw new Exception( "missing context" );
        }
        else
        {
            assertTrue( entry.getKey().equals( "home" ) );
            assertTrue( entry.getClassname().equals( "java.io.File" ) );
        }
    }

    public void testSecondaryType() throws Exception
    {
        Type type = m_secondary;
        assertTrue( "version", type.getInfo().getVersion().toString().equals( "2.4.0" ) );
        assertTrue( "name", type.getInfo().getName().equals( "secondary-component" ) );
        CategoryDescriptor[] loggers = type.getCategories();
        if( loggers.length == 1 )
        {
            CategoryDescriptor logger = loggers[0];
            if( !logger.getName().equals( "system" ) )
            {
                assertTrue( "Logger name is not system", false );
                throw new Exception( "Logger name is not system" );
            }
        }
        else
        {
            assertTrue( "Loggers length != 1", false );
            throw new Exception( "Loggers length != 1" );
        }
        DependencyDescriptor[] dependencies = type.getDependencies();
        if( dependencies.length == 1 )
        {
            DependencyDescriptor dep = dependencies[0];
            if( !dep.getReference().getClassname().equals( PRIMARY_S ) )
            {
                assertTrue( "dependency classname", false );
                throw new Exception( "Dependency name is incorrect" );
            }
            if( !dep.getReference().getVersion().toString().equals( "1.3.0" ) )
            {
                assertTrue( "dependency version: " + dep.getReference().getVersion(), false );
                throw new Exception( "Dependency version is incorrect" );
            }
            if( !dep.getKey().equals( "primary" ) )
            {
                assertTrue( "dependency role : " + dep.getKey(), false );
                throw new Exception( "Dependency role name is incortrect" );
            }
        }
        else
        {
            throw new Exception( "Dependency length != 1" );
        }

        //
        // test if the service descriptor is valid
        //

        ServiceDescriptor[] services = type.getServices();
        if( services.length == 1 )
        {
            ServiceDescriptor dep = services[0];
            if( !dep.getReference().getClassname().equals( SECONDARY_S ) )
            {
                assertTrue( "service classname: " + dep.getReference().getClassname(), false );
                throw new Exception( "Service classname is incorrect" );
            }
            if( !dep.getReference().getVersion().toString().equals( "0.1.0" ) )
            {
                assertTrue( "service version: " + dep.getReference().getVersion(), false );
                throw new Exception( "Service version is incorrect" );
            }
        }
        else
        {
            throw new Exception( "Services length != 1" );
        }
    }
}
