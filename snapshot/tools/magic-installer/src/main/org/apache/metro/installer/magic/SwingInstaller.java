/* 
 * Copyright 2004 Apache Software Foundation
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

package org.apache.metro.installer.magic;

import java.awt.Container;
import java.awt.Dimension;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;

public class SwingInstaller
    implements ActionListener
{
    private JFrame m_ProgressWindow;
    private JProgressBar m_ProgressBar;
    private JTextArea  m_TextArea;
    private JButton m_FinishButton;
    
    public SwingInstaller()
    {
        m_ProgressWindow = new JFrame();
        Container pane = m_ProgressWindow.getContentPane();
        final BoxLayout layout = new BoxLayout( pane, BoxLayout.X_AXIS );
        pane.setLayout( layout );
        
        JPanel leadColumn = new JPanel();
        leadColumn.setMinimumSize( new Dimension( 10, 10 ) );
        pane.add( leadColumn );

        JPanel column = new JPanel();
        final BoxLayout layout2 = new BoxLayout( column, BoxLayout.Y_AXIS );
        column.setLayout( layout2 );
        pane.add( column );

        JPanel trailColumn = new JPanel();
        trailColumn.setMinimumSize( new Dimension( 10, 10 ) );
        pane.add( trailColumn );
                
        JPanel leadRow = new JPanel();
        leadRow.setMinimumSize( new Dimension( 10, 10 ) );
        column.add( leadRow );
        
        final JLabel label = new JLabel( "Installing Magic - The new age of build systems." );
        column.add( label );
        
        m_ProgressWindow.setTitle( "Magic Installer" );
        final JPanel spring1 = new JPanel();
        setSizes( spring1, 0, 10, 100 );
        column.add( spring1 );

        m_ProgressBar = createProgressBar();
        column.add( m_ProgressBar );
        final JPanel spring2 = new JPanel();
        setSizes( spring2, 0, 10, 100 );
        column.add( spring2 );
        
        final JPanel spring3 = new JPanel();
        setSizes( spring3, 0, 10, 100 );
        column.add( spring3 );

        m_TextArea = createTextArea();
        column.add( m_TextArea );
        final JPanel spring4 = new JPanel();
        setSizes( spring4, 0, 10, 100 );
        column.add( spring4 );
        
        m_FinishButton = createFinishButton();
        column.add( m_FinishButton );
        
        final JPanel spring5 = new JPanel();
        setSizes( spring5, 0, 10, 100 );
        column.add( spring5 );
                
        m_ProgressWindow.pack();
        m_ProgressWindow.setVisible( true );
    }
    
    public void actionPerformed( ActionEvent event )
    {
        System.out.println( event.toString() );
    }
    
    public void start()
        throws Exception
    {
        File userHome = new File( System.getProperty( "user.home" ) );
        File magicHome = new File( userHome, ".magic" );
        File antLibDir = new File( userHome, ".ant/lib" );
        File cwDir = new File( System.getProperty( "user.dir" ) );
        
        ProgressIndicator indicator = new SwingProgress();
        Worker w = new Worker( indicator, magicHome, antLibDir, cwDir );
        w.start();
        m_FinishButton.setEnabled( true );
    }

    private void setSizes( JPanel panel, int min, int pref, int max )
    {
        Dimension minDim = new Dimension( min, min );
        panel.setMinimumSize( minDim );
        Dimension prefDim = new Dimension( pref, pref );
        panel.setPreferredSize( prefDim );
        Dimension maxDim = new Dimension( max, max );
        panel.setMaximumSize( maxDim );
    }

    private JProgressBar createProgressBar()
    {
        final Dimension minDim = new Dimension( 50, 10 );
        final Dimension prefDim = new Dimension( 300, 25 );
        final Dimension maxDim = new Dimension( 1200, 50 );
        JProgressBar bar = new JProgressBar();
        bar.setStringPainted( true );
        bar.setMinimumSize( minDim );
        bar.setPreferredSize( prefDim );
        bar.setMaximumSize( maxDim );
        bar.setMinimum( 0 );
        bar.setMaximum( 100 );
        return bar;
    }
            
    private JTextArea createTextArea()
    {
        final Dimension minDim = new Dimension( 50, 50 );
        final Dimension prefDim = new Dimension( 300, 300 );
        final Dimension maxDim = new Dimension( 1200, 1000 );
        JTextArea area = new JTextArea();
        area.setMinimumSize( minDim );
        area.setPreferredSize( prefDim );
        area.setMaximumSize( maxDim );
        return area;
    }

    private JButton createFinishButton()
    {
        final Dimension minDim = new Dimension( 20, 20 );
        final Dimension prefDim = new Dimension( 50, 30 );
        final Dimension maxDim = new Dimension( 100, 40 );
        JButton button = new JButton( "Finish" );
        button.setMinimumSize( minDim );
        button.setPreferredSize( prefDim );
        button.setMaximumSize( maxDim );
        button.setEnabled( false );
        button.addActionListener( this );    
        return button;
    }
        
    public class SwingProgress
        implements ProgressIndicator
    {
        public void message( String message )
        {
            m_TextArea.append( message );
            m_TextArea.append( "\n" );
        }
        
        public void start()
        {
        }
    
        public void progress( int percentage )
        {
            m_ProgressBar.setValue( percentage );
            m_ProgressBar.setString( percentage + "%" );
        }
    
        public void finished()
        {
        }
    }
}
 
 
