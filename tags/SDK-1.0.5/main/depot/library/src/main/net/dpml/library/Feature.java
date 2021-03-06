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

import java.io.File;

import net.dpml.lang.Version;
import net.dpml.lang.Enum;

import net.dpml.transit.Artifact;

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
    * Resource decimal version.
    */
    public static final Feature DECIMAL = new Feature( "decimal" );

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
    * Project basedir.
    */
    public static final Feature BASEDIR = new Feature( "basedir" );

   /**
    * Array of scope enumeration values.
    */
    private static final Feature[] ENUM_VALUES = 
      new Feature[]{NAME, GROUP, VERSION, DECIMAL, URI, SPEC, PATH, FILENAME, BASEDIR};

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
        else if( value.equalsIgnoreCase( "decimal" ) )
        {
            return DECIMAL;
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
        else if( value.equalsIgnoreCase( "basedir" ) )
        {
            return BASEDIR;
        }
        else
        {
            final String error =
              "Unrecognized feature argument [" + value + "]";
            throw new IllegalArgumentException( error );
        }
    }
    
   /**
    * Return the value of a feature.
    * @param resource the target resource
    * @param feature the feature
    * @return the resolved value
    */
    public static String resolve( Resource resource, Feature feature ) 
    {
        return resolve( resource, feature, null, false );
    }
    
   /**
    * Return the value of a feature.
    * @param resource the target resource
    * @param feature the feature
    * @param type the selected type
    * @return the resolved value
    */
    public static String resolve( Resource resource, Feature feature, Type type ) 
    {
        return resolve( resource, feature, type, false );
    }
    
   /**
    * Return the value of a feature.
    * @param resource the target resource
    * @param feature the feature
    * @param type the selected type
    * @param alias flag indicated that alias based uri resolution is requested
    * @return the resolved value
    */
    public static String resolve( Resource resource, Feature feature, Type type, boolean alias ) 
    {
        if( null != type && !resource.isa( type.getID() ) )
        {
            final String error = 
              "The feature request for the type [" 
              + type 
              + "] from the resource ["
              + resource 
              + "] cannot be fullfilled because the resource does not declare "
              + "production of the requested type.";
            throw new FeatureRuntimeException( error );
        }
        
        if( feature.equals( Feature.NAME ) )
        {
            return resource.getName();
        }
        else if( feature.equals( Feature.GROUP ) )
        {
            return resource.getParent().getResourcePath();
        }
        else if( feature.equals( Feature.VERSION ) )
        {
            String version = resource.getVersion();
            if( null == version )
            {
                return "";
            }
            else
            {
                return version;
            }
        }
        else if( feature.equals( Feature.DECIMAL ) )
        {
            Version version = resource.getDecimalVersion();
            if( null == version )
            {
                return "";
            }
            else
            {
                return version.toString();
            }
        }
        else if( feature.equals( Feature.URI ) )
        {
            return resolveURIFeature( resource, type, alias );
        }
        else if( feature.equals( Feature.SPEC ) )
        {
            String path = resource.getResourcePath();
            String version =resource.getVersion();
            if( null == version )
            {
                return path;
            }
            else
            {
                return path + "#" + version;
            }
        }
        else if( feature.equals( Feature.PATH ) )
        {
            if( null == type )
            {
                final String error = 
                  "Type must be supplied in conjuction with the uri feature.";
                throw new FeatureRuntimeException( error );
            }
            else
            {
                String id = type.getID();
                Artifact artifact = resource.getArtifact( id );
                try
                {
                    File cached = 
                      (File) artifact.toURL().getContent( new Class[]{File.class} );
                    return cached.getCanonicalPath();
                }
                catch( Exception e )
                {
                    final String error = 
                      "Unable to resolve resource path.";
                    throw new FeatureRuntimeException( error, e );
                }
            }
        }
        else if( feature.equals( Feature.FILENAME ) )
        {
            if( null == type )
            {
                final String error = 
                  "Type must be supplied in conjuction with the filename feature.";
                throw new IllegalArgumentException( error );
            }
            String id = type.getID();
            return resource.getLayoutPath( id );
        }
        else if( feature.equals( Feature.BASEDIR ) )
        {
            File base = resource.getBaseDir();
            if( null == base )
            {
                throw new IllegalArgumentException( "basedir" );
            }
            else
            {
                try
                {
                    return base.getCanonicalPath();
                }
                catch( Exception e )
                {
                    final String error = 
                      "Unexpected error while resolving project basedir [" + base + "].";
                    throw new FeatureRuntimeException( error, e );
                }
            }
        }
        else
        {
            final String error = 
              "Invalid feature [" + feature + "].";
            throw new FeatureRuntimeException( error );
        }
    }
    
    private static String resolveURIFeature( Resource resource, Type type )
    {
        return resolveURIFeature( resource, type, false );
    }
    
    private static String resolveURIFeature( Resource resource, Type type, boolean alias )
    {
        if( null == type )
        {
            final String error = 
              "Type must be supplied in conjuction with the uri feature request.";
            throw new FeatureRuntimeException( error );
        }
        else
        {
            String id = type.getID();
            if( alias )
            {
                Version version = type.getVersion();
                if( null != version )
                {
                    Artifact artifact = resource.getArtifact( id );
                    String group = artifact.getGroup();
                    String name = artifact.getName();
                    if( Version.NULL_VERSION.equals( version ) )
                    {
                        return "link:" 
                          + id 
                          + ":" 
                          + group
                          + "/" 
                          + name; 
                    }
                    else
                    {
                        int major = version.getMajor();
                        int minor = version.getMinor();
                        return "link:" 
                          + id 
                          + ":" 
                          + group 
                          + "/" 
                          + name
                          + "#"
                          + major
                          + "."
                          + minor;
                    }
                }
                else
                {
                    final String error = 
                      "Cannot resolve link from resource [" 
                      + resource
                      + "] because the resource does not declare production of an alias for the type ["
                      + id 
                      + "].";
                    throw new FeatureRuntimeException( error );
                }
            }
            else
            {
                Artifact artifact = resource.getArtifact( id );
                return artifact.toURI().toASCIIString();
            }
        }
    }    
}

