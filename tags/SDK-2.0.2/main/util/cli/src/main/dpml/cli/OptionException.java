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
package dpml.cli;

import java.util.Collections;
import java.util.Set;

import dpml.cli.resource.ResourceHelper;

/**
 * A problem found while dealing with command line options.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class OptionException extends Exception
{
   /**
    * The settings used when displaying the related Option.
    *
    * @see DisplaySetting
    */
    public static final Set HELP_SETTINGS =
        Collections.unmodifiableSet( 
          Collections.singleton( DisplaySetting.DISPLAY_PROPERTY_OPTION ) );

    /** resource HELPER instance */
    private static final ResourceHelper HELPER = ResourceHelper.getResourceHelper();

    /** The Option the exception relates to */
    private final Option m_option;

    /** The message explaining the Exception */
    private final String m_message;

    /**
     * Creates a new OptionException.
     *
     * @param option the Option the exception relates to
     */
    public OptionException( final Option option )
    {
        this( option, null, null );
    }

    /**
     * Creates a new OptionException.
     * @param option the Option the exception relates to
     * @param messageKey the id of the message to display
     */
    public OptionException(
      final Option option, final String messageKey )
    {
        this( option, messageKey, null );
    }

    /**
     * Creates a new OptionException.
     * @param option the Option the exception relates to
     * @param messageKey the id of the message to display
     * @param value a value to display with the message
     */
    public OptionException(
      final Option option, final String messageKey, final String value )
    {
        m_option = option;
        if( messageKey != null )
        {
            final StringBuffer buffer = new StringBuffer();
            if( value != null )
            {
                buffer.append( HELPER.getMessage( messageKey, value ) );
            }
            else
            {
                buffer.append( HELPER.getMessage( messageKey ) );
            }
            buffer.append( " " );
            m_option.appendUsage( buffer, HELP_SETTINGS, null );
            m_message = buffer.toString();
        }
        else
        {
            m_message = "";
        }
    }

    /**
     * Gets the Option the exception relates to
     *
     * @return The related Option
     */
    public Option getOption()
    {
        return m_option;
    }

   /**
    * Return the exception message.
    * @return the exception message
    */
    public String getMessage()
    {
        return m_message;
    }
}
