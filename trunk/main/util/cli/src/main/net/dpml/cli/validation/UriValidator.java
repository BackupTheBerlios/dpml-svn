/*
 * Copyright 2005 Stephen J. McConnell
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
package net.dpml.cli.validation;

import java.net.URISyntaxException;
import java.net.URI;

import java.util.List;
import java.util.ListIterator;

import net.dpml.cli.resource.ResourceConstants;
import net.dpml.cli.resource.ResourceHelper;

/**
 * The <code>UriValidator</code> validates the string argument
 * values are valid URIs.  If the value is a URI, the string value in
 * the {@link java.util.List} of values is replaced with the
 * {@link java.net.URI} instance.
 *
 * URIs can also be validated based on their scheme by using
 * the {@link #setScheme setScheme} method, or by using the specified
 * {@link #UriValidator( java.lang.String ) constructor}.
 *
 * The following example shows how to limit the valid values
 * for the site argument to 'artifact' URIs.
 *
 * <pre>
 * ...
 * ArgumentBuilder builder = new ArgumentBuilder();
 * Argument plugin =
 *   builder
 *     .withName("plugin");
 *     .withValidator( new URIValidator( "artifact" ) );
 * </pre>
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class UriValidator implements Validator 
{
    private String m_scheme = null;

   /**
    * Creates a UriValidator.
    */
    public UriValidator() 
    {
    }

   /**
    * Creates a UriValidator for the specified scheme.
    */
    public UriValidator( final String scheme ) 
    {
        setScheme( scheme );
    }

   /**
    * Validate the list of values against the list of permitted values.
    * If a value is valid, replace the string in the <code>values</code>
    * {@link java.util.List} with the {@link java.net.URI} instance.
    *
    * @see net.dpml.cli.validation.Validator#validate(java.util.List)
    */
    public void validate( final List values )
        throws InvalidArgumentException 
    {
        for( final ListIterator i = values.listIterator(); i.hasNext(); ) 
        {
            final String name = (String) i.next();
            try 
            {
                final URI uri = new URI( name );
                if( ( m_scheme != null ) && !m_scheme.equals( uri.getScheme() ) ) 
                {
                    throw new InvalidArgumentException( name );
                }
                i.set( uri );
            } 
            catch (final URISyntaxException e )
            {
                throw new InvalidArgumentException(
                  ResourceHelper.getResourceHelper().getMessage(
                    ResourceConstants.URIVALIDATOR_MALFORMED_URI,
                      new Object[]{name} ) );
            }
        }
    }

   /**
    * Returns the scheme that must be used by a valid URI.
    *
    * @return the scheme that must be used by a valid URI.
    */
    public String getScheme() 
    {
        return m_scheme;
    }

   /**
    * Specifies the scheme that a URI must have to be valid.
    *
    * @param scheme the scheme that a URI must have to be valid.
    */
    public void setScheme( String scheme )
    {
        m_scheme = scheme;
    }
}
