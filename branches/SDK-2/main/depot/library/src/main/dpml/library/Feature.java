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

package dpml.library;

import java.io.File;

import net.dpml.lang.Version;
import net.dpml.transit.Artifact;

/**
 * Enumeration identifying resource features.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public enum Feature
{
   /**
    * Resource name.
    */
    NAME,

   /**
    * Resource group.
    */
    GROUP,

   /**
    * Resource version.
    */
    VERSION,

   /**
    * Resource decimal version.
    */
    DECIMAL,

   /**
    * Resource version.
    */
    URI,

   /**
    * Resource spec.
    */
    SPEC,

   /**
    * Resource path.
    */
    PATH,

   /**
    * Resource filename.
    */
    FILENAME,

   /**
    * Project basedir.
    */
    BASEDIR;

    
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
        
        if( feature.equals( NAME ) )
        {
            if( null != type )
            {
                String name = type.getName();
                return getValue( name );
            }
            else
            {
                return resource.getName();
            }
        }
        else if( feature.equals( GROUP ) )
        {
            return resource.getParent().getResourcePath();
        }
        else if( feature.equals( VERSION ) )
        {
            if( null != type )
            {
                String version = type.getVersion();
                return getValue( version );
            }
            else
            {
                String version = resource.getVersion();
                return getValue( version );
            }
        }
        else if( feature.equals( DECIMAL ) ) // <-- TODO, think about decimal versus explicit version on types
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
        else if( feature.equals( URI ) )
        {
            return resolveURIFeature( resource, type, alias );
        }
        else if( feature.equals( SPEC ) ) // <-- TODO: what is this used for?
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
        else if( feature.equals( PATH ) ) // <!-- TODO: update for Type/name/version/test
        {
            if( null == type )
            {
                final String error = 
                  "Type must be supplied in conjuction with the a type.";
                throw new FeatureRuntimeException( error );
            }
            
            File file = type.getFile();
            try
            {
                return file.getCanonicalPath();
            }
            catch( Exception e )
            {
                final String error = 
                  "Unexpected error while resolving resource type path [" + file + "].";
                throw new FeatureRuntimeException( error, e );
            }
        }
        else if( feature.equals( FILENAME ) )  // needs to be fixed
        {
            if( null == type )
            {
                final String error = 
                  "Type must be supplied in conjuction with the filename feature.";
                throw new IllegalArgumentException( error );
            }
            if( type.getTest() )
            {
                throw new IllegalArgumentException( 
                  "Filename cannot be used in conjunction with a test type." );
            }
            return type.getLayoutPath();
        }
        else if( feature.equals( BASEDIR ) )
        {
            File base = resource.getBaseDir();
            if( null == base )
            {
                throw new IllegalArgumentException( "basedir" );
            }
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
        else
        {
            final String error = 
              "Invalid feature [" + feature + "].";
            throw new FeatureRuntimeException( error );
        }
    }
    
    private static String getValue( String value )
    {
        if( null != value )
        {
            return value;
        }
        else
        {
            return "";
        }
    } 
    
    private static String resolveURIFeature( Resource resource, Type type )
    {
        return resolveURIFeature( resource, type, false );
    }
    
    private static String resolveURIFeature( 
      Resource resource, Type type, boolean alias ) // TODO: update to handle version on types and type names
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
                if( type.getAliasProduction() )
                {
                    Artifact artifact = type.getLinkArtifact();
                    return artifact.toURI().toASCIIString();
                }
                else
                {
                    final String error = 
                      "Cannot resolve link feature from the resource [" 
                      + resource
                      + "] because the resource does not declare production of an alias for the type ["
                      + id 
                      + "].";
                    throw new FeatureRuntimeException( error );
                }
            }
            else
            {
                
                Artifact artifact = type.getArtifact();
                return artifact.toURI().toASCIIString();
            }
        }
    }
}

