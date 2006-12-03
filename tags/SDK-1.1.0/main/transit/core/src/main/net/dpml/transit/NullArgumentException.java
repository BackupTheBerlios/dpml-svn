/*
 * Copyright 2005 Niclas Hedhman.
 * Copyright 2005 Stephen McConnell
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

package net.dpml.transit;


/** 
 * Exception thrown when the argument to a method or constructor is
 *  <i>null</i> and not handled by the method/constructor/class.
 *
 * The argument in the only constructor of this exception should only
 * take the name of the declared argument that is null, for instance;
 * <code><pre>
 *     public Person( String name, int age )
 *     {
 *         if( name == null )
 *             throw new NullArgumentException( "name" );
 *         if( age > 120 )
 *             throw new IllegalArgumentException( "age > 120" );
 *         if( age < 0 )
 *             throw new IllegalArgumentException( "age < 0" );
 *     }
 * </pre></code>
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class NullArgumentException extends IllegalArgumentException
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    /** Constructor taking the name of the argument that was null.
     * @param argumentName the source code name of the argument that caused
     *        this exception.
     */
    public NullArgumentException( String argumentName )
    {
        super( argumentName );
    }
}