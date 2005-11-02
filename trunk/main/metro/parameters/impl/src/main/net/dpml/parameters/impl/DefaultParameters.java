/*
 * Copyright 2004-2005 Stephen J. McConnell.
 * Copyright 1999-2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.dpml.parameters.impl;

import java.io.Serializable;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import net.dpml.configuration.Configuration;

import net.dpml.parameters.Parameters;
import net.dpml.parameters.ParameterException;

/**
 * The <code>Parameters</code> class represents a set of key-value
 * pairs.
 * <p>
 * The <code>Parameters</code> object provides a mechanism to obtain
 * values based on a <code>String</code> name.  There are convenience
 * methods that allow you to use defaults if the value does not exist,
 * as well as obtain the value in any of the same formats that are in
 * the {@link Configuration} interface.
 * </p><p>
 * While there are similarities between the <code>Parameters</code>
 * object and the java.util.Properties object, there are some
 * important semantic differences.  First, <code>Parameters</code> are
 * <i>read-only</i>.  Second, <code>Parameters</code> are easily
 * derived from {@link Configuration} objects.  Lastly, the
 * <code>Parameters</code> object is derived from XML fragments that
 * look like this:
 * <pre><code>
 *  &lt;parameter name="param-name" value="param-value" /&gt;
 * </code></pre>
 * </p><p>
 * <strong>Note: this class is not thread safe by default.</strong> If you
 * require thread safety please synchronize write access to this class to
 * prevent potential data corruption.
 * </p>
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DefaultParameters implements Parameters, Serializable
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    /**
     * Empty Parameters object
     *
     * @since 4.1.2
     */
    public static final Parameters EMPTY_PARAMETERS;

    /** Static initializer to initialize the empty Parameters object */
    static
    {
        DefaultParameters params = new DefaultParameters();
        params.makeReadOnly();
        EMPTY_PARAMETERS = params;
    }

    ///Underlying store of parameters
    private Map m_parameters = new HashMap();

    private boolean m_readOnly;

   /**
    * Creation of a new parameters instance.
    */
    public DefaultParameters()
    {
         this( new HashMap(), false );
    }

   /**
    * Creation of a new parameters instance.
    * @param map the backing store for parameter key value pairs
    */
    public DefaultParameters( Map map )
    {
         this( map, false );
    }

   /**
    * Creation of a new parameters instance.
    * @param map the backing store for parameter key value pairs
    * @param sealed if true the instance will be immutable
    */
    public DefaultParameters( Map map, boolean sealed )
    {
         m_parameters = map;
         m_readOnly = sealed;
    }

   /**
    * Creation of a new parameters instance using a properties instance
    * as the definition.
    * @param properties the source for parameter key value pairs
    */
    public DefaultParameters( Properties properties )
    {
         this( properties, false );
    }

   /**
    * Creation of a new parameters instance using a properties instance
    * as the definition.
    * @param properties the source for parameter key value pairs
    * @param sealed if true the instance will be immutable
    */
    public DefaultParameters( Properties properties, boolean sealed )
    {
         m_parameters = mapFromProperties( properties );
         m_readOnly = sealed;
    }

    /**
     * Set the <code>String</code> value of a specified parameter.
     * <p />
     * If the specified value is <b>null</b> the parameter is removed.
     *
     * @param name a <code>String</code> value
     * @param value a <code>String</code> value
     * @return The previous value of the parameter or <b>null</b>.
     * @throws IllegalStateException if the Parameters object is read-only
     */
    public String setParameter( final String name, final String value )
        throws IllegalStateException
    {
        checkWriteable();

        if( null == name )
        {
            return null;
        }

        if( null == value )
        {
            return (String) m_parameters.remove( name );
        }

        return (String) m_parameters.put( name, value );
    }

    /**
     * Remove a parameter from the parameters object
     * @param name a <code>String</code> value
     */
    public void removeParameter( final String name )
    {
        setParameter( name, null );
    }

    /**
     * Retrieve an array of all parameter names.
     *
     * @return the parameters names
     */
    public String[] getNames()
    {
        return (String[]) m_parameters.keySet().toArray( new String[0] );
    }

    /**
     * Test if the specified parameter can be retrieved.
     *
     * @param name the parameter name
     * @return true if parameter is a name
     */
    public boolean isParameter( final String name )
    {
        return m_parameters.containsKey( name );
    }

    /**
     * Retrieve the <code>String</code> value of the specified parameter.
     * If the specified parameter cannot be found, an exception is thrown.
     *
     * @param name the name of parameter
     * @return the value of parameter
     * @throws ParameterException if the specified parameter cannot be found
     */
    public String getParameter( final String name )
        throws ParameterException
    {
        if( null == name )
        {
            throw new ParameterException( "You cannot lookup a null parameter" );
        }

        final String test = (String) m_parameters.get( name );

        if( null == test )
        {
            final String error = 
              "The parameter '" 
              + name
              + "' does not contain a value";
            throw new ParameterException( error );
        }
        else
        {
            return test;
        }
    }

    /**
     * Retrieve the <code>String</code> value of the specified parameter.
     * <p />
     * If the specified parameter cannot be found, <code>defaultValue</code>
     * is returned.
     *
     * @param name the name of parameter
     * @param defaultValue the default value, returned if parameter does not exist
     *        or parameter's name is null
     * @return the value of parameter
     */
    public String getParameter( final String name, final String defaultValue )
    {
        if( name == null )
        {
            return defaultValue;
        }

        final String test = (String) m_parameters.get( name );

        if( test == null )
        {
            return defaultValue;
        }
        else
        {
            return test;
        }
    }

    /**
     * Parses string represenation of the <code>int</code> value.
     * <p />
     * Hexadecimal numbers begin with 0x, Octal numbers begin with 0o and binary
     * numbers begin with 0b, all other values are assumed to be decimal.
     *
     * @param value the value to parse
     * @return the integer value
     * @throws NumberFormatException if the specified value can not be parsed
     */
    private int parseInt( final String value )
        throws NumberFormatException
    {
        if( value.startsWith( "0x" ) )
        {
            return Integer.parseInt( value.substring( 2 ), 16 );
        }
        else if( value.startsWith( "0o" ) )
        {
            return Integer.parseInt( value.substring( 2 ), 8 );
        }
        else if( value.startsWith( "0b" ) )
        {
            return Integer.parseInt( value.substring( 2 ), 2 );
        }
        else
        {
            return Integer.parseInt( value );
        }
    }

   /**
    * Retrieve the <code>int</code> value of the specified parameter.
    * If the specified parameter cannot be found, an exception is thrown.
    *
    * Hexadecimal numbers begin with 0x, Octal numbers begin with 0o and binary
    * numbers begin with 0b, all other values are assumed to be decimal.
    *
    * @param name the name of parameter
    * @return the integer parameter type
    * @throws ParameterException if the specified parameter cannot be found
    *         or is not an Integer value
    */
    public int getParameterAsInteger( final String name )
        throws ParameterException
    {
        try
        {
            return parseInt( getParameter( name ) );
        }
        catch( final NumberFormatException e )
        {
            throw new ParameterException( "Could not return an integer value", e );
        }
    }

   /**
    * Retrieve the <code>int</code> value of the specified parameter.
    * <p />
    * If the specified parameter cannot be found, <code>defaultValue</code>
    * is returned.
    *
    * Hexadecimal numbers begin with 0x, Octal numbers begin with 0o and binary
    * numbers begin with 0b, all other values are assumed to be decimal.
    *
    * @param name the name of parameter
    * @param defaultValue value returned if parameter does not exist or is of wrong type
    * @return the integer parameter type
    */
    public int getParameterAsInteger( final String name, final int defaultValue )
    {
        try
        {
            final String value = getParameter( name, null );
            if( value == null )
            {
                return defaultValue;
            }
            return parseInt( value );
        }
        catch( final NumberFormatException e )
        {
            return defaultValue;
        }
    }

   /**
    * Parses string represenation of the <code>long</code> value.
    * <p />
    * Hexadecimal numbers begin with 0x, Octal numbers begin with 0o and binary
    * numbers begin with 0b, all other values are assumed to be decimal.
    *
    * @param value the value to parse
    * @return the long value
    * @throws NumberFormatException if the specified value can not be parsed
    */
    private long parseLong( final String value )
        throws NumberFormatException
    {
        if( value.startsWith( "0x" ) )
        {
            return Long.parseLong( value.substring( 2 ), 16 );
        }
        else if( value.startsWith( "0o" ) )
        {
            return Long.parseLong( value.substring( 2 ), 8 );
        }
        else if( value.startsWith( "0b" ) )
        {
            return Long.parseLong( value.substring( 2 ), 2 );
        }
        else
        {
            return Long.parseLong( value );
        }
    }

   /**
    * Retrieve the <code>long</code> value of the specified parameter.
    * <p />
    * If the specified parameter cannot be found, an exception is thrown.
    *
    * Hexadecimal numbers begin with 0x, Octal numbers begin with 0o and binary
    * numbers begin with 0b, all other values are assumed to be decimal.
    *
    * @param name the name of parameter
    * @return the long parameter type
    * @throws ParameterException  if the specified parameter cannot be found
    *         or is not a Long value.
    */
    public long getParameterAsLong( final String name )
        throws ParameterException
    {
        try
        {
            return parseLong( getParameter( name ) );
        }
        catch( final NumberFormatException e )
        {
            throw new ParameterException( "Could not return a long value", e );
        }
    }

   /**
    * Retrieve the <code>long</code> value of the specified parameter.
    * <p />
    * If the specified parameter cannot be found, <code>defaultValue</code>
    * is returned.
    *
    * Hexadecimal numbers begin with 0x, Octal numbers begin with 0o and binary
    * numbers begin with 0b, all other values are assumed to be decimal.
    *
    * @param name the name of parameter
    * @param defaultValue value returned if parameter does not exist or is of wrong type
    * @return the long parameter type
    */
    public long getParameterAsLong( final String name, final long defaultValue )
    {
        try
        {
            final String value = getParameter( name, null );
            if( value == null )
            {
                return defaultValue;
            }

            return parseLong( value );
        }
        catch( final NumberFormatException e )
        {
            return defaultValue;
        }
    }

   /**
    * Retrieve the <code>float</code> value of the specified parameter.
    * If the specified parameter cannot be found,  an exception is thrown.
    *
    * @param name the parameter name
    * @return the value
    * @throws ParameterException if the specified parameter cannot be found
    *         or is not a Float value
    */
    public float getParameterAsFloat( final String name )
        throws ParameterException
    {
        try
        {
            return Float.parseFloat( getParameter( name ) );
        }
        catch( final NumberFormatException e )
        {
            throw new ParameterException( "Could not return a float value", e );
        }
    }

   /**
    * Retrieve the <code>float</code> value of the specified parameter.
    * <p />
    * If the specified parameter cannot be found, <code>defaultValue</code>
    * is returned.
    *
    * @param name the parameter name
    * @param defaultValue the default value if parameter does not exist or is of wrong type
    * @return the value
    */
    public float getParameterAsFloat( final String name, final float defaultValue )
    {
        try
        {
            final String value = getParameter( name, null );
            if( value == null )
            {
                return defaultValue;
            }
            return Float.parseFloat( value );
        }
        catch( final NumberFormatException pe )
        {
            return defaultValue;
        }
    }

   /**
    * Retrieve the <code>boolean</code> value of the specified parameter.
    * <p />
    * If the specified parameter cannot be found, an exception is thrown.
    *
    * @param name the parameter name
    * @return the value
    * @throws ParameterException if an error occurs
    * @throws ParameterException
    */
    public boolean getParameterAsBoolean( final String name )
        throws ParameterException
    {
        final String value = getParameter( name );
        if( value.equalsIgnoreCase( "true" ) )
        {
            return true;
        }
        else if( value.equalsIgnoreCase( "false" ) )
        {
            return false;
        }
        else
        {
            throw new ParameterException( "Could not return a boolean value" );
        }
    }

   /**
    * Retrieve the <code>boolean</code> value of the specified parameter.
    * If the specified parameter cannot be found, <code>defaultValue</code>
    * is returned.
    *
    * @param name the parameter name
    * @param defaultValue the default value if parameter does not exist or is of wrong type
    * @return the value
    */
    public boolean getParameterAsBoolean( final String name, final boolean defaultValue )
    {
        final String value = getParameter( name, null );
        if( value == null )
        {
            return defaultValue;
        }

        if( value.equalsIgnoreCase( "true" ) )
        {
            return true;
        }
        else if( value.equalsIgnoreCase( "false" ) )
        {
            return false;
        }
        else
        {
            return defaultValue;
        }
    }

   /**
    * Merge parameters from another <code>Parameters</code> instance
    * into this.
    *
    * @param other the other Parameters
    * @return This <code>Parameters</code> instance.
    */
    public Parameters merge( final Parameters other )
    {
        checkWriteable();

        final String[] names = other.getNames();

        for( int i = 0; i < names.length; i++ )
        {
            final String name = names[ i ];
            String value = null;
            try
            {
                value = other.getParameter( name );
            }
            catch( final ParameterException pe )
            {
                value = null;
            }
            setParameter( name, value );
        }
        return this;
    }

   /** 
    * Converts the Parameters instance to a java.util.Properties instance.
    * NOTE: Changes made to the returned Properties instance will not
    * affect the Parameters instance.
    * The Properties instance is created by cloning the underlying Map of
    *   this Parameters instance.
    * @return a java.util.Properties instance that contains the same keys
    *   and values as this Parameters instance.
    */
    public Properties toProperties()
    {
        Properties p = new Properties();
        p.putAll( m_parameters );
        return p;
    }

    /**
     * Make this Parameters read-only so that it will throw a
     * <code>IllegalStateException</code> if someone tries to
     * modify it.
     */
    public void makeReadOnly()
    {
        m_readOnly = true;
    }

   /**
    * Compare this parameters instance with the supplied object for equality.
    *
    * The equality is mainly driven by the underlying HashMap which forms the
    * basis of the class. I.e. if the underlying HashMaps are equal and the
    * Readonly attributes are equal in the Parameters instances being compared,
    * then this method returns equality.
    *
    * @param other the object to compare this parameters instance with
    *
    * @return true if this parameters instance is equal to the supplied object
    */
    public boolean equals( Object other )
    {
        if( null == other )
        {
            return false;
        }
        if( !( other instanceof Parameters ) )
        {
            return false;
        }
        Parameters p = (Parameters) other;
        String[] names = p.getNames();
        if( getNames().length != names.length )
        {
           return false;
        }
        String[] local = getNames();
        for( int i=0; i<local.length; i++ )
        {
            String name = local[i];
            if( !p.isParameter( name ) )
            {
                return false;
            }
            try
            {
                if( !getParameter( name ).equals( p.getParameter( name ) ) )
                {
                    return false;
                }
            }
            catch( ParameterException pe )
            {
                return false;
            }
        }
        return true;
    }

   /**
    * Returns a hashed value of the Parameters instance.
    *
    * This method returns a semi-unique value for all instances, yet an
    * identical value for instances where equals() returns true.
    *
    * @return a hashed value of the instance
    */
    public int hashCode()
    {
        int hash = m_parameters.hashCode();
        if( m_readOnly )
        {
            hash = hash + 173918901;
        }
        else
        {
            hash = hash - 2078391890;
        }
        return hash;
    }

   /**
    * Return a string representation of the parameters instance.
    * @return the string value
    */
    public String toString()
    {
        String s;
        if( m_readOnly )
        {
            s = "Parameters[r/o]:";
        }
        else
        {
            s = "Parameters[r/w]:";
        }
        return s + m_parameters;
    }

   /**
    * Checks is this <code>Parameters</code> object is writeable.
    *
    * @throws IllegalStateException if this <code>Parameters</code> object is read-only
    */
    protected final void checkWriteable()
        throws IllegalStateException
    {
        if( m_readOnly )
        {
            throw new IllegalStateException( "Context is read only and can not be modified" );
        }
    }

   /**
    * Create a <code>Parameters</code> object from a <code>Configuration</code>
    * object.  This acts exactly like the following method call:
    * <pre>
    *     Parameters.fromConfiguration(configuration, "parameter");
    * </pre>
    *
    * @param configuration the Configuration
    * @return This <code>Parameters</code> instance.
    * @throws ParameterException if an error occurs
    */
    public static Parameters fromConfiguration( final Configuration configuration )
        throws ParameterException
    {
        return fromConfiguration( configuration, "parameter" );
    }

   /**
    * Create a <code>Parameters</code> object from a <code>Configuration</code>
    * object using the supplied element name.
    *
    * @param configuration the Configuration
    * @param elementName   the element name for the parameters
    * @return This <code>Parameters</code> instance.
    * @throws ParameterException if an error occurs
    * @throws NullPointerException if the configuration argument is null.
    */
    public static Parameters fromConfiguration( 
      final Configuration configuration, final String elementName )
        throws ParameterException, NullPointerException 
    {
        if( null == configuration )
        {
            throw new NullPointerException( "configuration" );
        }
        final Configuration[] parameters = configuration.getChildren( elementName );
        final Map params = new HashMap();
        for( int i = 0; i < parameters.length; i++ )
        {
            try
            {
                final String name = parameters[ i ].getAttribute( "name" );
                final String value = parameters[ i ].getAttribute( "value" );
                params.put( name, value );
            }
            catch( final Exception e )
            {
                throw new ParameterException( "Cannot process Configurable",  e );
            }
        }
        return new DefaultParameters( params, true );
    }

   /**
    * Create a <code>Parameters</code> object from a <code>Properties</code>
    * object.
    *
    * @param properties the Properties
    * @return This <code>Parameters</code> instance.
    */
    public static Parameters fromProperties( final Properties properties )
    {
        final Map parameters = mapFromProperties( properties );
        return new DefaultParameters( parameters, true );
    }

   /**
    * Create a <code>Parameters</code> object from a <code>Properties</code>
    * object.
    *
    * @param properties the Properties
    * @return This <code>Parameters</code> instance.
    */
    private static Map mapFromProperties( final Properties properties )
    {
        final Map parameters = new HashMap();
        final Enumeration names = properties.propertyNames();
        while( names.hasMoreElements() )
        {
            final String key = names.nextElement().toString();
            final String value = properties.getProperty( key );
            parameters.put( key, value );
        }
        return parameters;
    }

   /**
    * Creates a <code>java.util.Properties</code> object from an Avalon
    *Parameters object.
    *
    * @param params a <code>Parameters</code> instance
    * @return a <code>Properties</code> instance
    */
    public static Properties toProperties( final Parameters params )
    {
        final Properties properties = new Properties();
        final String[] names = params.getNames();

        for( int i = 0; i < names.length; ++i )
        {
            // "" is the default value, since getNames() proves it will exist
            properties.setProperty( names[ i ], params.getParameter( names[ i ], "" ) );
        }
        return properties;
    }
}
