/*
 *     Copyright 2004. The Apache Software Foundation.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 *  
 */
package org.apache.metro.studio.eclipse.core.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>*
 */
public class ClassNameAnalyzer
{
    /**
     * @uml property=segments associationEnd={multiplicity={(0 -1)}
     * elementType=java.lang.String}
     *  
     */
    private List segments = new ArrayList();

    public void setPackageName( String name )
    {
        setFullClassName( name );
    }

    public void setFullClassName( String name )
    {
        StringTokenizer st = new StringTokenizer( name, ".", false );
        while( st.hasMoreTokens() )
        {
            String part = st.nextToken();
            segments.add( part );
        }
    }

    public String getPath()
    {
        StringBuffer buff = new StringBuffer();
        for( int i = 0 ; i < segments.size() - 2 ; i++ )
        {
            String segment = (String) segments.get( i );
            buff.append( segment );
            buff.append( "/" );
        }
        return buff.toString();
    }

    public String getFileName()
    {
        // TODO: NH: This is adding the segments in the reverse order.
        //       Is that really correct?
        /* Proposed alternative;
        
        StringBuffer buff = new StringBuffer();
        int size = segments.size();
        for( int i = 0; i < size ; i++ )
        {
            if( i != 0 )
                buff.append( "." );
            String segment = (String) segments.get( i );
            buff.append( segment );
        }
        return buff.toString();
        
        */
        
        StringBuffer buff = new StringBuffer();
        int size = segments.size();
        for( int i = size - 1 ; i < size ; i++ )
        {
            String segment = (String) segments.get( i );
            buff.append( segment );
            if( i < size - 1 )
                buff.append( ".") ;
        }
        return buff.toString();
    }

    /**
     * @param directory
     */
    public void setPath( String directory )
    {
        StringTokenizer st = new StringTokenizer( directory, "/", false );
        while( st.hasMoreTokens() )
        {
            String part = st.nextToken();
            segments.add( part );
        }
    }

    /**
     * @return Returns the segments. @uml property=segments
     */
    public List getSegments()
    {
        return segments;
    }

}
