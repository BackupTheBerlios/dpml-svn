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

package net.dpml.component.data;

import java.io.Serializable;

import net.dpml.part.Part;
import net.dpml.component.info.PartReference;

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
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: ContextDirective.java 2991 2005-07-07 00:00:04Z mcconnell@dpml.net $
 */
public final class ContextDirective implements Serializable
{
    //--------------------------------------------------------------------------
    // static
    //--------------------------------------------------------------------------
    
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    //--------------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------------
    
   /**
    * The set of entry directives.
    */
    private final PartReference[] m_entries;

   /**
    * The constext implementation classname.
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
            m_entries = entries;
        }
        else
        {
            m_entries = new PartReference[0];
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
    public Part getPartDirective( String key )
    {
        PartReference ref = getPartReference( key );
        if( null != ref )
        {
            return ref.getPart();
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
        if( null == other )
        {
            return false;
        }
        else
        {
            if( other instanceof ContextDirective )
            {
                ContextDirective context = (ContextDirective) other;
                if( null == m_classname )
                {
                    if( null != context.getClassname() )
                    {
                        return false;
                    }
                }
                else if( !m_classname.equals( context.getClassname() ) )
                {
                    return false;
                }
                if( getDirectives().length != context.getDirectives().length )
                {
                    return false;
                }
                else
                {
                    PartReference[] mine = getDirectives();
                    PartReference[] yours = context.getDirectives();
                    for( int i=0; i<mine.length; i++ )
                    {
                        PartReference p = mine[i];
                        PartReference q = yours[i];
                        if( false == p.equals( q ) )
                        {
                            return false;
                        }
                    }
                    return true;
                }
            }
            else
            {
                return false;
            }
        }
    }

   /**
    * Return the hashcode for the instance.
    * @return the instance hashcode
    */
    public int hashCode()
    {
        int hash = 9;
        if( null != m_classname )
        {
           hash ^= m_classname.hashCode();
        }
        for( int i=0; i<m_entries.length; i++ )
        {
            hash ^= m_entries[i].hashCode();
        }
        return hash;
    }
}
