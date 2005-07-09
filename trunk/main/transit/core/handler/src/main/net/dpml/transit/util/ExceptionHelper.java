/*
 * Copyright 2004 Stephen J. McConnell.
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

package net.dpml.transit.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.StringTokenizer;
import java.rmi.activation.ActivationGroup;
import java.rmi.activation.ActivationGroupID;

/**
 * General utilities supporting the packaging of exception messages.
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class ExceptionHelper
{
    // ------------------------------------------------------------------------
    // static
    // ------------------------------------------------------------------------

    /**
     * Returns the exception and causal exceptions as a formatted string.
     * @param e the exception
     * @return String the formatting string
     */
    public static String packException( final Throwable e )
    {
        return packException( null, e );
    }

    /**
     * Returns the exception and causal exceptions as a formatted string.
     * @param e the exception
     * @param stack TRUE to generate a stack trace
     * @return String the formatting string
     */
    public static String packException( final Throwable e, boolean stack )
    {
        return packException( null, e, stack );
    }


    /**
     * Returns the exception and causal exceptions as a formatted string.
     * @param message the header message
     * @param e the exception
     * @return String the formatting string
     */
    public static String packException( final String message, final Throwable e )
    {
        return packException( message, e, false );
    }

    /**
     * Returns the exception and causal exceptions as a formatted string.
     * @param message the header message
     * @param e the exception
     * @param stack TRUE to generate a stack trace
     * @return String the formatting string
     */
    public static String packException(
       final String message, final Throwable e, boolean stack )
    {
        StringBuffer buffer = new StringBuffer();
        packException( buffer, 0, message, e, stack );
        buffer.append( getLine( END ) );
        return buffer.toString();
    }


    /**
     * Returns the exception and causal exceptions as a formatted string.
     * @param message the header message
     * @param e the exceptions
     * @param stack TRUE to generate a stack trace
     * @return String the formatting string
     */
    public static String packException(
       final String message, final Throwable[] e, boolean stack )
    {
        final String lead = COMPOSITE + "(" + e.length + " entries) ";
        StringBuffer buffer = new StringBuffer( getLine( lead ) );
        if( null != message )
        {
            buffer.append( message );
            buffer.append( "\n" );
        }
        for( int i=0; i < e.length; i++ )
        {
            packException( buffer, i + 1, null, e[i], stack );
        }
        buffer.append( getLine( END ) );
        return buffer.toString();
    }

    // ------------------------------------------------------------------------
    // static implementation
    // ------------------------------------------------------------------------

   /**
    * Line separator character.
    */
    private static final String LINE_SEPARATOR =
      System.getProperty( "line.separator" );

   /**
    * Header token.
    */
    private static final String HEADER = "----";

   /**
    * Exception token.
    */
    private static final String EXCEPTION = HEADER + " exception report ";

   /**
    * Composite token.
    */
    private static final String COMPOSITE = HEADER + " composite report ";

   /**
    * Runtime token.
    */
    private static final String RUNTIME = HEADER + " runtime exception report ";

   /**
    * Error token.
    */
    private static final String ERROR = HEADER + " error report ";

    /**
    * Cause token.
    */
    private static final String CAUSE = HEADER + " cause ";

   /**
    * Trace token.
    */
    private static final String TRACE = HEADER + " stack trace ";

   /**
    * End token.
    */
    private static final String END = "";

   /**
    * Nominal width of character display.
    */
    private static final int WIDTH = 80;

    /**
     * Returns the exception and causal exceptions as a formatted string.
     * @param buffer the string buffer
     * @param j the causal message sequence
     * @param message the header message
     * @param e the exception
     * @param stack TRUE to generate a stack trace
     */
    private static void packException(
       final StringBuffer buffer, int j, final String message, final Throwable e, boolean stack )
    {
        if( e instanceof Error )
        {
            buffer.append( getLine( ERROR, j ) );
        }
        else if( e instanceof RuntimeException )
        {
            buffer.append( getLine( RUNTIME, j ) );
        }
        else
        {
            buffer.append( getLine( EXCEPTION, j ) );
        }

        if( null != message )
        {
            buffer.append( message );
            buffer.append( "\n" );
        }
        
        ActivationGroupID groupID = ActivationGroup.currentGroupID();
        if( null != groupID )
        {
            buffer.append( "Group: " + groupID );
            buffer.append( "\n" );
        }

        if( e == null )
        {
            return;
        }

        buffer.append( "Exception: " + e.getClass().getName() + "\n" );
        buffer.append( "Message: " + e.getMessage() + "\n" );
        packCause( buffer, getCause( e ) ).toString();
        Throwable root = getLastThrowable( e );
        if( ( root != null ) && stack )
        {
            buffer.append( getLine( TRACE ) );
            String[] trace = captureStackTrace( root );
            for( int i = 0; i < trace.length; i++ )
            {
                buffer.append( trace[i] + "\n" );
            }
        }
    }

   /**
    * Pack a causal exception.
    * @param buffer the buffer to pack the exception report
    * @param cause the causal exception to pack
    * @return the buffer
    */
    private static StringBuffer packCause( StringBuffer buffer, Throwable cause )
    {
        if( cause == null )
        {
            return buffer;
        }
        buffer.append( getLine( CAUSE ) );
        buffer.append( "Exception: " + cause.getClass().getName() + "\n" );
        buffer.append( "Message: " + cause.getMessage() + "\n" );
        return packCause( buffer, getCause( cause ) );
    }

   /**
    * Return the last throwable in the chain.
    * @param exception the exception to extract the last throwable from
    * @return the initiating cause
    */
    private static Throwable getLastThrowable( Throwable exception )
    {
        Throwable cause = getCause( exception );
        if( cause != null )
        {
            return getLastThrowable( cause );
        }
        else
        {
            return exception;
        }
    }

   /**
    * Get a causal exception using reflection.
    * @param exception the exception
    * @return the causal exception
    */
    private static Throwable getCause( Throwable exception )
    {
        if( null == exception )
        {
            return null;
        }

        try
        {
            Class clazz = exception.getClass();
            Method method = clazz.getMethod( "getCause", new Class[0] );
            return (Throwable) method.invoke( exception, new Object[0] );
        }
        catch( Throwable e )
        {
            return null;
        }
    }

    /**
     * Captures the stack trace associated with this exception.
     *
     * @param throwable a <code>Throwable</code>
     * @return an array of Strings describing stack frames.
     */
    private static String[] captureStackTrace( final Throwable throwable )
    {
        final StringWriter sw = new StringWriter();
        throwable.printStackTrace( new PrintWriter( sw, true ) );
        return splitString( sw.toString(), LINE_SEPARATOR );
    }

    /**
     * Splits the string on every token into an array of stack frames.
     *
     * @param string the string to split
     * @param onToken the token to split on
     * @return the resultant array
     */
    private static String[] splitString( final String string, final String onToken )
    {
        final int offset = 4;
        final StringTokenizer tokenizer = new StringTokenizer( string, onToken );
        final String[] result = new String[tokenizer.countTokens()];

        for( int i = 0; i < result.length; i++ )
        {
            String token = tokenizer.nextToken();
            if( token.startsWith( "\tat " ) )
            {
                result[i] = token.substring( offset );
            }
            else
            {
                result[i] = token;
            }
        }

        return result;
    }

   /**
    * Return a line of '-' characters.
    * @param lead the lead
    * @return the line
    */
    private static String getLine( String lead )
    {
        return getLine( lead, 0 );
    }

   /**
    * Get a line of '-' characters with padded offset.
    * @param lead the leading characters
    * @param count the number of characters to fill
    * @return the filled out line
    */
    private static String getLine( String lead, int count )
    {
        StringBuffer buffer = new StringBuffer( lead );
        int q = 0;
        if( count  > 0 )
        {
            String v = "" + count + " ";
            buffer.append( "" + count );
            buffer.append( " " );
            q = v.length() + 1;
        }
        int j = WIDTH - ( lead.length() + q );
        for( int i=0; i < j; i++ )
        {
            buffer.append( "-" );
        }
        buffer.append( "\n" );
        return buffer.toString();
    }

   /**
    * Disabled.
    */
    private ExceptionHelper()
    {
        // disable
    }
}
