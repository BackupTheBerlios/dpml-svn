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

import java.util.Arrays;

import net.dpml.component.Directive;

import net.dpml.metro.info.PartReference;

/**
 * A context descriptor declares the context creation criteria for
 * the context instance and context entries.
 *
 * <p><b>XML</b></p>
 * <p>A context directive may contain multiple import statements.  Each import
 * statement corresponds to a request for a context value from the container.</p>
 * <pre>
 *    &lt;context class="<font color="darkred">MyContextClass</font>"&gt;
 *       &lt;entry key="<font color="darkred">special</font>"&gt;
 *         &lt;import key="<font color="darkred">urn:avalon:classloader</font>"/&gt;
 *       &lt;/entry&gt;
 *       &lt;entry key="<font color="darkred">xxx</font>"&gt;
 *         &lt;param class="<font color="darkred">MySpecialClass</font>"&gt;
 *           &lt;param&gt<font color="darkred">hello</font>&lt;/param&gt;
 *           &lt;param class="<font color="darkred">java.io.File</font>"&gt;<font color="darkred">../lib</font>&lt;/param&gt;
 *         &lt;/param&gt;
 *       &lt;/entry&gt;
 *    &lt;/context&gt;
 * </pre>
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class ContextDirective extends AbstractDirective
{
    //--------------------------------------------------------------------------
    // static
    //--------------------------------------------------------------------------
    
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;
    
    private static final PartReference[] EMPTY_REFS = new PartReference[0];

    //--------------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------------
    
   /**
    * The set of entry directives.
    */
    private final PartReference[] m_entries;

   /**
    * The context implementation classname.
    */
    private final String m_classname;

    //--------------------------------------------------------------------------
    // constructors
    //--------------------------------------------------------------------------
    
   /**
    * Creation of a context directive.
    */
    public ContextDirective()
    {
        this( new PartReference[0] );
    }

   /**
    * Creation of a context directive
    * @param entries the set of entry descriptors
    */
    public ContextDirective( final PartReference[] entries )
    {
        this( null, entries );
    }

   /**
    * Creation of a new file target.
    * @param classname the context implementation class
    * @param entries the set of entry descriptors
    */
    public ContextDirective( final String classname, final PartReference[] entries )
    {
        m_classname = classname;
        if( entries != null )
        {
            for( int i=0; i<entries.length; i++ )
            {
                PartReference ref = entries[i];
                if( null == ref )
                {
                    throw new NullPointerException( "entry" );
                }
            }
            m_entries = entries;
        }
        else
        {
            m_entries = EMPTY_REFS;
        }
    }

    //--------------------------------------------------------------------------
    // implementation
    //--------------------------------------------------------------------------
    
   /**
    * Return the classname of the context implementation to use.
    * @return the classname
    */
    public String getClassname()
    {
        return m_classname;
    }

   /**
    * Return the set of entry directives.
    * @return the entries
    */
    public PartReference[] getDirectives()
    {
        return m_entries;
    }

   /**
    * Return part reference defining the value for the requested entry.
    * @param key the context entry key
    * @return the part reference corresponding to the supplied key or null if the
    *   key is unknown
    */
    public PartReference getPartReference( String key )
    {
        for( int i = 0; i < m_entries.length; i++ )
        {
            PartReference entry = m_entries[ i ];
            if( entry.getKey().equals( key ) )
            {
                return entry;
            }
        }
        return null;
    }

   /**
    * Return part defining the value for the requested entry.
    * @param key the context entry key
    * @return the part defintion corresponding to the supplied key or null if the
    *   key is unknown
    */
    public Directive getPartDirective( String key )
    {
        PartReference ref = getPartReference( key );
        if( null != ref )
        {
            return ref.getDirective();
        }
        else
        {
            return null;
        }
    }

   /**
    * Test if the supplied object is equal to this object.
    * @param other the object to compare with this instance
    * @return TRUE if the supplied object is equal to this object
    */
    public boolean equals( Object other )
    {
        if( !super.equals( other ) )
        {
            return false;
        }
        else if( !( other instanceof ContextDirective ) )
        {
            return false;
        }
        else
        {
            ContextDirective context = (ContextDirective) other;
            if( !equals( m_classname, context.m_classname ) )
            {
                return false;
            }
            else
            {
                return Arrays.equals( m_entries, context.m_entries );
            }
        }
    }

   /**
    * Return the hashcode for the instance.
    * @return the instance hashcode
    */
    public int hashCode()
    {
        int hash = super.hashCode();
        hash ^= hashValue( m_classname );
        hash ^= hashArray( m_entries );
        return hash;
    }
}
