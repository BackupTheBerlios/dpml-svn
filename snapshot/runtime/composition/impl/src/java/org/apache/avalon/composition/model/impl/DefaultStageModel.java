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

import org.apache.avalon.composition.data.StageDirective;
import org.apache.avalon.composition.data.SelectionDirective;
import org.apache.avalon.composition.model.StageModel;
import org.apache.avalon.composition.model.ModelException;

import org.apache.avalon.framework.logger.Logger;

import org.apache.avalon.meta.info.StageDescriptor;
import org.apache.avalon.meta.info.ExtensionDescriptor;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

/**
 * Default implementation of the stage model.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultStageModel.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class DefaultStageModel extends DefaultDependent implements StageModel
{
    //==============================================================
    // static
    //==============================================================

    private static final Resources REZ =
            ResourceManager.getPackageResources( DefaultDependencyModel.class );

    //==============================================================
    // immutable state
    //==============================================================

    private final StageDescriptor m_descriptor;

    private final StageDirective m_directive;

    private final String m_source;

    //==============================================================
    // constructor
    //==============================================================

   /**
    * Creation of a new stage model.
    *
    * @param logger the logging channel
    * @param partition the partition name
    * @param descriptor the stage descriptors
    * @param directive the stage directive (possibly null)
    */
    public DefaultStageModel( 
      final Logger logger, String partition,
      final StageDescriptor descriptor, StageDirective directive )
      throws ModelException
    {
        super( logger );

        if( descriptor == null ) throw new NullPointerException( "descriptor" );

        m_descriptor = descriptor;
        m_directive = directive;

        //
        // a stage directive is either declaring with an explicitly
        // identified provider, or, it is delcaring 0 or more selection 
        // constraints - if its an absolute source declaration then 
        // resolve it now
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

    //==============================================================
    // StageModel
    //==============================================================

   /**
    * Return the stage descriptor.
    *
    * @return the descriptor
    */
    public StageDescriptor getStage()
    {
        return m_descriptor;
    }

   /**
    * Return an explicit path to a supplier component.  
    * If a stage directive has been declared
    * and the directive contains a source declaration, the value 
    * returned is the result of parsing the source value relative 
    * to the absolute address of the dependent component.
    *
    * @return the explicit path
    */
    public String getPath()
    {
        return m_source;
    }

   /**
    * Filter a set of candidate extension descriptors and return the 
    * set of acceptable extensions as a ordered sequence.
    *
    * @param candidates the set of candidate stage providers
    * @return the accepted candidates in ranked order
    */
    public ExtensionDescriptor[] filter( ExtensionDescriptor[] candidates ) 
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
    * set of acceptable stage providers as a ordered sequence.
    *
    * @param directive the stage directive
    * @param providers the set of candidate extension descriptors
    * @return the accepted candidates in ranked order
    */
    private ExtensionDescriptor[] filter( 
      StageDirective directive, ExtensionDescriptor[] providers ) 
    {
        SelectionDirective[] filters = getFilters( directive );
        ArrayList list = new ArrayList();

        for( int i=0; i<providers.length; i++ )
        {
            ExtensionDescriptor provider = providers[i];
            if( isaCandidate( provider, filters ) )
            {
                list.add( provider );
            }
        }

        ExtensionDescriptor[] candidates = 
          (ExtensionDescriptor[]) list.toArray( new ExtensionDescriptor[0] );

        //
        // TODO: include ranking of candidates
        //

        return candidates;
    }

    private boolean isaCandidate( 
      ExtensionDescriptor provider, SelectionDirective[] filters )
    {
        for( int i=0; i<filters.length; i++ )
        {
            SelectionDirective filter = filters[i];
            if( !isaCandidate( provider, filter ) )
            {
                return false;
            }
        }
        return true;
    }

    private boolean isaCandidate( 
      ExtensionDescriptor provider, SelectionDirective filter )
    {
        final String feature = filter.getFeature();
        final String value = filter.getValue();
        final String criteria = filter.getCriteria();

        if( criteria.equals( SelectionDirective.EQUALS ) )
        {
            return value.equals( provider.getAttribute( feature ) );
        }
        else if( criteria.equals( SelectionDirective.EXISTS ) )
        {
            return provider.getAttribute( feature ) != null;
        }
        else if( criteria.equals( SelectionDirective.INCLUDES ) )
        {
            final String v = provider.getAttribute( feature );
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
              REZ.getString( "stage.invalid-criteria.error", criteria, feature );
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
                return partition.substring( 0, index );
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
    private SelectionDirective[] getFilters( StageDirective directive )
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
