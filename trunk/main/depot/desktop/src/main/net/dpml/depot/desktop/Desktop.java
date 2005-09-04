
package net.dpml.depot.desktop;

import java.net.URI;
import java.net.URL;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.prefs.Preferences;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.*;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.table.TableModel;
import javax.swing.table.TableColumnModel;

import com.jgoodies.looks.FontSizeHints;
import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.theme.Silver;
import com.jgoodies.looks.plastic.theme.SkyBlue;

import net.dpml.depot.Handler;

import net.dpml.profile.ApplicationRegistry;
import net.dpml.profile.ApplicationProfile;

import net.dpml.transit.Transit;
import net.dpml.transit.Repository;
import net.dpml.transit.model.Logger;

/**
 * The Depot Desktop main class. 
 */
public final class Desktop implements Handler
{
    private Logger m_logger;
    private String[] m_args;
    private JSplitPane m_splitPane;
    private ApplicationRegistryTreeNode m_root;
    private JTree m_tree;
    private ApplyButton m_apply;
    private UndoButton m_undo;

    private Node m_selected;

    public Desktop( Logger logger, String[] args ) throws Exception
    {
        m_logger = logger;
        m_args = args;

        configureUI();

        m_root = new ApplicationRegistryTreeNode( m_logger, m_args );
        m_tree = new JTree( m_root );

        buildInterface();
    }

    /**
     * Configures the UI; tries to set the system look on Mac, 
     * WindowsLookAndFeel on general Windows, and
     * Plastic3DLookAndFeel on Windows XP and all other OS.
     */
    private void configureUI()
    {
        PlasticLookAndFeel.setMyCurrentTheme( new Silver() );
        UIManager.put( Options.USE_SYSTEM_FONTS_APP_KEY, Boolean.TRUE );
        Options.setGlobalFontSizeHints( FontSizeHints.MIXED );
        Options.setDefaultIconSize( new Dimension( 18, 18 ) );

        try
        {
            if( LookUtils.IS_OS_WINDOWS_XP )
            {
                UIManager.setLookAndFeel( Options.getCrossPlatformLookAndFeelClassName() );
            }
            else
            {
                UIManager.setLookAndFeel( Options.getSystemLookAndFeelClassName() );
            }
        }
        catch( Exception e )
        {
            final String error = 
              "Internal error while attempting to set look and feel.";
            m_logger.warn( error, e );
        }

        final String lookAndFeel = getDefaultLookAndFeel();
        try            
        {
            UIManager.setLookAndFeel( lookAndFeel );
        }
        catch( Exception e )
        {
            final String error = 
              "Internal error while attempting to set look and feel.";
            m_logger.warn( error, e );
        }
    }

    public void destroy()
    {
        m_root.dispose();
    }

    /**
     * Creates and configures a frame, builds the menu bar, builds the
     * content, locates the frame on the screen, and finally shows the frame.
     */
    private void buildInterface()
    {
        JFrame frame = new JFrame();
        frame.setIconImage( readImageIcon( "16/folder.png" ).getImage() );
        frame.setJMenuBar( buildMenuBar() );
        frame.setContentPane( buildContentPane() );
        frame.setSize( 600, 500 );
        locateOnScreen( frame );
        frame.setTitle( "DPML :: Desktop" );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.setVisible( true );
    }

    /**
     * Locates the frame on the screen center.
     */
    private void locateOnScreen( Frame frame ) 
    {
        Dimension paneSize   = frame.getSize();
        Dimension screenSize = frame.getToolkit().getScreenSize();
        frame.setLocation(
            ( screenSize.width  - paneSize.width ) / 2,
            ( screenSize.height - paneSize.height ) / 2 );
    }

    /**
     * Builds and answers the menu bar.
     */
    private JMenuBar buildMenuBar()
    {
        JMenu menu;
        JMenuBar menuBar = new JMenuBar();
        menuBar.putClientProperty( Options.HEADER_STYLE_KEY, Boolean.TRUE );

        menu = new JMenu( "File" );
        menu.add( new JMenuItem( "New..." ) );
        menu.add( new JMenuItem( "Open..." ) );
        menu.add( new JMenuItem( "Save" ) );
        menu.addSeparator();
        menu.add( new JMenuItem( "Preferences..." ) );
        menu.addSeparator();
        menu.add( new JMenuItem( "Print..." ) );
        menuBar.add( menu );

        menu = new JMenu( "Edit" );
        menu.add( new JMenuItem( "Cut" ) );
        menu.add( new JMenuItem( "Copy" ) );
        menu.add( new JMenuItem( "Paste" ) );
        menuBar.add( menu );

        return menuBar;
    }

    /**
     * Builds the content pane.
     */
    private JComponent buildContentPane()
    {
        m_splitPane = buildSplitPane();
        JPanel panel = new JPanel( new BorderLayout() );
        panel.add( buildToolBar(), BorderLayout.NORTH );
        panel.add( m_splitPane, BorderLayout.CENTER );
        panel.add( buildStatusBar(), BorderLayout.SOUTH );
        return panel;
    }

    /**
     * Builds the tool bar.
     */
    private Component buildToolBar() 
    {
        JToolBar toolBar = new JToolBar();
        toolBar.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        toolBar.putClientProperty( Options.HEADER_STYLE_KEY, Boolean.TRUE );
        m_apply = new ApplyButton();
        toolBar.add( m_apply );
        m_undo = new UndoButton();
        toolBar.add( m_undo );
        return toolBar;
    }

    /**
     * Builds the split panel.
     */
    private JSplitPane buildSplitPane()
    {
        JSplitPane splitPane =
            new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                buildSideBar(),
                buildMainPanel() );
        splitPane.setDividerLocation( 200 );
        return splitPane;
    }

    /**
     * Builds the side bar and add a selection listener.
     */
    private Component buildSideBar() 
    {
        m_tree.setRootVisible( true );
        TreeCellRenderer renderer = m_tree.getCellRenderer();
        m_tree.setCellRenderer( new DesktopCellRenderer( renderer ) );
        m_tree.addTreeSelectionListener( new SelectionListener() );
        return createStrippedScrollPane( m_tree );
    }

   /**
    * Side-bar selection listener.
    */
    private class SelectionListener implements TreeSelectionListener, PropertyChangeListener
    {
        public void valueChanged( TreeSelectionEvent event )
        {
            TreePath path = event.getPath();
            Object object = path.getLastPathComponent();
            if( object instanceof Node )
            {
                Node node = (Node) object;
                setSelectedObject( node );
                int location = m_splitPane.getDividerLocation();
                Component component = node.getComponent();
                m_splitPane.setRightComponent( component );
                m_splitPane.setDividerLocation( location );
            }
        }

        private void setSelectedObject( Node node )
        {
            if( ( m_selected != node ) && ( null != m_selected ) )
            {
               m_selected.removePropertyChangeListener( this );
            }

            m_selected = node;

            if( ( m_selected != null ) )
            {
                node.addPropertyChangeListener( this );
                boolean modified = m_selected.isModified();
                m_apply.setEnabled( modified );
                m_undo.setEnabled( modified );
            }
            else
            {
                m_apply.setEnabled( false );
                m_undo.setEnabled( false );
            }
        }

        public void propertyChange( PropertyChangeEvent event )
        {
            if( null != m_selected )
            {
                boolean modified = m_selected.isModified();
                m_apply.setEnabled( modified );
                m_undo.setEnabled( modified );
            }
        }
    }

    private class DesktopCellRenderer implements TreeCellRenderer
    {
        private ImageIcon m_binary = readImageIcon( "16/binary.png" );
        private ImageIcon m_application = readImageIcon( "16/application.png" );
        TreeCellRenderer m_standard;
        DesktopCellRenderer( TreeCellRenderer standard )
        {
            m_standard = standard;
        }

        public Component getTreeCellRendererComponent( 
          JTree tree, Object value, boolean selected, boolean expanded, 
          boolean leaf, int row, boolean focus )
        {
            if( value instanceof Node )
            {
                Node node = (Node) value;
                return node.getLabel();
            }
            else
            {
                return m_standard.getTreeCellRendererComponent( 
                  tree, value, selected, expanded, leaf, row, focus );
            }
        }
    }

    private ImageIcon readImageIcon( String filename ) 
    {
        URL url = getClass().getClassLoader().getResource("net/dpml/depot/desktop/images/" + filename);
        return new ImageIcon( url );
    }

    /**
     * Builds the main panel.
     */
    private Component buildMainPanel() 
    {
        JScrollPane scrollPane = new JScrollPane( new JPanel() );
        scrollPane.getViewport().setBackground( new JTable().getBackground() );
        scrollPane.setBorder( null );
        return scrollPane;
    }

    /**
     * Builds the tool bar.
     */
    private Component buildStatusBar()
    {
        JPanel statusBar = new JPanel( new BorderLayout() );
        statusBar.add( createLeftAlignedLabel( "Status Bar" ) );
        return statusBar;
    }

    // Helper Code ********************************************************

    /**
     * Creates and answers a <code>JScrollpane</code> that has no border.
     */
    private JScrollPane createStrippedScrollPane( Component c )
    {
        JScrollPane scrollPane = new JScrollPane(c);
        scrollPane.setBorder(null);
        return scrollPane;
    }

    /**
     * Creates and answers a <code>JLabel</code> that has the text
     * centered and that is wrapped with an empty border.
     */
    private Component createLeftAlignedLabel(String text)
    {
        JLabel label = new JLabel( text );
        label.setHorizontalAlignment( SwingConstants.LEFT );
        label.setBorder( new EmptyBorder( 3, 3, 3, 3 ) );
        return label;
    }

    private String getDefaultLookAndFeel()
    {
        if( LookUtils.IS_OS_WINDOWS_XP )
        {
            return Options.getCrossPlatformLookAndFeelClassName();
        }
        else
        {
            return Options.getSystemLookAndFeelClassName();
        }
    }

    private abstract class ToolBarButton extends JButton
    {
        private ToolBarButton( Icon icon )
        {
            super( icon );
            setFocusable( false );
            setSelected( false );
            setEnabled( false );
        }
    }

    private class ApplyButton extends ToolBarButton 
    {
        private ApplyButton()
        {
            super( readImageIcon( "16/save.png" ) );
        }

        protected void fireActionPerformed( ActionEvent event )
        {
            super.fireActionPerformed( event );
            if( null != m_selected )
            {
                try
                {
                    m_selected.apply();
                    m_apply.setEnabled( false );
                }
                catch( Exception e )
                {
                    // TODO: add a warning dialog
                    m_apply.setEnabled( true );
                }
            }
        }
    }

    private class UndoButton extends ToolBarButton 
    {
        private UndoButton()
        {
            super( readImageIcon( "16/revert.png" ) );
        }

        protected void fireActionPerformed( ActionEvent event )
        {
            super.fireActionPerformed( event );
            if( null != m_selected )
            {
                try
                {
                    m_selected.revert();
                    m_undo.setEnabled( false );
                }
                catch( Exception e )
                {
                    // TODO: add a warning dialog
                    m_undo.setEnabled( true );
                }
            }
        }
    }

    private static final String DEPOT_PROFILE_URI = "@DEPOT-PROFILE-PLUGIN-URI@";

}