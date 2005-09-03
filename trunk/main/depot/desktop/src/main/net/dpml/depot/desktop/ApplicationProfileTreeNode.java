
package net.dpml.depot.desktop;

import java.awt.Component;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.net.URI;
import java.util.LinkedList;
import java.beans.PropertyChangeEvent;

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
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;

import net.dpml.station.Application;

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
    private final JButton m_apply = new JButton( readImageIcon( "16/save.png" ) );
    private final JButton m_undo = new JButton( readImageIcon( "16/undo.png" ) );

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

        m_undo.setEnabled( false );
        m_undo.setFocusable( false );
        //m_undo.setContentAreaFilled( false );
        m_undo.setBorderPainted( false );

        m_apply.setEnabled( false );
        m_apply.setFocusable( false );
        //m_apply.setContentAreaFilled( false );
        m_apply.setBorderPainted( false );

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
        JPanel panel = new JPanel( new BorderLayout() );
        Component controls = buildControlComponent();
        panel.add( controls, BorderLayout.CENTER );
        JPanel buttons = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
        buttons.add( new JButton( "Start" ) );
        buttons.add( new JButton( "Stop" ) );
        buttons.add( new JButton( "Restart" ) );
        panel.add( buttons, BorderLayout.SOUTH );
        return panel;
    }

    private Component buildControlComponent() throws Exception
    {
        FormLayout layout = new FormLayout(
          "right:pref, 3dlu, 60dlu, fill:max(60dlu;pref), 7dlu, pref", 
          "pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref" );

        //layout.setColumnGroups( new int[][]{{1, 5},{3, 7}} );

        PanelBuilder builder = new PanelBuilder( layout );
        builder.setDefaultDialogBorder();

        CellConstraints cc = new CellConstraints();

        builder.addSeparator( "Application Profile",       cc.xyw( 1, 1, 6 ) );

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

        return builder.getPanel();
    }

    private String getProfileID() throws Exception
    {
        return m_profile.getID();
    }

    private Component getTitleComponent() throws Exception
    {
        return new JTextField( m_profile.getTitle() );
    }

    private Component getStartupTimeoutComponent() throws Exception
    {
        int timeout = m_profile.getStartupTimeout();
        SpinnerNumberModel model = new SpinnerNumberModel( timeout, 0, 100, 1 );
        return new JSpinner( model );
    }

    private Component getShutdownTimeoutComponent() throws Exception
    {
        int timeout = m_profile.getShutdownTimeout();
        SpinnerNumberModel model = new SpinnerNumberModel( timeout, 0, 100, 1 );
        return new JSpinner( model );
    }

    private Component getCodebaseComponent() throws Exception
    {
        return new JTextField( m_profile.getCodeBaseURI().toString() );
    }

    private Component getBasedirComponent() throws Exception
    {
        return new JTextField( m_profile.getWorkingDirectoryPath() );
    }

    private Component getStartupPolicyComponent()
    {
        return m_policy;
    }

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

        public boolean isModified()
        {
            try
            {
                return m_profile.getStartupPolicy() != super.getSelectedItem();
            }
            catch( Exception e )
            {
                return false;
            }
        }

        public boolean undo()
        {
            try
            {
                super.setSelectedItem( m_profile.getStartupPolicy() );
                updateChangeList( this, false );
                return true;
            }
            catch( Exception e )
            {
                e.printStackTrace();
                return false;
            }
        }

        public boolean apply()
        {
            try
            {
                StartupPolicy policy = (StartupPolicy) super.getSelectedItem();
                m_profile.setStartupPolicy( policy );
                updateChangeList( this, false );
                return true;
            }
            catch( Exception e )
            {
                e.printStackTrace();
                return false;
            }
        }

    }

    public boolean isModified()
    {
        return m_changes.size() > 0;
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

    public boolean apply()
    {
        Volotile[] components = (Volotile[]) m_changes.toArray( new Volotile[0] );
        for( int i=0; i < components.length; i++ )
        {
            Volotile component = components[i];
            component.apply();
        }
        return true;
    }

    public boolean undo()
    {
        Volotile[] components = (Volotile[]) m_changes.toArray( new Volotile[0] );
        for( int i=0; i < components.length; i++ )
        {
            Volotile component = components[i];
            component.undo();
        }
        return true;
    }

}