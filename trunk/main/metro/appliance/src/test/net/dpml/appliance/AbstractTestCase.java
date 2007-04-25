/*
 * Copyright 2007 Stephen J. McConnell.
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

package net.dpml.appliance;

import dpml.station.info.ApplianceDescriptor;
import dpml.station.info.PlanDescriptor;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;

import junit.framework.TestCase;

import net.dpml.station.ApplianceContentHandler;
import net.dpml.station.PlanContentHandler;

/**
 * Test dealing with the appliance descriptor.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class AbstractTestCase extends TestCase
{
    static final ApplianceContentHandler HANDLER = new ApplianceContentHandler();
      
    ApplianceDescriptor loadApplianceDescriptor( String path ) throws Exception
    {
        ClassLoader classloader = getClass().getClassLoader();
        File dir = new File( System.getProperty( "project.test.dir" ) );
        File file = new File( dir, path );
        URL url = file.toURI().toURL();
        URLConnection connection = url.openConnection();
        ApplianceContentHandler handler = new ApplianceContentHandler();
        return (ApplianceDescriptor) handler.getContent( connection, new Class[]{ApplianceDescriptor.class} );
    }

    PlanDescriptor loadPlanDescriptor( String path ) throws Exception
    {
        ClassLoader classloader = getClass().getClassLoader();
        File dir = new File( System.getProperty( "project.test.dir" ) );
        File file = new File( dir, path );
        URL url = file.toURI().toURL();
        URLConnection connection = url.openConnection();
        PlanContentHandler handler = new PlanContentHandler();
        return (PlanDescriptor) handler.getContent( connection, new Class[]{PlanDescriptor.class} );
    }
}
