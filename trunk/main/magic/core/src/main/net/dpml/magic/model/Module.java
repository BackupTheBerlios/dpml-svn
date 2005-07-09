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

package net.dpml.magic.model;

import java.net.URL;

import net.dpml.magic.AntFileIndex;
import net.dpml.magic.builder.ElementHelper;

import org.apache.tools.ant.BuildException;

import org.w3c.dom.Element;

/**
 * A module represent a collection of resources associated with
 * a publisher, a source repository, a home site and api documentation, a set of package
 * names and the legal information concerning the license and legal notice under which the
 * content is made available.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class Module extends Resource
{
    public static String[] createImports( Element element )
    {
        Element resources = ElementHelper.getChild( element, "resources" );
        if( null == resources )
        {
            return new String[0];
        }
        else
        {
            Element[] links = ElementHelper.getChildren( resources, "import" );
            String[] list = new String[ links.length ];
            for( int i=0; i<links.length; i++ )
            {
                Element link = links[i];
                list[i] = ElementHelper.getAttribute( link, "uri" );
            }
            return list;
        }
    }

    private Header m_header;
    private ResourceRef[] m_entries;

    public Module(
      final AntFileIndex index, final String key, final Info info, final ResourceRef[] resources,
      String uri, Header header, ResourceRef[] entries )
    {
        super( index, key, info, resources, uri );
        m_header = header;
        m_entries = entries;
    }

    public Header getHeader()
    {
        return m_header;
    }

    public ResourceRef[] getEntries()
    {
        return m_entries;
    }

    public static class Header
    {
        public static Header create( Element element )
        {
            try
            {
                Element header = ElementHelper.getChild( element, "header" );
                String publisher = createPublisher( header );
                URL home = createHref( header, "home" );
                URL docs = createHref( header, "docs" );
                URL svn = createHref( header, "svn" );
                URL repository = createHref( header, "repository" );
                Legal legal = createLegal( header );
                return new Header( publisher, legal, home, svn, new String[0], repository, docs );
            }
            catch( Throwable e )
            {
                 final String error =
                   "Unexpected error while attempting to construct module.";
                 throw new BuildException( error, e );
            }
        }

        private static String createPublisher( Element header )
        {
            if( null == header )
            {
                return null;
            }
            else
            {
                Element publisher = ElementHelper.getChild( header, "publisher" );
                if( null == publisher )
                {
                    return null;
                }
                else
                {
                    return ElementHelper.getAttribute( publisher, "name", null );
                }
            }
        }

        private static URL createHref( Element element, String name ) throws Exception
        {
            if( null == element )
            {
                return null;
            }
            else
            {
                Element child = ElementHelper.getChild( element, name );
                if( null == child )
                {
                    return null;
                }
                else
                {
                    final String href = ElementHelper.getAttribute( child, "href" );
                    if( "".equals( href ) )
                    {
                        return null;
                    }

                    try
                    {
                        return new URL( href );
                    }
                    catch( Throwable e )
                    {
                        final String error =
                          "Invalid href attribute value ["
                          + href
                          + "] in the element ["
                          + name
                          + "]";
                        throw new BuildException( error, e );
                    }
                }
            }
        }

        private static Legal createLegal( Element header ) throws Exception
        {
            if( null == header )
            {
                return new Legal( null, null );
            }
            else
            {
                try
                {
                    Element legal = ElementHelper.getChild( header, "legal" );
                    URL license = createHref( legal, "license" );
                    URL notice = createHref( legal, "notice" );
                    return new Legal( license, notice );
                }
                catch( BuildException e )
                {
                    final String error =
                      "Error while attempt to construct legel element in module.";
                    throw new BuildException( error, e );
                }
            }
        }

        private final String m_publisher;
        private final Legal m_legal;
        private final URL m_home;
        private final URL m_svn;
        private final String[] m_packages;
        private final URL m_repository;
        private final URL m_docs;

        public Header( String publisher, Legal legal, URL home, URL svn, String[] packages,
          URL repository, URL docs )
        {
            m_publisher = publisher;
            m_legal = legal;
            m_home = home;
            m_svn = svn;
            m_packages = packages;
            m_repository = repository;
            m_docs = docs;
        }

        public String getPublisher()
        {
            return m_publisher;
        }

        public Legal getLegal()
        {
            return m_legal;
        }

        public URL getHome()
        {
            return m_home;
        }

        public URL getSvn()
        {
            return m_svn;
        }

        public String[] getPackages()
        {
            return m_packages;
        }

        public URL getRepository()
        {
            return m_repository;
        }

        public URL getDocs()
        {
            return m_docs;
        }

        public static class Legal
        {
            private URL m_license;
            private URL m_notice;

           /**
            * Creation of a new legal entry.
            * @param license a url referencing the module license
            * @param notice a url referencing the module notice
            */
            public Legal( URL license, URL notice )
            {
                m_license = license;
                m_notice = notice;
            }

            public URL getLicense()
            {
                return m_license;
            }

            public URL getNotice()
            {
                return m_notice;
            }
        }
    }
}
