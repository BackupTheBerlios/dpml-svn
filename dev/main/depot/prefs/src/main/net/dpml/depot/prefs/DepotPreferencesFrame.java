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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import net.dpml.depot.profile.DepotProfile;

import net.dpml.transit.Transit;
import net.dpml.transit.model.TransitModel;

/**
 * Dialog that presents the default preferences for DPML applications including
 * Transit cache and repository settings, transit content handler plugins, logging
 * preferences and application profiles runnable via the depot command line script.
 */
public class DepotPreferencesFrame extends JFrame 
{
   /**
    * The default dialog width.
    */
    private static final int DEFAULT_DIALOG_WIDTH = 550;
 
   /**
    * The default dialog height.
    */
    private static final int DEFAULT_DIALOG_HEIGHT = 700;

   /**
    * Empty boarder offset.
    */
    private static final int OFFSET = 10;

   /**
    * Empty boarder offset.
    */
    private static final int LEAD = 20;

   /**
    * Null offset
    */
    private static final int ZERO = 0;

    public DepotPreferencesFrame( TransitModel model, DepotProfile depot ) throws Exception
    {
        super();

        if( null == System.getProperty( "dpml.depot.metal" ) )
        {
            String os = System.getProperty( "os.name" ).toLowerCase();
            if( ( os.indexOf( "win" ) >= 0 ) || ( os.indexOf( "Mac OS" ) >= 0 ) )
            {
               String classname = UIManager.getSystemLookAndFeelClassName();
               UIManager.setLookAndFeel( classname );
            }
        }
        
        setTitle( "DPML DepotProfile" );
        Dimension size = new Dimension( DEFAULT_DIALOG_WIDTH, DEFAULT_DIALOG_HEIGHT );
        setSize( size );
        DepotPreferencesPanel panel = new DepotPreferencesPanel( this, model, depot );
        setContentPane( panel );
        getRootPane().setDefaultButton( panel.getDefaultButton() );
        addWindowListener( 
          new WindowAdapter() 
          {
	    	  public void windowClosing(WindowEvent e) 
              {
                  System.exit(0);
		  }
          } 
        );
        setLocation( 300, 200 );
        setVisible(true);
    }
}
