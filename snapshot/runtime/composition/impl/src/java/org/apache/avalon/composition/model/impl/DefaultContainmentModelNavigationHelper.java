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

package org.apache.avalon.composition.model.impl;

import org.apache.avalon.composition.model.ContainmentModel;
import org.apache.avalon.composition.model.DeploymentModel;
import org.apache.avalon.composition.provider.ContainmentContext;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

/**
 * A utility class that assists in the location of a model relative
 * a supplied path.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultContainmentModelNavigationHelper.java 30977 2004-07-30 08:57:54Z niclas $
 */
class DefaultContainmentModelNavigationHelper
{
    //-------------------------------------------------------------------
    // static
    //-------------------------------------------------------------------

    private static final Resources REZ =
      ResourceManager.getPackageResources( 
        DefaultContainmentModelNavigationHelper.class );

    //-------------------------------------------------------------------
    // immutable state
    //-------------------------------------------------------------------

    private final ContainmentContext m_context;
    private final ContainmentModel m_model;

    //-------------------------------------------------------------------
    // constructor
    //-------------------------------------------------------------------

    public DefaultContainmentModelNavigationHelper( 
      ContainmentContext context, ContainmentModel model )
    {
        m_context = context;
        m_model = model;
    }

    //-------------------------------------------------------------------
    // implementation
    //-------------------------------------------------------------------

    public DeploymentModel getModel( String path )
    {
        ContainmentModel parent = 
          m_context.getParentContainmentModel();

        if( path.equals( "" ) )
        {
            return m_model;
        }
        else if( path.startsWith( "/" ) )
        {
            //
            // its a absolute reference that need to be handled by the 
            // root container
            //

            if( null != parent )
            {
                return parent.getModel( path );
            }
            else
            {
                //
                // this is the root container thereforw the 
                // path can be transfored to a relative reference
                //

                return m_model.getModel( path.substring( 1 ) );
            }
        }
        else
        {
            //
            // its a relative reference in the form xxx/yyy/zzz
            // so if the path contains "/", then locate the token 
            // proceeding the "/" (i.e. xxx) and apply the remainder 
            // (i.e. yyy/zzz) as the path argument , otherwise, its 
            // a local reference that we can pull from the model 
            // repository
            //

            final String root = getRootName( path );

            if( root.equals( ".." ) )
            {
                //
                // its a relative reference in the form "../xxx/yyy" 
                // in which case we simply redirect "xxx/yyy" to the 
                // parent container
                //
 
                if( null != parent )
                {
                    final String remainder = getRemainder( root, path );
                    return parent.getModel( remainder );
                }
                else
                {
                    final String error = 
                      "Supplied path ["
                      + path 
                      + "] references a container above the root container.";
                    throw new IllegalArgumentException( error );
                }
            }
            else if( root.equals( "." ) )
            {
                //
                // its a path with a redundant "./xxx/yyy" which is 
                // equivalent to "xxx/yyy"
                //
 
                final String remainder = getRemainder( root, path );
                return m_model.getModel( remainder );
            }
            else if( path.indexOf( "/" ) < 0 )
            {
                // 
                // its a path in the form "xxx" so we can use this
                // to lookup and return a local child
                //

                return m_context.getModelRepository().getModel( path );
            }
            else
            {
                //
                // locate the relative root container, and apply 
                // getModel to the container
                //

                DeploymentModel model = 
                  m_context.getModelRepository().getModel( root );
                if( model != null )
                {
                    //
                    // we have the sub-container so we can apply 
                    // the relative path after subtracting the name of 
                    // this container and the path seperator character
                    //

                    if( model instanceof ContainmentModel )
                    {
                        ContainmentModel container = 
                          (ContainmentModel) model;
                        final String remainder = getRemainder( root, path );
                        return container.getModel( remainder );
                    }
                    else
                    {
                        final String error = 
                          "The path element [" + root 
                          + "] does not reference a containment model within ["
                          + m_model + "].";
                        throw new IllegalArgumentException( error );
                    }
                }
                else
                {
                    //
                    // path contains a token that does not map to 
                    // known container
                    //
                    
                    final String error = 
                      "Unable to locate a container with name [" 
                      + root + "] within the container [" 
                      + m_model + "].";
                    throw new IllegalArgumentException( error );
                }
            }
        }
    }

    private String getRootName( String path )
    {
        int n = path.indexOf( "/" );
        if( n < 0 ) 
        {
            return path;
        }
        else
        {
            return path.substring( 0, n ); 
        }
    }

    private String getRemainder( String name, String path )
    {
        return path.substring( name.length() + 1 );
    }

}
