/*
 * Copyright 2005 Stephen McConnell
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

import net.dpml.transit.Artifact;

/**
 * Application profile tree node. 
 */
public final class CodeBaseTreeNode extends Node
{
    private ApplicationProfile m_profile;
    private JLabel m_label;

    private ImageIcon m_icon = readImageIcon( "16/binary.png" );

    public CodeBaseTreeNode( ApplicationProfile profile )
    {
        super( profile );
        m_profile = profile;
        m_label = new JLabel( "Codebase", m_icon, SwingConstants.LEFT );
        m_label.setBorder( new EmptyBorder( 3, 3, 2, 2 ) );
    }

    Component getLabel()
    {
        return m_label;
    }
}