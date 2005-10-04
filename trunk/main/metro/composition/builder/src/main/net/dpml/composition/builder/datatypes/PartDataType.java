/*
 * Copyright (c) 2005 Stephen J. McConnell
 *
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

package net.dpml.composition.builder.datatypes;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import net.dpml.composition.builder.PartReferenceBuilder;
import net.dpml.composition.controller.CompositionController;
import net.dpml.component.info;.Type;

import net.dpml.component.control.ControllerContext;

import net.dpml.part.DelegationException;
import net.dpml.part.PartNotFoundException;
import net.dpml.part.Part;
import net.dpml.component.info;.PartReference;
import net.dpml.part.PartContentHandlerFactory;

import net.dpml.transit.tools.AntAdapter;
import net.dpml.transit.Logger;
import net.dpml.transit.model.ContentModel;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

/**
 * Task that handles the construction of a serialized container part.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class PartDataType extends Task implements PartReferenceBuilder
{
    private URI m_uri;
    private String m_key;

    public void setUri( URI uri )
    {
        m_uri = uri;
    }

    public void setKey( String key )
    {
        m_key = key;
    }

    //---------------------------------------------------------------------
    // Builder
    //---------------------------------------------------------------------

   /**
    * Return a uri identitifying the builder.
    *
    * @return the builder uri
    */
    public URI getBuilderURI()
    {
        return PART_BUILDER_URI;
    }

   /**
    * Return a urn identitifying the part handler for this builder.
    *
    * @return a strategy uri
    */
    public URI getPartHandlerURI()
    {
        return PART_HANDLER_URI;
    }

    //---------------------------------------------------------------------
    // PartReferenceBuilder
    //---------------------------------------------------------------------

   /**
    * Return the part key.
    *
    * @return the key
    */
    public String getKey()
    {
        if( null == m_key )
        {
            final String error = 
              "Missing key attribute for nested part reference.";
            throw new BuildException( error );
        }
        return m_key;
    }

   /**
    * Return the part uri.
    *
    * @return the uri
    */
    public URI getURI()
    {
        if( null == m_uri )
        {
            final String error = 
              "Missing uri attribute for nested part reference.";
            throw new BuildException( error );
        }
        return m_uri;
    }

    public PartReference buildPartReference( ClassLoader classloader, Type type )
    {
        String key = getKey();
        Part part = buildPart( type, classloader );
        return new PartReference( key, part );
    }

    private Part buildPart( Type type, ClassLoader classloader ) 
    {
        String key = getKey();
        URI uri = getURI();
        try
        {
            Logger logger = new AntAdapter( this );
            CompositionController controller = new CompositionController( logger );
            return controller.loadPart( uri );
        }
        catch( PartNotFoundException pnfe )
        {
            final String error =
              "Unable to include the part ["
              + key 
              + "] because part refernece ["
              + uri
              + "] could not be found.";
            throw new BuildException( error );
        }
        catch( DelegationException de )
        {
            final String error =
              "Delegation error while attempting to load part ["
              + m_uri
              + "] due to: " 
              + de.getMessage();
            throw new BuildException( error, de );
        }
        catch( IOException ioe )
        {
            final String error =
              "IO error while attempting to load part ["
              + m_uri
              + "] due to: " 
              + ioe.getMessage();
            throw new BuildException( error, ioe );
        }
        catch( Exception e )
        {
            final String error =
              "Unexpected error while attempting to load part ["
              + m_uri
              + "] due to: " 
              + e.getMessage();
            throw new BuildException( error, e );
        }
    }

    private static URI PART_HANDLER_URI = setupURI( "@PART-HANDLER-URI@" );
    private static URI PART_BUILDER_URI = setupURI( "@PART-BUILDER-URI@" );

    protected static URI setupURI( String spec )
    {
        try
        {
            return new URI( spec );
        }
        catch( URISyntaxException ioe )
        {
            return null;
        }
    }
}
