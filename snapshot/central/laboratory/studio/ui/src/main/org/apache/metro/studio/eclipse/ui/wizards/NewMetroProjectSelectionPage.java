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
package org.apache.metro.studio.eclipse.ui.wizards;

import java.util.Iterator;
import java.util.List;

import org.apache.metro.facility.presentationservice.api.IModelChannel;
import org.apache.metro.facility.presentationservice.api.ChannelException;

import org.apache.metro.studio.eclipse.ui.wizards.WizardUtil.SelectionPageCellModifier;

import org.eclipse.jface.action.Action;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;

import org.eclipse.jface.wizard.WizardPage;

import org.eclipse.pde.internal.ui.elements.ListContentProvider;
import org.eclipse.pde.internal.ui.parts.FormBrowser;

import org.eclipse.swt.SWT;

import org.eclipse.swt.custom.BusyIndicator;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a> 
 */
public class NewMetroProjectSelectionPage extends WizardPage 
    implements ISelectionChangedListener
{
    public static final String SELECTION_PANEL = "new.selection.panel";
    public static final String SELECTION_FINISH = "new.selection.finish";
    public static final String SELECTION_CONTROL = "new.selection.control";
    public static final String SELECTION_CONTROL_SELECTED = "new.selection.control.selected";
    public static final String SELECTION_CONTROL_LIST = "new.selection.control.list";
    
    private WizardSelectedAction doubleClickAction;
    private FormBrowser text;
    private TableViewer projectTable;
    private List projectModels;
    private String currentSelectedWizard = null;
    private IModelChannel channel;

    private class WizardSelectedAction extends Action
    {
        public WizardSelectedAction()
        {
            super( "wizardSelection" );
        }
    }

    public NewMetroProjectSelectionPage( String pageName, IModelChannel channel )
    {
        super( pageName );
        doubleClickAction = new WizardSelectedAction();
        this.channel = channel;
    }

    /**
     * Main method to create all controls of this page. This method is called
     * by the wizard framework.
     * 
     */
    public void createControl( Composite parent )
    {
        Composite composite = new Composite( parent, SWT.NULL );
        composite.setLayout( new GridLayout() );
        composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );
        
        createContainerGroup( composite );
        setControl( composite );
    }

    /**
     * create all controls.
     * 
     */
    protected void createContainerGroup( Composite parent )
    {
        Composite area = new Composite( parent, SWT.NULL );
        final GridLayout gridLayout = new GridLayout();
        gridLayout.makeColumnsEqualWidth = true;
        gridLayout.numColumns = 2;
        area.setLayout( gridLayout );
        area.setLayoutData( new GridData( GridData.FILL_BOTH ) );

        {
            /**
             * Titel for all controls
             */
            final Label label = new Label( area, SWT.NONE );
            final GridData gridData = new GridData();
            gridData.horizontalSpan = 2;
            label.setLayoutData( gridData );
            label.setText( "sample blocks" );
        }
        {
            /**
             * Tabel for Project Types
             */
            Table table = createTable( area );
            createTableView( table );
           
            try
            {
                channel.windowCreated( SELECTION_PANEL );
                String[] value = channel.getValueArray( SELECTION_CONTROL_LIST );
                projectTable.add( value );
                super.setPageComplete( true );
                
            } catch( ChannelException e )
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        {
            /**
             * HTML Text Editor foe showing the project description
             */
            text = new FormBrowser( SWT.BORDER | SWT.V_SCROLL );
            text.createControl( area );
            final GridData gridData = new GridData( GridData.FILL_BOTH );
            Control c = text.getControl();
            c.setLayoutData( gridData );
        }

    }

    /**
     * @param table
     */
    private void createTableView( Table table )
    {
        projectTable = new TableViewer( table );
        String[] colProps = new String [] {"", "", ""} ;
        projectTable.setColumnProperties( colProps );
        projectTable.setContentProvider( new ListContentProvider() );
        projectTable.setLabelProvider( WizardUtil.TABLE_LABEL_PROVIDER );
        
        // Set the cell modifier for the viewer
        projectTable.setCellModifier( new SelectionPageCellModifier( this ) );
        // Create the cell editors
        CellEditor[] editors = new CellEditor[ 3 ];
        // Column 1 : Completed (Checkbox)
        editors[0] = new CheckboxCellEditor( table );
        // Assign the cell editors to the viewer 
        projectTable.setCellEditors( editors );
        projectTable.addDoubleClickListener( new IDoubleClickListener()
        {
            public void doubleClick( DoubleClickEvent event )
            {
                doubleClickAction.run();
            }
        });
        projectTable.addSelectionChangedListener( this );

        // list view pane. Add a border to the pane.
        projectTable.getControl().setLayoutData( new GridData( GridData.FILL_BOTH ) );
    }

    public TableViewer getProjectTable()
    {
        return projectTable;
    }
    
    /**
     * Create the Table Control, which is part of the TableViewer
     * @param pParent
     * @param pStyle
     * @return
     */
    private Table createTable( Composite parent )
    {
        int style = SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION | SWT.HIDE_SELECTION;
        
        Table table = new Table( parent, style );
        table.setLayoutData( new GridData( GridData.FILL_VERTICAL ) );

        TableColumn column = new TableColumn( table, SWT.NONE, 0 );
        column.setWidth( 22 );
        column = new TableColumn( table, SWT.NONE, 1 );
        column.setWidth( 20 );
        column = new TableColumn( table, SWT.NONE, 2 );
        column.setWidth( 150 );
        TableLayout layout = new TableLayout();
        //layout.addColumnData(new ColumnWeightData(100));
        table.setLayout( layout );
        return table;
    }

    public void selectionChanged( SelectionChangedEvent event )
    {
        setErrorMessage( null );
        IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        Iterator iter = selection.iterator();

        if( iter.hasNext() )
        {
            currentSelectedWizard = (String) iter.next();
        }
        
        Display disp = projectTable.getControl().getDisplay();
        
        BusyIndicator.showWhile( disp, new Runnable()
        {
            public void run()
            {
                try
                {
                    channel.putValue( SELECTION_CONTROL_SELECTED, currentSelectedWizard );
                    channel.controlClicked( SELECTION_CONTROL );
                    // get the text and display it
                    final String value = channel.getValue("resource.help");
                    text.setText( value );
                } catch( ChannelException e )
                {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * @return
     */
    public String getSelectedProjectTemplate()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @param pTask
     */
    /*
    public void updateTable(ProjectModel pTask)
    {
        Iterator it = projectModels.iterator();
        while( it.hasNext() )
        {
            ProjectModel model = (ProjectModel) it.next();
            if( model.isSelected() && ! model.equals( pTask ) )
            {
                model.setSelected( false );
                projectTable.update( model, new String[]{"",""} );
            }
        }
        projectTable.update(pTask, new String[]{"",""});
    }
    */
}
