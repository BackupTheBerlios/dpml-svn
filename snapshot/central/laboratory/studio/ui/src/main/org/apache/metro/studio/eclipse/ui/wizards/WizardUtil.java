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

import org.apache.metro.studio.eclipse.ui.MetroStudioUI;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.pde.internal.ui.elements.ElementLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TableItem;

/**
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>*
 */
public class WizardUtil
{
    
    public static final ILabelProvider TABLE_LABEL_PROVIDER = new TableLabelProvider();

    public WizardUtil()
    {
        super();
    }
    
    /**
     * This class is used as Labelprovider in SelectionPage
     *  
     */
    static class TableLabelProvider extends ElementLabelProvider 
        implements ITableLabelProvider
    {
        // Names of images used to represent checkboxes
        public static final String CHECKED_IMAGE = "checked";
        public static final String UNCHECKED_IMAGE = "unchecked";
        public static final String MERLIN_IMAGE = "merlin";

        // For the checkbox images
        private static ImageRegistry imageRegistry;

        /**
         * Note: An image registry owns all of the image objects registered
         * with it, and automatically disposes of them the SWT Display is
         * disposed.
         */
        static 
        {
            imageRegistry = new ImageRegistry();
            imageRegistry.put(
                CHECKED_IMAGE,
                MetroStudioUI.getImageDescriptor( "icons/" + CHECKED_IMAGE + ".gif") );
            imageRegistry.put(
                UNCHECKED_IMAGE,
                MetroStudioUI.getImageDescriptor( "icons/" + UNCHECKED_IMAGE + ".gif") );
            imageRegistry.put(
                MERLIN_IMAGE,
                MetroStudioUI.getImageDescriptor( "icons/" + MERLIN_IMAGE + ".gif" ) );
        }

        /**
         * Returns the image with the given key, or <code>null</code> if not
         * found.
         */
        private Image getImage( boolean isSelected )
        {
            String key = isSelected ? CHECKED_IMAGE : UNCHECKED_IMAGE;
            return imageRegistry.get( key );
        }

        /**
         * show the text label
         * 
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
         *      int)
         */
        public String getColumnText( Object o, int index )
        {
            String result = "";
            switch( index )
            {
                case 0 : // COMPLETED_COLUMN
                    result = "";
                    break;
                case 1 : // merlin icon
                    result = "";
                    break;
                case 2 : // merlin icon
                    result = (String)o;
                    break;
                default :
            }
            return result;
        }
        
        /**
         * show the icon
         * 
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
         *      int)
         */
        public Image getColumnImage( Object element, int columnIndex )
        {
            Image result = null;
            switch( columnIndex )
            {
                case 0 : // COMPLETED_COLUMN
                    result = getImage( false );
                    break;
                case 1 : // merlin icon
                    result = imageRegistry.get( MERLIN_IMAGE );
                    break;
                case 2:
                    result = null;
                    break;
                default :
            }
            return result;
        }

    }
    
    public static class SelectionPageCellModifier 
        implements ICellModifier
    {
        private NewMetroProjectSelectionPage selectionPage;
        private String[] columnNames;

        /**
         * Constructor
         * 
         * @param selectionPage
         *            an instance of a selectionPage
         */
        public SelectionPageCellModifier( NewMetroProjectSelectionPage tableViewer )
        {
            super();
            this.selectionPage = tableViewer;
        }

        /**
         * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object,
         *      java.lang.String)
         */
        public boolean canModify( Object element, String property )
        {
            return true;
        }

        /**
         * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object,
         *      java.lang.String)
         */
        public Object getValue( Object element, String property )
        {

            // Find the index of the column
            int columnIndex = 0;

            Object result = null;
            //&& ProjectModel task = (ProjectModel) element;

            switch( columnIndex )
            {
                case 0 : // COMPLETED_COLUMN
                    //&& result = new Boolean(task.isSelected());
                    result = Boolean.TRUE;
                    break;
                default :
                    result = "";
            }
            return result;
        }

        /**
         * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object,
         *      java.lang.String, java.lang.Object)
         */
        public void modify( Object element, String property, Object value )
        {

            // Find the index of the column
            int columnIndex = 0;

            TableItem item = (TableItem) element;
            // ProjectModel task = (ProjectModel) item.getData();

            switch( columnIndex )
            {
                case 0 : // COMPLETED_COLUMN
                    // task.setSelected(((Boolean) value).booleanValue());
                    break;
                default :
            }
            //selectionPage.updateTable(task);
        }
    }
}
