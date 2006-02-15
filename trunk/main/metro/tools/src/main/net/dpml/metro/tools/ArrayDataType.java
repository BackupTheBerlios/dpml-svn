/*
 * Copyright (c) 2005 Stephen J. McConnell
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

package net.dpml.metro.tools;

import java.util.ArrayList;
import java.util.List;

import net.dpml.transit.Value;
import net.dpml.transit.Construct.Array;

import org.apache.tools.ant.BuildException;

/**
 * Defintion of a context entry parameter directive.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ArrayDataType implements ValueBuilder
{
    private String m_classname;
    private List m_params = new ArrayList();

   /**
    * Set the context entry classname.
    * @param classname the context entry classname
    */
    public void setClass( final String classname )
    {
        m_classname = classname;
    }

   /**
    * Return the context entry parameter classname.
    * @return the classname
    */
    public String getClassname()
    {
        return m_classname;
    }

   /**
    * Create, assign and return a new nested entry constructor parameter.
    * @param param the configured value data type
    */
    public void addConfiguredValue( ValueDataType param )
    {
        if( null == param.getClassname() )
        {
            param.setClass( m_classname );
        }
        m_params.add( param );
    }
    
   /**
    * Create, assign and return a new nested entry constructor parameter.
    * @param param the configured array data type
    */
    public void addConfiguredArray( ArrayDataType param )
    {
        if( null == param.getClassname() )
        {
            param.setClass( m_classname );
        }
        m_params.add( param );
    }

   /**
    * Return the set of nested param directives.
    * @return the params
    */
    private ValueBuilder[] getValueBuilders()
    {
        return (ValueBuilder[]) m_params.toArray( new ValueBuilder[0] );
    }
    
   /**
    * Build a value datastructure.
    * @param classloader the working classloader
    * @return the serializable value descriptor
    */
    public Value buildValue( ClassLoader classloader )
    {
        String classname = getClassname();
        Class base = getBaseClass( classloader, classname );
        try
        {
            ValueBuilder[] params = getValueBuilders();
            Value[] values = new Value[ params.length ];
            for( int i=0; i<values.length; i++ )
            {
                ValueBuilder p = params[i];
                Class target = p.getTargetClass( classloader );
                if( base.isAssignableFrom( target ) )
                {
                    values[i] = p.buildValue( classloader );
                }
                else
                {
                    final String error = 
                      "A value entry of the type [" 
                      + target.getName()
                      + "] is not assignable to the array class ["
                      + base.getName() 
                      + "].";
                    throw new BuildException( error );
                }
            }
            return new Array( classname, values );
        }
        catch( BuildException e )
        {
            throw e;
        }
        catch( Exception e )
        {
            final String error =
              "An error occured while building array datatype.";
            throw new BuildException( error, e );
        }
    }
    
   /**
    * Return the base classname.
    * @param classloader the working classloader
    * @return the target class
    */
    public Class getTargetClass( ClassLoader classloader )
    {
        String classname = getClassname();
        return getBaseClass( classloader, classname );
    }
    
    private Class getBaseClass( ClassLoader classloader, String classname )
    {
        if( null == classname )
        {
            return Object.class;
        }
        else
        {
            try
            {
                return classloader.loadClass( classname );
            }
            catch( ClassNotFoundException e )
            {
                final String error = 
                  "The array type [" + classname + "] is unknown.";
                throw new BuildException( error, e );
            }
        }
    }
}
