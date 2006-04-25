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

import net.dpml.lang.AbstractDirective;

/**
 * Definition of the RMIC compilation criteria.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public abstract class JavacDirective extends DataCollectionDirective
{
   /**
    * Reserved datatype key.
    */
    public static final String KEY = "compile-main";
    
   /**
    * Creation of a new data directive.
    * @param key the unique datatype key
    */
    public JavacDirective( String key, PatternDirective[] patterns )
    {
        super( key, patterns );
    }
}
