/*
 * Copyright 2004 Stephen J. McConnell.
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

package net.dpml.metro.data;

import java.io.Serializable;

import net.dpml.metro.info.Priority;

/**
 * A logging category descriptor hierachy.  The descriptor contains a category name, a
 * optional priority value, and an optional target.  If the priority or target values
 * null, the resulting value will be derived from the parent category desciptor. A
 * category descriptor may 0-n subsidiary categories.  CategoryDirective names are relative.
 * For example, the category "orb" will appear as "my-app.orb" if the parent category
 * name is "my-app".
 *
 * <p><b>XML</b></p>
 * <pre>
 *    &lt;categories priority="<font color="darkred">INFO</font>"&gt;
 *      &lt;category priority="<font color="darkred">DEBUG</font>"  name="<font color="darkred">loader</font>" /&gt;
 *      &lt;category priority="<font color="darkred">WARN</font>"  name="<font color="darkred">types</font>" /&gt;
 *      &lt;category priority="<font color="darkred">ERROR</font>"  name="<font color="darkred">types.builder</font>" target="<font color="darkred">default</font>"/&gt;
 *      &lt;category name="<font color="darkred">profiles</font>" /&gt;
 *      &lt;category name="<font color="darkred">lifecycle</font>" /&gt;
 *      &lt;category name="<font color="darkred">verifier</font>" /&gt;
 *    &lt;/categories&gt;
 * </pre>
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class CategoryDirective extends AbstractDirective implements Serializable
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    /**
     * The logging category name.
     */
    private final String m_name;

    /**
     * The default logging priority.
     */
    private final Priority m_priority;

    /**
     * The default logging target.
     */
    private final String m_target;

    /**
     * Creation of a new CategoryDirective using a supplied name.
     *
     * @param name the category name
     */
    public CategoryDirective( final String name )
    {
        this( name, null, null );
    }

    /**
     * Creation of a new CategoryDirective using a supplied name and priority.
     *
     * @param name the category name
     * @param priority the category priority - DEBUG, INFO, WARN, or ERROR
     */
    public CategoryDirective( final String name, Priority priority )
    {
        this( name, priority, null );
    }

    /**
     * Creation of a new CategoryDirective using a supplied name, priority, target and
     * collection of subsidiary categories.
     *
     * @param name the category name
     * @param priority the category priority - DEBUG, INFO, WARN, or ERROR
     * @param target the name of a logging category target
     *
     */
    public CategoryDirective(
        final String name, final Priority priority, final String target )
    {
        m_name = name;
        m_target = target;
        m_priority = priority;
    }

    /**
     * Return the category name.
     *
     * @return the category name
     */
    public String getName()
    {
        return m_name;
    }

    /**
     * Return the logging priority for the category.
     *
     * @return the logging priority for the category
     */
    public Priority getPriority()
    {
        return m_priority;
    }

    /**
     * Return the default log target for the category.
     *
     * @return the default target name
     */
    public String getTarget()
    {
        return m_target;
    }

   /**
    * Test this object for equality with the supplied object.
    * @param other the other object
    * @return true if the objects are equal
    */
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }

        if( !( other instanceof CategoryDirective ) )
        {
            return false;
        }

        CategoryDirective test = (CategoryDirective) other;
        if( !equals( m_name, test.m_name ) )
        {
            return false;
        }
        else if( !equals( m_priority, test.m_priority ) )
        {
            return false;
        }
        else
        {
            return equals( m_target, test.m_target );
        }
    }

   /**
    * Return the instance hash code value.
    * @return the hash value
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= hashValue( m_name );
        hash ^= hashValue( m_priority );
        hash ^= hashValue( m_target );
        return hash;
    }
}
