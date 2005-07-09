/*
 * Copyright 2005 Stephen J. McConnell.
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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URI;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;
import javax.swing.JTabbedPane;

import net.dpml.depot.profile.ActivationProfile;

/**
 * A interactive panel that presents the preferences for a single host.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */
class ActivationProfilePanel extends ClassicPanel 
  implements PropertyChangeListener, DocumentListener
{
    //--------------------------------------------------------------
    // static
    //--------------------------------------------------------------

    static EmptyBorder border5 = new EmptyBorder( 5, 5, 5, 5);

    //--------------------------------------------------------------
    // state
    //--------------------------------------------------------------

    private final JDialog m_parent;
    private final ActivationProfile m_profile;

    private JLabel m_label;
    private JCheckBox m_enabled;
    private JTextField m_base;
    private JButton m_ok;
    private JButton m_revert;

    private PropertyChangeSupport m_propertyChangeSupport;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

    public ActivationProfilePanel( JDialog parent, ActivationProfile profile ) throws Exception 
    {
        super();

        m_parent = parent;
        m_profile = profile;

        //m_profile.addActivationProfileListener( this );
        m_propertyChangeSupport = new PropertyChangeSupport( this );
        
        //
        // create the dialog label containing the host descriptor name
        //

        String name = profile.getID();
        m_label = 
          IconHelper.createImageIconJLabel( 
            getClass().getClassLoader(), 
            "net/dpml/depot/prefs/images/server.png", "Activation Profile", "Activation Profile: " + name );

        m_label.setBorder( new EmptyBorder( 0, 5, 0, 10 ) );
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout( new BorderLayout() );
        labelPanel.add( m_label, BorderLayout.WEST );
        labelPanel.setBorder( new EmptyBorder( 10, 6, 10, 6 ) );

        //
        // create a panel to hold things in the center of the dialog
        //

        JPanel panel = new JPanel( new BorderLayout() );

        //
        // include a tabbed pane to separate system properties from 
        // activation features
        //

        final JTabbedPane tabbedPane = new JTabbedPane();
        panel.add( tabbedPane );
        
        tabbedPane.addTab( "Features", new JPanel() ); 
        tabbedPane.addTab( "System", new JPanel() ); 

        //
        // add commmit/revert controls and assemble the dialog
        //

        m_ok = new JButton( new OKAction( "OK" ) );
        m_revert = new JButton( new RevertAction( "Undo Changes" ) );
        JPanel buttonHolder = new JPanel();
        buttonHolder.setLayout( new FlowLayout( FlowLayout.RIGHT ) );
        buttonHolder.add( m_revert );
        buttonHolder.add( m_ok );
        buttonHolder.add( new JButton( new CloseAction( "Cancel" ) ) );
        buttonHolder.setBorder( new EmptyBorder( 10, 10, 5, 0 ) );

        add( labelPanel, BorderLayout.NORTH );
        add( panel , BorderLayout.CENTER );
        add( buttonHolder, BorderLayout.SOUTH );

        m_propertyChangeSupport.addPropertyChangeListener( this );
    }

    public void dispose()
    {
        m_propertyChangeSupport.removePropertyChangeListener( this );
    }

    //--------------------------------------------------------------
    // PropertyChangeListener
    //--------------------------------------------------------------

   /**
    * The actions dealing with changes to the dialog raise change events that 
    * are captured here.  This listener checks changes and enables or disabled 
    * the ok and undo buttons based on the state of controls relative to the 
    * underlying preferences for this host.
    */
    public void propertyChange( PropertyChangeEvent event )
    {
        boolean flag = false;
        m_ok.setEnabled( flag );
        m_revert.setEnabled( flag );
    }

    //--------------------------------------------------------------
    // DocumentListener
    //--------------------------------------------------------------

    public void insertUpdate( DocumentEvent event )
    {
        fireBaseChangedEvent();
    }

    public void removeUpdate( DocumentEvent event )
    {
        fireBaseChangedEvent();
    }

    public void changedUpdate( DocumentEvent event )
    {
        fireBaseChangedEvent();
    }

    private void fireBaseChangedEvent()
    {
    }

    //--------------------------------------------------------------
    // utility classes
    //--------------------------------------------------------------

   /**
    * Action that handles the 'Cancel' dialog button.
    */
    private class CloseAction extends AbstractAction
    {
         CloseAction( String name )
         {
             super( name );
         }

         public void actionPerformed( ActionEvent event )
         {
             m_parent.hide();
             m_ok.setEnabled( false );
             dispose();
         }
     }

     private class OKAction extends AbstractAction 
     {
        OKAction( String name )
        {
            super( name );
            setEnabled( false );
        }

        public void actionPerformed( ActionEvent event )
        {
            m_parent.hide();
            dispose();
        }
    }

    private class RevertAction extends AbstractAction
    {
        RevertAction( String name )
        {
            super( name );
            setEnabled( false );
        }

        public void actionPerformed( ActionEvent event )
        {
            PropertyChangeEvent e = 
              new PropertyChangeEvent( 
                this, "revert", null, null );
            m_propertyChangeSupport.firePropertyChange( e );
        }
    }
}
