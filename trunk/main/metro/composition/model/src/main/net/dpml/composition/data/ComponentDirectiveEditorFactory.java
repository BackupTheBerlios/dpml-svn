/*
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

package net.dpml.composition.data;

import java.net.URI;

import net.dpml.part.Part;
import net.dpml.part.PartEditor;
import net.dpml.part.PartEditorFactory;

import net.dpml.transit.Logger;
import net.dpml.transit.Repository;
import net.dpml.transit.Transit;

/**
 * Edit factory for the composition datatypes. 
 */
public final class ComponentDirectiveEditorFactory implements PartEditorFactory
{
    private Logger m_logger;
    private PartEditorFactory m_factory;

    public ComponentDirectiveEditorFactory( Logger logger )
    {
        m_logger = logger;
        m_factory = loadFactory();
    }

    public PartEditor getPartEditor( Part part )
    {
        return m_factory.getPartEditor( part );
    }

    private PartEditorFactory loadFactory()
    {
        try
        {
            ClassLoader classloader = getClass().getClassLoader();
            URI uri = new URI( "@COMPOSITION-EDITOR-URI@" );
            Repository repository = Transit.getInstance().getRepository();
            return (PartEditorFactory) repository.getPlugin( classloader, uri, new Object[]{m_logger} );
        }
        catch( Throwable e )
        {
            final String error =
              "Internal error while attempting to establish the composition part editor factory.";
            throw new RuntimeException( error, e );
        }
    }
}

