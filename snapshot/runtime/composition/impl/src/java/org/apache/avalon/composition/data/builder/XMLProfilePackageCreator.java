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

import java.util.ArrayList;

import org.apache.avalon.composition.data.MetaDataException;
import org.apache.avalon.composition.data.ComponentProfile;
import org.apache.avalon.composition.data.ProfilePackage;

import org.apache.avalon.framework.configuration.Configuration;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;
import org.apache.avalon.util.configuration.ConfigurationUtil;

/**
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: XMLProfilePackageCreator.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class XMLProfilePackageCreator
{
    private static final Resources REZ =
        ResourceManager.getPackageResources( XMLProfilePackageCreator.class );

    private static final XMLComponentProfileCreator DEPLOYMENT_CREATOR = 
      new XMLComponentProfileCreator();

   /**
    * Creation of a {@link ProfilePackage} from an XML configuration.
    *
    * @param config the configuration
    * @return the profile package
    */
    public ProfilePackage createProfilePackage( 
      final String base, String classname, Configuration config )
      throws MetaDataException
    {
        ArrayList list = new ArrayList();
        Configuration[] children = config.getChildren();
        for( int i=0; i<children.length; i++ )
        {
            Configuration child = children[i];
            final String name = child.getName();
            if( name.equals( "profile" ) )
            {
                try
                {
                    list.add( 
                      DEPLOYMENT_CREATOR.createComponentProfile( 
                        base, classname, child ) );
                }
                catch( Throwable e )
                {
                    final String error = 
                      "Unable to create a packaged deployment profile."
                      + ConfigurationUtil.list( child );
                    throw new MetaDataException( error );
                }
            }
            else
            {
                final String error =
                  "Package defintion contains an unrecognized profile"
                  + ConfigurationUtil.list( child );
                throw new MetaDataException( error );
            }
        }

        return new ProfilePackage( 
          (ComponentProfile[]) list.toArray( new ComponentProfile[0] ) );
    }
}
