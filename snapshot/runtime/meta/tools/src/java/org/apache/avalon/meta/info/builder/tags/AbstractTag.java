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

package org.apache.avalon.meta.info.builder.tags;


import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.Type;

import org.apache.avalon.framework.Version;

/**
 * A doclet tag representing the name of the Type.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: AbstractTag.java 36802 2004-08-24 02:42:21Z tbennett $
 */
public class AbstractTag
{
   /**
    * The dependency tag type parameter name.
    */
    public static final String TYPE_PARAM = "type";

   /**
    * The dependency tag version parameter name.
    */
    public static final String VERSION_PARAM = "version";


    private JavaClass m_class;

   /**
    * Class constructor.
    * @param clazz the javadoc class descriptor
    */
    public AbstractTag( final JavaClass clazz )
    {
        m_class = clazz;
    }

   /**
    * Return the javadoc class descriptor.
    * @return the javadoc class descriptor
    */
    protected JavaClass getJavaClass()
    {
        return m_class;
    }

   /**
    * Return the user defined namespace for avalon tags. The value returned is 
    * established by the javadoc tag 'avalon.namespace [namespace-value]' where
    * [namespace-value] is a string representing the namespace identifier.
    * @return the namespace tag used to represent the avalon.meta tag space
    * @exception IllegalArgumentException if the namespace tag is declared by does not
    *   contain a value
    */
    public String getNS() throws IllegalArgumentException
    {
        return Tags.NAMESPACE;
    }

   /**
    * Return the user defined namespace for avalon tags including the
    * standard namespace delimiter.  The value returned is established
    * by the javadoc tag 'avalon.namespace [namespace-value]' where
    * [namespace-value] is a string representing the namespace identifier
    * with the namespace delimiter appended.
    * @return the namespace tag with delimeter
    * @exception IllegalArgumentException if the namespace tag is declared by does not
    *   contain a value
    */
    public String getNSD() throws IllegalArgumentException
    {
        return getNS() + Tags.DELIMITER;
    }

    /**
     * Retrieve specified named parameter from tag. If the parameter
     * does not exist then return specified default value.
     *
     * @param tag the tag
     * @param name the name of parameter
     * @param defaultValue the default value
     * @return the value of named parameter
     */
    protected String getNamedParameter( final DocletTag tag,
                                        final String name,
                                        final String defaultValue )
    {
        String value = tag.getNamedParameter( name );
        if( null == value )
        {
            return defaultValue;
        }
        value = value.trim();
        if( value.startsWith( "\"" ) || value.startsWith( "'" ) )
        {
            value = value.substring( 1 );
        }
        if( value.endsWith( "\"" ) || value.endsWith( "'" ) )
        {
            value = value.substring( 0, value.length() - 1 );
        }
        return value;
    }

    /**
     * Retrieve specified named parameter from tag. If the parameter
     * does not exist then throw an exception.
     *
     * @param tag the tag
     * @param name the name of parameter
     * @return the value of named parameter
     */
    protected String getNamedParameter( final DocletTag tag, final String name )
    {
        final String value = getNamedParameter( tag, name, null );
        if( null == value )
        {
            final String message =
                "Malformed tag '" + tag.getName() + "'. "
                + "Missing required parameter '" + name + "'";
            throw new IllegalArgumentException( message );
        }
        return value;
    }

    /**
     * Resolve a version form the supplied string.
     *
     * @param version the explicit version
     * @param type the unresolved type in the classname:version format
     * @return the version or null if no version specified
     */
    protected Version resolveVersion( final String version, final String type )
    {
        if( version != null )
        {
            return Version.getVersion( version );
        }
        if( type.indexOf(":") > -1 )
        {
            return Version.getVersion( type.substring( type.indexOf(":") + 1 ) );
        }
        return null;
    }

    /**
     * Resolve a version form the supplied string.
     *
     * @param type the unresolved type
     * @return the version or null if no version specified
     */
    protected Version resolveVersion( final String type )
    {
        if( type.indexOf(":") > -1 )
        {
            return Version.getVersion( type.substring( type.indexOf(":") + 1 ) );
        }
        return null;
    }

    /**
     * Resolve the specified type.
     * Resolving essentially means finding the fully qualified name of
     * a class from just it's short name.
     *
     * @param type the unresolved type classname
     * @return the resolved type classname
     */
    protected String resolveType( final String type )
    {
        final String resolvedType;
        if( type.indexOf(":") > -1 )
        {
            resolvedType = 
              this.doResolveType( getJavaClass(), 
              type.substring( 0, type.indexOf(":") ) );
        }    
        else
        {
            resolvedType = doResolveType( getJavaClass(), type );
        }
        if( resolvedType == null )
        {
            return type;
        }
        else 
        {
            return resolvedType;
        }
    }

    private String doResolveType( final JavaClass clazz, final String type )
    {
        final String resolvedType = clazz.getParentSource().resolveType( type );
        if( resolvedType != null )
        {
            return resolvedType;
        }
        else if( clazz.getSuperJavaClass() == null )
        {
            return null;
        }
        else 
        {
            return doResolveType( clazz.getSuperJavaClass(), type );
        }
    }

    /**
     * Retrieves all methods in the inheritance graph with specified name and 
     * one parameter of specified type. The methods must also return void.
     *
     * @param methodName the name of the methods
     * @param parameterType the class name of parameter
     * @return an array of such methods
     */
    protected JavaMethod[] getLifecycleMethods( final String methodName,
						    final String parameterType )
    {
	List result = new ArrayList();
	findLifecycleMethod( result, getJavaClass(), methodName, parameterType );
	return (JavaMethod[]) result.toArray( new JavaMethod[ result.size() ] );
    }

    private void findLifecycleMethod( 
            final List result,
            final JavaClass clazz,
            final String methodName,
            final String parameterType )
    {
        final JavaMethod[] methods = clazz.getMethods();
        for( int i = 0; i < methods.length; i++ )
        {
            final JavaMethod method = methods[ i ];
            if( methodName.equals( method.getName() )
                && method.getReturns().equals( new Type( "void", 0 ) )
                && method.getParameters().length == 1
                && method.getParameters()[ 0 ].getType().getValue().equals( parameterType ) )
            {
                result.add( method );
		    break;		
            }
        }

        if( clazz.getSuperJavaClass() != null )
        {
            this.findLifecycleMethod( 
              result, clazz.getSuperJavaClass(), methodName, parameterType );
        }
    }

    JavaMethod[] findTaggedMethods( final JavaClass clazz, String key )
    {
        ArrayList list = new ArrayList();
        return findTaggedMethods( clazz, key, list );
    }

    private JavaMethod[] findTaggedMethods( final JavaClass clazz, String key, List list )
    {
        final JavaMethod[] methods = clazz.getMethods();
        for( int i=0; i<methods.length; i++ )
        {
            JavaMethod method = methods[i];
            final DocletTag tag = 
              method.getTagByName( key );
            if( tag != null ) list.add( method );
        }

        if( clazz.getSuperJavaClass() != null )
        {
            return this.findTaggedMethods( clazz.getSuperJavaClass(), key, list );
        }

        return (JavaMethod[]) list.toArray( new JavaMethod[0] );

    }

    JavaMethod[] findConstructorMethods( final JavaClass clazz, String key )
    {
        ArrayList list = new ArrayList();
        return findConstructorMethods( clazz, key, list );
    }

    private JavaMethod[] findConstructorMethods( final JavaClass clazz, String key, List list )
    {
        final JavaMethod[] methods = clazz.getMethods();
        for( int i=0; i<methods.length; i++ )
        {
            JavaMethod method = methods[i];
            if( method.isConstructor() && method.isPublic() )
            {
                final DocletTag tag = 
                  method.getTagByName( key );
                if( tag != null ) list.add( method );
            }
        }
        return (JavaMethod[]) list.toArray( new JavaMethod[0] );
    }
}
