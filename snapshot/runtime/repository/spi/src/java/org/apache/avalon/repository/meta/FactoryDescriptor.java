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

package org.apache.avalon.repository.meta;

import javax.naming.directory.Attributes;
import javax.naming.directory.Attribute;
import javax.naming.NamingException;

import org.apache.avalon.repository.Artifact;
import org.apache.avalon.repository.RepositoryRuntimeException;

/**
 * A RelationalDescriptor represents a set of metadata describing the 
 * structural relationships that an artifact has on other 
 * artifacts.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: FactoryDescriptor.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class FactoryDescriptor extends ArtifactDescriptor
{
    //-----------------------------------------------------------
    // static
    //-----------------------------------------------------------

    public static final String API_KEY = 
      "avalon.artifact.dependency.api";

    public static final String SPI_KEY = 
      "avalon.artifact.dependency.spi";

    public static final String IMP_KEY = 
      "avalon.artifact.dependency";

    public static final String FACTORY_KEY = 
      "avalon.artifact.factory";

    public static final String EXPORT_KEY = 
      "avalon.artifact.export";

    //-----------------------------------------------------------
    // immutable state
    //-----------------------------------------------------------

    private final Artifact[] c_api;

    private final Artifact[] c_spi;

    private final Artifact[] c_imp;
    
    private final String m_factory;

    private final String m_interface;

    //-----------------------------------------------------------
    // constructor
    //-----------------------------------------------------------

    /**
     * Creates a new RelationalDescriptor.
     * 
     * @param attributes the meta data attributes
     */
    public FactoryDescriptor( final Attributes attributes ) 
      throws MetaException
    {
        super( attributes );

        c_api = buildDependents( attributes, API_KEY );
        c_spi = buildDependents( attributes, SPI_KEY );
        c_imp = buildDependents( attributes, IMP_KEY );

        m_factory = getFactory( attributes );
        m_interface = getInterface( attributes );
    }

    //-----------------------------------------------------------
    // public
    //-----------------------------------------------------------


   /**
    * Return the factory classname.
    * @return the classname
    */
    public String getFactory()
    {
        return m_factory;
    }

   /**
    * Return the factory interface.
    * @return the interface classname
    */
    public String getInterface()
    {
        return m_interface;
    }

   /**
    * Return the implementation dependencies
    * @return the artifacts
    */
    public Artifact[] getDependencies( String key )
    {
        if( key == API_KEY )
        {
            return c_api;
        }
        else if( key == SPI_KEY )
        {
            return c_spi;
        }
        else if( key == IMP_KEY )
        {
            return c_imp;
        }
        else
        {
            final String error = 
              "Invalid dependency key: " + key;
            throw new IllegalArgumentException( error ); 
        }
    }

    public Artifact[] getDependencies()
    {
        int j = c_api.length + c_spi.length + c_imp.length;
        Artifact[] all = new Artifact[ j ];
        int q = 0;
        for( int i=0; i<c_api.length; i++ )
        {
            all[q] = c_api[i];
            q++;
        }
        for( int i=0; i<c_spi.length; i++ )
        {
            all[q] = c_spi[i];
            q++;
        }
        for( int i=0; i<c_imp.length; i++ )
        {
            all[q] = c_imp[i];
            q++;
        }
        return all;
    }

    public String toString()
    {
        return "[factory:" + m_factory + "]";
    }

    //-----------------------------------------------------------
    // private
    //-----------------------------------------------------------

    private Artifact[] buildDependents( 
      Attributes attributes, String key )
    {
        try
        {
            Attribute attribute = attributes.get( key ) ;
            if( null == attribute ) return new Artifact[0];

            Artifact[] dependencies = 
              new Artifact[ attribute.size() ] ;
            for ( int i = 0; i < dependencies.length; i++ )
            {
                final String spec = (String) attribute.get( i );
                dependencies[i] = Artifact.createArtifact( spec ) ;
            }
            return dependencies;
        }
        catch ( NamingException e )
        {
            throw new RepositoryRuntimeException( 
              "Failed to resolve dependencies for [" + key 
              + "] on the attribute set [" + attributes + "].", e ) ;
        }
    }

    private String getFactory( Attributes attributes )
    {
        try
        {
            return getValue( attributes, FACTORY_KEY ); 
        }
        catch( Throwable e )
        {
            return null;
        }
    }

    private String getInterface( Attributes attributes )
    {
        try
        {
            return getValue( attributes, EXPORT_KEY ); 
        }
        catch( Throwable e )
        {
            return null;
        }
    }
}
