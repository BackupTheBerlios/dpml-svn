/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.library;

import net.dpml.lang.Enum;

/**
 * Enumeration identifying resource features.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class Feature extends Enum
{
    static final long serialVersionUID = 1L;

   /**
    * Resource name.
    */
    public static final Feature NAME = new Feature( "name" );

   /**
    * Resource group.
    */
    public static final Feature GROUP = new Feature( "group" );

   /**
    * Resource version.
    */
    public static final Feature VERSION = new Feature( "version" );

   /**
    * Resource version.
    */
    public static final Feature URI = new Feature( "uri" );

   /**
    * Resource spec.
    */
    public static final Feature SPEC = new Feature( "spec" );

   /**
    * Resource path.
    */
    public static final Feature PATH = new Feature( "path" );

   /**
    * Resource filename.
    */
    public static final Feature FILENAME = new Feature( "filename" );

   /**
    * Array of scope enumeration values.
    */
    private static final Feature[] ENUM_VALUES = new Feature[]{NAME, GROUP, VERSION, URI, SPEC, PATH, FILENAME};

   /**
    * Returns an array of activation enum values.
    * @return the activation policies array
    */
    public static Feature[] values()
    {
        return ENUM_VALUES;
    }
    
   /**
    * Internal constructor.
    * @param label the enumeration label.
    * @param index the enumeration index.
    */
    private Feature( String label )
    {
        super( label );
    }
    
   /**
    * Return a string representation of the scope.
    * @return the string value
    */
    public String toString()
    {
        return getName().toUpperCase();
    }
    
   /**
    * Return a feature.
    * @param value the feature name
    * @return the feature
    * @exception IllegalArgumentException if the name if not a recognized feature.
    */
    public static Feature parse( String value ) throws IllegalArgumentException
    {
        if( value.equalsIgnoreCase( "name" ) )
        {
            return NAME;
        }
        else if( value.equalsIgnoreCase( "group" ) )
        {
            return GROUP;
        }
        else if( value.equalsIgnoreCase( "version" ) )
        {
            return VERSION;
        }
        else if( value.equalsIgnoreCase( "uri" ) )
        {
            return URI;
        }
        else if( value.equalsIgnoreCase( "spec" ) )
        {
            return SPEC;
        }
        else if( value.equalsIgnoreCase( "path" ) )
        {
            return PATH;
        }
        else if( value.equalsIgnoreCase( "filename" ) )
        {
            return FILENAME;
        }
        else
        {
            final String error =
              "Unrecognized feature argument [" + value + "]";
            throw new IllegalArgumentException( error );
        }
    }
}

