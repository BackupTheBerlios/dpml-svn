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

package org.apache.avalon.composition.model.impl;

import java.util.ArrayList;

import org.apache.avalon.composition.model.DependencyModel;
import org.apache.avalon.composition.model.ModelException;
import org.apache.avalon.composition.data.DependencyDirective;
import org.apache.avalon.composition.data.SelectionDirective;

import org.apache.avalon.framework.logger.Logger;

import org.apache.avalon.meta.info.DependencyDescriptor;
import org.apache.avalon.meta.info.ServiceDescriptor;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

/**
 * Default implementation of the deplendency model.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultDependencyModel.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class DefaultDependencyModel extends DefaultDependent implements DependencyModel
{
    //--------------------------------------------------------------
    // static
    //--------------------------------------------------------------

    private static final Resources REZ =
            ResourceManager.getPackageResources( DefaultDependencyModel.class );

    //--------------------------------------------------------------
    // immutable state
    //--------------------------------------------------------------

    private final DependencyDescriptor m_descriptor;

    private final DependencyDirective m_directive;

    private final String m_partition;

    private final String m_name;

    private final String m_source;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

   /**
    * Creation of a new dependency model.
    *
    * @param logger the logging channel
    * @param partition the partition
    * @param name the name
    * @param descriptor the dependency descriptor
    * @param directive the dependency directive (possibly null)
    */
    public DefaultDependencyModel( 
      final Logger logger, final String partition, final String name, 
      final DependencyDescriptor descriptor, DependencyDirective directive )
      throws ModelException
    {
        super( logger );

        if( descriptor == null ) throw new NullPointerException( "descriptor" );

        m_descriptor = descriptor;
        m_directive = directive;
        m_partition = partition;
        m_name = name;

        //
        // a dependency directive is either declaring an explicitly
        // identified provider, or, it is delcaring 0 or more selection 
        // constraints - if its a an absolute source declaration then 
        // add it to the table to paths keyed by depedency key names
        //

        if( directive != null )
        {
            if( directive.getSource() != null )
            {
                m_source = resolvePath( partition, directive.getSource() );
                final String message =
                  REZ.getString( "dependency.path.debug", m_source, directive.getKey() );
                getLogger().debug( message );
            }
            else
            {
                m_source = null;
            }
        }
        else
        {
            m_source = null;
        }
    }

    //--------------------------------------------------------------
    // DependencyModel
    //--------------------------------------------------------------

   /**
    * Return the dependency descriptor.
    *
    * @return the descriptor
    */
    public DependencyDescriptor getDependency()
    {
        return m_descriptor;
    }

   /**
    * Return an explicit path to a supplier component.  
    * If a dependency directive has been declared
    * and the directive contains a source declaration, the value 
    * returned is the result of parsing the source value relative 
    * to the absolute address of the implementing component.
    *
    * @return the explicit path
    */
    public String getPath()
    {
        return m_source;
    }

   /**
    * Filter a set of candidate service descriptors and return the 
    * set of acceptable service as a ordered sequence.
    *
    * @param candidates the set of candidate services for the dependency
    *    matching the supplied key
    * @return the accepted candidates in ranked order
    * @exception IllegalArgumentException if the key is unknown
    */
    public ServiceDescriptor[] filter( ServiceDescriptor[] candidates ) 
    {
        if( m_directive != null )
        {
            if( m_directive.getSource() == null )
            {
                return filter( m_directive, candidates );
            }
        }
        return candidates;
    }

   /**
    * Filter a set of candidate service descriptors and return the 
    * set of acceptable service as a ordered sequence.
    *
    * @param directive the dependency directive
    * @param services the set of candidate services for the dependency
    * @return the accepted candidates in ranked order
    */
    private ServiceDescriptor[] filter( 
      DependencyDirective directive, ServiceDescriptor[] services ) 
    {
        SelectionDirective[] filters = getFilters( directive );
        ArrayList list = new ArrayList();

        for( int i=0; i<services.length; i++ )
        {
            ServiceDescriptor service = services[i];
            if( isaCandidate( service, filters ) )
            {
                list.add( service );
            }
        }

        ServiceDescriptor[] candidates = 
          (ServiceDescriptor[]) list.toArray( new ServiceDescriptor[0] );

        //
        // TODO: include ranking of candidates
        //

        return candidates;
    }

    private boolean isaCandidate( 
      ServiceDescriptor service, SelectionDirective[] filters )
    {
        for( int i=0; i<filters.length; i++ )
        {
            SelectionDirective filter = filters[i];
            if( !isaCandidate( service, filter ) )
            {
                return false;
            }
        }
        return true;
    }

    private boolean isaCandidate( 
      ServiceDescriptor service, SelectionDirective filter )
    {
        final String feature = filter.getFeature();
        final String value = filter.getValue();
        final String criteria = filter.getCriteria();

        if( criteria.equals( SelectionDirective.EQUALS ) )
        {
            return value.equals( service.getAttribute( feature ) );
        }
        else if( criteria.equals( SelectionDirective.EXISTS ) )
        {
            return service.getAttribute( feature ) != null;
        }
        else if( criteria.equals( SelectionDirective.INCLUDES ) )
        {
            final String v = service.getAttribute( feature );
            if( v != null )
            {
                return v.indexOf( value ) > -1;
            }
            else
            {
                return false;
            }
        }
        else
        {
            final String error = 
              REZ.getString( "dependency.invalid-criteria.error", criteria, feature );
            throw new IllegalArgumentException( error );
        }
    }

    private String resolvePath( String partition, String path )
    {
        if( path.startsWith( "/" ) )
        {
            return path;
        }
        else if( path.startsWith( "../" ) )
        {
            final String parent = getParentPath( partition );
            return resolvePath( parent, path.substring( 3 ) );
        }
        else if( path.startsWith( "./" ) )
        {
            return resolvePath( partition, path.substring( 2 ) );
        }
        else
        {
            return partition + path;
        }
    }

    private String getParentPath( String partition )
    {
        int n = partition.lastIndexOf( "/" );
        if( n > 0 )
        {
            int index = partition.substring( 0, n-1 ).lastIndexOf( "/" );
            if( index == 0 )
            {
                return "/";
            }
            else
            {
                return partition.substring( 0, index ) + "/";
            }
        }
        else
        {
            final String error = 
              "Illegal attempt to reference a containment context above the root context.";
            throw new IllegalArgumentException( error );
        }
    }

   /**
    * Return the required selection constraints.
    * @param directive the dependency directive
    * @return the set of required selection directives
    */
    private SelectionDirective[] getFilters( DependencyDirective directive )
    {
        ArrayList list = new ArrayList();
        SelectionDirective[] selections = directive.getSelectionDirectives();
        for( int i=0; i<selections.length; i++ )
        {
            SelectionDirective selection = selections[i];
            if( selection.isRequired() )
            {
                list.add( selection );
            }
        }
        return (SelectionDirective[]) list.toArray( new SelectionDirective[0] );
    }
}
