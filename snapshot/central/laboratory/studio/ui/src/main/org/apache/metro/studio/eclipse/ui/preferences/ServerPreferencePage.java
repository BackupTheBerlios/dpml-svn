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
package org.apache.metro.studio.eclipse.ui.preferences;

import org.apache.metro.studio.eclipse.ui.MetroStudioUI;

import org.apache.metro.studio.eclipse.ui.common.ModelObject;

import org.apache.metro.studio.eclipse.ui.panels.ServerSettingsPanel;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;

import org.eclipse.swt.SWT;

import org.eclipse.swt.layout.FillLayout;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage </samp>,
 * we can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */
public class ServerPreferencePage extends PreferencePage
    implements IWorkbenchPreferencePage 
{
    
    ServerSettingsPanel panel;
    
    public ServerPreferencePage() 
    {
        super();
        MetroStudioUI ui = MetroStudioUI.getDefault();
        IPreferenceStore prefStore = ui.getPreferenceStore();
        setPreferenceStore( prefStore );
        setDescription( "A demonstration of a preference page implementation" );
    }
    
    /**
     * Create all controls.
     * This is extracted to a Panel class so that the layout can be 
     * reused in Property- or Wizard- classes
     */
    protected Control createContents( Composite parent ) 
    {
        Composite composite = new Composite( parent, SWT.NONE );
        FillLayout layout = new FillLayout();
        composite.setLayout( layout );

        panel = new ServerSettingsPanel();
        panel.createControls( composite );
        ModelObject model = new ModelObject( this );
        panel.initializeControls( model );
        return composite;
    }
    
    /**
     * Apply button pressed
     */
    protected void performApply() 
    {
        panel.performApply();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    protected void performDefaults() 
    {
        panel.performDefaults();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.IPreferencePage#performOk()
     */
    public boolean performOk() 
    {
        panel.performApply();
        return super.performOk();
    }
    
    public void init(IWorkbench workbench) 
    {
    }
}
