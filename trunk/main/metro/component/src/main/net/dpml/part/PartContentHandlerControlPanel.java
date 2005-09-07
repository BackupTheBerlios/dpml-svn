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

package net.dpml.part;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.rmi.RemoteException;

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

import net.dpml.transit.model.ContentModel;
import net.dpml.transit.model.UnknownKeyException;
import net.dpml.transit.model.Parameter;


/**
 * A registry of descriptions of plugable content handlers.  This implementation
 * maps user defined preferences to instance of ContentHandlerDescriptor.
 */
public class PartContentHandlerControlPanel extends JDialog implements PropertyChangeListener
{
    private PropertyChangeSupport m_propertyChangeSupport;

    private final ContentModel m_model;

    public PartContentHandlerControlPanel( Dialog parent, ContentModel model ) throws Exception
    {
        super( parent );

        m_model = model;

        m_propertyChangeSupport = new PropertyChangeSupport( this );

        setTitle( "DPML Part Content Controller" );
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

        setSize( new Dimension( 400, 360 ) );
        getRootPane().setDefaultButton( buttons.getDefaultButton() );

        m_propertyChangeSupport.addPropertyChangeListener( this );

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
       // TODO
    }

    private JLabel createHeaderLabel()
    {
        JLabel label = createImageIconJLabel( 
            getClass().getClassLoader(), METRO_ICON_FILENAME, "", "DPML Part Content Handler" ); 
        label.setBorder( new EmptyBorder( 15, 10, 10, 10 ) );
        return label;
    }

    private JPanel createBodyContent( JDialog parent ) throws Exception
    {
        JPanel panel = new JPanel( new BorderLayout() );
        panel.setBackground( Color.white );
        JPanel stack = createStack();
        panel.add( stack, BorderLayout.NORTH );
        return panel;
    }

    private JPanel createStack() throws Exception
    {
        JPanel stack = new JPanel();
	  stack.setLayout( new BoxLayout( stack, BoxLayout.Y_AXIS ) );
        stack.setBackground( Color.white );
        JPanel spec = createPluginSpecPanel();
        JPanel dirs = createDirectoryPanel();
        stack.add( spec );
        stack.add( dirs );
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

    private JPanel createDirectoryPanel() throws Exception
    {
        JPanel panel = new JPanel( new BorderLayout() );
        panel.setBackground( Color.white );
        panel.setBorder( 
          new CompoundBorder(
            new TitledBorder( 
              null, "Directories", TitledBorder.LEFT, TitledBorder.TOP), 
              new EmptyBorder( 5, 5, 5, 10) 
            )
          );
       
        JPanel stack = new JPanel();
	  stack.setLayout( new BoxLayout( stack, BoxLayout.Y_AXIS ) );

        String workPath = getWorkingDirectoryPath();
        String tempPath = getTempDirectoryPath();

        JPanel working = createWorkingDirectoryPanel( workPath );
        JPanel temp = createTempDirectoryPanel( tempPath );
        stack.add( working );
        stack.add( temp );
        panel.add( stack, BorderLayout.NORTH );
        return panel;
    }

    private JPanel createWorkingDirectoryPanel( String path )
    {
        JPanel panel = new JPanel( new BorderLayout() );
        panel.setBackground( Color.white );

        ImageIcon label = 
          createImageIcon( 
            getClass().getClassLoader(), WORK_ICON_FILENAME, "Working Directory" );
        JPanel labelHolder = new JPanel( new BorderLayout() );
        labelHolder.setBackground( Color.white );
        labelHolder.setBorder( new EmptyBorder( 3, 0, 0, 10 ) );
        labelHolder.add( new JLabel( label ), BorderLayout.SOUTH );
        panel.add( labelHolder, BorderLayout.WEST );

        JPanel stack = new JPanel();
	  stack.setLayout( new BoxLayout( stack, BoxLayout.Y_AXIS ) );
        stack.setBackground( Color.white );
        JLabel dirLabel = new JLabel( "Working Directory:" );
        JPanel dirLabelHolder = new JPanel( new BorderLayout() );
        dirLabelHolder.add( dirLabel, BorderLayout.WEST );
        dirLabelHolder.setBackground( Color.white );
        dirLabelHolder.setBorder( new EmptyBorder( 0, 0, 3, 0 ) );
        JTextField dirValue = new JTextField( path );
        stack.add( dirLabelHolder );
        stack.add( dirValue );

        JPanel holder = new JPanel( new BorderLayout() );
        holder.add( stack, BorderLayout.NORTH );
        panel.add( holder );
        return panel;
    }

    private JPanel createTempDirectoryPanel( String path )
    {
        JPanel panel = new JPanel( new BorderLayout() );
        panel.setBackground( Color.white );
        panel.setBorder( new EmptyBorder( 10, 0, 5, 0 ) );

        ImageIcon label = createImageIcon( 
            getClass().getClassLoader(), TEMP_ICON_FILENAME, "Temp Directory" );
        JPanel labelHolder = new JPanel( new BorderLayout() );
        labelHolder.setBackground( Color.white );
        labelHolder.setBorder( new EmptyBorder( 3, 0, 0, 10 ) );
        labelHolder.add( new JLabel( label ), BorderLayout.SOUTH );
        panel.add( labelHolder, BorderLayout.WEST );

        JPanel stack = new JPanel();
	  stack.setLayout( new BoxLayout( stack, BoxLayout.Y_AXIS ) );
        stack.setBackground( Color.white );
        JLabel dirLabel = new JLabel( "Temporary Directory:" );
        JPanel dirLabelHolder = new JPanel( new BorderLayout() );
        dirLabelHolder.add( dirLabel, BorderLayout.WEST );
        dirLabelHolder.setBackground( Color.white );
        dirLabelHolder.setBorder( new EmptyBorder( 0, 0, 3, 0 ) );
        JTextField dirValue = new JTextField( path );
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

        ButtonPanel( PartContentHandlerControlPanel dialog )
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
        private PartContentHandlerControlPanel m_dialog;

        public CancelAction( String label, PartContentHandlerControlPanel dialog )
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
        private PartContentHandlerControlPanel m_dialog;

        OKAction( String name, PartContentHandlerControlPanel dialog )
        {
            super( name );
            setEnabled( false );
            m_dialog = dialog;
        }

        // TODO: we need a solution in place under which we log 
        // information about preference changes

        public void actionPerformed( ActionEvent event )
        {
            m_propertyChangeSupport.removePropertyChangeListener( m_dialog );
            m_dialog.hide();
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

    private static JLabel createImageIconJLabel( 
      ClassLoader classloader, String path, String description, String text )
    {
        ImageIcon icon = createImageIcon( classloader, path, description );
        return new JLabel( text, icon, JLabel.LEFT );
    }

    public static ImageIcon createImageIcon( ClassLoader classloader, String path, String description )
    {
        URL url = classloader.getResource( path );
        if( null != url )
        {
            return new ImageIcon( url );
        }
        else
        {
            final String error =
              "Supplied image icon resource path is unknown ["
              + path 
              + "].";
            throw new IllegalArgumentException( error );
        }
    }

   /**
    * Return the root working directory path.
    *
    * @return directory path representing the root of the working directory hierachy
    */
    public String getWorkingDirectoryPath()
    {
        try
        {
            Parameter p =  m_model.getParameter( WORK_PATH_KEY );
            Object value = p.getValue();
            if( null == value )
            {
                return DEFAULT_WORK_PATH;
            }
            else if( value instanceof String )
            {
                return (String) value;
            }
            else
            {
                final String error = 
                  "Illegal argument assigned to the parameter ["
                  + WORK_PATH_KEY
                  + "]. String expected but found '"
                  + value.getClass().getName()
                  + "'.";
                throw new IllegalArgumentException( error );
            }
        }
        catch( UnknownKeyException e )
        {
            return DEFAULT_WORK_PATH;
        }
        catch( RemoteException e )
        {
            final String error = 
              "Remote error while attempting to reslve working directory.";
            throw new RuntimeException( error, e );
        }
    }

   /**
    * Return the root temporary directory path.
    *
    * @return directory path representing the root of the temporary directory hierachy.
    */
    public String getTempDirectoryPath()
    {
        try
        {
            Parameter p =  m_model.getParameter( TEMP_PATH_KEY );
            Object value = p.getValue();
            if( null == value )
            {
                return DEFAULT_TEMP_PATH;
            }
            else if( value instanceof String )
            {
                return (String) value;
            }
            else
            {
                final String error = 
                  "Illegal argument assigned to the parameter ["
                  + TEMP_PATH_KEY
                  + "]. String expected but found '"
                  + value.getClass().getName()
                  + "'.";
                throw new IllegalArgumentException( error );
            }
        }
        catch( UnknownKeyException e )
        {
            return DEFAULT_TEMP_PATH;
        }
        catch( RemoteException e )
        {
            final String error = 
              "Remote error while attempting to reslve temp directory.";
            throw new RuntimeException( error, e );
        }
    }

    private static final String WORK_PATH_KEY = "work.dir";
    private static final String TEMP_PATH_KEY = "temp.dir";
    private static final String DEFAULT_WORK_PATH = "${dpml.data}/work";
    private static final String DEFAULT_TEMP_PATH = "${java.io.tmpdir}";

    private static String METRO_ICON_FILENAME = "net/dpml/part/images/setup.png";
    private static String SOURCE_ICON_FILENAME = "net/dpml/part/images/source.png";
    private static String WORK_ICON_FILENAME = "net/dpml/part/images/working.png";
    private static String TEMP_ICON_FILENAME = "net/dpml/part/images/temp.png";

    private static String PLUGIN_VERSION = "@VERSION@";
    private static String PLUGIN_GROUP = "@GROUP@";
    private static String PLUGIN_NAME = "@NAME@";


}
