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
package dpml.cli.validation;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.List;
import java.util.ListIterator;

import dpml.cli.resource.ResourceConstants;
import dpml.cli.resource.ResourceHelper;

/**
 * The <code>URLValidator</code> validates the string argument
 * values are URLs.  If the value is a URL, the string value in
 * the {@link java.util.List} of values is replaced with the
 * {@link java.net.URL} instance.
 *
 * URLs can also be validated based on their scheme by using
 * the {@link #setProtocol setProtocol} method, or by using the specified
 * {@link #URLValidator(java.lang.String) constructor}.
 *
 * The following example shows how to limit the valid values
 * for the site argument to 'https' URLs.
 *
 * <pre>
 * ...
 * ArgumentBuilder builder = new ArgumentBuilder();
 * Argument site =
 *     builder.withName("site");
 *            .withValidator(new URLValidator("https"));
 * </pre>
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class URLValidator implements Validator 
{
    /** allowed protocol */
    private String m_protocol = null;

    /**
     * Creates a URLValidator.
     */
    public URLValidator() 
    {
    }

    /**
     * Creates a URLValidator for the specified protocol.
     * @param protocol the url protocol
     */
    public URLValidator( final String protocol ) 
    {
        setProtocol( protocol );
    }

   /**
    * Validate the list of values against the list of permitted values.
    * If a value is valid, replace the string in the <code>values</code>
    * {@link java.util.List} with the { java.net.URL} instance.
    *
    * @param values the list of values to validate 
    * @exception InvalidArgumentException if a value is invalid
    * @see dpml.cli.validation.Validator#validate(java.util.List)
    */
    public void validate( final List values ) throws InvalidArgumentException
    {
        for( final ListIterator i = values.listIterator(); i.hasNext();) 
        {
            final Object next = i.next();
            if( next instanceof URL )
            {
                return;
            }
            final String name = (String) next;
            try
            {
                final URL url = new URL( name );
                if( ( m_protocol != null ) && !m_protocol.equals( url.getProtocol() ) ) 
                {
                    throw new InvalidArgumentException( name );
                }
                i.set( url );
            }
            catch( final MalformedURLException mue ) 
            {
                throw new InvalidArgumentException(
                  ResourceHelper.getResourceHelper().getMessage(
                    ResourceConstants.URLVALIDATOR_MALFORMED_URL,
                    new Object[]{name} ) );
            }
        }
    }

    /**
     * Returns the protocol that must be used by a valid URL.
     *
     * @return the protocol that must be used by a valid URL.
     */
    public String getProtocol()
    {
        return m_protocol;
    }

    /**
     * Specifies the protocol that a URL must have to be valid.
     *
     * @param protocol the protocol that a URL must have to be valid.
     */
    public void setProtocol( String protocol )
    {
        m_protocol = protocol;
    }
}
