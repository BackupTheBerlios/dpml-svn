/*
 * Copyright 2004 Stephen J. McConnell.
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

package net.dpml.depot.prefs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.rmi.RemoteException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.dpml.profile.ApplicationRegistry;
import net.dpml.profile.ApplicationProfile;

import net.dpml.transit.model.DuplicateKeyException;
import net.dpml.transit.model.UnknownKeyException;

/**
 * Runnable plugin that handles DPML environment setup.
 *
 * @author <a href="mailto:mcconnell@osm.net">OSM</a>
 */
class ApplicationsRegistryPanel extends ClassicPanel implements PropertyChangeListener
{
    //--------------------------------------------------------------
    // state
    //--------------------------------------------------------------

    private final Window m_parent;

    private ClassicTable m_table;
    private EditAction m_editAction;
    private JButton m_edit;
    private JButton m_delete;
    private String m_selection;

    private ApplicationRegistry m_model;
    private ApplicationsRegistryTableModel m_tableModel;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

    public ApplicationsRegistryPanel( Window parent, ApplicationRegistry model ) throws Exception
    {
        super();

        m_model = model;
        m_parent = parent;

        m_editAction = new EditAction( "Edit" );
        m_edit = new JButton( m_editAction );
        m_delete = new JButton( new DeleteAction( "Delete" ) );

        //JLabel label = 
        //  IconHelper.createImageIconJLabel( 
        //    getClass().getClassLoader(), MISC_IMG_PATH, 
        //    "Application", "Application profiles." ); 
        //label.setBorder( new EmptyBorder( 0, 5, 0, 0 ) );
        //getHeader().addEntry( label, "Application Profile Registry", null );

        JPanel panel = new JPanel();
	  panel.setLayout( new BorderLayout() );
        TitledBorder tb = 
          new TitledBorder( 
            new EmptyBorder( 0,0,0,0 ), "Profiles", TitledBorder.LEFT, TitledBorder.TOP );
        panel.setBorder( new CompoundBorder( tb, border5 ) );
        getBody().add( panel );

        TableColumnModel columns = createProfilesColumnModel();
        m_tableModel = new ApplicationsRegistryTableModel( model );
        m_table = new ClassicTable( m_tableModel, columns );
        m_table.addPropertyChangeListener( this );
        m_table.setShowVerticalLines( false );
        m_table.setShowHorizontalLines( false );

        JButton[] buttons = new JButton[ 3 ];
        buttons[0] = new JButton( new AddAction( "Add" ) );
        buttons[1] = m_edit;
        buttons[2] = m_delete;
        getBody().addScrollingEntry( m_table, "Application Profiles", buttons );
    }

    public void dispose()
    {
        m_table.removePropertyChangeListener( this );
        m_tableModel.dispose();
    }

    //--------------------------------------------------------------
    // PropertyChangelistener
    //--------------------------------------------------------------

   /**
    * handle property change events raised by the table model.
    */
    public void propertyChange( PropertyChangeEvent event )
    {
        if( "selection".equals( event.getPropertyName() ) )
        {
            m_selection = (String) event.getNewValue();
            if( null != m_selection )
            {
                try
                {
                    ApplicationProfile profile = m_model.getApplicationProfile( m_selection );
                    m_edit.setEnabled( true );
                    m_delete.setEnabled( true );
                    getRootPane().setDefaultButton( m_edit );
                }
                catch( UnknownKeyException e )
                {
                    m_edit.setEnabled( false );
                    m_delete.setEnabled( false );
                }
                catch( RemoteException e )
                {
                    final String error = 
                      "Unexpected remote exception while resolving selected application profile.";
                    throw new RuntimeException( error, e );
                }
            }
            else
            {
                m_edit.setEnabled( false );
                m_delete.setEnabled( false );
            }
        }
        else if( "doubleclick".equals( event.getPropertyName() ) )
        {
            m_editAction.editSelection( m_edit );
        }
    }

    //--------------------------------------------------------------
    // utilities
    //--------------------------------------------------------------

   /**
    * Creation of a parameterized scroll pane.
    * @param view the viewport view
    * @return the scroll pane wrapping the supplied view
    */
    private static JScrollPane createScrollPanel( Component view )
    {
        JScrollPane scroller = new JScrollPane();
        scroller.setVerticalScrollBarPolicy( 
          JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED );
        scroller.setHorizontalScrollBarPolicy( 
          JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
        scroller.setViewportView( view );
        return scroller;
    }

   /**
    * Utility method to construct the hosts table column model.
    * @return the table
    */
    private static TableColumnModel createProfilesColumnModel()
    {
        TableColumnModel model = new DefaultTableColumnModel();

	  TableColumn app = new TableColumn( 0, 30, new ClassicCellRenderer(), null );
        app.setHeaderValue( "Application" );
	  model.addColumn( app );

	  TableColumn codebase = new TableColumn( 1, 350, new ClassicCellRenderer(), null );
        codebase.setHeaderValue( "Codebase" );
	  model.addColumn( codebase );

	  return model;
    }

    private class AddAction extends EditAction
    {
        public AddAction( String name )
        {
             super( name );
             setEnabled( true );
        }

        public void actionPerformed( ActionEvent event )
        {
            String id = JOptionPane.showInputDialog( m_parent, "Application Key:" );
            System.out.println( "Add to " + m_model );
        }
    }

    private class EditAction extends AbstractAction
    {
        public EditAction( String name )
        {
             super( name );
             setEnabled( false );
        }

        public void actionPerformed( ActionEvent event )
        {
            editSelection( (Component) event.getSource() );
        }

        public void editSelection( Component source )
        {
            try
            {
                final String title = "Application Profile: " + m_selection;
                final Dimension size = new Dimension( 400, 280 );
                ClassicDialog dialog = ClassicDialog.createDialog( m_parent, title, size );
                ApplicationProfile profile = m_model.getApplicationProfile( m_selection );
                ApplicationProfilePanel panel = 
                   new ApplicationProfilePanel( dialog, profile );
                dialog.getBody().add( panel );
                dialog.setLocationRelativeTo( source );
                dialog.setResizable( false );
                dialog.setVisible(true);
            }
            catch( Throwable e )
            {
                final String error = 
                  "Unexpected error while attempting to construct application profile dialog."
                  + "\nCODEBASE: "
                  + getClass().getProtectionDomain().getCodeSource().getLocation().toString();
                Logger logger = Logger.getLogger( "depot.prefs" );
                logger.log( Level.SEVERE, error, e );
            }
        }
    }

    private class DeleteAction extends AbstractAction
    {
        public DeleteAction( String name )
        {
             super( name );
             setEnabled( false );
        }

        public void actionPerformed( ActionEvent event )
        {
            if( null == m_selection )
            {
                return;
            }
            try
            {
                System.out.println( "Delete: " + m_selection );
                //LayoutModel layout = m_model.getLayoutModel( m_selection );
                //m_model.removeLayoutModel( layout );
            }
            catch( Throwable e )
            {
                final String error = 
                  "Unexpected error while attempting to delete a application profile."
                  + "\nCODEBASE: "
                  + getClass().getProtectionDomain().getCodeSource().getLocation().toString();
                Logger logger = Logger.getLogger( "depot.prefs" );
                logger.log( Level.SEVERE, error, e );
            }
        }
    }

    private static String MISC_IMG_PATH = "net/dpml/depot/prefs/images/misc.png";
}
