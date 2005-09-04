
package net.dpml.depot.desktop;

import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.net.URI;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.beans.PropertyChangeEvent;

import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JToggleButton;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JOptionPane;
import javax.swing.JFrame;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;

import net.dpml.station.Application;
import net.dpml.station.ApplicationEvent;
import net.dpml.station.ApplicationListener;
import net.dpml.station.Application.State;

import net.dpml.profile.ApplicationRegistry;
import net.dpml.profile.ApplicationProfile;
import net.dpml.profile.ApplicationProfile.StartupPolicy;

import net.dpml.transit.Artifact;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Application profile tree node. 
 */
public final class ApplicationProfileTreeNode extends Node
{
    private static final ImageIcon ICON = readImageIcon( "16/application.png" );
    private static final Object[] POLICY_OPTIONS = 
      new Object[]
      {
        ApplicationProfile.DISABLED,
        ApplicationProfile.MANUAL,
        ApplicationProfile.AUTOMATIC,
      };

    private final Application m_application;
    private final ApplicationProfile m_profile;
    private final String m_title;
    private final JLabel m_label;
    private final Component m_component;
    private final PolicyComboBox m_policy;
    private JButton m_start = new JButton( "Start" );
    private JButton m_stop = new JButton( "Stop" );
    private JButton m_restart = new JButton( "Restart" );

    private LinkedList m_changes = new LinkedList();

    public ApplicationProfileTreeNode( Application application ) throws Exception
    {
        super( application );
        
        m_application = application;
        m_profile = application.getProfile();
        m_policy = new PolicyComboBox();
        m_title = m_profile.getTitle();

        m_label = new JLabel( m_title, ICON, SwingConstants.LEFT );
        m_label.setBorder( new EmptyBorder( 3, 3, 2, 2 ) );

        m_application.addApplicationListener( new RemoteApplicationListener() );

        setupControlButtons();

        m_start.addActionListener( 
          new ActionListener() 
          {
            public void actionPerformed( ActionEvent event ) 
            {
                Thread thread = 
                  new Thread()
                  {
                      public void run()
                      {
                          try
                          {
                              m_application.start();
                          }
                          catch( Exception e ) 
                          {
                              final String message = 
                                "An application startup error occured."
                                + "\n" + e.getClass().getName()
                                + "\n" + e.getMessage();
                              JOptionPane.showMessageDialog(
                                getParentFrame( m_start ), message, "Error", JOptionPane.ERROR_MESSAGE );
                          }
                      }
                  };
                thread.start();
             }
          } 
        );
        m_stop.addActionListener( 
          new ActionListener() 
          {
            public void actionPerformed( ActionEvent event ) 
            {
                Thread thread = 
                  new Thread()
                  {
                      public void run()
                      {
                          try
                          {
                              m_application.stop();
                          }
                          catch( Exception e ) 
                          {
                              final String message = 
                                "An application shutdown error occured."
                                + "\n" + e.getClass().getName()
                                + "\n" + e.getMessage();
                              JOptionPane.showMessageDialog(
                                getParentFrame( m_start ), message, "Error", JOptionPane.ERROR_MESSAGE );
                          }
                      }
                  };
                thread.start();
            }
        });
        m_restart.addActionListener( 
          new ActionListener() 
          {
            public void actionPerformed( ActionEvent event ) 
            {
                Thread thread = 
                  new Thread()
                  {
                      public void run()
                      {
                          try
                          {
                              m_application.restart();
                          }
                          catch( Exception e ) 
                          {
                              final String message = 
                                "An application restart error occured."
                                + "\n" + e.getClass().getName()
                                + "\n" + e.getMessage();
                              JOptionPane.showMessageDialog(
                                getParentFrame( m_start ), message, "Error", JOptionPane.ERROR_MESSAGE );
                          }
                      }
                  };
                thread.start();
            }
        });

        add( new SystemPropertiesTreeNode( m_profile ) );
        add( new ParametersTreeNode( m_profile ) );

        URI uri = m_profile.getCodeBaseURI();
        Artifact artifact = Artifact.createArtifact( uri );
        String type = artifact.getType();
        if( "plugin".equals( type ) )
        {
            add( new CodeBaseTreeNode( m_profile ) );
        }
        else if( "part".equals( type ) )
        {
            add( new HandlerTreeNode( m_profile ) );
            add( new PartTreeNode( m_profile ) );
        }
        m_component = buildComponent();
    }

    Component getLabel()
    {
        return m_label;
    }

    Component getComponent()
    {
        return m_component;
    }

    private Component buildComponent() throws Exception
    {
        FormLayout layout = new FormLayout(
          "right:pref, 3dlu, 60dlu, fill:max(60dlu;pref), 7dlu, pref", 
          "pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref" );

        PanelBuilder builder = new PanelBuilder( layout );
        builder.setDefaultDialogBorder();

        CellConstraints cc = new CellConstraints();

        builder.addSeparator( "Configuration",             cc.xyw( 1, 1, 6 ) );

        builder.addLabel( "ID:",                            cc.xy( 1, 3 ) ); 
        builder.addLabel( getProfileID() ,                 cc.xyw( 3, 3, 4 ) );

        builder.addLabel( "Title:",                         cc.xy( 1, 5 ) ); 
        builder.add( getTitleComponent(),                  cc.xyw( 3, 5, 4 ) );

        builder.addLabel( "Codebase:",                      cc.xy( 1, 7 ) ); 
        builder.add( getCodebaseComponent(),               cc.xyw( 3, 7, 4 ) );

        builder.addLabel( "Base Directory:",                cc.xy( 1, 9 ) ); 
        builder.add( getBasedirComponent(),                cc.xyw( 3, 9, 2 ) );
        builder.add( new JButton( "chooser" ),              cc.xy( 6, 9 ) );

        builder.addLabel( "Startup Policy",                 cc.xy( 1, 11 ) ); 
        builder.add( getStartupPolicyComponent(),          cc.xyw( 3, 11, 1 ) ); 

        builder.addSeparator( "Timeouts",                  cc.xyw( 1, 13, 6 ) );

        builder.addLabel( "Startup",                        cc.xy( 1, 15 ) ); 
        builder.add( getStartupTimeoutComponent(),         cc.xyw( 3, 15, 1 ) ); 

        builder.addLabel( "Shutdown",                       cc.xy( 1, 17 ) ); 
        builder.add( getShutdownTimeoutComponent(),        cc.xyw( 3, 17, 1 ) ); 

        builder.addSeparator( "Process",                   cc.xyw( 1, 19, 6 ) );

        builder.add( getProcessComponent(),                cc.xyw( 3, 21, 4 ) ); 

        return builder.getPanel();
    }

    private Component getProcessComponent() throws Exception
    {
        JPanel buttons = new JPanel( new FlowLayout( FlowLayout.LEFT ) ); 
        buttons.setBorder( new EmptyBorder( 0, 0, 0, 0 ) );
        buttons.add( m_start );
        buttons.add( m_stop );
        buttons.add( m_restart );
        return buttons;
    }

    private String getProfileID() throws Exception
    {
        return m_profile.getID();
    }

    private Component getTitleComponent() throws Exception
    {
        return new TitleDocument();
    }

    private Component getStartupTimeoutComponent() throws Exception
    {
        StartupTimeoutSpinnerModel model = new StartupTimeoutSpinnerModel();
        return new JSpinner( model );
    }

    private Component getShutdownTimeoutComponent() throws Exception
    {
        ShutdownTimeoutSpinnerModel model = new ShutdownTimeoutSpinnerModel();
        return new JSpinner( model );
    }

    private Component getCodebaseComponent() throws Exception
    {
        return new CodeBaseDocument();
    }

    private Component getBasedirComponent() throws Exception
    {
        return new JTextField( m_profile.getWorkingDirectoryPath() );
    }

    private Component getStartupPolicyComponent()
    {
        return m_policy;
    }

    public boolean isModified()
    {
        return m_changes.size() > 0;
    }

    public void apply() throws Exception
    {
        Volotile[] components = (Volotile[]) m_changes.toArray( new Volotile[0] );
        for( int i=0; i < components.length; i++ )
        {
            Volotile component = components[i];
            component.apply();
        }
    }

    public void revert() throws Exception
    {
        Volotile[] components = (Volotile[]) m_changes.toArray( new Volotile[0] );
        for( int i=0; i < components.length; i++ )
        {
            Volotile component = components[i];
            component.revert();
        }
    }

   /**
    * If the supplied component is modified we add it to the list of modified 
    * controls otherwise the control if removed from the list.  When a request
    * to apply or revert changes is received we grap all of the modified 
    * componenets and invoke the respective apply/revert operation.
    */
    private void updateChangeList( Volotile component, boolean modified )
    {
        boolean originalState = isModified();
        m_changes.remove( component );
        if( modified )
        {
            m_changes.add( component );
        }
        boolean newState = isModified();
        if( originalState != newState )
        {
            PropertyChangeEvent e = 
              new PropertyChangeEvent( 
                this, "modified", new Boolean( originalState ), new Boolean( newState ) );
            firePropertyChange( e );
        }
    }

    private void setupControlButtons()
    {
        try
        {
            State state = m_application.getState();
            setupControlButtons( state );
        }
        catch( Throwable e )
        {
            // ignore
        }
    }

    private void setupControlButtons( State state )
    {
        if( Application.READY.equals( state ) )
        {
            m_start.setEnabled( true );
            m_stop.setEnabled( false );
            m_restart.setEnabled( false );
        }
        else if( Application.STARTING.equals( state ) )
        {
            m_start.setEnabled( false );
            m_stop.setEnabled( false );
            m_restart.setEnabled( false );
        }
        else if( Application.RUNNING.equals( state ) )
        {
            m_start.setEnabled( false );
            m_stop.setEnabled( true );
            m_restart.setEnabled( true );
        }
        else if( Application.STOPPING.equals( state ) )
        {
            m_start.setEnabled( false );
            m_stop.setEnabled( false );
            m_restart.setEnabled( false );
        }
        else
        {
            System.out.println( "## STATE?: " + state.getClass().getName() + ", " + state );
        }
    }

    private JFrame getParentFrame( Component component ) 
    {
        return (JFrame) SwingUtilities.getWindowAncestor( component );
    }


    //--------------------------------------------------------------------
    // startup policy components
    //--------------------------------------------------------------------

   /**
    * The PolicyComboBox is a volotile component that maintains the 
    * presentation of the application profile startup policy. 
    */
    private class PolicyComboBox extends JComboBox implements Volotile
    {
        private PolicyComboBox() throws Exception
        {
            super( POLICY_OPTIONS );
            setSelectedItem( m_profile.getStartupPolicy() );
        }

        public void selectedItemChanged()
        {
            super.selectedItemChanged();
            updateChangeList( this, isModified() );
        }

       /**
        * Returns TRUE if the selected value is not the same as the application
        * profile startup policy value.
        * @return the modified status of the component
        */
        public boolean isModified()
        {
            try
            {
                return !m_profile.getStartupPolicy().equals( super.getSelectedItem() );
            }
            catch( Exception e )
            {
                return false;
            }
        }

       /**
        * Apply changes such that the application profile statup policy
        * is synchronized with the component value.
        * @return true if the update was successfull
        */
        public void apply() throws Exception
        {
            StartupPolicy policy = (StartupPolicy) super.getSelectedItem();
            m_profile.setStartupPolicy( policy );
            updateChangeList( this, false );
        }

       /**
        * Revert changes such that the component reflects the status of the 
        * application profile statup policy.
        * @return true if the reversion was successfull
        */
        public void revert() throws Exception
        {
            super.setSelectedItem( m_profile.getStartupPolicy() );
            updateChangeList( this, false );
        }
    }

    //--------------------------------------------------------------------
    // timeout components
    //--------------------------------------------------------------------

    private abstract class TimeoutSpinnerModel extends SpinnerNumberModel implements Volotile
    {
        TimeoutSpinnerModel()
        {
            super();
        }

        public void setValue( Object value )
        {
            super.setValue( value );
            updateChangeList( this, true );
        }

        abstract Integer getTimeout();
        abstract void setTimeout( Integer value );

        public boolean isModified()
        {
            try
            {
                Object value = getTimeout();
                return value.equals( getValue() );
            }
            catch( Throwable e )
            {
                return false;
            }
        }

        public void apply() throws Exception
        {
            Integer value = (Integer) getValue();
            setTimeout( value );
            updateChangeList( this, false );
        }

        public void revert() throws Exception
        {
            Integer value = getTimeout();
            setValue( value );
            updateChangeList( this, false );
        }
    }

    private class StartupTimeoutSpinnerModel extends TimeoutSpinnerModel
    {
        StartupTimeoutSpinnerModel() throws Exception
        {
            super();
            revert();
        }

        Integer getTimeout()
        {
            try
            {
                return new Integer( m_profile.getStartupTimeout() );
            }
            catch( Throwable e )
            {
                e.printStackTrace();
                return new Integer( 0 );
            }
        }

        void setTimeout( Integer value )
        {
            try
            {
                int n = value.intValue();
                m_profile.setStartupTimeout( n );
            }
            catch( Throwable e )
            {
                e.printStackTrace();
            }
        }
    }

    private class ShutdownTimeoutSpinnerModel extends TimeoutSpinnerModel
    {
        ShutdownTimeoutSpinnerModel() throws Exception
        {
            super();
            revert();
        }

        Integer getTimeout()
        {
            try
            {
                return new Integer( m_profile.getShutdownTimeout() );
            }
            catch( Throwable e )
            {
                e.printStackTrace();
                return new Integer( 0 );
            }
        }

        void setTimeout( Integer value )
        {
            try
            {
                int n = value.intValue();
                m_profile.setShutdownTimeout( n );
            }
            catch( Throwable e )
            {
                // ignore
            }
        }
    }

    //--------------------------------------------------------------------
    // field components
    //--------------------------------------------------------------------

    private abstract class VolotileDocument extends JTextField implements DocumentListener, Volotile
    {
        public VolotileDocument( String text )
        {
            super( text );
            getDocument().addDocumentListener( this );
        }

        public void insertUpdate( DocumentEvent event )
        {
            setModified();
        }

        public void removeUpdate( DocumentEvent event )
        {
            setModified();
        }

        public void changedUpdate( DocumentEvent event )
        {
            setModified();
        }

        private void setModified()
        {
            updateChangeList( this, true );
        }

        public abstract boolean isModified();
        public abstract void apply() throws Exception;
        public abstract void revert() throws Exception;

    }

    private class TitleDocument extends VolotileDocument 
    {
        public TitleDocument() throws Exception
        {
            super( m_profile.getTitle() );
        }

        public boolean isModified()
        {
            try
            {
                String text = getText();
                String title = m_profile.getTitle();
                return text.equals( title );
            }
            catch( Exception e )
            {
                return false;
            }
        }

        public void apply() throws Exception
        {
            String text = getText();
            m_profile.setTitle( text );
            updateChangeList( this, false );
        }

        public void revert() throws Exception
        {
            String text = m_profile.getTitle();
            setText( text );
            updateChangeList( this, false );
        }
    }

    private class CodeBaseDocument extends VolotileDocument 
    {
        public CodeBaseDocument() throws Exception
        {
            super( m_profile.getCodeBaseURI().toString() );
        }

        public boolean isModified()
        {
            try
            {
                String text = getText();
                String codebase = m_profile.getCodeBaseURI().toString();
                return text.equals( codebase );
            }
            catch( Exception e )
            {
                return false;
            }
        }

        public void apply() throws Exception
        {
            String text = getText();
            m_profile.setCodeBaseURI( new URI( text ) );
            updateChangeList( this, false );
        }

        public void revert() throws Exception
        {
            String text = m_profile.getCodeBaseURI().toString();
            setText( text );
            updateChangeList( this, false );
        }
    }

    private class RemoteApplicationListener extends UnicastRemoteObject implements ApplicationListener
    {
        private Thread m_thread;

        private RemoteApplicationListener() throws RemoteException
        {
            super();
        }

        public void applicationStateChanged( final ApplicationEvent event ) throws RemoteException
        {
            SwingUtilities.invokeLater( 
              new Runnable() 
              {
                public void run()
                {
                   try
                   {
                       State state = event.getState();
                       setupControlButtons( state );
                   }
                   catch( Throwable e )
                   {
                       e.printStackTrace();
                   }
                }
              }
            );
        }
    }
}