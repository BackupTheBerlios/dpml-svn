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
import java.io.ObjectInputStream;
import org.apache.avalon.composition.data.ProfilePackage;

/**
 * Create {@link ProfilePackage} from stream made up of
 * serialized object.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: SerializedProfilePackageCreator.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class SerializedProfilePackageCreator
    implements ProfilePackageCreator
{

    /**
     * Create a {@link ProfilePackage} from a class.
     *
     * @param name the component profile name (ignored)
     * @param clazz the component profile class
     * @return the profile package
     * @exception Exception if a error occurs during package creation
     */
    public ProfilePackage createProfilePackage( String name, Class clazz )
        throws Exception
    {
        final String classname = clazz.getName();
        final String zprofile =
          classname.replace( '.', '/' ) + ".zprofile";
        final InputStream stream =
          clazz.getClassLoader().getResourceAsStream( zprofile );
        if( stream == null )
        {
            return null;
        }
        else
        {
            return createProfilePackage( stream );
        }
    }

    /**
     * Create a {@link ProfilePackage} from a stream.
     *
     * @param inputStream the stream that the resource is loaded from
     * @return the composition profile
     * @exception Exception if a error occurs during package creation
     */
    public ProfilePackage createProfilePackage( InputStream inputStream )
        throws Exception
    {
        final ObjectInputStream ois = new ObjectInputStream( inputStream );
        return (ProfilePackage)ois.readObject();
    }

}
