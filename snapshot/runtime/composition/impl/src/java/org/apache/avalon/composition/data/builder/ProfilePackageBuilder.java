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

package org.apache.avalon.composition.data.builder;

import java.io.InputStream;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.meta.ConfigurationBuilder;
import org.apache.avalon.composition.data.ProfilePackage;
import org.apache.avalon.composition.data.ComponentProfile;

import org.xml.sax.InputSource;


/**
 * A ProfileBuilder is responsible for building a {@link ProfilePackage}
 * object from a source.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ProfilePackageBuilder.java 30977 2004-07-30 08:57:54Z niclas $
 */
public final class ProfilePackageBuilder implements ProfilePackageCreator
{
    private XMLProfilePackageCreator m_xml = 
      new XMLProfilePackageCreator( );

    private final SerializedProfilePackageCreator m_serial =
      new SerializedProfilePackageCreator();

    /**
     * Create a {@link ProfilePackage} from a type.
     *
     * @param name the component type name
     * @param clazz the component class
     * @return the profile package
     * @exception Exception if a error occurs during package creation
     */
    public ProfilePackage createProfilePackage( String name, Class clazz )
        throws Exception
    {
        ProfilePackage profiles = m_serial.createProfilePackage( name, clazz );
        if( profiles != null )
        {
            return profiles;
        }
        else
        {
            final String classname = clazz.getName();
            final String xprofile =
              classname.replace( '.', '/' ) + ".xprofile";
            final InputStream stream =
              clazz.getClassLoader().getResourceAsStream( xprofile );
            if( stream == null )
            {
                ComponentProfile profile = 
                  new ComponentProfile( name, classname );
                return new ProfilePackage( new ComponentProfile[]{ profile } );
            }
            else
            {
                return buildFromXMLDescriptor( name, classname, stream );
            }
        }
    }

    /**
     * Build ProfilePackage from the serialized format.
     *
     * @param input the object input stream
     * @throws Exception if an error occurs
     */
    private ProfilePackage buildFromSerDescriptor( InputStream input )
        throws Exception
    {
        return m_serial.createProfilePackage( input );
    }

    /**
     * Build a ProfilePackage from an XML descriptor.
     *
     * @param name the type name
     * @param classname the type classname
     * @param input the input stream for the profile
     * @throws Exception if an error occurs
     */
    private ProfilePackage buildFromXMLDescriptor( 
      final String name, final String classname, InputStream input )
        throws Exception
    {
        final InputSource source = new InputSource( input );
        Configuration config = ConfigurationBuilder.build( source );
        return m_xml.createProfilePackage( name, classname, config );
    }

}
