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

import org.apache.avalon.composition.model.ModelException;
import org.apache.avalon.composition.model.ContainmentModel;
import org.apache.avalon.composition.provider.ComponentContext;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

import org.apache.avalon.framework.context.ContextException;

import org.apache.avalon.meta.info.EntryDescriptor;



/**
 * Default implementation of a the context entry import model.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultImportModel.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class DefaultImportModel extends DefaultEntryModel
{
    //==============================================================
    // static
    //==============================================================

    private static final Resources REZ =
      ResourceManager.getPackageResources( DefaultImportModel.class );

    //==============================================================
    // immutable state
    //==============================================================

    private final EntryDescriptor m_descriptor;

    private final String m_key;

    private final ComponentContext m_context;

    //==============================================================
    // mutable state
    //==============================================================

    private Object m_value;

    //==============================================================
    // constructor
    //==============================================================

   /**
    * Creation of a new context entry import model.
    *
    * @param descriptor the context entry descriptor
    * @param key the container scoped key
    * @param context the containment context
    */
    public DefaultImportModel( 
      EntryDescriptor descriptor, String key, 
      ComponentContext context ) throws ModelException
    {
        super( descriptor );
        if( key == null )
        {
            throw new NullPointerException( "key" );
        }
        if( context == null )
        {
            throw new NullPointerException( "context" );
        }

        m_key = key;
        m_context = context;
        m_descriptor = descriptor;

        if( !DefaultContextModel.isaStandardKey( key ) )
        {
            final String error = 
              REZ.getString( 
                "context.non-standard-key.error", key );
            throw new ModelException( error );
        }

        if( !descriptor.isVolatile() )
        {
            m_value = getValue();
        }
    }

    //--------------------------------------------------------------
    // EntryModel
    //--------------------------------------------------------------

   /**
    * Return the context entry value.
    * 
    * @return the context entry value
    */
    public Object getValue() throws ModelException
    {
        if( m_value != null ) return m_value;
        return getStandardEntry( m_key );
    }


    private Object getStandardEntry( String key )
    {
        if( key.startsWith( "urn:avalon:" ) )
        {
            return getStandardAvalonEntry( key );
        }
        else if( key.startsWith( "urn:composition:" ) )
        {
            return getStandardCompositionEntry( key );
        }
        else
        {
            final String error = 
              "Unknown key [" + key + "]";
            throw new IllegalArgumentException( error );
        }
    }

    private Object getStandardAvalonEntry( String key )
    {
        if( key.equals( ComponentContext.NAME_KEY ) )
        {
            return m_context.getName();
        }
        else if( key.equals( ComponentContext.PARTITION_KEY ) )
        {
            return m_context.getPartitionName();
        }
        else if( key.equals( ComponentContext.CLASSLOADER_KEY ) )
        {
            return m_context.getClassLoader();
        }
        else if( key.equals( ComponentContext.HOME_KEY ) )
        {
            return m_context.getHomeDirectory();
        }
        else if( key.equals( ComponentContext.TEMP_KEY ) )
        {
            return m_context.getTempDirectory();
        }
        return null;
    }

    private Object getStandardCompositionEntry( String key )
    {
        if( key.equals( ContainmentModel.KEY ) )
        {
            return m_context.getContainmentModel();
        }
        else
        {
            try
            {
                return m_context.getSystemContext().get( key );
            }
            catch( ContextException e )
            {
                return null;
            }
        }
    }
}
