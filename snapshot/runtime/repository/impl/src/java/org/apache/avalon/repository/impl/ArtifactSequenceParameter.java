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

package org.apache.avalon.repository.impl;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.avalon.repository.Artifact;

import org.apache.avalon.util.criteria.Parameter;
import org.apache.avalon.util.criteria.CriteriaException;


/**
 * A parameter descriptor that supports transformation of a 
 * a string to a string array based on a supplied token.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ArtifactSequenceParameter.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class ArtifactSequenceParameter extends Parameter
{
    //--------------------------------------------------------------
    // immutable state
    //--------------------------------------------------------------

    private final String m_delimiter;

    //--------------------------------------------------------------
    // constructors
    //--------------------------------------------------------------

   /**
    * Transform a string to a string array.
    * @param key the parameter key
    * @param delimiter the delimiter character
    * @param defaults the default string array
    */
    public ArtifactSequenceParameter( 
      final String key, final String delimiter, Artifact[] defaults ) 
    {
        super( key, Artifact[].class, defaults );
        m_delimiter = delimiter;
    }

   /**
    * Resolve a supplied string or string array to a artifact array value.
    * @param value the value to resolve
    * @exception CriteriaException if an error occurs
    */
    public Object resolve( Object value ) 
      throws CriteriaException
    {
        if( value == null ) return null;
        if( value instanceof Artifact[] )
        {
            return value;
        }
        if( value instanceof String[] )
        {
            String[] specs = (String[]) value;
            Artifact[] artifacts = new Artifact[ specs.length ];
            for( int i=0; i<specs.length; i++ )
            {
                final String spec = specs[i];
                artifacts[i] = Artifact.createArtifact( spec );
            }
            return artifacts;
        }
        else if( value instanceof String )
        {
            ArrayList list = new ArrayList();
            String s = (String) value;
            StringTokenizer tokenizer = 
            new StringTokenizer( s, m_delimiter );
            while( tokenizer.hasMoreTokens() )
            {
               final String spec = tokenizer.nextToken();
               Artifact artifact = Artifact.createArtifact( spec );
               list.add( artifact );
            }
            return list.toArray( new Artifact[0] );
        }
        else
        {
            final String error = 
              "Don't know how to transform an instance of [" 
              + value.getClass().getName() 
              + " to a Artifact[].";
            throw new CriteriaException( error );
        }
    }
}
