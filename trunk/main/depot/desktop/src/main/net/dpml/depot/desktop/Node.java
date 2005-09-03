
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

    public boolean apply()
    {
        return true;
    }

    public boolean undo()
    {
        return true;
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