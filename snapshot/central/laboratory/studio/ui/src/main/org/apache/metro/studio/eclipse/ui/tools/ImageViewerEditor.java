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
package org.apache.metro.studio.eclipse.ui.tools;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.swt.SWT;

import org.eclipse.swt.custom.ScrolledComposite;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 */
public class ImageViewerEditor extends EditorPart
{
    private Label imageLabel;
    private FileEditorInput input;
    private Image originalImage;
    private double factor;
    private Composite cImage;

    public ImageViewerEditor()
    {
        super();
    }
    
    public void dispose()
    {
        if( originalImage != null )
        {
            originalImage.dispose();
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void doSave( IProgressMonitor pMonitor )
    {
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.EditorPart#doSaveAs()
     */
    public void doSaveAs()
    {
        // TODO Auto-generated method stub
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite,
     *      org.eclipse.ui.IEditorInput)
     */
    public void init( IEditorSite pSite, IEditorInput pInput ) 
        throws PartInitException
    {
        input = (FileEditorInput) pInput;
        this.setSite( pSite );
        this.setInput( pInput );
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.EditorPart#isDirty()
     */
    public boolean isDirty()
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
     */
    public boolean isSaveAsAllowed()
    {
        // TODO Auto-generated method stub
        return false;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    public void createPartControl( Composite pParent )
    {
        pParent.setLayoutData( new FillLayout() );

        Composite cMain = new Composite( pParent, SWT.NONE );
        cMain.setLayoutData( new GridData( GridData.FILL_BOTH ) );
        cMain.setLayout( new GridLayout() );

        // Bar composite
        {
            final Composite composite = new Composite( cMain, SWT.BORDER );
            composite.setBackground( new Color( null, 192, 192, 192 ) );
            final GridData gridData = new GridData( GridData.FILL_HORIZONTAL );
            gridData.heightHint = 30;
            composite.setLayoutData( gridData );
            final GridLayout gridLayout = new GridLayout();
            gridLayout.numColumns = 8;
            composite.setLayout( gridLayout );
            {
                final Label label = new Label( composite, SWT.NONE );
                label.setBackground( new Color( null, 192, 192, 192 ) );
                label.setText( "Zoom" );
            }
            {
                final Combo combo = new Combo( composite, SWT.NONE );
                combo.addSelectionListener( new SelectionAdapter()
                {
                    public void widgetSelected( SelectionEvent e )
                    {
                        Combo combo = (Combo) e.getSource();
                        int f = Integer.parseInt( combo.getText() );
                        factor = f / 100.0;
                        showImage( factor );
                        cImage.setSize( cImage.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
                    }
                });

                final GridData gridData_1 = new GridData();
                gridData_1.widthHint = 40;
                combo.setLayoutData( gridData_1 );
                combo.setItems( new String[] 
                { 
                    "50", "60", "70", "80", "90", "100", "110", "120", "150" 
                });
                
                combo.setText( "100" );
                factor = 1;
            }
            {
                final Button button = new Button( composite, SWT.NONE );
                button.setText( "in" );
            }
            {
                final Button button = new Button( composite, SWT.NONE );
                button.setText( "out" );
            }
            {
                final Label label = new Label( composite, SWT.RIGHT );
                final GridData gridData_1 = new GridData( GridData.HORIZONTAL_ALIGN_END );
                gridData_1.widthHint = 80;
                label.setLayoutData( gridData_1 );
                label.setBackground( new Color( null, 192, 192, 192 ) );
                label.setText( "image size:" );
            }
            {
                final Label label = new Label( composite, SWT.NONE );
                final GridData gridData_1 = new GridData();
                gridData_1.widthHint = 30;
                label.setLayoutData( gridData_1 );
                label.setBackground( new Color( null, 192, 192, 192 ) );
                label.setText( "0" );
            }
            {
                final Label label = new Label( composite, SWT.NONE );
                final GridData gridData_1 = new GridData();
                gridData_1.widthHint = 30;
                label.setLayoutData( gridData_1 );
                label.setBackground( new Color( null, 192, 192, 192 ) );
                label.setText( "0" );
            }
        }

        // Scroll area
        {
            Composite cScroll = new Composite( cMain, SWT.NONE );
            final GridData gridData = new GridData( GridData.FILL_BOTH );
            cScroll.setLayoutData( gridData );
            cScroll.setLayout( new FillLayout() );
            ScrolledComposite sc =
                new ScrolledComposite( cScroll, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER );
            sc.setAlwaysShowScrollBars( true );
            // image composite
            cImage = new Composite( sc, SWT.NONE );
            sc.setContent( cImage );
            cImage.setLayout( new FillLayout() );

            // create image
            imageLabel = new Label( cImage, SWT.NONE );
            String location = input.getFile().getLocation().toString();
            originalImage = new Image( Display.getDefault(), location );
            showImage( factor );
            cImage.setSize( cImage.computeSize( SWT.DEFAULT, SWT.DEFAULT ) );
        }
    }

    /**
     * @param pFactor
     */
    protected void showImage( double pFactor )
    {
        Image tmp = imageLabel.getImage();
        if( tmp != null )
        {
            tmp.dispose();
        }
        int width = originalImage.getBounds().width;
        int height = originalImage.getBounds().height;

        Display disp = Display.getDefault();
        ImageData imgData = originalImage.getImageData();
        ImageData newData = imgData.scaledTo( (int) (width * pFactor),
                                          (int) (height * pFactor)
        );
        
        Image scaled = new Image( disp, newData );
        imageLabel.setImage(scaled);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    public void setFocus()
    {
        // TODO Auto-generated method stub

    }
}
