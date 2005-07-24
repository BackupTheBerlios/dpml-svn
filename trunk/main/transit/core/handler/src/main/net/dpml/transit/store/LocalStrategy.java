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

package net.dpml.transit.store;

/**
 * The LocalStrategy  class declares the creation criteria for an instance
 * that is locally available withuin the classloader of the invoking client.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class LocalStrategy extends Strategy
{
    private String m_classname;
    private boolean m_bootstrap;

   /**
    * Creation of a new llocal stratey instance.
    * @param classname the classname of the locally available class
    * @param bootstrap the bootstrap policy
    */
    public LocalStrategy( String classname, boolean bootstrap )
    {
        m_classname = classname;
        m_bootstrap = bootstrap;
    }

   /**
    * Return the local classname.
    * @return the name of the local class
    */
    public String getClassname()
    {
        return m_classname;
    }

   /**
    * Return the bootstrap policy.
    * @return the bootstrap policy value
    */
    public boolean isBootstrap()
    {
        return m_bootstrap;
    }

   /**
    * Test if this object is equal to the supplied object.
    * @param other the other object
    * @return TRUE if the supplied object is equal to this object
    */
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        else if( other instanceof LocalStrategy )
        {
            LocalStrategy local = (LocalStrategy) other;
            if( false == m_classname.equals( local.getClassname() ) )
            {
                return false;
            }
            else if( isBootstrap() != local.isBootstrap() )
            {
                return false;
            }
            else
            {
                return true;
            }
        }
        else
        {
            return false;
        }
    }

   /**
    * Return the object hashcode.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = new Boolean( m_bootstrap ).hashCode();
        hash ^= m_classname.hashCode();
        return hash;
    }
}
