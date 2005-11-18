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

/**
 * The <code>URIValidator</code> validates the string argument
 * values are valid URIs.  If the value is a URI, the string value in
 * the {@link java.util.List} of values is replaced with the
 * {@link java.net.URI} instance.
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
 *     .withValidator( new URIValidator( "artifact", "link" ) );
 * </pre>
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class URIValidator implements Validator 
{
    private final String[] m_schemes;

   /**
    * Creates a UriValidator.
    */
    public URIValidator() 
    {
        m_schemes = new String[0];
    }

   /**
    * Creates a UriValidator for the specified scheme.
    * @param scheme the uri scheme
    */
    public URIValidator( final String scheme ) 
    {
        m_schemes = new String[]{scheme};
    }
    
   /**
    * Creates a UriValidator for the specified schemes.
    * @param schemes an array of schemes
    */
    public URIValidator( final String[] schemes ) 
    {
        m_schemes = schemes;
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
        for( final ListIterator i = values.listIterator(); i.hasNext();) 
        {
            final Object object = i.next();
            if( object instanceof URI )
            {
                break;
            }
            final String name = (String) object;
            try 
            {
                final URI uri = new URI( name );
                if( m_schemes.length == 0 )
                {
                    i.set( uri );
                }
                else 
                {
                    if( match( uri ) )
                    {
                        i.set( uri );
                    }
                    else
                    {
                        throw new InvalidArgumentException( name );
                    }
                }
            } 
            catch( final URISyntaxException e )
            {
                final String error =
                  "Bad uri syntax in value [" + name + "].";
                throw new InvalidArgumentException( error );
            }
        }
    }
    
    private boolean match( URI uri )
    {
        String scheme = uri.getScheme();
        for( int i=0; i<m_schemes.length; i++ )
        {
            if( scheme.startsWith( m_schemes[i] ) )
            {
                return true;
            }
        }
        return false;
    }
}
