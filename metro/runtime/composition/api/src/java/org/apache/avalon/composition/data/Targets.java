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


/**
 * <p>A target is a tagged configuration fragment.  The tag is a path
 * seperated by "/" charaters qualifying the component that the target
 * configuration is to be applied to.</p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: Targets.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class Targets
{
    //========================================================================
    // state
    //========================================================================

    /**
     * The set of targets.
     */
    private final TargetDirective[] m_targets;

    //========================================================================
    // constructors
    //========================================================================

    /**
     * Create an empty Targets instance.
     */
    public Targets()
    {
        m_targets = new TargetDirective[0];
    }

    /**
     * Create a new Targets instance.
     *
     * @param targets the set of targets
     */
    public Targets( final TargetDirective[] targets )
    {
        m_targets = targets;
    }

    //========================================================================
    // implementation
    //========================================================================

    /**
     * Return all targets.
     *
     * @return all the targets in this targets instance.
     */
    public TargetDirective[] getTargets()
    {
        return m_targets;
    }

    /**
     * Return a matching target.
     *
     * @param path the target path to lookup
     * @return the target or null if no matching target
     */
    public TargetDirective getTarget( String path )
    {
        final String key = getKey( path );

        for( int i=0; i<m_targets.length; i++ )
        {
            TargetDirective target = m_targets[i];
            if( target.getPath().equals( key ) )
            {
                return target;
            }
        }
        return null;
    }

   /**
    * Return a set of targets relative to the supplied path.
    *
    * @param path the base path to match against
    * @return the set of relative targets
    */
    public Targets getTargets( String path )
    {
        final String key = getKey( path );
        ArrayList list = new ArrayList();
        for( int i=0; i<m_targets.length; i++ )
        {
            TargetDirective target = m_targets[i];
            if( target.getPath().startsWith( key ) )
            {
                String name = target.getPath().substring( key.length() );
                if( name.length() > 0 )
                {
                    list.add( 
                       new TargetDirective( 
                          getKey( name ), 
                          target.getConfiguration(),
                          target.getCategoriesDirective(),
                          target.getSecurityProfileName() ) );
                }
            }
        }

        return new Targets( 
          (TargetDirective[]) list.toArray( new TargetDirective[0] ) );
    }

    /**
     * Convert the supplied path to a valid path.
     * @param path the path to convert
     * @return a good path value
     */
    private String getKey( final String path ) throws IllegalArgumentException
    {
        if( !path.startsWith("/") )
        {
            return "/" + path;
        }
        return path;
    }

    /**
     * Return a string representation of the target.
     * @return a string representing the target instance
     */
    public String toString()
    {
        StringBuffer buffer = new StringBuffer( "[targets: " );
        for( int i=0; i<m_targets.length; i++ )
        {
            buffer.append( m_targets[i] );
            if( i < ( m_targets.length -1 ) )
            {
               buffer.append( ", " );
            }
        }
        buffer.append( " ]" );
        return buffer.toString();
    }
}
