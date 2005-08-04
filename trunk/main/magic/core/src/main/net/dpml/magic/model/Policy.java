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

package net.dpml.magic.model;


/**
 * Policy datastructure that declares a build, test, or runtime association.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class Policy
{
   /**
    * Constant declaring a dependency as applicable to all categories.
    */
    public static final int ANY = -1;

   /**
    * Constant declaring a build time dependency.
    */
    public static final int BUILD = 0;

   /**
    * Constant declaring a test time dependency.
    */
    public static final int TEST = 1;

   /**
    * Constant declaring a runtime time dependency.
    */
    public static final int RUNTIME = 2;

   /**
    * Enabled constant.
    */
    public static final boolean ENABLED = true;

   /**
    * Disabled constant.
    */
    public static final boolean DISABLED = false;

    private final boolean m_build;
    private final boolean m_test;
    private final boolean m_runtime;

   /**
    * Return a policy value as an int given a supplied policy string argument.
    * @param policy the policy value
    * @return the value as a policy int
    * @exception IllegalArgumentException if the supplied value is invalid
    */
    public static int getPolicy( final String policy ) throws IllegalArgumentException
    {
        String p = policy.trim().toLowerCase();
        if( "runtime".equals( p ) )
        {
            return RUNTIME;
        }
        else if( "test".equals( p ) )
        {
            return TEST;
        }
        else if( "build".equals( p ) )
        {
            return BUILD;
        }
        else if( "any".equals( p ) )
        {
            return ANY;
        }
        else
        {
            final String error =
              "Policy value [" + policy + "] out of range.";
            throw new IllegalArgumentException( error );
        }
    }

   /**
    * Creation of a new policy object with build, test and runtime dependencies.
    */
    public Policy()
    {
        this( true, true, true );
    }

   /**
    * Creation of a new policy object.
    * @param build TRUE if policy is build time
    * @param test TRUE if policy is test time
    * @param runtime TRUE if policy is runtime
    */
    public Policy( final boolean build, final boolean test, final boolean runtime )
    {
        m_build = build;
        m_test = test;
        m_runtime = runtime;
    }

   /**
    * Return the build time enabled state.
    * @return TRUE if build time enabled
    */
    public boolean isBuildEnabled()
    {
        return m_build;
    }

   /**
    * Return the test time enabled state.
    * @return TRUE if test time enabled
    */
    public boolean isTestEnabled()
    {
        return m_test;
    }

   /**
    * Return the runtime enabled state.
    * @return TRUE if runtime time enabled
    */
    public boolean isRuntimeEnabled()
    {
        return m_runtime;
    }

   /**
    * Return TRUE if this policy matches the supplied mode.
    * @param mode the made value to match against
    * @return TRUE if the mode matches
    */
    public boolean matches( final int mode )
    {
        if( mode == BUILD )
        {
            return isBuildEnabled();
        }
        else if( mode == TEST )
        {
            return isTestEnabled();
        }
        else if( mode == RUNTIME )
        {
            return isRuntimeEnabled();
        }
        else
        {
            return true;
        }
    }

   /**
    * Return the string representation of the policy instance.
    * @return the string value
    */
    public String toString()
    {
        return "{" + m_build + ", " + m_test + ", " + m_runtime + "}";
    }

   /**
    * Compare this object with another for equality.
    * @param other the other object
    * @return the equality status
    */
    public boolean equals( final Object other )
    {
        if( !( other instanceof Policy ) )
        {
            return false;
        }
        final Policy policy = (Policy) other;
        if( m_build != policy.m_build )
        {
            return false;
        }
        if( m_test != policy.m_test )
        {
            return false;
        }
        if( m_runtime != policy.m_runtime )
        {
            return false;
        }
        return true;
    }

   /**
    * Return the hashcode for this instance.
    * @return the hashcode value
    */
    public int hashCode()
    {
        int hash = MAGIC_A;
        if( m_build )
        {
            hash = hash + MAGIC_B;
        }
        else
        {
            hash = hash - MAGIC_C;
        }
        if( m_test )
        {
            hash = hash + MAGIC_G;
        }
        else
        {
            hash = hash - MAGIC_D;
        }
        if( m_runtime )
        {
            hash = hash + MAGIC_E;
        }
        else
        {
            hash = hash - MAGIC_F;
        }
        return hash;
    }

    private static final int MAGIC_A = 913823634;
    private static final int MAGIC_B = 611623192;
    private static final int MAGIC_C = 171623434;
    private static final int MAGIC_D = 725635214;
    private static final int MAGIC_E = 1237983478;
    private static final int MAGIC_F = 2094897234;
    private static final int MAGIC_G = 145126277;

}
