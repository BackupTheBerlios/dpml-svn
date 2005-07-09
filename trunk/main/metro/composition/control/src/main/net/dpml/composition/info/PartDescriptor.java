/*
 * Copyright 2004 Stephen J. McConnell.
 * Copyright 1999-2004 The Apache Software Foundation
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

package net.dpml.composition.info;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.beans.Introspector;

/**
 * A part descriptor identifies a key, access semantics and return type
 * based on the method signatures within an internal Parts interface.
 *
 * <source>
 * public class DefaultWidget
 * {
 *    public interface Parts
 *    {
 *        Gizmo createGizmo();
 *        Gizmo getGizmo();
 *        Gizmo getGizmo( Object id );
 *        void releaseGizmo( Gizmo gizmo );
 *    }
 *
 *    public DefaultWidget( Parts parts )
 *    {
 *        // does stuff with the supplied parts
 *    }
 * }
 * 
 * </source>
 * 
 * In the above example the Parts interface declares three directivesm each containing
 * the key 'gizmo', the Gizmo classname as the return type, and assigned with
 * the CREATE_METHOD, GET_METHOD and RELEASE_METHOD semantics respectively.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: ReferenceDescriptor.java 1874 2005-02-22 17:47:49Z mcconnell $
 */
public final class PartDescriptor
        implements Serializable
{
    public static final int GET = 1;
    public static final int RELEASE = -1;

    public static final String GET_KEY = "get";
    public static final String RELEASE_KEY = "release";

    public static final String CONTEXT_MAP_KEY = "ContextMap";
    public static final String CONTEXT_MANAGER_KEY = "ContextManager";
    public static final String COMPONENT_KEY = "Component";

    public static String toString( int semantic )
    {
        if( GET == semantic )
        {
            return GET_KEY;
        }
        else if( RELEASE == semantic )
        {
            return RELEASE_KEY;
        }
        else
        {
            return "?";
        }
    }

    public static String getPartPostfix( Method method )
    {
        String name = method.getName();
        if( name.endsWith( CONTEXT_MANAGER_KEY ) )
        {
            return CONTEXT_MANAGER_KEY;
        }
        else if( name.endsWith( CONTEXT_MAP_KEY ) )
        {
            return CONTEXT_MAP_KEY;
        }
        else if( name.endsWith( COMPONENT_KEY ) )
        {
            return COMPONENT_KEY;
        }
        else
        {
            return null;
        }
    }

    public static int getPartSemantic( Method method )
    {
        String name = method.getName();
        if( name.startsWith( GET_KEY ) )
        {
            return GET;
        }
        else if( name.startsWith( RELEASE_KEY ) )
        {
            return RELEASE;
        }
        else
        {
            final String error = 
              "Unrecognized part accessor method signature ["
              + name 
              + "]";
            throw new IllegalArgumentException( error );
        }
    }

    public static String getPartKey( Method method )
    {
        int semantic = getPartSemantic( method );
        return getPartKey( method, semantic );
    }

    public static String getPartKey( Method method, int semantic )
    {
        String name = method.getName();
        if( GET == semantic )
        {
            if( name.endsWith( CONTEXT_MANAGER_KEY ) )
            {
                int n = CONTEXT_MANAGER_KEY.length();
                int j = name.length() - n;
                String substring = name.substring( 0, j );
                return formatKey( substring, 3 );
            }
            else if( name.endsWith( CONTEXT_MAP_KEY ) )
            {
                int n = CONTEXT_MAP_KEY.length();
                int j = name.length() - n;
                String substring = name.substring( 0, j );
                return formatKey( substring, 3 );
            }
            else if( name.endsWith( COMPONENT_KEY ) )
            {
                int n = COMPONENT_KEY.length();
                int j = name.length() - n;
                String substring = name.substring( 0, j );
                return formatKey( substring, 3 );
            }
            else
            {
               return formatKey( name, 3 );
            }
        }
        else if( RELEASE == semantic )
        {
            return formatKey( name, 7 );
        }
        else
        {
            final String error = 
              "Unrecognized part accessor method signature ["
              + name 
              + "]";
            throw new IllegalArgumentException( error );
        }
    }

    /**
     * The key.
     */
    private final String m_key;

    /**
     * The set of operations applicable to the assigned part.
     */
    private final Operation[] m_operations;

    /**
     * Construct a part descriptor with the supplied key, type and default get semantics.
     *
     * @param key the key resolved from the method name
     * @param operations an array of operations
     * @exception NullPointerException if key or operations array is null
     */
    public PartDescriptor( final String key, final Operation[] operations ) throws NullPointerException
    {
        if ( null == key )
        {
            throw new NullPointerException( "key" );
        }
        if ( null == operations )
        {
            throw new NullPointerException( "operations" );
        }

        m_key = key;
        m_operations = operations;
    }

    /**
     * Return the key.
     *
     * @return the key
     */
    public String getKey()
    {
        return m_key;
    }

    /**
     * Return the array of operations.
     *
     * @return the operations array
     */
    public Operation[] getOperations()
    {
        return m_operations;
    }

    public Operation getOperation( int semantic, String postfix )
    {
        Operation[] operations = getOperations();
        for( int i=0; i<operations.length; i++ )
        {
            Operation operation = operations[i];
            if( semantic == operation.getSemantic() )
            {
                if( null == postfix )
                {
                    if( null == operation.getPostfix() )
                    {
                        return operation;
                    }
                }
                else
                {
                    if( postfix.equals( operation.getPostfix() ) )
                    {
                        return operation;
                    }
                }
            }
        }
        return null;
    }

    public String getReturnType()
    {
        Operation[] operations = getOperations();
        for( int i=0; i<operations.length; i++ )
        {
            Operation operation = operations[i];
            if( GET == operation.getSemantic() )
            {
                if( null == operation.getPostfix() )
                {
                    return operation.getType();
                }
            }
        }
        return null;
    }

    

    /**
     * Compare this object with another for equality.
     * @param other the object to compare this object with
     * @return TRUE if the supplied object is equal
     */
    public boolean equals( Object other )
    {
        boolean match = false;

        if( false == ( other instanceof PartDescriptor ) )
        {
            return false;
        }
        else
        {
            PartDescriptor part = (PartDescriptor) other;
            if( false == m_key.equals( part.getKey() ) )
            {
                return false;
            }
            else
            {
                return equals( getOperations(), part.getOperations() );
            }
        }
    }

    private boolean equals( Operation[] ops1, Operation[] ops2 )
    {
        if( ops1.length != ops2.length )
        {
                return false;
        }
        for( int i=0; i<ops1.length; i++ )
        {
            if( false == ops1[i].equals( ops2[i] ) )
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the cashcode.
     * @return the hascode value
     */
    public int hashCode()
    {
        int hash = m_key.hashCode();
        for( int i=0; i<m_operations.length; i++ )
        {
            Operation operation = m_operations[i];
            hash ^= operation.hashCode();
        }
        return hash;
    }

    public static class Operation implements Serializable
    {
        private final String m_type;
        private int m_semantic;
        private String m_postfix;

        public Operation( int semantic, String type )
        {
            this( semantic, null, type );
        }

        public Operation( int semantic, String postfix, String type )
        {
            if( null == type )
            {
                throw new NullPointerException( "type" );
            }
            m_type = type;
            m_semantic = semantic;
            m_postfix = postfix;
        }

        public String getType()
        {
            return m_type;
        }

        public int getSemantic()
        {
            return m_semantic;
        }

        public String getPostfix()
        {
            return m_postfix;
        }

        public boolean equals( Object other )
        {
            if( null == other )
            {
                return false;
            }
            else if( false == ( other instanceof Operation ) )
            {
                return false;
            }
            else
            {
                Operation a = (Operation) other;
                if( false == m_type.equals( a.getType() ) )
                {
                    return false;
                }
                else if( false == ( m_semantic == a.getSemantic() ) )
                {
                    return false;
                }
                else
                {
                    if( null == m_postfix )
                    {
                        return ( null == a.getPostfix() );
                    }
                    else
                    {
                        return m_postfix.equals( a.getPostfix() );
                    }
                }
            }
        }
        
        public int hashCode()
        {
            int hash = m_semantic;
            hash ^= m_type.hashCode();
            if( null != m_postfix )
            {
                hash ^= m_postfix.hashCode();
            }
            return hash;
        }
    }

    private static String formatKey( String method, int offset )
    {
        String string = method.substring( offset );
        return formatKey( string );
    }

    private static String formatKey( String key )
    {
        return Introspector.decapitalize( key );
    }
}
