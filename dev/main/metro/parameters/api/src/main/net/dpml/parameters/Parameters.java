/*
 * Copyright 2004 Stephen J. McConnell.
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
package net.dpml.parameters;

import java.util.Properties;

/**
 * The <code>Parameters</code> class represents a set of key-value
 * pairs.
 * <p>
 * The <code>Parameters</code> object provides a mechanism to obtain
 * values based on a <code>String</code> name.  There are convenience
 * methods that allow you to use defaults if the value does not exist,
 * as well as obtain the value in any of the same formats that are in
 * the {@link net.dpml.configuration.Configuration} interface.
 * </p><p>
 * While there are similarities between the <code>Parameters</code>
 * object and the java.util.Properties object, there are some
 * important semantic differences.  First, <code>Parameters</code> are
 * <i>read-only</i>.  Second, <code>Parameters</code> are easily
 * derived from {@link net.dpml.configuration.Configuration} objects.  Lastly, the
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
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: Parameters.java 2022 2005-03-10 22:28:11Z niclas@hedhman.org $
 */
public interface Parameters
{
    /**
     * Retrieve an array of all parameter names.
     *
     * @return the parameters names
     */
    String[] getNames();

    /**
     * Test if the specified parameter can be retrieved.
     *
     * @param name the parameter name
     * @return true if parameter is a name
     */
    boolean isParameter( final String name );


    /**
     * Retrieve the <code>String</code> value of the specified parameter.
     * <p />
     * If the specified parameter cannot be found, an exception is thrown.
     *
     * @param name the name of parameter
     * @return the value of parameter
     * @throws ParameterException if the specified parameter cannot be found
     */
    String getParameter( final String name )
        throws ParameterException;

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
    String getParameter( final String name, final String defaultValue );


    /**
     * Retrieve the <code>int</code> value of the specified parameter.
     * <p />
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
    int getParameterAsInteger( final String name )
        throws ParameterException;

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
    int getParameterAsInteger( final String name, final int defaultValue );

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
    long getParameterAsLong( final String name )
        throws ParameterException;

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
    long getParameterAsLong( final String name, final long defaultValue );

    /**
     * Retrieve the <code>float</code> value of the specified parameter.
     * <p />
     * If the specified parameter cannot be found,  an exception is thrown.
     *
     * @param name the parameter name
     * @return the value
     * @throws ParameterException if the specified parameter cannot be found
     *         or is not a Float value
     */
    float getParameterAsFloat( final String name )
        throws ParameterException;

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
    float getParameterAsFloat( final String name, final float defaultValue );

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
    boolean getParameterAsBoolean( final String name )
        throws ParameterException;

    /**
     * Retrieve the <code>boolean</code> value of the specified parameter.
     * <p />
     * If the specified parameter cannot be found, <code>defaultValue</code>
     * is returned.
     *
     * @param name the parameter name
     * @param defaultValue the default value if parameter does not exist or is of wrong type
     * @return the value
     */
    boolean getParameterAsBoolean( final String name, final boolean defaultValue );

    /** Converts the Parameters instance to a java.util.Properties instance.
     * <p>
     *   NOTE: Changes made to the returned Properties instance will not
     *   affect the Parameters instance.
     * </p>
     * @return a java.util.Properties instance that contains the same keys
     *         and values as this Parameters instance.
     */
    Properties toProperties();
}
