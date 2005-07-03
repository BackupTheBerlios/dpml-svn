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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import net.dpml.transit.Transit;
import net.dpml.transit.model.CacheModel;
import net.dpml.transit.model.CacheListener;
import net.dpml.transit.model.FileChangeEvent;
import net.dpml.transit.model.CacheEvent;

/**
 * Control panel for editing the cache preferences.
 *
 * @author <a href="mailto:mcconnell@osm.net">OSM</a>
 */
class CacheDirectoryPanel extends ClassicPanel implements PropertyChangeListener
{
    //--------------------------------------------------------------
    // static
    //--------------------------------------------------------------

    static EmptyBorder border5 = new EmptyBorder( 5, 5, 5, 5);

    //--------------------------------------------------------------
    // state
    //--------------------------------------------------------------

    private final JDialog m_parent;
    private JLabel m_label;
    private JButton m_ok;
    private JButton m_revert;

    private File m_target;

    private final CacheModel m_manager;

    private final PropertyChangeSupport m_propertyChangeSupport;
    private final RemoteCacheListener m_cacheListener;

    //--------------------------------------------------------------
    // constructor
    //--------------------------------------------------------------

    public CacheDirectoryPanel( JDialog parent, CacheModel manager ) throws Exception 
    {
        m_parent = parent;
        m_manager = manager;

        m_propertyChangeSupport = new PropertyChangeSupport( this );

        File cache = getCurrentCacheFile();
        String filename = cache.getCanonicalPath();
        JLabel label = 
          IconHelper.createImageIconJLabel( 
            getClass().getClassLoader(), CACHE_ICON_SRC, "Filename", "Directory: " + filename ); 
        label.setBorder( new EmptyBorder( 0, 5, 0, 0 ) );

        m_label = label;
        m_ok = new JButton( new OKAction( "OK" ) );
        m_revert = new JButton( new RevertAction( "Undo" ) );

        m_cacheListener = new RemoteCacheListener();
        m_manager.addCacheListener( m_cacheListener );

        if( null == cache )
        {
            // setup an error dialog content
        }
        else
        {
            // create a label containing an icon and the current filename

            JPanel panel = new JPanel();
            panel.setLayout( new BorderLayout() );
            panel.add( label, BorderLayout.WEST );
            add( panel, BorderLayout.NORTH );

            // add controls for modification of the cache value

            JPanel buttonHolder = new JPanel();
            buttonHolder.setLayout( new FlowLayout( FlowLayout.RIGHT ) );
            buttonHolder.add( m_revert );
            buttonHolder.add( new JButton( new ChangeAction( "Change" ) ) );
            buttonHolder.add( m_ok );
            buttonHolder.add( new JButton( new CloseAction( "Cancel" ) ) );
            buttonHolder.setBorder( new EmptyBorder( 10, 10, 5, 0 ) );
            add( buttonHolder, BorderLayout.SOUTH );

            m_propertyChangeSupport.addPropertyChangeListener( this );
        }
    }

    public void dispose()
    {
        m_propertyChangeSupport.removePropertyChangeListener( this );
        try
        {
            m_manager.removeCacheListener( m_cacheListener );
        }
        catch( RemoteException e )
        {
            System.err.println( "CacheDirectoryPanel disposal error." );
            e.printStackTrace();
        }
    }
  
    //--------------------------------------------------------------------------
    // CacheListener
    //--------------------------------------------------------------------------

    private class RemoteCacheListener extends UnicastRemoteObject implements CacheListener
    {
        public RemoteCacheListener() throws RemoteException
        {
            super();
        }

       /**
        * Notify the listener of a change to the cache directory.
        * @param event the cache directory change event
        */
        public void cacheDirectoryChanged( FileChangeEvent event )
        {
            File cache = getCurrentCacheFile();
            String path = convertToPath( cache );
            String text = "Directory: " + path;
            m_label.setText( text );
        }

       /**
        * Notify the listener of the addition of a new host.
        * @param event the host added event
        */
        public void hostAdded( CacheEvent event )
        {
            // ignore
        }
    
       /**
        * Notify the listener of the removal of a host.
        * @param event the host removed event
        */
        public void hostRemoved( CacheEvent event )
        {
            // ignore
        }
    }

    //--------------------------------------------------------------------------
    // PropertyChangeListener
    //--------------------------------------------------------------------------

   /**
    * The actions dealing with changes to the dialog raise change events that 
    * are captured here.  This listener checks changes and enables or disabled 
    * the ok and undo buttons based on the state of controls relative to the 
    * underlying preferences for the cache.
    */
    public void propertyChange( PropertyChangeEvent event )
    {
        File cache = getCurrentCacheFile();
        boolean flag = ( false == cache.equals( m_target ) );
        m_ok.setEnabled( flag );
        m_revert.setEnabled( flag );
    }

    private class CloseAction extends AbstractAction
    {
         CloseAction( String name )
         {
             super( name );
         }

         public void actionPerformed( ActionEvent event )
         {
             m_parent.hide();
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
            if( null != m_target )
            {
                File file = getCurrentCacheFile();
                if( false == file.equals( m_target ) )
                {
                    String path = convertToPath( m_target );
                    try
                    {
                        m_manager.setCacheDirectory( new File( path ) );
                    }
                    catch( RemoteException e )
                    {
                        final String error = 
                          "Unable to set cache directory due to a remote exception.";
                        throw new RuntimeException( error, e );
                    }
                }
            }
            m_parent.hide();
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
            File old = m_target;
            m_target = getCurrentCacheFile();
            String text = "Directory: " + convertToPath( m_target );
            m_label.setText( text );
            PropertyChangeEvent e = 
              new PropertyChangeEvent( 
                this, "location", old, m_target);
            m_propertyChangeSupport.firePropertyChange( e );
        }
    }

    private class ChangeAction extends AbstractAction
    {
        ChangeAction( String name )
        {
            super( name );
        }

        public void actionPerformed( ActionEvent event )
        {
            File file = getCurrentCacheFile();
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
            chooser.setCurrentDirectory( file );
            int result = chooser.showOpenDialog( m_parent );
            if( result == JFileChooser.APPROVE_OPTION )
            {
                m_target = chooser.getSelectedFile();
                String text = "Directory: " + convertToPath( m_target );
                m_label.setText( text );
                PropertyChangeEvent e = 
                  new PropertyChangeEvent( 
                    this, "location", file, m_target );
                m_propertyChangeSupport.firePropertyChange( e );
            }
        }
    }

    private String convertToPath( File file )
    {
        try
        {
            return file.getCanonicalPath();
        }
        catch( Exception e )
        {
            return file.toString();
        }
    }

    private File getCurrentCacheFile()
    {
        try
        {
            return m_manager.getCacheDirectory();
        }
        catch( RemoteException e )
        {
            final String error = 
              "Unexpected remote exception while reading cache directory value.";
            throw new RuntimeException( error, e );
        }
    }

    //--------------------------------------------------------------
    // static (utils)
    //--------------------------------------------------------------

    private static String CACHE_ICON_SRC = "net/dpml/depot/prefs/images/cache.jpg";

}
