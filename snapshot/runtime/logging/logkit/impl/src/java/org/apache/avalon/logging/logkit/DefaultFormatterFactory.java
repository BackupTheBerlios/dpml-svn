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

package org.apache.avalon.logging.logkit;

import org.apache.avalon.framework.configuration.Configuration;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

import org.apache.log.format.ExtendedPatternFormatter;
import org.apache.log.format.Formatter;
import org.apache.log.format.PatternFormatter;
import org.apache.log.format.RawFormatter;
import org.apache.log.format.SyslogFormatter;
import org.apache.log.format.XMLFormatter;

/**
 * Factory for Formatter-s.
 */
public class DefaultFormatterFactory implements FormatterFactory
{
    //--------------------------------------------------------------
    // static
    //--------------------------------------------------------------

    private static final Resources REZ =
      ResourceManager.getPackageResources( DefaultFormatterFactory.class );
    
    //--------------------------------------------------------------
    // implementation
    //--------------------------------------------------------------

   /**
    * Creation of a new formatter using a supplied configuration.
    * @param config the formatter configuration
    * @return the formatter instance
    * @exception IllegalArgumentException if the formatter type is unknown
    */
    public Formatter createFormatter( final Configuration config )
    {
        if( null == config ) return new StandardFormatter( DEFAULT_FORMAT );
        final String type = config.getAttribute( "type", "pattern" );
        final String format = config.getValue( DEFAULT_FORMAT );
        return createFormatter( type, format );
    }

   /**
    * Creation of a new formatter.
    * @param type the formatter type identifier
    * @param format the format specification
    * @return the formatter instance
    * @exception IllegalArgumentException if the formatter type is unknown
    */
    public Formatter createFormatter( String type, String format )
    {
        if( "avalon".equals( type ) )
        {
            return new StandardFormatter( format, true );
        }
        else if( "console".equals( type ) )
        {
            return new StandardFormatter( format, false );
        }
        else if( "extended".equals( type ) )
        {
            //
            // Normally ExtendPatternFormatter would look for callers
            // of Logger.class.  But when Excalibur Logger provides a
            // facade, the user class/method is actually one-level deeper.
            // We therefore create the pattern-formatter with an
            // additional depth-offset of 1.
            //

            return new ExtendedPatternFormatter( format, 1 );
        }
        else if( "raw".equals( type ) )
        {
            return new RawFormatter();
        }
        else if( "xml".equals( type ) )
        {
            return new XMLFormatter();
        }
        else if( "syslog".equals( type ) )
        {
            return new SyslogFormatter();
        }
        else if( "pattern".equals( type ) )
        {
            return new PatternFormatter( format );
        }
        else
        {
            final String error = 
              REZ.getString( "formatter.error.unknown-type", type );
            throw new IllegalArgumentException( error );
        }
    }
}
