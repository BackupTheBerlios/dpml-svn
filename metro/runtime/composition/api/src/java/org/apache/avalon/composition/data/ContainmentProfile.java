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

package org.apache.avalon.composition.data;

import java.util.ArrayList;

import org.apache.avalon.meta.info.ServiceDescriptor;

import org.apache.avalon.logging.data.CategoriesDirective;

/**
 * A containment profile describes a containment context including
 * a classloader and the set of profiles explicitly included within
 * the a container.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ContainmentProfile.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class ContainmentProfile extends DeploymentProfile
{
    //========================================================================
    // static
    //========================================================================

    /**
     * Container path delimiter.
     */
    public static final String DELIMITER = "/";

    private static final ServiceDirective[] EMPTY_SERVICES =
      new ServiceDirective[0];

    private static final DeploymentProfile[] EMPTY_PROFILES = 
      new DeploymentProfile[0];

    private static final ClassLoaderDirective EMPTY_CLASSLOADER =
      new ClassLoaderDirective( 
        new LibraryDirective(), 
        new ClasspathDirective());

    //========================================================================
    // state
    //========================================================================

    /**
     * The classloader directive.
     */
    private ClassLoaderDirective m_classloader;

   /**
    * The published service directives.
    */
    private final ServiceDirective[] m_export;

    /**
     * The profiles described within the scope of the containment profile.
     */
    private final DeploymentProfile[] m_profiles;

    //--------------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------------

   /**
    * Creation of a new empty containment profile.
    */
    public ContainmentProfile()
    {
        this( "container", null, null, null, null );
    }

   /**
    * Creation of a new containment profile.
    *
    * @param name the profile name
    * @param classloader the description of the classloader to be 
    *    created for this containment profile
    * @param exports the set of servides that this component is 
    *    dependent on for normal execution
    * @param profiles the set of profiles contained within this 
    *    containment profile
    */
    public ContainmentProfile( 
      final String name, final ClassLoaderDirective classloader, 
      final ServiceDirective[] exports,
      final CategoriesDirective categories, 
      DeploymentProfile[] profiles )
    {
        super( name, DeploymentProfile.ENABLED, Mode.EXPLICIT, categories );

        m_classloader = classloader;
        m_profiles = profiles;
        m_export = exports;
    }

    //--------------------------------------------------------------------------
    // implementation
    //--------------------------------------------------------------------------

    /**
     * Return the classloader directive that describes the creation
     * arguments for the classloader required by this container.
     *
     * @return the classloader directive
     */
    public ClassLoaderDirective getClassLoaderDirective()
    {
        if( m_classloader == null ) return EMPTY_CLASSLOADER;
        return m_classloader;
    }

    /**
     * Return the set of service directives that describe the mapping 
     * between services exposrted by the container and its implementation
     * model.
     *
     * @return the array of service directives
     */
    public ServiceDirective[] getExportDirectives()
    {
        if( m_export == null ) return EMPTY_SERVICES;
        return m_export;
    }

    /**
     * Retrieve a service directive matching a supplied class.
     *
     * @param clazz the class to match
     * @return the service directive or null if it does not exist
     */
    public ServiceDirective getExportDirective( final Class clazz )
    {
        final String classname = clazz.getName();
        ServiceDescriptor[] services = getExportDirectives();
        for( int i = 0; i <services.length; i++ )
        {
            final ServiceDirective virtual = (ServiceDirective) services[i];
            if( virtual.getReference().getClassname().equals( classname ) )
            {
                return virtual;
            }
        }
        return null;
    }

    /**
     * Return the set of nested profiles wihin this containment profile.
     *
     * @return the profiles nested in this containment profile
     */
    public DeploymentProfile[] getProfiles()
    {
        if( m_profiles == null ) return EMPTY_PROFILES;
        return m_profiles;
    }

    /**
     * Return the set of nested profiles contained within this profile matching
     * the supplied mode.
     *
     * @param mode one of enumerated value {@link Mode#IMPLICIT}, 
     *    {@link Mode#PACKAGED}, or {@link Mode#EXPLICIT}
     * @return the profiles matching the supplied creation mode
     */
    public DeploymentProfile[] getProfiles( Mode mode )
    {
        DeploymentProfile[] profiles = getProfiles();
        return selectProfileByMode( profiles, mode );
    }

    /**
     * Returns a sub-set of the supplied containers matching the supplied creation mode.
     * @param profiles the profiles to select from
     * @param mode the creation mode to retrict the returned selection to
     * @return the subset of the supplied profiles with a creation mode matching
     *   the supplied mode value
     */
    private DeploymentProfile[] selectProfileByMode( DeploymentProfile[] profiles, Mode mode )
    {
        ArrayList list = new ArrayList();
        for( int i = 0; i < profiles.length; i++ )
        {
            DeploymentProfile profile = profiles[ i ];
            if( profile.getMode().equals( mode ) )
            {
                list.add( profile );
            }
        }
        return (DeploymentProfile[])list.toArray( new DeploymentProfile[0] );
    }
}
