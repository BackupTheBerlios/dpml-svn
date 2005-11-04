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

import java.net.URI;

import net.dpml.magic.AntFileIndex;

import net.dpml.transit.util.ElementHelper;

import org.apache.tools.ant.BuildException;

import org.w3c.dom.Element;

/**
 * A module represent a collection of resources associated with
 * a publisher, a source repository, a home site and api documentation, a set of package
 * names and the legal information concerning the license and legal notice under which the
 * content is made available.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class Module extends Resource
{
   /**
    * Construct an array of import names from the supplied element. 
    * @param element the element
    * @return the import names
    */
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
            for( int i=0; i < links.length; i++ )
            {
                Element link = links[i];
                list[i] = ElementHelper.getAttribute( link, "uri" );
            }
            return list;
        }
    }

    private Header m_header;
    private ResourceRef[] m_entries;

   /**
    * Creation of a new module.
    * @param index the index
    * @param key the reource key
    * @param info the info datastructure
    * @param resources the enclosed resources
    * @param uri the module uri
    * @param header the module header
    * @param entries array of resource references
    */
    public Module(
      final AntFileIndex index, final String key, final Info info, final ResourceRef[] resources,
      String uri, Header header, ResourceRef[] entries )
    {
        super( index, key, info, resources, uri );
        m_header = header;
        m_entries = entries;
    }

   /**
    * Return the module header.
    * @return the module header
    */
    public Header getHeader()
    {
        return m_header;
    }

   /**
    * Return the module entries.
    * @return the module entries
    */
    public ResourceRef[] getEntries()
    {
        return m_entries;
    }

   /**
    * Module header defintion.
    */
    public static class Header
    {
       /**
        * Creation of a new module header using a supplied dopm element.
        * @param element the dom element
        * @return the header
        */
        public static Header create( Element element )
        {
            try
            {
                Element header = ElementHelper.getChild( element, "header" );
                String publisher = createPublisher( header );
                URI home = createHref( header, "home" );
                URI docs = createHref( header, "docs" );
                URI svn = createHref( header, "svn" );
                URI repository = createHref( header, "repository" );
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

        private static URI createHref( Element element, String name ) throws Exception
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
                        return new URI( href );
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
                    URI license = createHref( legal, "license" );
                    URI notice = createHref( legal, "notice" );
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
        private final URI m_home;
        private final URI m_svn;
        private final String[] m_packages;
        private final URI m_repository;
        private final URI m_docs;

       /**
        * Creation of a module header.
        * @param publisher the module publisher
        * @param legal the legal info data
        * @param home the home page uri
        * @param svn the svn repository uri
        * @param packages an array of package names
        * @param repository the primary repository uri
        * @param docs the doc uri
        */
        public Header( String publisher, Legal legal, URI home, URI svn, String[] packages,
          URI repository, URI docs )
        {
            m_publisher = publisher;
            m_legal = legal;
            m_home = home;
            m_svn = svn;
            m_packages = packages;
            m_repository = repository;
            m_docs = docs;
        }

       /**
        * Return the module publisher.
        * @return the module publisher name
        */
        public String getPublisher()
        {
            return m_publisher;
        }

       /**
        * Return the module legal data.
        * @return the legal datastructure
        */
        public Legal getLegal()
        {
            return m_legal;
        }

       /**
        * Return the module home page uri.
        * @return the uri
        */
        public URI getHome()
        {
            return m_home;
        }

       /**
        * Return the module svn uri.
        * @return the uri
        */
        public URI getSvn()
        {
            return m_svn;
        }

       /**
        * Return an array of package names.
        * @return the name array
        */
        public String[] getPackages()
        {
            return m_packages;
        }

       /**
        * Return the module primary repository uri.
        * @return the uri
        */
        public URI getRepository()
        {
            return m_repository;
        }

       /**
        * Return the module docs uri.
        * @return the uri
        */
        public URI getDocs()
        {
            return m_docs;
        }

       /**
        * Legal datastructure.
        */
        public static class Legal
        {
            private URI m_license;
            private URI m_notice;

           /**
            * Creation of a new legal entry.
            * @param license a uri referencing the module license
            * @param notice a uri referencing the module notice
            */
            public Legal( URI license, URI notice )
            {
                m_license = license;
                m_notice = notice;
            }

           /**
            * Return the uri referencing the licensing terms and conditions.
            * @return the uri
            */
            public URI getLicense()
            {
                return m_license;
            }

           /**
            * Return the uri referencing the license notice file.
            * @return the uri
            */
            public URI getNotice()
            {
                return m_notice;
            }
        }
    }
}
