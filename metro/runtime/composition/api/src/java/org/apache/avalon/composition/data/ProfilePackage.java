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

import java.io.Serializable;

/**
 * A collection of profiles packaged with a component type.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ProfilePackage.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class ProfilePackage implements Serializable
{
    //--------------------------------------------------------------------------
    // static
    //--------------------------------------------------------------------------

    public static final ProfilePackage EMPTY_PACKAGE = new ProfilePackage();

    //--------------------------------------------------------------------------
    // immutable state
    //--------------------------------------------------------------------------

    /**
     * The set of component profiles contained within the package.
     */
    private final ComponentProfile[] m_profiles;

    //--------------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------------

    public ProfilePackage() 
    {
        this( new ComponentProfile[0] );
    }

    /**
     * Create a new profile package instance.
     *
     * @param profiles the set of contained profiles
     */
    public ProfilePackage( final ComponentProfile[] profiles ) 
    {
        m_profiles = profiles;
    }

    //--------------------------------------------------------------------------
    // implementation
    //--------------------------------------------------------------------------

    /**
     * Return the set of profile.
     *
     * @return the profiles
     */
    public ComponentProfile[] getComponentProfiles()
    {
        return m_profiles;
    }
}
