/*
 * Copyright 2003-2005 The Apache Software Foundation
 * Copyright 2005 Stephen McConnell
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
package net.dpml.cli.builder;

import java.util.ArrayList;
import java.util.List;

import net.dpml.cli.Argument;
import net.dpml.cli.option.ArgumentImpl;
import net.dpml.cli.resource.ResourceConstants;
import net.dpml.cli.resource.ResourceHelper;
import net.dpml.cli.validation.Validator;

/**
 * Builds Argument instances.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ArgumentBuilder
{
    /** i18n */
    private static final ResourceHelper RESOURCES = ResourceHelper.getResourceHelper();
    
    /** name of the argument. Used for display and lookups in CommandLine */
    private String m_name;

    /** description of the argument. Used in the automated online help */
    private String m_description;

    /** minimum number of values required */
    private int m_minimum;

    /** maximum number of values permitted */
    private int m_maximum;

    /** character used to separate the values from the option */
    private char m_initialSeparator;

    /** character used to separate the values from each other */
    private char m_subsequentSeparator;

    /** object that should be used to ensure the values are valid */
    private Validator m_validator;

    /** used to identify the consume remaining option, typically "--" */
    private String m_consumeRemaining;

    /** default values for argument */
    private List m_defaultValues;

    /** id of the argument */
    private int m_id;

    /**
     * Creates a new ArgumentBuilder instance
     */
    public ArgumentBuilder()
    {
        reset();
    }

    /**
     * Creates a new Argument instance using the options specified in this
     * ArgumentBuilder.
     * 
     * @return A new Argument instance using the options specified in this
     * ArgumentBuilder.
     */
    public final Argument create()
    {
        final Argument argument =
            new ArgumentImpl(
              m_name,
              m_description,
              m_minimum,
              m_maximum,
              m_initialSeparator,
              m_subsequentSeparator,
              m_validator,
              m_consumeRemaining,
              m_defaultValues,
              m_id );
        reset();
        return argument;
    }

    /**
     * Resets the ArgumentBuilder to the defaults for a new Argument. The
     * method is called automatically at the end of a create() call.
     * @return the argument builder
     */
    public final ArgumentBuilder reset()
    {
        m_name = "arg";
        m_description = null;
        m_minimum = 0;
        m_maximum = Integer.MAX_VALUE;
        m_initialSeparator = ArgumentImpl.DEFAULT_INITIAL_SEPARATOR;
        m_subsequentSeparator = ArgumentImpl.DEFAULT_SUBSEQUENT_SEPARATOR;
        m_validator = null;
        m_consumeRemaining = "--";
        m_defaultValues = null;
        m_id = 0;
        return this;
    }

    /**
     * Sets the name of the argument. The name is used when displaying usage
     * information and to allow lookups in the CommandLine object.
     * 
     * @see net.dpml.cli.CommandLine#getValue(String)
     * 
     * @param newName the name of the argument
     * @return this ArgumentBuilder
     */
    public final ArgumentBuilder withName( final String newName )
    {
        if( newName == null )
        {
            throw new IllegalArgumentException(
              RESOURCES.getMessage(
                ResourceConstants.ARGUMENT_BUILDER_NULL_NAME ) );
        }
        if( "".equals( newName ) )
        {
            throw new IllegalArgumentException(
              RESOURCES.getMessage(
                ResourceConstants.ARGUMENT_BUILDER_EMPTY_NAME ) );
        }
        m_name = newName;
        return this;
    }

    /**
     * Sets the description of the argument.
     * 
     * The description is used when displaying online help.
     * 
     * @param newDescription a description of the argument
     * @return this ArgumentBuilder
     */
    public final ArgumentBuilder withDescription( final String newDescription )
    {
        m_description = newDescription;
        return this;
    }

    /**
     * Sets the minimum number of values needed for the argument to be valid.
     * 
     * @param newMinimum the number of values needed
     * @return this ArgumentBuilder
     */
    public final ArgumentBuilder withMinimum( final int newMinimum )
    {
        if( newMinimum < 0 )
        {
            throw new IllegalArgumentException(
              RESOURCES.getMessage(
                ResourceConstants.ARGUMENT_BUILDER_NEGATIVE_MINIMUM ) );
        }
        m_minimum = newMinimum;
        return this;
    }

    /**
     * Sets the maximum number of values allowed for the argument to be valid.
     * 
     * @param newMaximum the number of values allowed
     * @return this ArgumentBuilder
     */
    public final ArgumentBuilder withMaximum( final int newMaximum )
    {
        if( newMaximum < 0 )
        {
            throw new IllegalArgumentException(
              RESOURCES.getMessage(
                ResourceConstants.ARGUMENT_BUILDER_NEGATIVE_MAXIMUM ) );
        }
        m_maximum = newMaximum;
        return this;
    }

    /**
     * Sets the character used to separate the values from the option. When an
     * argument is of the form -libs:dir1,dir2,dir3 the initialSeparator would
     * be ':'.
     * 
     * @param newInitialSeparator the character used to separate the values 
     * from the option
     * @return this ArgumentBuilder
     */
    public final ArgumentBuilder withInitialSeparator(
        final char newInitialSeparator )
    {
        m_initialSeparator = newInitialSeparator;
        return this;
    }

    /**
     * Sets the character used to separate the values from each other. When an
     * argument is of the form -libs:dir1,dir2,dir3 the subsequentSeparator
     * would be ','.
     * 
     * @param newSubsequentSeparator the character used to separate the values 
     * from each other
     * @return this ArgumentBuilder
     */
    public final ArgumentBuilder withSubsequentSeparator(
        final char newSubsequentSeparator )
    {
        m_subsequentSeparator = newSubsequentSeparator;
        return this;
    }

    /**
     * Sets the validator instance used to perform validation on the Argument
     * values.
     * 
     * @param newValidator a Validator instance
     * @return this ArgumentBuilder
     */
    public final ArgumentBuilder withValidator( final Validator newValidator )
    {
        if( newValidator == null )
        {
            throw new IllegalArgumentException(
              RESOURCES.getMessage(
                ResourceConstants.ARGUMENT_BUILDER_NULL_VALIDATOR ) );
        }
        m_validator = newValidator;
        return this;
    }

    /**
     * Sets the "consume remaining" option, defaults to "--". Use this if you
     * want to allow values that might be confused with option strings.
     * 
     * @param newConsumeRemaining the string to use for the consume 
     * remaining option
     * @return this ArgumentBuilder
     */
    public final ArgumentBuilder withConsumeRemaining( final String newConsumeRemaining )
    {
        if( newConsumeRemaining == null )
        {
            throw new IllegalArgumentException(
              RESOURCES.getMessage(
                ResourceConstants.ARGUMENT_BUILDER_NULL_CONSUME_REMAINING ) );
        } 
        if( "".equals( newConsumeRemaining ) )
        {
            throw new IllegalArgumentException(
              RESOURCES.getMessage(
                ResourceConstants.ARGUMENT_BUILDER_EMPTY_CONSUME_REMAINING ) );
        }
        m_consumeRemaining = newConsumeRemaining;
        return this;
    }

    /**
     * Sets the default value.
     * 
     * @param defaultValue the default value for the Argument
     * @return this ArgumentBuilder
     */
    public final ArgumentBuilder withDefault( final Object defaultValue )
    {
        if( defaultValue == null )
        {
            throw new IllegalArgumentException(
              RESOURCES.getMessage(
                ResourceConstants.ARGUMENT_BUILDER_NULL_DEFAULT ) );
        }
        
        if( m_defaultValues == null )
        {
            m_defaultValues = new ArrayList( 1 );
        }
        m_defaultValues.add( defaultValue );
        return this;
    }

    /**
     * Sets the default values.
     * 
     * @param newDefaultValues the default values for the Argument
     * @return this ArgumentBuilder
     */
    public final ArgumentBuilder withDefaults( final List newDefaultValues )
    {
        if( newDefaultValues == null )
        {
            throw new IllegalArgumentException(
              RESOURCES.getMessage(
                ResourceConstants.ARGUMENT_BUILDER_NULL_DEFAULTS ) );
        }
        m_defaultValues = newDefaultValues;
        return this;
    }

    /**
     * Sets the id
     * 
     * @param newId the id of the Argument
     * @return this ArgumentBuilder
     */
    public final ArgumentBuilder withId( final int newId )
    {
        m_id = newId;
        return this;
    }
}
