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

package org.apache.avalon.examples.translator.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.examples.translator.*;

/**
 * Simple implementation of the <code>Translator</code> component, which
 * maintains a simple mapping of keys to translated values, created during
 * configuration.
 *
 * <p>
 * Configuration format:
 *
 * <pre>
 * &lt;translations&gt;
 *   &lt;entry key="hello-world"&gt;
 *    &lt;value language="Deutsch"&gt;Hallo Welt&lt;/value&gt;
 *    &lt;value language="English"&gt;Hello World&lt;/value&gt;
 *   &lt;/entry&gt;
 * &lt;/translations&gt;
 * </pre>
 * </p>
 *
 * @avalon.component name="translator"
 * @avalon.service type=Translator
 * @x-avalon.info name=translator
 * @x-avalon.lifestyle type=singleton
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version CVS $Revision: 1.1 $ $Date: 2004/02/29 08:41:42 $
 */
public class TranslatorImpl
    extends AbstractLogEnabled
    implements Translator, Configurable
{

    // internal store of translation mappings
    private Map m_keys = new java.util.HashMap();

    /**
     * Create a new TranslatorImpl.
     */
    public TranslatorImpl()
    {
    }

    /**
     * Configures this component. Reads configuration information
     * from container and appropriately sets up the internal mapping
     * array. Configuration syntax is specified in the class header.
     *
     * @param config <code>Configuration</code> details
     * @exception org.apache.avalon.framework.configuration.ConfigurationException if an error occurs
     */
    public void configure( Configuration config )
        throws ConfigurationException
    {
        if( config != null )
        {
            Configuration[] entries =
                config.getChild( "dictionary" ).getChildren( "translation" );

            for( int i = 0; i < entries.length; ++i )
            {
                String key = entries[ i ].getAttribute( "key" );
                Configuration[] values = entries[ i ].getChildren( "value" );

                Map translations = new HashMap();

                for( int j = 0; j < values.length; ++j )
                {
                    translations.put(
                        values[ j ].getAttribute( "language" ),
                        values[ j ].getValue()
                    );
                }

                m_keys.put( key, translations );
            }

            if( getLogger().isDebugEnabled() )
            {
                getLogger().debug(
                    "Translator configured with " + m_keys.size() + " translations"
                );
            }
        }

        else
        {
            if( getLogger().isWarnEnabled() )
            {
                getLogger().warn( "No configuration specified" );
            }
        }
    }

    /**
     * <code>getSupportedLanguages</code> returns an array of String
     * objects detailing which languages are supported for the given
     * key.
     *
     * @param key a <code>String</code> value identifying a translation
     * @return a <code>String[]</code> array containing available language
     * translations for the given key
     */
    public String[] getSupportedLanguages( String key )
    {
        Map translations = (Map)m_keys.get( key );
        Set keys = translations.keySet();
        return (String[])keys.toArray( new String[]{} );
    }

    /**
     * <code>getTranslation</code> obtains a translation for a given
     * key in a given language. The language parameter must be listed
     * in <code>getSupportedLanguages</code>.
     *
     * @param key a <code>String</code> value identifying a translation
     * @param language a <code>String</code> value identifying the language
     * @return translated text
     */
    public String getTranslation( String key, String language )
    {
        Map translationMap = (Map)m_keys.get( key );
        return (String)translationMap.get( language );
    }
}

