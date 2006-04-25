/*
 * Copyright 2005 Stephen J. McConnell
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

package net.dpml.library.info;

import java.util.ArrayList;

import net.dpml.lang.AbstractDirective;

/**
 * A datatype definining a collection of source includes and excludes.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class DataCollectionDirective extends DataDirective
{
    private final PatternDirective[] m_patterns;
    
   /**
    * Creation of a new data directive.
    * @param key the unique datatype key
    */
    public DataCollectionDirective( String key, PatternDirective[] patterns )
    {
        super( key );
        if( null == patterns )
        {
            throw new NullPointerException( "patterns" );
        }
        m_patterns = patterns;
    }
    
   /**
    * Return the array of pattern directives (includes and excludes).
    * @return the pattern array
    */
    public PatternDirective[] getPatternDirectives()
    {
        return m_patterns;
    }
    
   /**
    * Return the set of include directives.
    * @return the includes
    */
    public IncludePatternDirective[] getIncludes()
    {
        ArrayList list = new ArrayList();
        for( int i=0; i<m_patterns.length; i++ )
        {
            if( m_patterns[i] instanceof IncludePatternDirective )
            {
                list.add( m_patterns[i] );
            }
        }
        return (IncludePatternDirective[]) list.toArray( new IncludePatternDirective[0] );
    }
    
   /**
    * Return the set of exclude directives.
    * @return the excludes
    */
    public ExcludePatternDirective[] getExcludes()
    {
        ArrayList list = new ArrayList();
        for( int i=0; i<m_patterns.length; i++ )
        {
            if( m_patterns[i] instanceof ExcludePatternDirective )
            {
                list.add( m_patterns[i] );
            }
        }
        return (ExcludePatternDirective[]) list.toArray( new ExcludePatternDirective[0] );
    }
}
