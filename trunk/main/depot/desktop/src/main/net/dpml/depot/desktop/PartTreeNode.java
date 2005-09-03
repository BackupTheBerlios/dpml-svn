
package net.dpml.depot.desktop;

import java.awt.Component;
import java.net.URL;
import java.net.URI;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;

import net.dpml.profile.ApplicationRegistry;
import net.dpml.profile.ApplicationProfile;

/**
 * Application profile tree node. 
 */
public final class PartTreeNode extends Node
{
    private ApplicationProfile m_profile;
    private JLabel m_label;

    private ImageIcon m_icon = readImageIcon( "16/binary.png" );

    public PartTreeNode( ApplicationProfile profile )
    {
        super( profile );
        m_profile = profile;
        m_label = new JLabel( "Part", m_icon, SwingConstants.LEFT );
        m_label.setBorder( new EmptyBorder( 3, 3, 2, 2 ) );
    }

    Component getLabel()
    {
        return m_label;
    }
}