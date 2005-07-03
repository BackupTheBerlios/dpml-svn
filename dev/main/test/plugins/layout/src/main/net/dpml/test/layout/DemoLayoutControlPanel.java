/*
 * Copyright 2005 Stephen McConnell
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

package net.dpml.test.layout;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import javax.swing.JDialog;
import javax.swing.border.EmptyBorder;
import javax.swing.JPanel;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JButton;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.ImageIcon;
import javax.swing.JTextField;

import net.dpml.transit.model.LayoutModel;

import net.dpml.depot.prefs.IconHelper;

/**
 * A registry of descriptions of plugable content handlers.  This implementation
 * maps user defined preferences to instance of ContentHandlerDescriptor.
 */
public class DemoLayoutControlPanel extends JDialog implements PropertyChangeListener
{
    //--------------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------------

    private PropertyChangeSupport m_propertyChangeSupport;

    private LayoutModel m_manager;

    //--------------------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------------------

    public DemoLayoutControlPanel( Dialog parent, LayoutModel manager )
    {
        super( parent );

        m_propertyChangeSupport = new PropertyChangeSupport( this );

        setTitle( "Transit Test Layout Plugin" );
        setModal( true );
        setBackground( Color.white );

        // create the header label

        JLabel header = createHeaderLabel();

        // create the body content

        JPanel body = createBodyContent( this );
        body.setBorder( new EmptyBorder( 0, 7, 7, 7 ) );

        // create the buttons in the footer

        ButtonPanel buttons = new ButtonPanel( this );
        buttons.setBorder( new EmptyBorder( 0, 7, 7, 7 ) );

        //
        // package
        //
        
        JPanel assembly = new JPanel( new BorderLayout() );
        assembly.setBackground( Color.white );
        assembly.add( header, BorderLayout.NORTH );
        assembly.add( body, BorderLayout.CENTER );
        assembly.add( buttons, BorderLayout.SOUTH );
        setContentPane( assembly );

        setSize( new Dimension( 400, 300 ) );
        getRootPane().setDefaultButton( buttons.getDefaultButton() );

        m_propertyChangeSupport.addPropertyChangeListener( this );
    }

    //--------------------------------------------------------------------------
    // PropertyChangeListener
    //--------------------------------------------------------------------------

   /**
    * The actions dealing with changes to the dialog raise change events that 
    * are captured here.  This listener checks changes and enables or disabled 
    * the ok and undo buttons based on the state of controls relative to the 
    * underlying preferences for this layout.
    */
    public void propertyChange( PropertyChangeEvent event )
    {
       // TODO
    }

    //--------------------------------------------------------------------------
    // internals
    //--------------------------------------------------------------------------

    private JLabel createHeaderLabel()
    {
        JLabel label = 
          IconHelper.createImageIconJLabel( 
            getClass().getClassLoader(), TOOLS_ICON_FILENAME, "", "DPML Test Layout Plugin" ); 
        label.setBorder( new EmptyBorder( 15, 10, 10, 10 ) );
        return label;
    }

    private JPanel createBodyContent( JDialog parent )
    {
        JPanel panel = new JPanel( new BorderLayout() );
        panel.setBackground( Color.white );
        JPanel stack = createStack();
        panel.add( stack, BorderLayout.NORTH );
        return panel;
    }

    private JPanel createStack()
    {
        JPanel stack = new JPanel();
	  stack.setLayout( new BoxLayout( stack, BoxLayout.Y_AXIS ) );
        stack.setBackground( Color.white );
        JPanel spec = createPluginSpecPanel();
        JPanel name = createFeaturesPanel();
        stack.add( spec );
        stack.add( name );
        return stack;
    }

    private JPanel createPluginSpecPanel()
    {
        JPanel panel = new JPanel( new BorderLayout() );
        panel.setBackground( Color.white );
        panel.setBorder( 
          new CompoundBorder(
            new TitledBorder( 
              null, "Plugin", TitledBorder.LEFT, TitledBorder.TOP), 
              new EmptyBorder( 5, 5, 5, 10) 
            )
          );

        JPanel stack = new JPanel();
	  stack.setLayout( new BoxLayout( stack, BoxLayout.Y_AXIS ) );
        stack.setBackground( Color.white );
        JLabel group = new JLabel( "Group: " + PLUGIN_GROUP );
        JLabel name = new JLabel( "Name: " + PLUGIN_NAME );
        JLabel version = new JLabel( "Version: " + PLUGIN_VERSION );
        stack.add( group );
        stack.add( name );
        stack.add( version );

        JPanel holder = new JPanel( new BorderLayout() );
        holder.add( stack, BorderLayout.NORTH );
        panel.add( holder );
        return panel;
    }

    private JPanel createFeaturesPanel()
    {
        JPanel panel = new JPanel( new BorderLayout() );
        panel.setBackground( Color.white );
        panel.setBorder( 
          new CompoundBorder(
            new TitledBorder( 
              null, "Layout Features", TitledBorder.LEFT, TitledBorder.TOP), 
              new EmptyBorder( 5, 5, 5, 10) 
            )
          );
       
        JPanel stack = new JPanel();
	  stack.setLayout( new BoxLayout( stack, BoxLayout.Y_AXIS ) );
        JPanel working = createTitlePanel();
        stack.add( working );
        panel.add( stack, BorderLayout.NORTH );
        return panel;
    }

    private JPanel createTitlePanel()
    {
        JPanel panel = new JPanel( new BorderLayout() );
        panel.setBackground( Color.white );

        JPanel stack = new JPanel();
	  stack.setLayout( new BoxLayout( stack, BoxLayout.Y_AXIS ) );
        stack.setBackground( Color.white );
        JLabel dirLabel = new JLabel( "Title:" );
        JPanel dirLabelHolder = new JPanel( new BorderLayout() );
        dirLabelHolder.add( dirLabel, BorderLayout.WEST );
        dirLabelHolder.setBackground( Color.white );
        dirLabelHolder.setBorder( new EmptyBorder( 0, 0, 3, 0 ) );
        JTextField dirValue = new JTextField( "Demo Layout" );
        stack.add( dirLabelHolder );
        stack.add( dirValue );

        JPanel holder = new JPanel( new BorderLayout() );
        holder.add( stack, BorderLayout.NORTH );
        panel.add( holder );
        return panel;
    }

    private class ButtonPanel extends Box
    {
        private JButton m_close = new JButton();
        private JButton m_ok = new JButton();
        private JButton m_revert = new JButton( new RevertAction( "Undo" ) );

        ButtonPanel( DemoLayoutControlPanel dialog )
        {
            super( BoxLayout.Y_AXIS );
            setBackground( Color.white );

            OKAction ok = new OKAction( "OK", dialog );
            m_ok.setAction( ok );
            Action closeAction = new CancelAction( "Close", dialog );
            m_close.setAction( closeAction );

            JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
            buttonPanel.setBackground( Color.white );
            buttonPanel.add( m_revert );
            buttonPanel.add( m_ok );
            buttonPanel.add( m_close );
            add( buttonPanel );
        }

        JButton getDefaultButton()
        {
            return m_close;
        }
    }

    private class CancelAction extends AbstractAction
    {
        private DemoLayoutControlPanel m_dialog;

        public CancelAction( String label, DemoLayoutControlPanel dialog )
        {
            super( label );
            m_dialog = dialog;
        }

       /**
        * Called when the cancel button is trigged.
        * @param event the action event
        */
        public void actionPerformed( ActionEvent event )
        {
            m_propertyChangeSupport.removePropertyChangeListener( m_dialog );
            m_dialog.hide();
        }
    }

     private class OKAction extends AbstractAction 
     {
        private DemoLayoutControlPanel m_dialog;

        OKAction( String name, DemoLayoutControlPanel dialog )
        {
            super( name );
            setEnabled( false );
            m_dialog = dialog;
        }

        public void actionPerformed( ActionEvent event )
        {
            m_dialog.hide();
            m_propertyChangeSupport.removePropertyChangeListener( m_dialog );
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

    //--------------------------------------------------------------------------
    // static (utils)
    //--------------------------------------------------------------------------

    private static String TOOLS_ICON_FILENAME = "net/dpml/test/layout/tools.png";

    private static String MENU_ICON_FILENAME = "net/dpml/test/layout/menu.png";

    private static String PLUGIN_VERSION = "@VERSION@";

    private static String PLUGIN_GROUP = "@GROUP@";

    private static String PLUGIN_NAME = "@NAME@";
}
