/*
 * Copyright 2004 Niclas Hedman.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dpml.http.impl;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Internal string tokenizer utility.
 */
final class StringUtils
{
    private StringUtils()
    {
        // static util
    }
    
   /**
    * Tokenize the supplied string using a ' ,' delimiter.
    * @param string the string to tokenize
    * @return the array of values
    */
    public static String[] tokenize( String string )
    {
        ArrayList result = new ArrayList();
        StringTokenizer st = new StringTokenizer( string, " ,", false );
        while( st.hasMoreTokens() )
        {
            result.add( st.nextToken() );
        }
        String[] retVal = new String[ result.size() ];
        result.toArray( retVal );
        return retVal;
    }
}
 