/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.test.array;

/**
 * Test the supply of a string array as a context value.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@ 
 */
public class ArrayTestComponent
{
    //------------------------------------------------------------------
    // concerns
    //------------------------------------------------------------------

   /**
    * The construction criteria.
    */
    public interface Context
    {
       /**
        * Return the assigned values
        * @return the values array
        */
        String[] getValues();
    }

    //------------------------------------------------------------------
    // state
    //------------------------------------------------------------------

   /**
    * The assigned context instance.
    */
    private final Context m_context;

    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------

   /**
    * Creation of a new test component instance.
    * @param context a component context 
    */
    public ArrayTestComponent( final Context context )
    {
        m_context = context;
    }

    //------------------------------------------------------------------
    // Example
    //------------------------------------------------------------------
   
   /**
    * Return the string array assigned to the context.
    * @return the color value
    */
    public String[] getValues()
    {
        return m_context.getValues();
    }
}
