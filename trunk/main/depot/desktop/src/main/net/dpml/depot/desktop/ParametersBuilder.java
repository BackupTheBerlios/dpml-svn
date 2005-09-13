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
import java.util.Enumeration;
import java.util.Properties;
import java.lang.reflect.Constructor;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.ImageIcon;
import javax.swing.border.EmptyBorder;

import javax.swing.JTree;
import javax.swing.JLabel;
import javax.swing.JSplitPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import net.dpml.profile.ApplicationRegistry;
import net.dpml.profile.ApplicationProfile;

import net.dpml.transit.model.Value;
import net.dpml.transit.model.Construct;
import net.dpml.transit.Repository;
import net.dpml.transit.Transit;

/**
 * Application profile tree node. 
 */
public final class ParametersBuilder
{
    JComponent buildParametersPanel( ApplicationProfile profile ) throws Exception
    {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode( profile );
        DefaultTreeModel model = new DefaultTreeModel( root );

        Value[] values = profile.getParameters();
        for( int i=0; i<values.length; i++ )
        {
            Value value = values[i];
            if( value instanceof Construct )
            {
                root.add( new ValueTreeNode( (Construct) value ) );
            }
            else
            {
                final String error =
                  "Value class not recognized: " + value.getClass().getName();
                throw new RuntimeException( error );
            }
        }

        JTree tree = new JTree( model );
        tree.setRootVisible( false );
        tree.setShowsRootHandles( true );
        tree.setCellRenderer( new ValueCellRenderer() );

        expand( tree, new TreePath( root ), true );
 
        JSplitPane panel = new ParametersSplitPane( tree );
        panel.setOrientation( JSplitPane.VERTICAL_SPLIT );
        panel.setTopComponent( tree );
        panel.setBottomComponent( new JPanel() );
        panel.setDividerLocation( 200 );
        
        JScrollPane scrollPane = new JScrollPane( panel );
        scrollPane.getViewport().setBackground( tree.getBackground() );
        scrollPane.setBorder( null );
        return scrollPane;
    }

    private class ParametersSplitPane extends JSplitPane implements TreeSelectionListener
    {
        private JTree m_tree;

        private ParametersSplitPane( JTree tree )
        {
            m_tree = tree;
            m_tree.addTreeSelectionListener( this );
        }

        public void valueChanged( TreeSelectionEvent event )
        {
            TreePath path = event.getPath();
            Object object = path.getLastPathComponent();
            if( object instanceof ValueTreeNode )
            {
                ValueTreeNode node = (ValueTreeNode) object;
                Value construct = node.getValueInstance();
                int location = getDividerLocation();
                Component component = buildConstructEditor( construct );
                setBottomComponent( component );
                setDividerLocation( location );
            }
        }
    }

    private Component buildConstructEditor( Value value ) 
    {
        FormLayout layout = new FormLayout(
          "right:pref, 3dlu, fill:max(120dlu;pref)", 
          "pref, 3dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, pref" );

        PanelBuilder builder = new PanelBuilder( layout );
        builder.setDefaultDialogBorder();
        CellConstraints cc = new CellConstraints();
        builder.addSeparator( "Value", cc.xyw( 1, 1, 3 ) );
        builder.addLabel( "Type:", cc.xy( 1, 3 ) ); 
        builder.add( getType( value ), cc.xy( 3, 3 ) );
         
        builder.addLabel( "Class:", cc.xy( 1, 5 ) ); 
        builder.add( getClass( value ), cc.xy( 3, 5 ) );
        if( value instanceof Construct )
        {
            Construct construct = (Construct) value;
            if( construct.getValues().length == 0 )
            {
                builder.addLabel( "Value:", cc.xy( 1, 7 ) );
                builder.add( getValue( construct ), cc.xy( 3, 7 ) );
            }
        }
        return builder.getPanel();
    }

    private Component getType( Value construct )
    {
        return new TypeDocument( construct );
    }

    private Component getClass( Value construct )
    {
        return new ClassDocument( construct );
    }

    private Component getValue( Construct construct )
    {
        return new ValueDocument( construct );
    }

    private void expand( JTree tree, TreePath parent, boolean expand )
    {
        TreeNode node = (TreeNode) parent.getLastPathComponent();
        if( node.getChildCount() >= 0 )
        {
            Enumeration enum = node.children();
            while( enum.hasMoreElements() )
            {
                TreeNode n = (TreeNode) enum.nextElement();
                TreePath path = parent.pathByAddingChild( n );
                expand( tree, path, expand );
            }
        }

        if( expand )
        {
            tree.expandPath( parent );
        }
        else
        {
            tree.collapsePath( parent );
        }
    }

    private class ValueTreeNode extends DefaultMutableTreeNode
    {
        private final Construct m_value;

        private ValueTreeNode( final Construct value )
        {
            super( value );

            m_value = value;

            if( value.getBaseValue() == null )
            {
                //
                // add subsidiary value nodes
                //

                Value[] values = value.getValues();
                for( int i=0; i< values.length; i++ )
                {
                    Value v = values[i];
                    if( value instanceof Construct )
                    {
                        add( new ValueTreeNode( (Construct) v ) );
                    }
                    else
                    {
                        final String error =
                          "Value class not recognized: " + v.getClass().getName();
                        throw new RuntimeException( error );
                    }
                }
            }
        }

        public Value getValueInstance()
        {
            return m_value;
        }

        public String getBaseClassname()
        {
            return m_value.getBaseClassname();
        }

        public String toString()
        {
            return m_value.getTypeClassname();
        }
    }

    private class StringTreeNode extends DefaultMutableTreeNode
    {
        private StringTreeNode( final String value )
        {
            super( value );
        }
    }

    private class ValueCellRenderer extends DefaultTreeCellRenderer
    {
        public Component getTreeCellRendererComponent( 
          JTree tree, Object value, boolean selected, boolean expanded, 
          boolean leaf, int row, boolean focus )
        {
            if( value instanceof ValueTreeNode )
            {
                ValueTreeNode vtn = (ValueTreeNode) value;
                String base = vtn.getBaseClassname();
                setToolTipText( base );
                setClosedIcon( VALUE_ICON );
                setOpenIcon( VALUE_ICON );
                setLeafIcon( VALUE_ICON );
            }
            else if( value instanceof StringTreeNode )
            {
                setClosedIcon( null );
                setOpenIcon( null );
                setLeafIcon( null );
                setToolTipText( null );
            }
            else
            {
                setToolTipText( null );
            }
            return super.getTreeCellRendererComponent( 
                tree, value, selected, expanded, leaf, row, focus );
        }
    }

    private abstract class AbstractDocument extends JTextField implements DocumentListener
    {
        private Value m_value;
        private boolean m_modified;

        public AbstractDocument( Value value, String s )
        {
            super( s );
            m_value = value;
            getDocument().addDocumentListener( this );
        }

        protected Value getValueInstance()
        {
            return m_value;
        }

        public void insertUpdate( DocumentEvent event )
        {
            checkModified();
        }

        public void removeUpdate( DocumentEvent event )
        {
            checkModified();
        }

        public void changedUpdate( DocumentEvent event )
        {
            checkModified();
        }

        public abstract void checkModified();

        protected void setModified( boolean modified )
        {
            m_modified = modified;
        }

        protected boolean getModified()
        {
            return m_modified;
        }
    }

    private class ClassDocument extends AbstractDocument
    {
        public ClassDocument( Value construct )
        {
            super( construct, construct.getBaseClassname() );
        }

        public void checkModified()
        {
            String text = getText();
            String classname = getValueInstance().getBaseClassname();
            setModified( text.equals( classname ) );
        }
    }

    private class TypeDocument extends AbstractDocument
    {
        public TypeDocument( Value construct )
        {
            super( construct, construct.getTypeClassname()  );
        }

        public void checkModified()
        {
            String text = getText();
            String classname = getValueInstance().getTypeClassname();
            setModified( text.equals( classname ) );
        }
    }

    private class ValueDocument extends AbstractDocument
    {
        public ValueDocument( Construct construct )
        {
            super( construct, construct.getBaseValue()  );
        }

        public void checkModified()
        {
            String text = getText();
            String value = ((Construct)getValueInstance()).getBaseValue();
            setModified( text.equals( value ) );
        }
    }

    protected static ImageIcon readImageIcon( final String filename ) 
    {
        return readImageIcon( BASE_IMAGE_PATH, filename );
    }

    protected static ImageIcon readImageIcon( final String base, final String filename ) 
    {
        final String path = base + filename;
        URL url = Node.class.getClassLoader().getResource( path );
        if( null == url )
        {
           final String error = "Invalid icon path: " + path;
           throw new IllegalArgumentException( error );
        }
        return new ImageIcon( url );
    }

    private static final String BASE_IMAGE_PATH = "net/dpml/depot/desktop/images/";

    private static final ImageIcon VALUE_ICON = readImageIcon( "16/object.png" );

}
