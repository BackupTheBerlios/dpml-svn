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

package org.apache.avalon.logging.logkit.factory;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.ContextException;

import org.apache.avalon.logging.logkit.DefaultLoggingManager;
import org.apache.avalon.logging.logkit.FormatterFactory;
import org.apache.avalon.logging.logkit.LogTargetFactory;
import org.apache.avalon.logging.logkit.LogTargetException;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

import org.apache.log.LogTarget;
import org.apache.log.format.Formatter;
import org.apache.log.output.io.FileTarget;
import org.apache.log.output.io.rotate.FileStrategy;
import org.apache.log.output.io.rotate.OrRotateStrategy;
import org.apache.log.output.io.rotate.RevolvingFileStrategy;
import org.apache.log.output.io.rotate.RotateStrategy;
import org.apache.log.output.io.rotate.RotateStrategyByDate;
import org.apache.log.output.io.rotate.RotateStrategyBySize;
import org.apache.log.output.io.rotate.RotateStrategyByTime;
import org.apache.log.output.io.rotate.RotateStrategyByTimeOfDay;
import org.apache.log.output.io.rotate.RotatingFileTarget;
import org.apache.log.output.io.rotate.UniqueFileStrategy;



/**
 * FileTargetFactory class.
 *
 * This factory is able to create different FileLogTargets according to the following
 * configuration syntax:
 *
 * <pre>
 * &lt;file id="foo"&gt;
 *  &lt;filename&gt;${context-key}/real-name/...&lt;/filename&gt;
 *  &lt;format type="avalon|raw|pattern|extended"&gt;pattern to be used if needed&lt;/format&gt;
 *  &lt;append&gt;true|false&lt;/append&gt;
 *  &lt;rotation type="revolving" init="5" max="10"&gt;
 *
 * or
 *
 *  &lt;rotation type="unique" pattern="yyyy-MM-dd-hh-mm-ss" suffix=".log"&gt;
 *   &lt;or&gt;
 *    &lt;size&gt;10000000&lt;/size&gt;
 *    &lt;time&gt;24:00:00&lt;/time&gt;
 *    &lt;time&gt;12:00:00&lt;/time&gt;
 *   &lt;/or&gt;
 *  &lt;/rotation&gt;
 * &lt;/file&gt;
 * </pre>
 *
 * <p>Some explanations about the Elements used in the configuration:</p>
 * <dl>
 *  <dt>&lt;filename&gt;</dt>
 *  <dd>
 *   This denotes the name of the file to log to. It can be constructed
 *   out of entries in the passed Context object as ${context-key}.
 *   This element is required.
 *  </dd>
 *  <dt>&lt;format&gt;</dt>
 *  <dd>
 *   The type attribute of the pattern element denotes the type of
 *   Formatter to be used and according to it the pattern to use for.
 *   This elements defaults to:
 *   <p>
 *    %7.7{priority} %5.5{time}   [%8.8{category}] (%{context}): %{message}\\n%{throwable}
 *   </p>
 *  </dd>
 *  <dt>&lt;append&gt;<dt>
 *  <dd>
 *   If the log file should be deleted every time the logger is creates
 *   (normally at the start of the applcation) or not and thus the log
 *   entries will be appended. This elements defaults to false.
 *  </dd>
 *  <dt>&lt;rotation&gt;</dt>
 *  <dd>
 *   This is an optional element.
 *   The type attribute determines which FileStrategy to user
 *   (revolving=RevolvingFileStrategy, unique=UniqueFileStrategy).
 *   The required init and max attribute are used to determine the initial and
 *   maximum rotation to use on a type="revolving" attribute.
 *   The optional pattern and suffix attribute are used to form filenames on
 *   a type="unique" attribute.
 *   <p> The initial rotation
 *   can be set to -1 in which case the system will first create the maximum
 *   number of file rotations by selecting the next available rotation and thereafter
 *   will overwrite the oldest log file.
 *  </dd>
 *  <dt>&lt;or&gt;</dt>
 *  <dd>uses the OrRotateStrategy to combine the children</dd>
 *  <dt>&lt;size&gt;</dt>
 *  <dd>
 *   The number of bytes if no suffix used or kilo bytes (1024) if suffixed with
 *   'k' or mega bytes (1024k) if suffixed with 'm' when a file rotation should
 *   occur. It doesn't make sense to specify more than one.
 *  </dd>
 *  <dt>&lt;time&gt;</dt>
 *  <dd>
 *   The time as HH:MM:SS when a rotation should occur. If you like to rotate
 *   a logfile more than once a day put an &lt;or&gt; element immediately after the
 *   &lt;rotation&gt; element and specify the times (and one size, too) inside the
 *   &lt;or&gt; element.
 *  </dd>
 *  <dt>&lt;date&gt;</dt>
 *  <dd>
 *   Rotation occur when string formatted date changed. Specify date formatting pattern.
 *  </dd>
 *  <dt>&lt;interval&gt;</dt>
 *  <dd>
 *   Interval at which a rotation should occur.  The interval should be given in the
 *   format ddd:hh:mm:ss.
 *  </dd>
 * </dl>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: FileTargetFactory.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class FileTargetFactory implements LogTargetFactory
{
    //--------------------------------------------------------------
    // static
    //--------------------------------------------------------------

    private static final Resources REZ =
      ResourceManager.getPackageResources( FileTargetFactory.class );

    private static final long SECOND = 1000;
    private static final long MINUTE = 60 * SECOND;
    private static final long HOUR = 60 * MINUTE;
    private static final long DAY = 24 * HOUR;

    private static final long KILOBYTE = 1000;
    private static final long MEGABYTE = 1000 * KILOBYTE;

    //--------------------------------------------------------------
    // immutable state
    //--------------------------------------------------------------

    private final File m_basedir;
    private final FormatterFactory m_formatter;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

    public FileTargetFactory( File basedir, FormatterFactory formatter )
    {
        m_basedir = basedir;
        m_formatter = formatter;
    }

    //--------------------------------------------------------------
    // LogFactory
    //--------------------------------------------------------------

    /**
     * Create a LogTarget based on a Configuration
     */
    public final LogTarget createTarget( final Configuration configuration )
        throws LogTargetException
    {
        try
        {
            final Configuration confFilename = 
              configuration.getChild( "filename" );
            final String filename = 
              confFilename.getValue();

            final Configuration confRotation = 
              configuration.getChild( "rotation", false );

            final Configuration confFormat = 
              configuration.getChild( "format", false );

            final Configuration confAppend = 
              configuration.getChild( "append" );
            final boolean append = 
              confAppend.getValueAsBoolean( false );

            final LogTarget logtarget;
   
            final File file = resolveFile( filename );
          
            final Formatter formatter = 
              m_formatter.createFormatter( confFormat );

            if( null == confRotation )
            {
                return new FileTarget( file, append, formatter );
            }
            else
            {
                if( confRotation.getChildren().length == 0 )
                {
                    final String error = 
                      REZ.getString( "file.error.missing-rotation" );
                    throw new LogTargetException( error );
                }
                final Configuration confStrategy = 
                  confRotation.getChildren()[ 0 ];
                final RotateStrategy rotateStrategy = 
                  getRotateStrategy( confStrategy );
                final FileStrategy fileStrategy = 
                  getFileStrategy( confRotation, file );

                try
                {
                    return new RotatingFileTarget( 
                      append, formatter, rotateStrategy, fileStrategy );
                }
                catch( Throwable e )
                {
                    final String error = 
                      REZ.getString( "file.error.logkit-rotation" );
                    throw new LogTargetException( error, e );
                }
            }
        }
        catch( final IOException e )
        {
            final String error = 
              REZ.getString( "file.error.io" );
            throw new LogTargetException( error, e );
        }
        catch( ConfigurationException e )
        {
            final String error = 
              REZ.getString( "file.error.config" );
            throw new LogTargetException( error, e );
        }
        catch( Throwable e )
        {
            final String error = 
              REZ.getString( "file.error.internal" );
            throw new LogTargetException( error, e );
        }
    }

    private File resolveFile( final String filename )
    {
        final File file = new File( filename );
        if( file.isAbsolute() ) return file;
        return new File( m_basedir, filename );
    }

    private RotateStrategy getRotateStrategy( final Configuration conf )
    {
        final String type = conf.getName();

        if( "or".equals( type ) )
        {
            final Configuration[] configurations = conf.getChildren();
            final int size = configurations.length;

            final RotateStrategy[] strategies = new RotateStrategy[ size ];
            for( int i = 0; i < size; i++ )
            {
                strategies[ i ] = getRotateStrategy( configurations[ i ] );
            }

            return new OrRotateStrategy( strategies );
        }
        else if( "size".equals( type ) )
        {
            final String value = conf.getValue( "2m" );

            final int count = value.length();
            final char end = value.charAt( count - 1 );
            final long no;
            final long size;

            switch( end )
            {
                case 'm':
                    no = Long.parseLong( value.substring( 0, count - 1 ) );
                    size = no * MEGABYTE;
                    break;
                case 'k':
                    no = Long.parseLong( value.substring( 0, count - 1 ) );
                    size = no * KILOBYTE;
                    break;
                default:
                    size = Long.parseLong( value );
            }

            return new RotateStrategyBySize( size );
        }
        else if( "date".equals( type ) )
        {
            final String value = conf.getValue( "yyyyMMdd" );
            return new RotateStrategyByDate( value );
        }
        else if( "interval".equals( type ) )
        {
            // default rotate strategy
            final String value = conf.getValue( "24:00:00" );

            // interpret a string like: ddd:hh:mm:ss ...
            final StringTokenizer tokenizer = new StringTokenizer( value, ":" );
            final int count = tokenizer.countTokens();
            long time = 0;
            for( int i = count; i > 0; i-- )
            {
                final long no = Long.parseLong( tokenizer.nextToken() );
                if( 4 == i )
                {
                    time += no * DAY;
                }
                if( 3 == i )
                {
                    time += no * HOUR;
                }
                if( 2 == i )
                {
                    time += no * MINUTE;
                }
                if( 1 == i )
                {
                    time += no * SECOND;
                }
            }

            return new RotateStrategyByTime( time );
        }
        else // "time"
        {
            // default rotate strategy
            final String value = conf.getValue( "24:00:00" );

            // interpret a string like: hh:mm:ss ...
            final StringTokenizer tokenizer = new StringTokenizer( value, ":" );
            final int count = tokenizer.countTokens();
            long time = 0;
            for( int i = count; i > 0; i-- )
            {
                final long no = Long.parseLong( tokenizer.nextToken() );
                if( 3 == i )
                {
                    time += no * HOUR;
                }
                if( 2 == i )
                {
                    time += no * MINUTE;
                }
                if( 1 == i )
                {
                    time += no * SECOND;
                }
            }

            return new RotateStrategyByTimeOfDay( time );
        }
    }

    protected FileStrategy getFileStrategy( final Configuration conf, final File file )
    {
        final String type = conf.getAttribute( "type", "unique" );

        if( "revolving".equals( type ) )
        {
            final int initialRotation =
                conf.getAttributeAsInteger( "init", 5 );
            final int maxRotation =
                conf.getAttributeAsInteger( "max", 10 );

            return new RevolvingFileStrategy( file, initialRotation, maxRotation );
        }

        // default file strategy
        final String pattern = conf.getAttribute( "pattern", null );
        final String suffix = conf.getAttribute( "suffix", null );
        if( pattern == null )
        {
            return new UniqueFileStrategy( file );
        }
        else
        {
            if( suffix == null )
            {
                return new UniqueFileStrategy( file, pattern );
            }
            else
            {
                return new UniqueFileStrategy( file, pattern, suffix );
            }
        }
    }
}

