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

import java.util.Comparator;

import org.apache.avalon.extension.DeweyDecimal;
import org.apache.avalon.extension.Extension;
import org.apache.avalon.extension.manager.OptionalPackage;

/**
 * A simple class to compare two extensions and sort them
 * on spec version and then on impl version. Unspecified
 * versions rate lower than specified versions.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: OptionalPackageComparator.java 30977 2004-07-30 08:57:54Z niclas $
 */
class OptionalPackageComparator
    implements Comparator
{
    /**
     * The name of extension the comparator is working with.
     */
    private final String m_name;

    public OptionalPackageComparator( final String name )
    {
        if( null == name )
        {
            throw new NullPointerException( "name" );
        }

        m_name = name;
    }

    public int compare( final Object o1,
                        final Object o2 )
    {
        final OptionalPackage pkg1 = (OptionalPackage)o1;
        final OptionalPackage pkg2 = (OptionalPackage)o2;
        final Extension e1 = getMatchingExtension( pkg1 );
        final Extension e2 = getMatchingExtension( pkg2 );
        int result = compareSpecVersion( e1, e2 );
        if( 0 != result )
        {
            return result;
        }
        else
        {
            return compareImplVersion( e1, e2 );
        }
    }

    private Extension getMatchingExtension( final OptionalPackage pkg )
    {
        final Extension[] extensions = pkg.getAvailableExtensions();
        for( int i = 0; i < extensions.length; i++ )
        {
            final Extension extension = extensions[ i ];
            if( extension.getExtensionName().equals( m_name ) )
            {
                return extension;
            }
        }

        final String message = "Unable to locate extension " +
            m_name + " in package " + pkg;
        throw new IllegalStateException( message );
    }

    private int compareImplVersion( final Extension e1, final Extension e2 )
    {
        final String implVersion1 = e1.getImplementationVersion();
        final String implVersion2 = e2.getImplementationVersion();
        if( null == implVersion1 && null == implVersion2 )
        {
            return 0;
        }
        else if( null != implVersion1 && null == implVersion2 )
        {
            return -1;
        }
        else if( null == implVersion1 && null != implVersion2 )
        {
            return 1;
        }
        else
        {
            return -implVersion1.compareTo( implVersion2 );
        }
    }

    private int compareSpecVersion( final Extension e1,
                                    final Extension e2 )
    {
        final DeweyDecimal specVersion1 = e1.getSpecificationVersion();
        final DeweyDecimal specVersion2 = e2.getSpecificationVersion();
        if( null == specVersion1 && null == specVersion2 )
        {
            return 0;
        }
        else if( null != specVersion1 && null == specVersion2 )
        {
            return -1;
        }
        else if( null == specVersion1 && null != specVersion2 )
        {
            return 1;
        }
        else
        {
            if( specVersion1.isEqual( specVersion2 ) )
            {
                return 0;
            }
            else if( specVersion1.isGreaterThan( specVersion2 ) )
            {
                return -1;
            }
            else
            {
                return 1;
            }
        }
    }
}
