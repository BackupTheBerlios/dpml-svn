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
import java.util.Properties;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.table.TableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableColumnModel;

import net.dpml.profile.ApplicationRegistry;
import net.dpml.profile.ApplicationProfile;

/**
 * Abstract tree node. 
 */
public abstract class Node extends DefaultMutableTreeNode implements Volotile
{
    private static final JPanel DEFAULT_COMPONENT = new JPanel();

    private final PropertyChangeSupport m_propertyChangeSupport;

    public Node()
    {
        this( null );
    }

    public Node( Object object )
    {
        super( object );
        m_propertyChangeSupport = new PropertyChangeSupport( this );
    }

    public void addPropertyChangeListener( PropertyChangeListener listener )
    {
        m_propertyChangeSupport.addPropertyChangeListener( listener );
    }

    public void removePropertyChangeListener( PropertyChangeListener listener )
    {
        m_propertyChangeSupport.removePropertyChangeListener( listener );
    }

    protected void firePropertyChange( PropertyChangeEvent event )
    {
        m_propertyChangeSupport.firePropertyChange( event );
    }

    public boolean isModified()
    {
        return false;
    }

    public void apply() throws Exception
    {
    }

    public void revert() throws Exception
    {
    }

    abstract Component getLabel();

    Component getComponent()
    {
        return DEFAULT_COMPONENT;
    }

    protected static ImageIcon readImageIcon( final String filename ) 
    {
        URL url = Node.class.getClassLoader().getResource( "net/dpml/depot/desktop/images/" + filename );
        return new ImageIcon( url );
    }

}