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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URI;
import java.rmi.RemoteException;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.dpml.profile.ApplicationProfile;

/**
 * An activation profile editor panel.
 *
 * @author <a href="mailto:mcconnell@osm.net">Stephen McConnell</a>
 */
class ApplicationProfilePanel extends ClassicPanel 
  implements PropertyChangeListener, DocumentListener
{
    //--------------------------------------------------------------
    // static
    //--------------------------------------------------------------

    private static final EmptyBorder BORDER_5 = new EmptyBorder( 5, 5, 5, 5 );

    //--------------------------------------------------------------
    // state
    //--------------------------------------------------------------

    private final JDialog m_parent;
    private final ApplicationProfile m_profile;

    private JLabel m_label;
    private JCheckBox m_enabled;
    private JTextField m_base;
    private JButton m_ok;
    private JButton m_revert;

    private PropertyChangeSupport m_propertyChangeSupport;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

   /**
    * Creation of a new activation profile editor panel.
    * @param parent the parent dialog
    * @param profile the application profile
    * @exception Excetion if an error occurs
    */
    public ApplicationProfilePanel( JDialog parent, ApplicationProfile profile ) throws Exception 
    {
        super();

        m_parent = parent;
        m_profile = profile;

        //m_profile.addApplicationProfileListener( this );
        m_propertyChangeSupport = new PropertyChangeSupport( this );
        
        //
        // create the dialog label containing the host descriptor name
        //

        String name = profile.getTitle();
        m_label = 
          IconHelper.createImageIconJLabel( 
            getClass().getClassLoader(), 
            "net/dpml/depot/prefs/images/server.png", "Application", "Application: " + name );

        m_label.setBorder( new EmptyBorder( 0, 5, 0, 10 ) );
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout( new BorderLayout() );
        labelPanel.add( m_label, BorderLayout.WEST );
        labelPanel.setBorder( new EmptyBorder( 10, 6, 10, 6 ) );

        //
        // create a panel to hold things in the center of the dialog
        //

        JPanel panel = new JPanel();
        panel.setLayout( new BoxLayout( panel, BoxLayout.Y_AXIS ) );

        JPanel paths = new JPanel();
        panel.add( paths );
        paths.setLayout( new BorderLayout() );
        paths.setBorder( 
          new CompoundBorder(
            new TitledBorder( null, "Plugin URI", TitledBorder.LEFT, TitledBorder.TOP ), 
            BORDER_5 ) 
        );

        m_base = new JTextField( getBasePath() );
        m_base.getDocument().addDocumentListener( this ); // listen for changes
        JPanel basePanel = new JPanel();
        basePanel.setLayout( new BorderLayout() );
        basePanel.setBorder( new EmptyBorder( 0, 0, 0, 30 ) );
        basePanel.add( m_base, BorderLayout.CENTER );
        paths.add( basePanel, BorderLayout.CENTER );

        //
        // create a box layout for the enabled and trusted checkboxs
        //

        JPanel holder = new JPanel();
        holder.setLayout( new BoxLayout( holder, BoxLayout.Y_AXIS ) );
        holder.setBorder( 
          new CompoundBorder(
            new TitledBorder( null, "Parameters", TitledBorder.LEFT, TitledBorder.TOP ), 
            BORDER_5 ) );
        panel.add( holder );

        // add enabled status

        m_enabled = new JCheckBox( new EnableAction( "Enabled" ) );
        m_enabled.setSelected( m_profile.isEnabled() );

        JPanel enabledPanel = new JPanel();
        enabledPanel.setLayout( new BorderLayout() );
        enabledPanel.add( m_enabled, BorderLayout.WEST );
        enabledPanel.add( m_enabled );
        holder.add( enabledPanel );

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

        JPanel thing = new JPanel();
        thing.setLayout( new BorderLayout() );
        thing.add( panel, BorderLayout.NORTH  );

        add( labelPanel, BorderLayout.NORTH );
        add( thing , BorderLayout.CENTER );
        add( buttonHolder, BorderLayout.SOUTH );

        m_propertyChangeSupport.addPropertyChangeListener( this );
    }

    public void dispose()
    {
        m_propertyChangeSupport.removePropertyChangeListener( this );
        m_base.getDocument().removeDocumentListener( this );
    }

    //--------------------------------------------------------------
    // ApplicationProfileListener
    //--------------------------------------------------------------

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
        boolean value = m_enabled.isSelected();
        if( isProfileEnabled() != value )
        {
            flag = true;
        }
        String path = m_base.getText();
        if( !path.equals( getBasePath() ) )
        {
            flag = true;
        }
        m_ok.setEnabled( flag );
        m_revert.setEnabled( flag );
        try
        {
            new URI( m_base.getText() );
            m_enabled.setEnabled( true );
        }
        catch( Throwable e )
        {
            m_enabled.setSelected( isProfileEnabled() );
        }
    }

    private boolean isProfileEnabled()
    {
        try
        {
            return m_profile.isEnabled();
        }
        catch( RemoteException e )
        {
            final String error = 
              "Unexpected remote exception while getting application profile enabled state.";
            throw new RuntimeException( error, e );
        }
    }

    //--------------------------------------------------------------
    // DocumentListener
    //--------------------------------------------------------------

   /**
    * Document insert event.
    * @param event the document event
    */
    public void insertUpdate( DocumentEvent event )
    {
        fireBaseChangedEvent();
    }

   /**
    * Document remove event.
    * @param event the document event
    */
    public void removeUpdate( DocumentEvent event )
    {
        fireBaseChangedEvent();
    }

   /**
    * Document change event.
    * @param event the document event
    */
    public void changedUpdate( DocumentEvent event )
    {
        fireBaseChangedEvent();
    }

    private void fireBaseChangedEvent()
    {
        String path = getBasePath();
        PropertyChangeEvent e = 
          new PropertyChangeEvent( 
            m_base, "base", path, m_base.getText() );
        m_propertyChangeSupport.firePropertyChange( e );
    }

    //--------------------------------------------------------------
    // utility classes
    //--------------------------------------------------------------

   /**
    * Enable action handler.
    */
    private class EnableAction extends AbstractAction
    {
        EnableAction( String name )
        {
            super( name );
        }

        public void actionPerformed( ActionEvent event )
        {
            boolean selected = m_enabled.isSelected();
            PropertyChangeEvent e = 
              new PropertyChangeEvent( 
                this, "enabled", new Boolean( !selected ), new Boolean( selected ) );
            m_propertyChangeSupport.firePropertyChange( e );
        }
    }

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

    /**
     * OK action handler.
     */
     private class OKAction extends AbstractAction 
     {
        OKAction( String name )
        {
            super( name );
            setEnabled( false );
        }
        public void actionPerformed( ActionEvent event )
        {
            String baseValue = m_base.getText();
            URI base = resolveBaseURI( baseValue );
            boolean enabled = m_enabled.isSelected();
            m_parent.hide();
            dispose();
        }
    }

    private URI resolveBaseURI( String path )
    {
        try
        {
            return new URI( path );
        }
        catch( Exception e )
        {
            final String error = 
              "Invalid URI value: " + path;
            throw new RuntimeException( error, e );
        }
    }

   /**
    * Revert action handler.
    */
    private class RevertAction extends AbstractAction
    {
        RevertAction( String name )
        {
            super( name );
            setEnabled( false );
        }

        public void actionPerformed( ActionEvent event )
        {
            m_enabled.setSelected( isProfileEnabled() );
            m_base.setText( getBasePath() );
            PropertyChangeEvent e = 
              new PropertyChangeEvent( this, "revert", null, null );
            m_propertyChangeSupport.firePropertyChange( e );
        }
    }
    
    private String getBasePath()
    {
        try
        {
            URI base = m_profile.getCodeBaseURI();
            if( null == base ) 
            {
                return "";
            }
            else
            {
                return base.toASCIIString();
            }
        }
        catch( RemoteException e )
        {
            final String error = 
              "Unexpected remote exception wyhile resolving codebase uri.";
            throw new RuntimeException( error, e );
        }
    }
}
