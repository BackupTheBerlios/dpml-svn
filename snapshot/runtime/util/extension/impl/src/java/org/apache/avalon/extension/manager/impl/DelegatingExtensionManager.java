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

package org.apache.avalon.extension.manager.impl;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.avalon.extension.Extension;
import org.apache.avalon.extension.manager.ExtensionManager;
import org.apache.avalon.extension.manager.OptionalPackage;

/**
 * A {@link ExtensionManager} that can delegate to multiple
 * different package repositories.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DelegatingExtensionManager.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class DelegatingExtensionManager
    implements ExtensionManager
{
    /**
     * The list containing the {@link ExtensionManager} objects.
     */
    private final ArrayList m_extensionManagers = new ArrayList();

    /**
     * Default constructor that does not add any repositories.
     */
    public DelegatingExtensionManager()
    {
    }

    /**
     * Default constructor that delegates to specified extensionManagers.
     */
    public DelegatingExtensionManager( final ExtensionManager[] extensionManagers )
    {
        for( int i = 0; i < extensionManagers.length; i++ )
        {
            addExtensionManager( extensionManagers[ i ] );
        }
    }

    /**
     * Add a extensionManager to list of repositories delegated to
     * to find Optional Packages.
     *
     * @param extensionManager the extensionManager to add
     */
    protected synchronized void addExtensionManager( final ExtensionManager extensionManager )
    {
        if( !m_extensionManagers.contains( extensionManager ) )
        {
            m_extensionManagers.add( extensionManager );
        }
    }

    /**
     * Add a extensionManager to list of repositories delegated to
     * to find Optional Packages.
     *
     * @param extensionManager the extensionManager to add
     * @deprecated Use addExtensionManager instead
     */
    protected void addPackageRepository( final ExtensionManager extensionManager )
    {
        addExtensionManager( extensionManager );
    }

    /**
     * Remove a repository from list of repositories delegated to
     * to find Optional Packages.
     *
     * @param repository the repository to remove
     */
    protected synchronized void removeExtensionManager( final ExtensionManager repository )
    {
        m_extensionManagers.remove( repository );
    }

    /**
     * Remove a extensionManager from list of repositories delegated to
     * to find Optional Packages.
     *
     * @param extensionManager the extensionManager to remove
     * @deprecated Use removeExtensionManager instead.
     */
    protected void removePackageRepository( final ExtensionManager extensionManager )
    {
        removeExtensionManager( extensionManager );
    }

    /**
     * Scan through list of respositories and return all the matching {@link OptionalPackage}
     * objects that match in any repository.
     *
     * @param extension the extension to search for
     * @return the matching {@link OptionalPackage} objects.
     */
    public synchronized OptionalPackage[] getOptionalPackages( final Extension extension )
    {
        final ArrayList resultPackages = new ArrayList();

        final int size = m_extensionManagers.size();
        for( int i = 0; i < size; i++ )
        {
            final ExtensionManager repository =
                (ExtensionManager)m_extensionManagers.get( i );
            final OptionalPackage[] packages =
                repository.getOptionalPackages( extension );
            if( null == packages || 0 == packages.length )
            {
                continue;
            }

            for( int j = 0; j < packages.length; j++ )
            {
                resultPackages.add( packages[ j ] );
            }
        }

        final OptionalPackageComparator comparator =
            new OptionalPackageComparator( extension.getExtensionName() );
        Collections.sort( resultPackages, comparator );
        final OptionalPackage[] resultData =
            new OptionalPackage[ resultPackages.size() ];
        return (OptionalPackage[])resultPackages.toArray( resultData );
    }
}
