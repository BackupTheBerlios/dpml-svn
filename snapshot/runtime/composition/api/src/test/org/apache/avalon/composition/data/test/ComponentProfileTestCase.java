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

package org.apache.avalon.composition.data.test;

import junit.framework.TestCase;

import org.apache.avalon.meta.info.*;

import org.apache.avalon.composition.data.*;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.context.ContextException;

import org.apache.avalon.logging.data.CategoriesDirective;
import org.apache.avalon.logging.data.CategoryDirective;

/**
 * ProfileTestCase does XYZ
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ComponentProfileTestCase.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class ComponentProfileTestCase extends TestCase
{
    private Type m_type;
    private CategoriesDirective m_categories;
    private Mode m_mode;
    private int m_activation;
    private Parameters m_parameters;
    private Configuration m_configuration;
    private ContextDirective m_context;
    private DependencyDirective[] m_dependencies;
    private StageDirective[] m_stages;
    private String m_name;
    private String m_classname;
    private int m_collection;

    public ComponentProfileTestCase( String name )
    {
        super( name );
    }

    public void setUp()
    {
        m_name = "Test";
        m_classname = ComponentProfileTestCase.class.getName();
        m_context = 
          new ContextDirective( getClass().getName(), new ImportDirective[0] );
        m_configuration = new DefaultConfiguration("test");
        m_dependencies = new DependencyDirective[0];
        m_stages = new StageDirective[0];
        m_parameters = Parameters.fromProperties(System.getProperties());
        m_activation = DeploymentProfile.ENABLED;
        m_mode = Mode.IMPLICIT;
        m_categories = new CategoriesDirective( new CategoryDirective[0] );
        m_collection = InfoDescriptor.DEMOCRAT;
    }

    public void testProfile() throws ContextException
    {
        ComponentProfile profile = new ComponentProfile(
          m_name, m_activation, m_collection, m_classname, m_categories, 
          m_context, m_dependencies, m_stages, m_parameters, 
          m_configuration, m_mode );

        assertEquals( "name", m_name, profile.getName() );
        assertEquals( "collection", m_collection, profile.getCollectionPolicy() );
        assertEquals( "classname", m_classname, profile.getClassname() );
        assertEquals( "categories", m_categories, profile.getCategories() );
        assertEquals( "mode", m_mode, profile.getMode() );
        assertEquals( "activation", m_activation, profile.getActivationDirective() );
        assertEquals( "parameters", m_parameters, profile.getParameters() );
        assertEquals( "configuration", m_configuration, profile.getConfiguration() );
        assertEquals( "context", m_context, profile.getContext() );
    }
}
