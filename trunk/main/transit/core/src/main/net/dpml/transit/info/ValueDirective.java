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

package net.dpml.transit.info;

import java.util.Arrays;

/**
 * A object resolvable from primitive arguments.
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ValueDirective extends AbstractDirective
{
    private final String m_method;
    private final String m_target;
    private final String m_value;
    private final ValueDirective[] m_args;
    private final boolean m_compound;

   /**
    * Create a new value descriptor using the default java.lang.String class as the base type.
    * @param value the construct value
    */
    public ValueDirective( String value )
    {
        this( null, null, value );
    }

   /**
    * Create a new construct using a supplied target definition.  The target argument 
    * may be either a classname or a symbolic object reference in the form ${[key]}.
    *
    * @param target a classname or symbolic object reference
    * @param value the construct value
    */
    public ValueDirective( String target, String value )
    {
        this( target, null, value );
    }

   /**
    * Create a new construct using a supplied target defintion.  The target argument 
    * may be either a classname or a symbolic reference in the form ${[key]}.  If the 
    * argument is symbolic it is resolved relative to a context map supplied by the 
    * application resolving construct values.  If the construct value is symbolic
    * the implementation will attempt to expand the reference relative to a context
    * map (if supplied) otherwise the implementation will attempt to expand the value 
    * using system properties.
    *
    * @param target a classname or symbolic reference
    * @param method the method to invoke on the target
    * @param value the construct value
    */
    public ValueDirective( String target, String method, String value )
    {
        super();
        m_target = target;
        m_method = method;
        m_value = value;
        m_args = new ValueDirective[0];
        m_compound = false;
    }

   /**
    * Create a new construct using a supplied target defintion.  The target argument 
    * may be either a classname or a symbolic reference in the form ${[key]}.  If the 
    * argument is symbolic it is resolved relative to a context map supplied by the 
    * application resolving construct values. Instance values resolved from the 
    * supplied Value[] will be used as constructor arguments when resolving the target.
    *
    * @param target the construct classname
    * @param args an array of unresolved parameter values
    */
    public ValueDirective( String target, ValueDirective[] args )
    {
        this( target, null, args );
    }
    
   /**
    * Create a new construct using a supplied target defintion.  The target argument 
    * may be either a classname or a symbolic reference in the form ${[key]}.  If the 
    * argument is symbolic it is resolved relative to a context map supplied by the 
    * application resolving construct values. Instance values resolved from the 
    * supplied Value[] will be used as method arguments when resolving the target.
    *
    * @param target the construct classname
    * @param method the method to invoke on the target
    * @param args an array of unresolved parameter values
    */
    public ValueDirective( String target, String method, ValueDirective[] args )
    {
        super();
        if( null == args )
        {
            m_args = new ValueDirective[0];
        }
        else
        {
            m_args = args;
        }
        m_value = null;
        m_target = target;
        m_method = method;
        m_compound = true;
    }
    
   /**
    * Return TRUE if this construct is a compund construct else FALSE.
    * @return TRUE if this ia a compound construct
    */
    public boolean isCompound()
    {
        return m_compound;
    }

   /**
    * Return the method name to be applied to the target object.
    * @return the method name
    */
    public String getMethodName()
    {
        return m_method;
    }

   /**
    * Return the set of nested values within this value.
    * @return the nested values array
    */
    public ValueDirective[] getValueDirectives()
    {
        return m_args;
    }

   /**
    * Return the base value of the resolved value.
    * @return the base value
    */
    public String getBaseValue()
    {
        return m_value;
    }

   /**
    * Return the classname of the resolved value.
    * @return the classname
    */
    public String getTargetExpression()
    {
        return m_target;
    }

   /**
    * Return a string representation of the construct.
    * @return the string value
    */
    public String toString()
    {
        if( !m_compound )
        {
            return "value "
              + " target: " + m_target 
              + " method: " + m_method 
              + " value: " + m_value;
        }
        else
        {
            return "value "
              + " target: " + m_target 
              + " method: " + m_method 
              + " values: " + m_args.length;
        }
    }
    
   /**
    * Compare this instance with a supplied object for equality.
    * @param other the other object
    * @return true if the supplied instance is equal to this instance
    */
    public boolean equals( Object other )
    {
        if( super.equals( other ) && ( other instanceof ValueDirective ) )
        {
            ValueDirective construct = (ValueDirective) other;
            if( !equals( m_target, construct.m_target ) )
            {
                return false;
            }
            if( m_compound != construct.m_compound )
            {
                return false;
            }
            if( !equals( m_method, construct.m_method ) )
            {
                return false;
            }
            if( m_compound )
            {
                return Arrays.equals( m_args, construct.m_args );
            }
            else
            {
                return equals( m_value, construct.m_value );
            }
        }
        else
        {
            return false;
        }
    }
    
   /**
    * Compute the instance hashcode value.
    * @return the hashcode
    */
    public int hashCode()
    {
        int hash = 0;
        hash ^= hashValue( m_target );
        hash ^= hashValue( m_method );
        hash ^= hashArray( m_args );
        hash ^= hashValue( m_value );
        return hash;
    }
}
