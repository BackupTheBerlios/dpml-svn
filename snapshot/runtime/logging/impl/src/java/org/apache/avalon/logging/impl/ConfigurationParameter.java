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

package org.apache.avalon.logging.impl;

import java.io.File;
import java.net.URL;
import org.apache.avalon.util.criteria.Parameter;
import org.apache.avalon.util.criteria.CriteriaException;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

/**
 * A parameter descriptor that supports transformation of a 
 * a string url to an URL instance.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ConfigurationParameter.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class ConfigurationParameter extends Parameter
{
    //--------------------------------------------------------------
    // static
    //--------------------------------------------------------------

    private static final Resources REZ =
      ResourceManager.getPackageResources( ConfigurationParameter.class );

    //--------------------------------------------------------------
    // constructors
    //--------------------------------------------------------------

   /**
    * Transform a string to a string array.
    * @param key the parameter key
    */
    public ConfigurationParameter( final String key ) 
    {
        super( key, URL.class );
    }

   /**
    * Resolve a supplied string to a configuration
    * @param value the value to resolve
    * @exception CriteriaException if an error occurs
    */
    public Object resolve( Object value ) 
      throws CriteriaException
    {
        if( value == null ) 
            return null;
        if( value instanceof URL )
        {
            return value;
        }
        if( value instanceof String )
        {
            return resolve( super.resolve( URL.class, value ) );
        }
        else if( value instanceof File )
        {
            File file = (File) value;
            if( ! file.exists() )
            {
                final String error = 
                  REZ.getString( 
                    "parameter.configuration.fnf.error", 
                    file.toString() );
                throw new CriteriaException( error );
            }

            try
            {
                return file.toURL();
            }
            catch( Throwable e )
            {
                final String error = 
                  REZ.getString( 
                    "parameter.configuration.file.error", 
                    file.toString() );
                throw new CriteriaException( error );
            }
        }
        else
        {
            final String error = 
              REZ.getString( 
                "parameter.unknown", 
                value.getClass().getName(), URL.class.getName() );
            throw new CriteriaException( error );
        }
    }
}
