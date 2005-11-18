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
public class CategoryDirective implements Serializable
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    /**
     * Constant category priority value for debug mode.
     */
    public static final String DEBUG = "DEBUG";

    /**
     * Constant category priority value for info mode.
     */
    public static final String INFO = "INFO";

    /**
     * Constant category priority value for warning mode.
     */
    public static final String WARN = "WARN";

    /**
     * Constant category priority value for error mode.
     */
    public static final String ERROR = "ERROR";

    /**
     * The logging category name.
     */
    private final String m_name;

    /**
     * The default logging priority.
     */
    private final String m_priority;

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
    public CategoryDirective( final String name, String priority )
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
        final String name, final String priority, final String target )
    {
        m_name = name;
        m_target = target;
        if( priority != null )
        {
            m_priority = priority.trim().toUpperCase();
        }
        else
        {
            m_priority = null;
        }
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
    public String getPriority()
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
        return ( equalName( test.getName() )
              && equalPriority( test.getPriority() )
              && equalTarget( test.getTarget() ) );
    }

    private boolean equalName( String other )
    {
        if( m_name == null )
        {
            return other == null;
        }
        else
        {
            return m_name.equals( other );
        }
    }

    private boolean equalPriority( String other )
    {
        if( m_priority == null )
        {
            return other == null;
        }
        else
        {
            return m_priority.equals( other );
        }
    }

    private boolean equalTarget( String other )
    {
        if( m_target == null )
        {
            return other == null;
        }
        else
        {
            return m_target.equals( other );
        }
    }

   /**
    * Return the instance hash code value.
    * @return the hash value
    */
    public int hashCode()
    {
        int hash = m_name.hashCode();
        if( m_priority != null )
        {
            hash ^= m_priority.hashCode();
        }
        if( m_target != null )
        {
            hash ^= m_target.hashCode();
        }
        return hash;
    }
}
