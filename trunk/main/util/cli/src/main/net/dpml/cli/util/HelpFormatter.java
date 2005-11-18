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
package net.dpml.cli.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.dpml.cli.DisplaySetting;
import net.dpml.cli.Group;
import net.dpml.cli.HelpLine;
import net.dpml.cli.Option;
import net.dpml.cli.OptionException;
import net.dpml.cli.resource.ResourceConstants;
import net.dpml.cli.resource.ResourceHelper;

/**
 * Presents on screen help based on the application's Options
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class HelpFormatter 
{
    /**
     * The default screen width
     */
    public static final int DEFAULT_FULL_WIDTH = 80;

    /**
     * The default screen furniture left of screen
     */
    public static final String DEFAULT_GUTTER_LEFT = "";

    /**
     * The default screen furniture right of screen
     */
    public static final String DEFAULT_GUTTER_CENTER = "    ";

    /**
     * The default screen furniture between columns
     */
    public static final String DEFAULT_GUTTER_RIGHT = "";

    /**
     * The default DisplaySettings used to select the elements to display in the
     * displayed line of full usage information.
     *
     * @see DisplaySetting
     */
    public static final Set DEFAULT_FULL_USAGE_SETTINGS;

    /**
     * The default DisplaySettings used to select the elements of usage per help
     * line in the main body of help
     *
     * @see DisplaySetting
     */
    public static final Set DEFAULT_LINE_USAGE_SETTINGS;

    /**
     * The default DisplaySettings used to select the help lines in the main
     * body of help
     */
    public static final Set DEFAULT_DISPLAY_USAGE_SETTINGS;

    static 
    {
        final Set fullUsage = new HashSet( DisplaySetting.ALL );
        fullUsage.remove( DisplaySetting.DISPLAY_ALIASES );
        fullUsage.remove( DisplaySetting.DISPLAY_GROUP_NAME );
        DEFAULT_FULL_USAGE_SETTINGS = Collections.unmodifiableSet( fullUsage );

        final Set lineUsage = new HashSet();
        lineUsage.add( DisplaySetting.DISPLAY_ALIASES );
        lineUsage.add( DisplaySetting.DISPLAY_GROUP_NAME );
        lineUsage.add( DisplaySetting.DISPLAY_PARENT_ARGUMENT );
        DEFAULT_LINE_USAGE_SETTINGS = Collections.unmodifiableSet( lineUsage );

        final Set displayUsage = new HashSet( DisplaySetting.ALL );
        displayUsage.remove( DisplaySetting.DISPLAY_PARENT_ARGUMENT );
        DEFAULT_DISPLAY_USAGE_SETTINGS = Collections.unmodifiableSet( displayUsage );
    }

    private Set m_fullUsageSettings = new HashSet( DEFAULT_FULL_USAGE_SETTINGS );
    private Set m_lineUsageSettings = new HashSet( DEFAULT_LINE_USAGE_SETTINGS );
    private Set m_displaySettings = new HashSet( DEFAULT_DISPLAY_USAGE_SETTINGS );
    private OptionException m_exception = null;
    private Group m_group;
    private Comparator m_comparator = null;
    private String m_divider = null;
    private String m_header = null;
    private String m_footer = null;
    private String m_shellCommand = "";
    private PrintWriter m_out = new PrintWriter( System.out );

    //or should this default to .err?
    private final String m_gutterLeft;
    private final String m_gutterCenter;
    private final String m_gutterRight;
    private final int m_pageWidth;

    /**
     * Creates a new HelpFormatter using the defaults
     */
    public HelpFormatter()
    {
        this( DEFAULT_GUTTER_LEFT, DEFAULT_GUTTER_CENTER, DEFAULT_GUTTER_RIGHT, DEFAULT_FULL_WIDTH );
    }

    /**
     * Creates a new HelpFormatter using the specified parameters
     * @param gutterLeft the string marking left of screen
     * @param gutterCenter the string marking center of screen
     * @param gutterRight the string marking right of screen
     * @param fullWidth the width of the screen
     */
    public HelpFormatter(
      final String gutterLeft, final String gutterCenter, final String gutterRight, final int fullWidth )
    {
        // default the left gutter to empty string
        if( null == gutterLeft )
        {
            m_gutterLeft = DEFAULT_GUTTER_LEFT;
        }
        else
        {
            m_gutterLeft = gutterLeft;
        }
        
        if( null == gutterCenter )
        {
            m_gutterCenter = DEFAULT_GUTTER_CENTER;
        }
        else
        {
            m_gutterCenter = gutterCenter;
        }
        
        if( null == gutterRight )
        {
            m_gutterRight = DEFAULT_GUTTER_RIGHT;
        }
        else
        {
            m_gutterRight = gutterRight;
        }

        // calculate the available page width
        m_pageWidth = fullWidth - m_gutterLeft.length() - m_gutterRight.length();

        // check available page width is valid
        int availableWidth = fullWidth - m_pageWidth + m_gutterCenter.length();

        if( availableWidth < 2 )
        {
            throw new IllegalArgumentException(
              ResourceHelper.getResourceHelper().getMessage(
                ResourceConstants.HELPFORMATTER_GUTTER_TOO_LONG ) );
        }
    }

    /**
     * Prints the Option help.
     * @throws IOException if an error occurs
     */
    public void print() throws IOException
    {
        printHeader();
        printException();
        printUsage();
        printHelp();
        printFooter();
        m_out.flush();
    }

    /**
     * Prints any error message.
     * @throws IOException if an error occurs
     */
    public void printException() throws IOException
    {
        if( m_exception != null )
        {
            printDivider();
            printWrapped( m_exception.getMessage() );
        }
    }

    /**
     * Prints detailed help per option.
     * @throws IOException if an error occurs
     */
    public void printHelp() throws IOException
    {
        printDivider();
        final Option option;
        if( ( m_exception != null ) && ( m_exception.getOption() != null ) )
        {
            option = m_exception.getOption();
        } 
        else
        {
            option = m_group;
        }

        // grab the HelpLines to display
        final List helpLines = option.helpLines( 0, m_displaySettings, m_comparator );

        // calculate the maximum width of the usage strings
        int usageWidth = 0;

        for( final Iterator i = helpLines.iterator(); i.hasNext();)
        {
            final HelpLine helpLine = (HelpLine) i.next();
            final String usage = helpLine.usage( m_lineUsageSettings, m_comparator );
            usageWidth = Math.max( usageWidth, usage.length() );
        }

        // build a blank string to pad wrapped descriptions
        final StringBuffer blankBuffer = new StringBuffer();

        for( int i = 0; i < usageWidth; i++ )
        {
            blankBuffer.append( ' ' );
        }

        // determine the width available for descriptions
        final int descriptionWidth = 
          Math.max( 1, m_pageWidth - m_gutterCenter.length() - usageWidth );

        // display each HelpLine
        for( final Iterator i = helpLines.iterator(); i.hasNext();)
        {
            // grab the HelpLine
            final HelpLine helpLine = (HelpLine) i.next();

            // wrap the description
            final List descList = wrap( helpLine.getDescription(), descriptionWidth );
            final Iterator descriptionIterator = descList.iterator();

            // display usage + first line of description
            printGutterLeft();
            pad( helpLine.usage( m_lineUsageSettings, m_comparator ), usageWidth, m_out );
            m_out.print( m_gutterCenter );
            pad( (String) descriptionIterator.next(), descriptionWidth, m_out );
            printGutterRight();
            m_out.println();

            // display padding + remaining lines of description
            while( descriptionIterator.hasNext() )
            {
                printGutterLeft();

                //pad(helpLine.getUsage(),usageWidth,m_out);
                m_out.print( blankBuffer );
                m_out.print( m_gutterCenter );
                pad( (String) descriptionIterator.next(), descriptionWidth, m_out );
                printGutterRight();
                m_out.println();
            }
        }
        printDivider();
    }

    /**
     * Prints a single line of usage information (wrapping if necessary)
     * @throws IOException if an error occurs
     */
    public void printUsage() throws IOException
    {
        printDivider();
        final StringBuffer buffer = new StringBuffer( "Usage:\n" );
        buffer.append( m_shellCommand ).append( ' ' );
        String separator = getSeparator();
        m_group.appendUsage( buffer, m_fullUsageSettings, m_comparator, separator );
        printWrapped( buffer.toString() );
    }
    
    private String getSeparator()
    {
        if( m_group.getMaximum() == 1 )
        {
            return " | ";
        }
        else
        {
            return " ";
        }
    }

    /**
     * Prints a m_header string if necessary
     * @throws IOException if an error occurs
     */
    public void printHeader() throws IOException
    {
        if( m_header != null )
        {
            printDivider();
            printWrapped( m_header );
        }
    }

    /**
     * Prints a m_footer string if necessary
     * @throws IOException if an error occurs
     */
    public void printFooter() throws IOException
    {
        if( m_footer != null )
        {
            printWrapped( m_footer );
            printDivider();
        }
    }

    /**
     * Prints a string wrapped if necessary
     * @param text the string to wrap
     * @throws IOException if an error occurs
     */
    protected void printWrapped( final String text ) throws IOException
    {
        for( final Iterator i = wrap( text, m_pageWidth ).iterator(); i.hasNext();)
        {
            printGutterLeft();
            pad( (String) i.next(), m_pageWidth, m_out );
            printGutterRight();
            m_out.println();
        }
    }

    /**
     * Prints the left gutter string
     */
    public void printGutterLeft()
    {
        if( m_gutterLeft != null )
        {
            m_out.print( m_gutterLeft );
        }
    }

    /**
     * Prints the right gutter string
     */
    public void printGutterRight()
    {
        if( m_gutterRight != null )
        {
            m_out.print( m_gutterRight );
        }
    }

    /**
     * Prints the m_divider text
     */
    public void printDivider()
    {
        if( m_divider != null )
        {
            m_out.println( m_divider );
        }
    }

   /**
    * Pad the supplied string.
    * @param text the text to pad
    * @param width the padding width
    * @param writer the writer
    * @exception IOException if an I/O error occurs
    */
    protected static void pad(
      final String text, final int width, final Writer writer )
      throws IOException
    {
        final int left;

        // write the text and record how many characters written
        if ( text == null )
        {
            left = 0;
        }
        else
        {
            writer.write( text );
            left = text.length();
        }

        // pad remainder with spaces
        for( int i = left; i < width; ++i )
        {
            writer.write( ' ' );
        }
    }

   /**
    * Return a list of strings resulting from the wrapping of a supplied
    * target string.
    * @param text the target string to wrap
    * @param width the wrappping width
    * @return the list of wrapped fragments
    */
    protected static List wrap( final String text, final int width ) 
    {
        // check for valid width
        if( width < 1 ) 
        {
            throw new IllegalArgumentException(
              ResourceHelper.getResourceHelper().getMessage(
                ResourceConstants.HELPFORMATTER_WIDTH_TOO_NARROW,
                new Object[]{new Integer( width )} ) );
        }

        // handle degenerate case
        if( text == null )
        {
            return Collections.singletonList( "" );
        }

        final List lines = new ArrayList();
        final char[] chars = text.toCharArray();
        int left = 0;

        // for each character in the string
        while( left < chars.length )
        {
            // sync left and right indeces
            int right = left;

            // move right until we run m_out of characters, width or find a newline
            while( 
              ( right < chars.length ) 
              && ( chars[right] != '\n' ) 
              && ( right < ( left + width + 1 ) ) ) 
            {
                right++;
            }

            // if a newline was found
            if( ( right < chars.length ) && ( chars[right] == '\n' ) )
            {
                // record the substring
                final String line = new String( chars, left, right - left );
                lines.add( line );

                // move to the end of the substring
                left = right + 1;

                if( left == chars.length )
                {
                    lines.add( "" );
                }

                // restart the loop
                continue;
            }

            // move to the next ideal wrap point 
            right = ( left + width ) - 1;

            // if we have run m_out of characters
            if( chars.length <= right )
            {
                // record the substring
                final String line = new String( chars, left, chars.length - left );
                lines.add( line );

                // abort the loop
                break;
            }

            // back track the substring end until a space is found
            while( ( right >= left ) && ( chars[right] != ' ' ) )
            {
                right--;
            }

            // if a space was found
            if( right >= left ) 
            {
                // record the substring to space
                final String line = new String( chars, left, right - left );
                lines.add( line );

                // absorb all the spaces before next substring
                while( ( right < chars.length ) && ( chars[right] == ' ' ) )
                {
                    right++;
                }

                left = right;

                // restart the loop
                continue;
            }

            // move to the wrap position irrespective of spaces
            right = Math.min( left + width, chars.length );

            // record the substring
            final String line = new String( chars, left, right - left );
            lines.add( line );

            // absorb any the spaces before next substring
            while( ( right < chars.length ) && ( chars[right] == ' ' ) ) 
            {
                right++;
            }

            left = right;
        }

        return lines;
    }

    /**
     * The Comparator to use when sorting Options
     * @param comparator Comparator to use when sorting Options
     */
    public void setComparator( Comparator comparator ) 
    {
        m_comparator = comparator;
    }

    /**
     * The DisplaySettings used to select the help lines in the main body of
     * help
     *
     * @param displaySettings the settings to use
     * @see DisplaySetting
     */
    public void setDisplaySettings( Set displaySettings )
    {
        m_displaySettings = displaySettings;
    }

    /**
     * Sets the string to use as a m_divider between sections of help
     * @param divider the dividing string
     */
    public void setDivider( String divider ) 
    {
        m_divider = divider;
    }

    /**
     * Sets the exception to document
     * @param exception the exception that occured
     */
    public void setException( OptionException exception ) 
    {
        m_exception = exception;
    }

    /**
     * Sets the footer text of the help screen
     * @param footer the footer text
     */
    public void setFooter( String footer )
    {
        m_footer = footer;
    }

    /**
     * The DisplaySettings used to select the elements to display in the
     * displayed line of full usage information.
     * @see DisplaySetting
     * @param fullUsageSettings
     */
    public void setFullUsageSettings( Set fullUsageSettings )
    {
        m_fullUsageSettings = fullUsageSettings;
    }

    /**
     * Sets the Group of Options to document
     * @param group the options to document
     */
    public void setGroup( Group group )
    {
        m_group = group;
    }

    /**
     * Sets the header text of the help screen
     * @param header the m_footer text
     */
    public void setHeader( String header ) 
    {
        m_header = header;
    }

    /**
     * Sets the DisplaySettings used to select elements in the per helpline
     * usage strings.
     * @see DisplaySetting
     * @param lineUsageSettings the DisplaySettings to use
     */
    public void setLineUsageSettings( Set lineUsageSettings ) 
    {
        m_lineUsageSettings = lineUsageSettings;
    }

    /**
     * Sets the command string used to invoke the application
     * @param shellCommand the invocation command
     */
    public void setShellCommand( String shellCommand )
    {
        m_shellCommand = shellCommand;
    }

    /**
    * Return the comparator.
     * @return the Comparator used to sort the Group
     */
    public Comparator getComparator() 
    {
        return m_comparator;
    }

    /**
     * Return the display settings.
     * @return the DisplaySettings used to select HelpLines
     */
    public Set getDisplaySettings() 
    {
        return m_displaySettings;
    }

    /**
     * Return the divider.
     * @return the String used as a horizontal section m_divider
     */
    public String getDivider() 
    {
        return m_divider;
    }

    /**
    * Return the option exception
    * @return the Exception being documented by this HelpFormatter
    */
    public OptionException getException() 
    {
        return m_exception;
    }

   /**
    * Return the footer text.
    * @return the help screen footer text
    */
    public String getFooter() 
    {
        return m_footer;
    }

    /**
     * Return the full usage display settings.
     * @return the DisplaySettings used in the full usage string
     */
    public Set getFullUsageSettings() 
    {
        return m_fullUsageSettings;
    }

   /**
    * Return the group.
    * @return the group documented by this HelpFormatter
    */
    public Group getGroup()
    {
        return m_group;
    }

   /**
    * Return the gutter center string.
    * @return the String used as the central gutter
    */
    public String getGutterCenter() 
    {
        return m_gutterCenter;
    }

   /**
    * Return the gutter left string.
    * @return the String used as the left gutter
    */
    public String getGutterLeft()
    {
        return m_gutterLeft;
    }

   /**
    * Return the gutter right string.
    * @return the String used as the right gutter
    */
    public String getGutterRight() 
    {
        return m_gutterRight;
    }

   /**
    * Return the header string.
    * @return the help screen header text
    */
    public String getHeader()
    {
        return m_header;
    }

   /**
    * Return the line usage settings.
    * @return the DisplaySettings used in the per help line usage strings
    */
    public Set getLineUsageSettings() 
    {
        return m_lineUsageSettings;
    }

   /**
    * Return the page width.
    * @return the width of the screen in characters
    */
    public int getPageWidth()
    {
        return m_pageWidth;
    }

   /**
    * Return the shell command.
    * @return the command used to execute the application
    */
    public String getShellCommand() 
    {
        return m_shellCommand;
    }

   /**
    * Set the print writer.
    * @param out the PrintWriter to write to
    */
    public void setPrintWriter( PrintWriter out ) 
    {
        m_out = out;
    }

   /**
    * Return the print writer.
    * @return the PrintWriter that will be written to
    */
    public PrintWriter getPrintWriter() 
    {
        return m_out;
    }
}
