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
package org.apache.metro.studio.eclipse.ui.panels;

import org.apache.metro.studio.eclipse.ui.common.CommonDialogs;
import org.apache.metro.studio.eclipse.ui.common.ModelObject;

import org.eclipse.swt.SWT;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author EH2OBCK
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * 
 */
public class RepositorySettingsPanel 
{

    public static final String REPOSITORY_PANEL = "repository.panel";
    public static final String REPOSITORY_APPLY = "repository.apply";
    public static final String REPOSITORY_DEFAULT = "repository.default";
    
    public static final String REPOSITORY_USER = "repository.user";
    public static final String REPOSITORY_SYSTEM = "repository.system";
    public static final String REPOSITORY_REMOTE = "repository.remote";
    public static final String REPOSITORY_PROXY_HOST = "repository.proxyhost";
    public static final String REPOSITORY_PROXY_PORT = "repository.proxyport";
    public static final String REPOSITORY_PROXY_USER = "repository.proxyuser";
    public static final String REPOSITORY_PROXY_PWD = "repository.proxypwd";
    
    private Text password;
    private Text userName;
    private Text proxyPort;
    private Text proxyHome;
    private List remoteHosts;
    private Text userRepository;
    private Text systemRepository;
    private Text metroHome;
    private Shell shell;
    
    private ModelObject model;
    
    public RepositorySettingsPanel() 
    {
        super();
    }
    
    public void initializeControls( ModelObject cModel )
    {
        this.model = cModel.windowCreated( REPOSITORY_PANEL );
        showControls();
        
    }
    
    public void performApply()
    {
        storeControls();
        model.controlClicked( REPOSITORY_APPLY );
    }
    
    public void performDefaults()
    {
        model = model.controlClicked(REPOSITORY_DEFAULT);
        showControls();
    }
    
    private void storeControls()
    {
        model.setString( REPOSITORY_PROXY_PWD, password.getText() );
        model.setString( REPOSITORY_PROXY_USER, userName.getText() );
        model.setString( REPOSITORY_PROXY_PORT, proxyPort.getText() );
        model.setString( REPOSITORY_PROXY_HOST, proxyHome.getText() );
        model.setString( REPOSITORY_USER, userRepository.getText() );
        model.setString( REPOSITORY_SYSTEM, systemRepository.getText() );
    }
    
    private void showControls()
    {
        password.setText( model.getString(REPOSITORY_PROXY_PWD) );
        userName.setText( model.getString(REPOSITORY_PROXY_USER) );
        proxyPort.setText( model.getString(REPOSITORY_PROXY_PORT) );
        proxyHome.setText( model.getString(REPOSITORY_PROXY_HOST) );
        //remoteHosts.setText( model.getString(REPOSITORY_PROXY_PWD) );
        userRepository.setText( model.getString(REPOSITORY_USER) );
        systemRepository.setText( model.getString(REPOSITORY_SYSTEM) );
    }
    
    /**
     * Create all controls
     * @param parent
     * @return
     */
    public Control createControls( Composite parent ) 
    {
        shell = parent.getShell();
        Composite contents = new Composite( parent, SWT.NONE );
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        contents.setLayout( gridLayout );
        contents.setLayoutData( new GridData( GridData.FILL_BOTH ) );
        {
            final Label label = new Label( contents, SWT.NONE );
            label.setText( "Metro Home:" );
        }
        {
            metroHome = new Text( contents, SWT.BORDER );
            metroHome.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        }
        {
            final Button button = new Button( contents, SWT.NONE );
            button.addSelectionListener( new SelectionAdapter() 
            {
                public void widgetSelected( SelectionEvent e)  
                {
                    metroHome.setText( CommonDialogs.getPath( shell ) );
                }
            });
            button.setText( "..." );
        }
        {
            final Label label = new Label( contents, SWT.NONE );
            label.setText( "System Repository:" );
        }
        {
            systemRepository = new Text( contents, SWT.BORDER );
            systemRepository.setLayoutData( new GridData( GridData.HORIZONTAL_ALIGN_FILL ) );
        }
        {
            final Button button = new Button( contents, SWT.NONE );
            button.addSelectionListener( new SelectionAdapter() 
            {
                public void widgetSelected( SelectionEvent e) 
                {
                    systemRepository.setText( CommonDialogs.getPath( shell ) );
                }
            });
            button.setText( "..." );
        }
        {
            final Label label = new Label( contents, SWT.NONE );
            label.setText( "User Repository:" );
        }
        {
            userRepository = new Text( contents, SWT.BORDER );
            userRepository.setLayoutData( new GridData( GridData.HORIZONTAL_ALIGN_FILL ) );
        }
        {
            final Button button = new Button( contents, SWT.NONE );
            button.addSelectionListener( new SelectionAdapter() 
            {
                public void widgetSelected( SelectionEvent e ) 
                {
                    userRepository.setText( CommonDialogs.getPath( shell ) );
                }
            });
            button.setText( "..." );
        }
        {
            final Label label = new Label( contents, SWT.NONE );
            final GridData gridData = new GridData( GridData.VERTICAL_ALIGN_BEGINNING );
            gridData.verticalSpan = 2;
            label.setLayoutData( gridData) ;
            label.setText( "Remote Repositories:" );
        }
        {
            remoteHosts = new List( contents, SWT.BORDER );
            final GridData gridData = new GridData( GridData.HORIZONTAL_ALIGN_FILL );
            gridData.heightHint = 80;
            gridData.verticalSpan = 2;
            remoteHosts.setLayoutData( gridData );
        }
        {
            final Button button = new Button( contents, SWT.NONE );
            final GridData gridData = new GridData( GridData.VERTICAL_ALIGN_BEGINNING );
            gridData.verticalSpan = 2;
            button.setLayoutData( gridData );
            button.setText( "..." );
        }
        {
            final Label label = new Label( contents, SWT.NONE );
            label.setText( "Proxy Host:" );
        }
        {
            proxyHome = new Text( contents, SWT.BORDER );
            final GridData gridData = new GridData( GridData.HORIZONTAL_ALIGN_FILL );
            proxyHome.setLayoutData( gridData );
        }
        {
            final Label label = new Label( contents, SWT.NONE );
        }
        {
            final Label label = new Label( contents, SWT.NONE );
            label.setText( "Proxy Port:" );
        }
        {
            proxyPort = new Text( contents, SWT.BORDER );
            final GridData gridData = new GridData();
            gridData.widthHint = 150;
            gridData.horizontalSpan = 2;
            proxyPort.setLayoutData( gridData );
        }
        {
            final Label label = new Label( contents, SWT.NONE );
            label.setText( "Username:" );
        }
        {
            userName = new Text( contents, SWT.BORDER );
            final GridData gridData = new GridData();
            gridData.widthHint = 150;
            gridData.horizontalSpan = 2;
            userName.setLayoutData( gridData );
        }
        {
            final Label label = new Label( contents, SWT.NONE );
            label.setText( "Password:" );
        }
        {
            password = new Text( contents, SWT.BORDER );
            final GridData gridData = new GridData();
            gridData.widthHint = 150;
            gridData.horizontalSpan = 2;
            password.setLayoutData( gridData );
        }
        {
            final Label label = new Label( contents, SWT.NONE );
        }
        {
            final Button button = new Button( contents, SWT.CHECK );
            button.setText( "Test Remote Repository" );
        }
        return contents;
    }

}
