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

package org.apache.avalon.tools.tasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DynamicAttributeNS;
import org.apache.tools.ant.DynamicConfiguratorNS;

import org.apache.avalon.tools.model.Definition;

import java.io.IOException;
import java.io.File;
import java.io.Writer;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Hashtable;

/**
 * Create meta-data for a block.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class BlockTask extends DeclareTask
{
    //--------------------------------------------------------------------------
    // static
    //--------------------------------------------------------------------------

    private static final String BLOCK = "block";
    private static final String MAIN = "main";
    private static final String TEST = "test";

   /**
    * An classes exposing a name attribute setter and getter. Used as a 
    * supertype to a number of the block meta-data classes.
    */
    public static class Identifiable
    {
        private String m_name;

       /**
        * Set the name of the identifiable to the supplied value.
        * @param name the identifying name
        */
        public void setName( final String name )
        {
            m_name = name;
        }

       /**
        * Return the name assigned to the identifiable.
        * @return the identifying name
        */
        public String getName()
        {
            return m_name;
        }
    }

   /**
    * A class representing a nested component within a block.
    */
    public static class Component extends Identifiable
    {
        private String m_classname;
        private String m_profile;
        private boolean m_context = false;
        private boolean m_configured = false;
        private boolean m_parameters = false;
        private boolean m_dependencies = false;
        private boolean m_activation = true;
        private ArrayList m_children = new ArrayList();

       /**
        * Set the classname of the component.
        * @param classname the component classname
        */
        public void setClass( final String classname )
        {
            m_classname = classname;
        }

       /**
        * Set the component activation on startup flag.
        * @param flag the component activation policy
        */
        public void setActivation( final boolean flag )
        {
            m_activation = flag ;
        }

       /**
        * Declaration of the assignment of a packaged profile to the component
        * directive.
        * @param profile the packaged profile name to use for this component directive
        */
        public void setProfile( final String profile )
        {
            m_profile = profile;
        }

       /**
        * Get the activation policy.
        * @return the activation policy
        */
        public boolean getActivation()
        {
            return m_activation;
        }

       /**
        * Return the classname of the component type.
        * @return the classname
        */
        public String getClassname()
        {
            return m_classname;
        }

       /**
        * Return the profile name (possibly null)
        * @return the profile name
        */
        public String getProfile()
        {
            return m_profile;
        }

       /**
        * Add a context to this compoent.
        * @return the context directive
        */
        public Context createContext()
        {
            if( !m_context )
            {
                Context context = new Context();
                m_children.add( context );
                m_context = true;
                return context;
            }
            else
            {
                final String error = 
                  "Context entry already set!";
                throw new BuildException( error );
            }
        }

       /**
        * Add a configuration to this component.
        * @return the configuration directive
        */
        public Configuration createConfiguration()
        {
            if( !m_configured )
            {
                Configuration config = new Configuration();
                m_children.add( config );
                m_configured = true;
                return config;
            }
            else
            {
                final String error = 
                  "Configuration entry already set!";
                throw new BuildException( error );
            }
        }

       /**
        * Add a parameters to this component.
        * @return the parameters directive
        */
        public Parameters createParameters()
        {
            if( !m_parameters )
            {
                Parameters parameters = new Parameters();
                m_children.add( parameters );
                m_parameters = true;
                return parameters;
            }
            else
            {
                final String error = 
                  "Parameters definition already set!";
                throw new BuildException( error );
            }
        }

       /**
        * Add a dependencies override directive to this component.
        * @return the depednencies directive
        */
        public Dependencies createDependencies()
        {
            if( !m_dependencies )
            {
                Dependencies deps = new Dependencies();
                m_children.add( deps );
                m_dependencies = true;
                return deps;
            }
            else
            {
                final String error = 
                  "Dependencies definition already set!";
                throw new BuildException( error );
            }
        }

       /**
        * Return the set of child directives within the component.
        * @return the nested directive
        */
        public Object[] getChildren()
        {
            return m_children.toArray();
        }
    }

   /**
    * Definition of an include directive.
    */
    public static class Include extends Identifiable
    {
        private String m_artifact;
        private List m_targets = new LinkedList();

       /**
        * Set the artifact uri.
        * @param the artifact uri
        */
        public void setArtifact( final String spec )
        {
            m_artifact = spec;
        }

       /**
        * Return the artifact url
        * @return the artifact uri value
        */
        public String getArtifact()
        {
            return m_artifact;
        }

       /**
        * Create, add and return a new target directive to the include directive.
        * @return a new target directive
        */
        public Target createTarget()
        {
            final Target target = new Target();
            m_targets.add( target );
            return target;
        }

       /**
        * Return all of the target directives within the include directive.
        * @return the set of target directives
        */
        public Target[] getTargets()
        {
            return (Target[]) m_targets.toArray( new Target[0] );
        }
    }

   /**
    * Declaration of a service export by a container.
    */
    public static class Service
    {
        private String m_type;
        private String m_source;

       /**
        * Set the interface type that is to be exported.
        * @param type the interface classname
        */
        public void setType( final String type )
        {
            m_type = type;
        }

       /**
        * Return the interface classname assigned to the service export directive
        * @return the service interface classname
        */
        public String getType()
        {
            return m_type;
        }

       /**
        * Set the name of the component within the container providing the 
        * exported service.
        * @param the source component address
        */
        public void setSource( final String source )
        {
            m_source = source;
        }

       /**
        * Return the name of the component within the container providing the 
        * exported service.
        * @return the source component address
        */
        public String getSource()
        {
            return m_source;
        }
    }

   /**
    * A context directive class.
    */
    public static class Context
    {
        private String m_class;
        private List m_entries = new ArrayList();

       /**
        * Declare a custom context implementation classname.
        * @param classname the classname of an optional context implementation class
        */
        public void setClass( final String classname )
        {
            m_class = classname ;
        }

       /**
        * Return the optional context implementation classname.
        * @return the classname
        */
        public String getClassname()
        {
            return m_class;
        }

       /**
        * Create, add and return a new entry directive to the context.
        * @return a new context entry directive
        */
        public Entry createEntry()
        {
            final Entry entry = new Entry();
            m_entries.add( entry );
            return entry;
        }

       /**
        * Return all of the context entries within the context directive.
        * @return the set of context entries
        */
        public Entry[] getEntries()
        {
            return (Entry[]) m_entries.toArray( new Entry[0] );
        }
    }

   /**
    * Defintion of a context entry directive.
    */
    public static class Entry extends Param
    {
        private String m_key;

       /**
        * Set the context enty key that this directive qualifies.
        * @param key the context entry key
        */
        public void setKey( final String key )
        {
            m_key = key ;
        }

       /**
        * Return the context entry key.
        * @return the entry key
        */
        public String getKey()
        {
            return m_key;
        }
    }

   /**
    * Defintion of a context entry parameter directive.
    */
    public static class Param
    {
        private String m_classname;
        private String m_value;
        private List m_params = new ArrayList();

       /**
        * Set the context entry classname.
        * @param classname the context entry classname
        */
        public void setClass( final String classname )
        {
            m_classname = classname;
        }

       /**
        * Return the context entry parameter classname.
        * @return the classname
        */
        public String getClassname()
        {
            return m_classname;
        }

       /**
        * Set the value of the context entry parameter.
        * @param the param value
        */
        public void setValue( final String value )
        {
            m_value = value;
        }

       /**
        * Return the value of the context entry param.
        * @return the value
        */
        public String getValue()
        {
            return m_value;
        }

       /**
        * Create, assign anfd return a new nested entry constructor parameter.
        * @return the new context entry param
        */
        public Param createParam()
        {
            final Param param = new Param();
            m_params.add( param );
            return param;
        }

       /**
        * Return the set of nested param directives.
        * @return the params
        */
        public Param[] getParams()
        {
            return (Param[]) m_params.toArray( new Param[0] );
        }
    }

   /**
    * A dependency directive.
    */
    public static class Dependency
    {
        private String m_source;

        private String m_key;

       /**
        * Set the key that this depedency directive qualifies.
        * @param key the dependency key
        */
        public void setKey( final String key )
        {
            m_key = key;
        }

       /**
        * Get the dependency directive key.
        */
        public String getKey()
        {
            return m_key;
        }

       /**
        * Set the address of the source component to fulofill the dependency.
        * @param the source component address
        */
        public void setSource( final String source )
        {
            m_source = source;
        }

       /**
        * Return the address of the source component to use to fulfill this dependency.
        * @return the source component address
        */
        public String getSource()
        {
            return m_source;
        }
    }

   /**
    * A dependencies directive.
    */
    public static class Dependencies
    {
        private List m_dependencies = new ArrayList();

       /**
        * Create, assiciate and return a new dependency within this set of dependencies.
        * @return the new dependnecy directive
        */
        public Dependency createDependency()
        {
            final Dependency dep = new Dependency();
            m_dependencies.add( dep );
            return dep;
        }

       /**
        * Return the setr of dependency directives withi this dependencies directive.
        * @return the dependency directives
        */
        public Dependency[] getDependencies()
        {
            return (Dependency[]) m_dependencies.toArray( new Dependency[0] );
        }
    }

   /**
    * A parameter directive.
    */
    public static class Parameter extends Identifiable
    {
        private String m_value;

       /**
        * Set the value assigned to the named parameter.
        * @param value the parameter value
        */
        public void setValue( final String value )
        {
            m_value = value;
        }

       /**
        * Return the value assigned to the parameter.
        * @return the parameter value
        */
        public String getValue()
        {
            return m_value;
        }
    }

   /**
    * A parameters directive declares a set of n parameters.
    */
    public static class Parameters
    {
        private List m_parameters = new ArrayList();

       /**
        * Create, allocate and return a new parameter with this set of parameters.
        * @return a new parameter directive
        */
        public Parameter createParameter()
        {
            final Parameter parameter = new Parameter();
            m_parameters.add( parameter );
            return parameter;
        }

       /**
        * Return the set of parameter directives declarared within this parameters directives.
        * @return the set of parameter directives
        */
        public Parameter[] getParameters()
        {
            return (Parameter[]) m_parameters.toArray( new Parameter[0] );
        }
    }

   /**
    * A configuration directive.
    */
    public static class Configuration implements DynamicConfiguratorNS
    {
        private String m_value;
        private Map m_attributes = new Hashtable();
        private List m_children = new LinkedList();
        private String m_name;

       /**
        * Creation of a root configuration directive.
        */
        public Configuration()
        {
            this( "configuration" );
        }

       /**
        * Creation of a named configuration element.
        * @param name the element name
        */
        public Configuration( String name )
        {
            m_name = name;
        }

       /**
        * Add nested text.
        * @param value the test value
        */
        public void addText(String value ) 
        {
            String s = value.trim();
            if( s.length() > 0 )
            {
                m_value = s;
            }
        }

       /**
        * Set a named attribute to the given value
        * 
        * @param uri The namespace uri for this attribute, "" is
        *            used if there is no namespace uri.
        * @param localName The localname of this attribute.
        * @param qName The qualified name for this attribute
        * @param value The value of this attribute.
        * @throws BuildException when any error occurs
        */
        public void setDynamicAttribute(
            String uri, String localName, String qName, String value)
            throws BuildException
        {
             m_attributes.put( qName, value );
        }

       
       /**
        * Create an element with the given name
        *
        * @param name the element nbame
        * @throws BuildException when any error occurs
        * @return the element created
        */
        public Object createDynamicElement(
           String uri, String localName, String qName) throws BuildException
        {
             Configuration conf = new Configuration( qName );
             m_children.add( conf );
             return conf;
        }

       /**
        * Return the name of the configuration element.
        * @return the node name
        */
        public String getName()
        {
            return m_name;
        }

       /**
        * Return a value associated with the element.
        * @return the assigned value
        */
        public String getValue()
        {
            return m_value;
        }

       /**
        * Return the map of the assigned attributes.
        * @return the attribute name value map
        */
        public Map getAttributes()
        {
            return m_attributes;
        }

       /**
        * Return he set of nest child configuration directives.
        * @return the configuration directives within this directive
        */
        public Configuration[] getChildren()
        {
            return (Configuration[]) m_children.toArray( new Configuration[m_children.size()] );
        }
    }

   /**
    * A target directive.
    */
    public static class Target
    {
        private String m_path;
        private Configuration m_configuration;
        private Parameters m_parameters;

       /**
        * Set the path that this target is overriding.
        */
        public void setPath( String path )
        {
             m_path = path;
        }

       /**
        * Return the target path.
        * @return the target that this override is overriding.
        * @exception BuildException if the target is not declared
        */
        public String getPath()
        {
             if( null == m_path )
             {
                  final String error = 
                    "Required path attribute has not been declared.";
                  throw new BuildException( error );
             }
             else
             {
                  return m_path;
             }
        }

       /**
        * Create a configuration directive.
        * @return the configuration directive
        */
        public Configuration createConfiguration()
        {
            if( null == m_configuration )
            {
                m_configuration = new Configuration();
                return m_configuration;
            }
            else
            {
                final String error = 
                  "Configuration entry already set!";
                throw new BuildException( error );
            }
        }

       /**
        * Reurn the configuration directive.
        * @return the configuration directive (possibly null)
        */
        public Configuration getConfiguration()
        {
            return m_configuration;
        }


       /**
        * Return the parameters assigned this target override (possibly null).
        * @return the parameters directive
        */
        public Parameters getParameters()
        {
            return m_parameters;
        }
    }

    //--------------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------------

    private String m_target;
    private String m_container;
    private List m_content = new ArrayList();
    private boolean m_standalone = true;
    private Service m_service;

    //--------------------------------------------------------------------------
    // features
    //--------------------------------------------------------------------------

   /**
    * Set the name of the block.
    * @param name the block name
    */
    public void setName( final String name )
    {
        m_container = name;
    }

   /**
    * Optional attribute indicating that the block is to be generated
    * as an embedded BLOCK-INF/block.xml into either the MAIN or TEST
    * classes directory as indicated by the target parameter.
    */
    public void setEmbed( final String target )
    {
        if( MAIN.equalsIgnoreCase( target ) )
        {
            m_target = MAIN;
        }
        else if( TEST.equalsIgnoreCase( target ) )
        {
            m_target = TEST;
        }
        else
        {
            final String error = 
              "Embed policy not recognized (use MAIN or TEST)";
            throw new BuildException( error );
        }
    }

   /**
    * Optional attribute indicating that the block is to be generated
    * as a standalone block.
    *
    * @param flag the standalone flag
    */
    public void setStandalone( final boolean flag )
    {
        m_standalone = flag;
    }

   /**
    * Create and add a component directive to this block.
    * @return the new component directive
    */
    public Component createComponent()
    {
        final Component component = new Component();
        m_content.add( component );
        return component;
    }

   /**
    * Create and add a new block include directive to the block.
    * @return the include directive
    */
    public Include createInclude()
    {
        final Include include = new Include();
        m_content.add( include );
        return include;
    }

   /**
    * Create and add a single service export directive to the block.
    * @return the service export directive
    */
    public Service createService()
    {
        if( null == m_service )
        {
            m_service = new Service();
            return m_service;
        }
        else
        {
            final String error = 
              "Multiple service export not supported nor recomended.";
            throw new BuildException( error );
        }
    }

   /**
    * Initialize the task.
    */
    public void init()
    {
        super.init();
        super.setType( BLOCK );
    }

   /**
    * Execute the task.
    */
    public void execute()
    {
        if( null != m_target )
        {
            super.execute(); // generate the embedded block
            m_target = null;
        }
        if( m_standalone )
        {
            super.execute(); // generate the standalone block
        }
    }

    //--------------------------------------------------------------------------
    // implementation
    //--------------------------------------------------------------------------

    private String getName( Definition def )
    {
        if( null == m_container )
        {
            return def.getInfo().getName();
        }
        else
        {
            return m_container;
        }
    }

    protected File getPluginFile()
    {
        if( null == m_target )
        {
            return super.getPluginFile();
        }
        else
        {
            File root = getEmbeddedRoot( m_target );
            File blockinf = new File( root, "BLOCK-INF" );
            return new File( blockinf, "block.xml" );
        }
    }

    private File getEmbeddedRoot( String target )
    {
        if( MAIN.equals( target ) )
        {
            return getContext().getClassesDirectory();
        }
        else
        {
            return getContext().getTestClassesDirectory();
        }
    }

    protected void writePlugin( final Writer writer, final Definition def )
        throws IOException
    {
        writeContainer( "", writer, def );
    }

    protected void writeContainer( final String pad, final Writer writer, final Definition def )
        throws IOException
    {
        writer.write( "\n\n" + pad + "<container name=\"" + getName( def ) + "\">" );

        if( null != m_service )
        {
            writeService( pad + "  ", writer, m_service );
        }

        final String indent = pad + "  ";
        writer.write( "\n\n" + indent + "<classloader>" );
        boolean standalone = (null == m_target);
        writeClasspath( writer, def, indent + "  ", standalone );
        writer.write( "\n" + indent + "</classloader>" );

        Identifiable[] components = 
          (Identifiable[]) m_content.toArray( new Identifiable[0] );
        for( int i=0; i<components.length; i++ )
        {
            Identifiable identifiable = components[i];
            if( identifiable instanceof Component )
            {
                writeComponent( indent, writer, (Component) identifiable );
            }
            else if( identifiable instanceof Include )
            {
                writeInclude( indent, writer, (Include) identifiable );
            }
        }

        writer.write( "\n\n" + pad + "</container>\n" );
    }

    private void writeComponent( final String pad, final Writer writer, final Component component )
        throws IOException
    {
        writer.write( "\n\n" + pad + "<component name=\"" + component.getName() 
          + "\" class=\"" + component.getClassname() + "\"" );

        if( null != component.getProfile() )
        {
            writer.write( " profile=\"" + component.getProfile() + "\"" );
        }

        if( !component.getActivation() )
        {
            writer.write( " activation=\"false\" " );
        }

        Object[] children = component.getChildren();
        if( children.length == 0 )
        {
            writer.write( "/>" );
        }
        else
        {
            writer.write( ">" );
            for( int i=0; i<children.length; i++ )
            {
                Object child = children[i];
                if( child instanceof Context )
                {
                    writeContext( pad + "  ", writer, (Context) child );
                }
                else if( child instanceof Dependencies )
                {
                    writeDependencies( pad + "  ", writer, (Dependencies) child );
                }
                else if( child instanceof Parameters )
                {
                    writeParameters( pad + "  ", writer, (Parameters) child );
                }
                else if( child instanceof Configuration )
                {
                    writeConfiguration( pad + "  ", writer, (Configuration) child );
                }
                else
                {
                    final String error = 
                      "Component declaration contains an unrecognized class: "
                      + child.getClass().getName();
                    throw new IllegalStateException( error );
                }
            }
            writer.write( "\n" + pad + "</component>" );
        }
    }

    private void writeConfiguration( final String pad, final Writer writer, final Configuration config )
        throws IOException
    {
        String name = config.getName();
        writer.write( "\n" + pad + "<" + name );
        Map attributes = config.getAttributes();
        if( attributes.size() > 0 )
        {
            Map.Entry[] values = (Map.Entry[]) attributes.entrySet().toArray( new Map.Entry[0] );
            for( int i=0; i < values.length; i++ )
            {
                 Map.Entry entry = values[i];
                 writer.write( " " + entry.getKey() + "=\"" + entry.getValue() + "\"" );
            }
        }

        Configuration[] children = config.getChildren();
        if( children.length > 0 )
        {
            writer.write( ">" );
            for( int i=0; i < children.length; i++ )
            {
                 writeConfiguration( pad + "  ", writer, children[i] );
            }
            writer.write( "\n" + pad + "</" + name + ">" );
        }
        else
        {
            if( null == config.getValue() )
            {
                writer.write( "/>" );
            }
            else
            {
            	writer.write( ">" );
                writer.write( config.getValue() );
                writer.write( "</" + name + ">" );
            }
        }
    }

    private void writeContext( final String pad, final Writer writer, final Context context )
        throws IOException
    {
        writer.write( "\n" + pad + "<context" ); 
        if( null != context.getClassname() )
        {
            writer.write( " class=\"" + context.getClassname() + "\"" );
        }
        Entry[] entries = context.getEntries();
        if( entries.length == 0 )
        {
            writer.write( "/>" );
        }
        else
        {
            writer.write( ">" );
            for( int i=0; i<entries.length; i++ )
            {
                writeEntry( pad + "  ", writer, entries[i] );
            }  
            writer.write( "\n" + pad + "</context>" ); 
        }
    }

    private void writeEntry( final String pad, final Writer writer, final Entry entry )
        throws IOException
    {
        writer.write( 
          "\n" + pad + "<entry key=\"" + entry.getKey() + "\"" );

        if( null != entry.getClassname() )
        {
            writer.write( " class=\"" + entry.getClassname() + "\"" );
        }

        if( null != entry.getValue() )
        {
            writer.write( ">" + entry.getValue() + "</entry>" );
        }
        else
        {
            Param[] params = entry.getParams();
            if( params.length == 0 )
            {
                writer.write( "/>" );  // is this legal?
            }
            else
            {
                writer.write( ">" );
                for( int i=0; i<params.length; i++ )
                {
                    writeParam( pad + "  ", writer, params[i] );
                } 
                writer.write( "\n" + pad + "</entry>" ); 
            }
        }
    }

    private void writeInclude( final String pad, final Writer writer, final Include include )
        throws IOException
    {
        writer.write( 
          "\n\n" + pad + "<include name=\"" 
          + include.getName() + "\" artifact=\"" + include.getArtifact() + "\"" );
        Target[] targets = include.getTargets();
        if( targets.length > 0 )
        {
            writer.write( ">" );
            for( int i=0; i<targets.length; i++ )
            {
                writeTarget( pad + "  ", writer, targets[i] );
            }
            writer.write( "\n" + pad + "</include>" );
        }
        else
        {
            writer.write( "/>" );
        }
    }

    private void writeTarget( final String pad, final Writer writer, final Target target )
        throws IOException
    {
        String path = target.getPath();
        writer.write( 
          "\n" + pad + "<target path=\"" + path + "\">" );
        Configuration config = target.getConfiguration();
        if( null != config )
        {
            writeConfiguration( pad + "  ", writer, config );
        }
        Parameters parameters = target.getParameters();
        if( null != parameters )
        {
            writeParameters( pad + "  ", writer, parameters );
        }
        writer.write( 
          "\n" + pad + "</target>" );
    }

    private void writeService( final String pad, final Writer writer, final Service service )
        throws IOException
    {
        final String lead = "\n" + pad;
        writer.write( "\n" );
        writer.write( lead + "<services>" );
        writer.write( lead + "  <service type=\"" + service.getType() + "\">" );
        writer.write( lead + "    <source>" + service.getSource() + "</source>" );
        writer.write( lead + "  </service>" );
        writer.write( lead + "</services>" );
    }

    private void writeParam( final String pad, final Writer writer, final Param param )
        throws IOException
    {
        if( null == param.getClassname() )
        {
            writer.write( "\n" + pad + "<param>" );
        }
        else
        {
            writer.write( "\n" + pad + "<param class=\"" + param.getClassname() + "\">" );
        }

        String value = param.getValue();
        if( null != value )
        {
            writer.write( value + "</param>" );
        }
        else
        {
            Param[] parameters = param.getParams();
            if( parameters.length == 0 )
            {
                writer.write( "/>" );  // is this legal?
            }
            else
            {
                writer.write( ">" );
                for( int i=0; i<parameters.length; i++ )
                {
                    writeParam( pad + "  ", writer, parameters[i] );
                } 
                writer.write( "\n" + pad + "</param>" ); 
            }
        }
    }

    private void writeDependencies( final String pad, final Writer writer, final Dependencies deps )
        throws IOException
    {
        writer.write( "\n" + pad + "<dependencies>" );
        Dependency[] dependencies = deps.getDependencies();
        for( int i=0; i<dependencies.length; i++ )
        {
            writeDependency( pad + "  ", writer, dependencies[i] );
        } 
        writer.write( "\n" + pad + "</dependencies>" );
    }

    private void writeDependency( final String pad, final Writer writer, final Dependency dep )
        throws IOException
    {
        writer.write( 
          "\n" + pad + "<dependency key=\"" 
          + dep.getKey() + "\" source=\"" 
          + dep.getSource() + "\"/>" );
    }

    private void writeParameters( final String pad, final Writer writer, final Parameters parameters )
        throws IOException
    {
        writer.write( "\n" + pad + "<parameters>" );
        Parameter[] params = parameters.getParameters();
        for( int i=0; i<params.length; i++ )
        {
            writeParameter( pad + "  ", writer, params[i] );
        } 
        writer.write( "\n" + pad + "</parameters>" );
    }

    private void writeParameter( final String pad, final Writer writer, final Parameter param )
        throws IOException
    {
        writer.write( 
          "\n" + pad + "<parameter name=\"" 
          + param.getName() + "\" value=\"" 
          + param.getValue() + "\"/>" );
    }

}
