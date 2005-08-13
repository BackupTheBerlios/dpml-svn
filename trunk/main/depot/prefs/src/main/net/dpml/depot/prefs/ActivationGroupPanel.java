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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.rmi.RemoteException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;    

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.dpml.profile.ActivationProfile;
import net.dpml.profile.ActivationGroupProfile;

import net.dpml.transit.model.UnknownKeyException;

/**
 * Runnable plugin that handles DPML environment setup.
 *
 * @author <a href="mailto:mcconnell@osm.net">OSM</a>
 */
class ActivationGroupPanel extends ClassicPanel implements PropertyChangeListener
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

    private ActivationGroupProfile m_model;
    private ActivationGroupTableModel m_tableModel;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

   /**
    * Creation of a new panel presenting the state of a activation group.
    * @param window the parent window
    * @param model the activation group model
    * @exception Exception if an error occurs
    */
    public ActivationGroupPanel( Window parent, ActivationGroupProfile model ) throws Exception
    {
        super();

        m_model = model;
        m_parent = parent;

        m_editAction = new EditAction( "Edit" );
        m_edit = new JButton( m_editAction );
        m_delete = new JButton( new DeleteAction( "Delete" ) );

        JLabel label = 
          IconHelper.createImageIconJLabel( 
            getClass().getClassLoader(), MISC_IMG_PATH, 
            "Artifact", "System Properties." ); 
        label.setBorder( new EmptyBorder( 0, 5, 0, 0 ) );
        getHeader().addEntry( label, "JVM Properties", new JButton( "Properties" ) );

        JPanel panel = new JPanel();
        panel.setLayout( new BorderLayout() );
        TitledBorder tb = 
          new TitledBorder( 
            new EmptyBorder( 0, 0, 0, 0 ), "Group", TitledBorder.LEFT, TitledBorder.TOP );
        panel.setBorder( new CompoundBorder( tb, border5 ) );
        getBody().add( panel );

        TableColumnModel columns = createProfilesColumnModel();
        m_tableModel = new ActivationGroupTableModel( model );
        m_table = new ClassicTable( m_tableModel, columns );
        m_table.addPropertyChangeListener( this );
        m_table.setShowVerticalLines( false );
        m_table.setShowHorizontalLines( false );

        JButton[] buttons = new JButton[ 3 ];
        buttons[0] = new JButton( new AddAction( "Add" ) );
        buttons[1] = m_edit;
        buttons[2] = m_delete;

        getBody().addScrollingEntry( m_table, "Activation Profiles", buttons );
    }

   /**
    * Panel disposal.
    */
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
                    ActivationProfile profile = m_model.getActivationProfile( m_selection );
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
    * Utility method to construct the hosts table column model.
    * @return the table
    */
    private static TableColumnModel createProfilesColumnModel()
    {
        TableColumn iconColumn = new TableColumn( 0, 30, new ClassicCellRenderer(), null );
          iconColumn.setHeaderValue( "" );
        iconColumn.setMaxWidth( 30 );
        iconColumn.setMinWidth( 30 );
        TableColumn typeColumn = new TableColumn( 1, 100, new ClassicCellRenderer(), null );
        typeColumn.setHeaderValue( "Profile" );
        TableColumnModel model = new DefaultTableColumnModel();
        model.addColumn( iconColumn  );
        model.addColumn( typeColumn );
        return model;
    }

   /**
    * Add action handler.
    */
    private class AddAction extends EditAction
    {
        public AddAction( String name )
        {
            super( name );
            setEnabled( true );
        }

        public void actionPerformed( ActionEvent event )
        {
            String id = JOptionPane.showInputDialog( m_parent, "Activation Profile ID:" );
            System.out.println( "Add to " + m_model );
        }
    }

   /**
    * Edit action handler.
    */
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
                final String title = "Activation Profile: " + m_selection;
                final Dimension size = new Dimension( 400, 380 );
                ClassicDialog dialog = ClassicDialog.createDialog( m_parent, title, size );
                ActivationProfile profile = m_model.getActivationProfile( m_selection );
                ActivationProfilePanel panel = new ActivationProfilePanel( dialog, profile );
                dialog.getBody().add( panel );
                dialog.setLocationRelativeTo( source );
                dialog.setResizable( false );
                dialog.setVisible( true );
            }
            catch( Throwable e )
            {
                final String error = 
                  "Unexpected error while attempting to construct activation profile dialog."
                  + "\nCODEBASE: "
                  + getClass().getProtectionDomain().getCodeSource().getLocation().toString();
                Logger logger = Logger.getLogger( "depot.prefs" );
                logger.log( Level.SEVERE, error, e );
            }
        }
    }

   /**
    * Delete action handler.
    */
    private class DeleteAction extends AbstractAction
    {
        public DeleteAction( String name )
        {
             super( name );
             setEnabled( false );
        }

        public void actionPerformed( ActionEvent event )
        {
            System.out.println( "Delete: " + m_selection );
        }
    }

    private static final String MISC_IMG_PATH = "net/dpml/depot/prefs/images/source.png";

}
