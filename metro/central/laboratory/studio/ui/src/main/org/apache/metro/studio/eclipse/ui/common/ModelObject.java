/*
 *     Copyright 2004. The Apache Software Foundation.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 *  
 */
package org.apache.metro.studio.eclipse.ui.common;

import java.util.Hashtable;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * @author EH2OBCK
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 *
 * This ModelObject implements the Presentation IModelObject
 * without using the event handling
 * It only shield the differences between the stores used by PreferencePages
 * or PropertyPages so that this Panel can be reused in either situation.
 */
public class ModelObject implements IModelObject 
{
    private transient Hashtable defaults;
    
    private IResource element;
    private IViewPresentationService service;
    private IPreferenceStore store;
    
    /**
     * 
     */
    public ModelObject( DialogPage page ) 
    {
        super();
        defaults = new Hashtable();
        
        if( page instanceof PropertyPage )
        {
            final PropertyPage propPage = (PropertyPage) page;
            element = (IResource) propPage.getElement();
        } else if( page instanceof PreferencePage )
        {
            PreferencePage prefPage = (PreferencePage) page;
            store = prefPage.getPreferenceStore();
        } else
        {
            final String message = "Unsupported DialogPage type: " + 
                page.getClass().getName();
            throw new IllegalArgumentException( message );
        }
    }
    
    public String getDefaultString( String name )
    {
        if( element != null )
        {
            return (String) defaults.get( name );
        }
        if( store != null )
        {
            return store.getDefaultString( name );
        }
        return null;
    }
    
    public String getString( String name )
    {
        try 
        {
            if( element != null )
            {
                QualifiedName qname = new QualifiedName( "", name );
                return (String) element.getPersistentProperty( qname );
            }
            if( store != null )
            {
                return store.getString( name );
            }
        } catch( CoreException e ) 
        {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * @param repository_panel
     */
    public ModelObject controlClicked( String controlName ) 
    {
        
        // local = service.clicked(controlName, this);
        return this;
    }
    
    public void setDefaultString( String name, String defaultValue )
    {
        if( element != null )
        {
            defaults.put( name, defaultValue );
        }
        if( store != null )
        {
            store.setDefault( name, defaultValue );
        }
    }
    
    public void setString( String name, String value )
    {
        try 
        {
            if( element != null )
            {   
                final QualifiedName qname = new QualifiedName( "", name );
                element.setPersistentProperty( qname, value);
            }
            
            if( store != null )
            {
                store.setValue(name, value);
            }
        } catch( CoreException e ) 
        {
            e.printStackTrace();
        }
    }
    
    /**
     * @param repository_panel
     * @return
     */
    public ModelObject windowCreated( String repository_panel ) 
    {
        
        return this;
    }

}
