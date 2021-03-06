/*
 * Copyright 2005 Stephen J. McConnell
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

package dpml.library.info;

import java.io.OutputStream;
import java.io.IOException;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.util.Properties;

import dpml.util.Category;
import net.dpml.lang.Version;

import dpml.library.Classifier;
import dpml.library.Scope;

import dpml.library.info.IncludeDirective.Mode;

/**
 * Utility class used for construction of a module model from an XML source.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class LibraryEncoder extends LibraryConstants
{
   /**
    * Write a module directive to an output stream as a portable XML definition.
    * During export dependencies are limited to runtime concerns (eliminating 
    * build and test scoped dependencies).  Artifact production is strippped down
    * to a generic type declaration.  The resulting XML file is suitable for 
    * publication and usage by external projects.
    *
    * @param module the module directive to externalize
    * @param output the output stream
    * @exception IOException if an error occurs during module externalization
    */
    public void export( final ModuleDirective module, final OutputStream output ) throws IOException
    {
        if( !module.getExportPolicy() )
        {
            return;
        }
        final Writer writer = new OutputStreamWriter( output );
        try
        {
            writer.write( XML_HEADER );
            writer.write( "\n" );
            
            String name = module.getName();
            String version = module.getVersion();
            
            if( null != name )
            {
                writer.write( "<module name=\"" + name + "\"" );
            }
            if( null != version )
            {
                writer.write( " version=\"" + version + "\"" );
            }
            
            writer.write( 
              "\n    xmlns=\"" 
              + MODULE_XSD_URI
              + "\">" );
            
            String basedir = module.getBasedir();
            InfoDirective info = module.getInfoDirective();
            Properties properties = module.getProperties();
            TypeDirective[] types = module.getTypeDirectives();
            DependencyDirective[] dependencies = module.getDependencyDirectives();
            ResourceDirective[] resources = module.getResourceDirectives();
            
            if( !info.isNull() )
            {
                writer.write( "\n" );
                writeInfo( writer, info, "  " );
            }
            if( properties.size() > 0 )
            {
                writer.write( "\n" );
                writeProperties( writer, properties, "  ", true );
            }
            if( types.length > 0 )
            {
                writer.write( "\n" );
                writeTypes( writer, types, "  " );
            }
            if( dependencies.length > 0 )
            {
                writer.write( "\n" );
                writeDependencies( writer, dependencies, "  " );
            }
            if( resources.length > 0 )
            {
                writeResources( writer, resources, "  " );
            }
            writer.write( "\n\n</module>" );
            writer.write( "\n" );
        }
        finally
        {
            writer.flush();
            writer.close();
        }
    }
    
    private void writeModule( Writer writer, ModuleDirective module, String lead ) throws IOException
    {
        if( !module.getExportPolicy() )
        {
            return;
        }
        
        String name = module.getName();
        String version = module.getVersion();
        
        InfoDirective info = module.getInfoDirective();
        Properties properties = module.getProperties();
        String basedir = module.getBasedir();
        TypeDirective[] types = module.getTypeDirectives();
        DependencyDirective[] dependencies = module.getDependencyDirectives();
        ResourceDirective[] resources = module.getResourceDirectives();
        
        writer.write( "\n" + lead + "<module" );
        if( null != name )
        {
            writer.write( " name=\"" + name + "\"" );
        }
        if( null != version )
        {
            writer.write( " version=\"" + version + "\"" );
        }
        writer.write( ">" );
        
        if( !info.isNull() )
        {
            writer.write( "\n" );
            writeInfo( writer, info, lead + "  " );
        }
        if( properties.size() > 0 )
        {
            writer.write( "\n" );
            writeProperties( writer, properties, lead + "  ", true );
        }

        if( types.length > 0 )
        {
            writer.write( "\n" );
            writeTypes( writer, types, lead + "  " );
        }
        
        if( dependencies.length > 0 )
        {
            writer.write( "\n" );
            writeDependencies( writer, dependencies, lead + "  " );
        }
        
        if( resources.length > 0 )
        {
            writeResources( writer, resources, lead + "  " );
        }
        writer.write( "\n\n" + lead + "</module>" );
    }
    
    private void writeResource( Writer writer, ResourceDirective resource, String lead ) throws IOException
    {
        if( !resource.getExportPolicy() )
        {
            return;
        }
        
        String name = resource.getName();
        String version = resource.getVersion();
        
        InfoDirective info = resource.getInfoDirective();
        Properties properties = resource.getProperties();
        String basedir = resource.getBasedir();
        TypeDirective[] types = resource.getTypeDirectives();
        DependencyDirective[] dependencies = resource.getDependencyDirectives();
        
        writer.write( "\n" + lead + "<resource"  );
        if( null != name )
        {
            writer.write( " name=\"" + name + "\"" );
        }
        if( null != version )
        {
            writer.write( " version=\"" + version + "\"" );
        }
        writer.write( ">" );
        
        if( !info.isNull() )
        {
            writer.write( "\n" );
            writeInfo( writer, info, lead + "  " );
        }
        if( properties.size() > 0 )
        {
            writeProperties( writer, properties, lead + "  ", true );
        }
        if( types.length > 0 )
        {
            writeTypes( writer, types, lead + "  " );
        }
        if( dependencies.length > 0 )
        {
            writeDependencies( writer, dependencies, lead + "  " );
        }
        writer.write( "\n" + lead + "</resource>" );
    }
    
    private void writeInfo( 
      Writer writer, InfoDirective info, String lead ) throws IOException
    {
        writer.write( lead + "<info" );
        if( null != info.getTitle() )
        {
            writer.write( " title=\"" + info.getTitle() + "\"" );
        }
        String description = info.getDescription();
        if( null != description )
        {
            writer.write( ">" );
            writer.write( "\n" + lead + "  <description>" );
            writer.write( "\n" + lead + "  " + description );
            writer.write( "\n" + lead + "  </description>" );
            writer.write( "\n" + lead + "</info>" );
        }
        else
        {
            writer.write( "/>" );
        }
    }
    
    private void writeProperties( 
      Writer writer, Properties properties, String lead, boolean flag ) throws IOException
    {
        if( properties.size() > 0 )
        {
            if( flag )
            {
                writer.write( "\n" + lead + "<properties>" );
            }
            String[] names = (String[]) properties.keySet().toArray( new String[0] );
            for( int i=0; i<names.length; i++ )
            {
                String name = names[i];
                String value = properties.getProperty( name );
                writer.write( "\n" + lead );
                if( flag )
                {
                    writer.write( "  " );
                }
                writer.write( "<property name=\"" + name + "\" value=\"" + value + "\"/>" );
            }
            if( flag )
            {
                writer.write( "\n" + lead + "</properties>" );
            }
        }
    }
    
    private void writeTypes( Writer writer, TypeDirective[] types, String lead ) throws IOException
    {
        if( types.length > 0 )
        {
            writer.write( "\n" + lead + "<types>" );
            for( int i=0; i<types.length; i++ )
            {
                TypeDirective type = types[i];
                writeType( writer, type, lead + "  " );
            }
            writer.write( "\n" + lead + "</types>" );
        }
    }
    
    private void writeType( Writer writer, TypeDirective type, String lead ) throws IOException
    {
        String id = type.getID();
        writer.write( "\n" + lead + "<type id=\"" + id + "\"" );
        String version = type.getVersion();
        if( null != version )
        {
            writer.write( " version=\"" + version + "\"" );
        }
        String name = type.getName();
        if( null != name )
        {
            writer.write( " name=\"" + name + "\"" );
        }
        if( type.getAliasProduction() )
        {
            writer.write( " alias=\"true\"" );
        }
        writer.write( "/>" );
    }
    
    private void writeDependencies( 
      Writer writer, DependencyDirective[] dependencies, String lead ) throws IOException
    {
        if( dependencies.length > 0 )
        {
            writer.write( "\n" + lead + "<dependencies>" );
        
            for( int i=0; i<dependencies.length; i++ )
            {
                DependencyDirective dependency = dependencies[i];
                IncludeDirective[] includes = dependency.getIncludeDirectives();
                if( includes.length > 0 )
                {
                    Scope scope = dependency.getScope();
                    String label = scope.name().toLowerCase();
                    writer.write( "\n" + lead + "  <" + label + ">" );
                    for( int j=0; j<includes.length; j++ )
                    {
                        IncludeDirective include = includes[j];
                        Mode mode = include.getMode();
                        String value = include.getValue();
                        writer.write( "\n" + lead + "    <include" );
                        if( Mode.KEY.equals( mode ) )
                        {
                            writer.write( " key=\"" + value + "\"" );
                        }
                        else if( Mode.REF.equals( mode ) )
                        {
                            writer.write( " ref=\"" + value + "\"" );
                        }
                        else if( Mode.URI.equals( mode ) )
                        {
                            writer.write( " uri=\"" + value + "\"" );
                        }
                        
                        if( Scope.RUNTIME.equals( scope ) )
                        {
                            Category category = include.getCategory();
                            if( !Category.PRIVATE.equals( category ) )
                            {
                                String name = category.name().toLowerCase();
                                writer.write( " tag=\"" + name + "\"" );
                            }
                        }
                        
                        Properties props = include.getProperties();
                        if( props.size() > 0 )
                        {
                            writer.write( ">" );
                            writeProperties( writer, props, lead + "    ", false );
                            writer.write( "\n" + lead + "    </include>" );
                        }
                        else
                        {
                            writer.write( "/>" );
                        }
                    }
                    writer.write( "\n" + lead + "  </" + label + ">" );
                }
            }
            writer.write( "\n" + lead + "</dependencies>" );
        }
    }
    
    private void writeResources( Writer writer, ResourceDirective[] resources, String lead ) throws IOException
    {
        for( int i=0; i<resources.length; i++ )
        {
            ResourceDirective resource = resources[i];
            if( resource instanceof ModuleDirective )
            {
                writer.write( "\n" );
                ModuleDirective module = (ModuleDirective) resource;
                writeModule( writer, module, lead );
            }
            else
            {
                writer.write( "\n" );
                writeResource( writer, resource, lead );
            }
        }
    }
}
