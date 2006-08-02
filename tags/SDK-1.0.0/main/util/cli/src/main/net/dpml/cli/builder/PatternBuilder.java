/**
 * Copyright 2003-2004 The Apache Software Foundation
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.dpml.cli.Argument;
import net.dpml.cli.Option;
import net.dpml.cli.validation.ClassValidator;
import net.dpml.cli.validation.DateValidator;
import net.dpml.cli.validation.FileValidator;
import net.dpml.cli.validation.NumberValidator;
import net.dpml.cli.validation.URLValidator;
import net.dpml.cli.validation.Validator;

/**
 * Builds Options using a String pattern
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PatternBuilder 
{
    private final GroupBuilder m_gbuilder;
    private final DefaultOptionBuilder m_obuilder;
    private final ArgumentBuilder m_abuilder;
    private final Set m_options = new HashSet();

    /**
     * Creates a new PatternBuilder
     */
    public PatternBuilder()
    {
        this(
            new GroupBuilder(),
            new DefaultOptionBuilder(),
            new ArgumentBuilder() );
    }

    /**
     * Creates a new PatternBuilder
     * @param gbuilder the GroupBuilder to use
     * @param obuilder the DefaultOptionBuilder to use
     * @param abuilder the ArgumentBuilder to use
     */
    public PatternBuilder(
        final GroupBuilder gbuilder,
        final DefaultOptionBuilder obuilder,
        final ArgumentBuilder abuilder )
    {
        m_gbuilder = gbuilder;
        m_obuilder = obuilder;
        m_abuilder = abuilder;
    }

    /**
     * Creates a new Option instance.
     * @return a new Option instance
     */
    public Option create()
    {
        final Option option;
        if( m_options.size() == 1 )
        {
            option = (Option) m_options.iterator().next();
        }
        else
        {
            m_gbuilder.reset();
            for( final Iterator i = m_options.iterator(); i.hasNext();)
            {
                m_gbuilder.withOption( (Option) i.next() );
            }
            option = m_gbuilder.create();
        }
        reset();
        return option;
    }

    /**
     * Resets this builder
     * @return the builder
     */
    public PatternBuilder reset()
    {
        m_options.clear();
        return this;
    }

    private void createOption( final char type, final boolean required, final char opt ) 
    {
        final Argument argument;
        if( type != ' ' )
        {
            m_abuilder.reset();
            m_abuilder.withValidator( validator( type ) );
            if( required )
            {
                m_abuilder.withMinimum( 1 );
            }
            if( type != '*' )
            {
                m_abuilder.withMaximum( 1 );
            }
            argument = m_abuilder.create();
        }
        else
        {
            argument = null;
        }

        m_obuilder.reset();
        m_obuilder.withArgument( argument );
        m_obuilder.withShortName( String.valueOf( opt ) );
        m_obuilder.withRequired( required );
        m_options.add( m_obuilder.create() );
    }

    /**
     * Builds an Option using a pattern string.
     * @param pattern the pattern to build from
     */
    public void withPattern( final String pattern )
    {
        int sz = pattern.length();
        char opt = ' ';
        char ch = ' ';
        char type = ' ';
        boolean required = false;

        for( int i=0; i < sz; i++ )
        {
            ch = pattern.charAt( i );
            switch( ch ) 
            {
                case '!' :
                    required = true;
                    break;
                case '@' :
                case ':' :
                case '%' :
                case '+' :
                case '#' :
                case '<' :
                case '>' :
                case '*' :
                case '/' :
                    type = ch;
                    break;
                default :
                    if( opt != ' ' )
                    {
                        createOption( type, required, opt );
                        required = false;
                        type = ' ';
                    }
                    opt = ch;
            }
        }
        if( opt != ' ' )
        {
            createOption( type, required, opt );
        }
    }

    private static Validator validator( final char c )
    {
        switch( c )
        {
            case '@' :
                final ClassValidator classv = new ClassValidator();
                classv.setInstance( true );
                return classv;
            case '+' :
                final ClassValidator instancev = new ClassValidator();
                return instancev;
                //case ':':// no validator needed for a string
            case '%' :
                return NumberValidator.getNumberInstance();
            case '#' :
                return DateValidator.getDateInstance();
            case '<' :
                final FileValidator existingv = new FileValidator();
                existingv.setExisting( true );
                existingv.setFile( true );
                return existingv;
            case '>' :
            case '*' :
                return new FileValidator();
            case '/' :
                return new URLValidator();
            default :
                return null;
        }
    }
}
