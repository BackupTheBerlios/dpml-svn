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

package org.apache.avalon.composition.data.writer;

import java.io.IOException;
import java.io.Writer;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.parameters.ParameterException;

import org.apache.avalon.composition.data.ComponentProfile;
import org.apache.avalon.composition.data.DeploymentProfile;
import org.apache.avalon.composition.data.ContextDirective;
import org.apache.avalon.composition.data.DependencyDirective;
import org.apache.avalon.composition.data.SelectionDirective;

import org.apache.avalon.logging.data.CategoriesDirective;
import org.apache.avalon.logging.data.CategoryDirective;

import org.apache.avalon.composition.data.ImportDirective;
import org.apache.avalon.composition.data.EntryDirective;
import org.apache.avalon.composition.data.ConstructorDirective;
import org.apache.avalon.composition.data.StageDirective;
import org.apache.avalon.composition.data.Parameter;

import org.apache.avalon.meta.info.InfoDescriptor;

import org.apache.avalon.util.configuration.ConfigurationUtil;

/**
 * Write a {@link ComponentProfile} to a stream as xml documents.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: XMLComponentProfileWriter.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class XMLComponentProfileWriter
{

    public static final String INDENT = "  ";

   /**
    * Write out a containment deployment profile to XML.
    * @param writer the writer
    * @throws IOException if unable to write xml
    */
    protected void writeComponentProfile( 
      final Writer writer, ComponentProfile profile, String pad )
      throws Exception
    {
        //
        // write the component declaration
        //

        writer.write( pad + "<component name=\"" + profile.getName() + "\"");
        writer.write( "\n" + pad + "  class=\"" + profile.getClassname() + "\"");

        int activation = profile.getActivationDirective();
        if( activation != DeploymentProfile.DEFAULT )
        {
            if( activation == DeploymentProfile.ENABLED )
            {
                writer.write( 
                  "\n" + pad 
                  + "  activation=\"false\"" );
            }
            else
            {
                writer.write( 
                  "\n" + pad 
                  + "  activation=\"true\"" );
            } 
        }

        final int collection = profile.getCollectionPolicy();
        if( collection > InfoDescriptor.UNDEFINED )
        {
            writer.write( 
              "\n" + pad 
              + "  collection=\"" 
              + InfoDescriptor.getCollectionPolicyKey( collection ) 
              + "\"" ); 
        }

        if(( profile.getCategories() == null ) && ( profile.getContext() == null )
         && ( isEmptyConfiguration( profile.getConfiguration() ) ) && ( profile.getParameters() == null ))
        {
            writer.write( "/>");
        }
        else
        {
            writer.write( ">" );
            final String padding = pad + INDENT;
            writeDeploymentBody( writer, profile, padding );
            writer.write( "\n" + pad + "</component>" );
        }
    }

    private boolean isEmptyConfiguration( Configuration config )
    {
        if( config == null ) return true;
        if( config.getChildren().length > 0 ) return false;
        return config.getAttributeNames().length == 0;
    }

   /**
    * Write out a containment deployment content.
    * @param writer the writer
    * @throws IOException if unable to write xml
    */
    protected void writeDeploymentBody( 
      final Writer writer, ComponentProfile profile, String pad )
      throws Exception
    {
        //
        // write out the lifecycle directives
        //
     
        writeCategories( writer, profile.getCategories(), pad );
        writeContext( writer, profile.getContext(), pad );
        writeDependencies( writer, profile.getDependencyDirectives(), pad );
        writeStages( writer, profile.getStageDirectives(), pad );
        writeConfiguration( writer, profile.getConfiguration(), pad );
        writeParameters( writer, profile.getParameters(), pad );
    }

    /**
     * Write out xml representation of the dependency directives.
     *
     * @param writer the writer
     * @param dependencies the dependency directives
     * @throws IOException if unable to write xml
     */
    private void writeDependencies( 
      final Writer writer, final DependencyDirective[] dependencies, String pad )
      throws IOException
    {
        if( dependencies.length == 0 ) return;
        writer.write( "\n" + pad + "<dependencies>" );
        final String padding = pad + INDENT;
        for( int i=0; i<dependencies.length; i++ )
        {
            writeDependency( writer, dependencies[i], padding );
        }
        writer.write( "\n" + pad + "</dependencies>" );
    }

    /**
     * Write out xml representation of a dependency directive.
     *
     * @param writer the writer
     * @param dependency the dependency directive
     * @throws IOException if unable to write xml
     */
    private void writeDependency( 
      final Writer writer, final DependencyDirective dependency, String pad )
      throws IOException
    {
        writer.write( "\n" + pad + "<dependency key=\"" + dependency.getKey() + "\"" );
        if( dependency.getSource() != null )
        {
            writer.write( " source=\"" + dependency.getSource() + "\"/>" );
        }
        else
        {
            writer.write( ">" );
            SelectionDirective[] features = dependency.getSelectionDirectives();
            final String padding = pad + INDENT;
            for( int i=0; i<features.length; i++ )
            {
                writeSelectionDirective( writer, features[i], padding );
            }
            writer.write( "\n" + pad + "</dependency>" );
        }
    }

    /**
     * Write out xml representation of the dependency directives.
     *
     * @param writer the writer
     * @param dependencies the dependency directives
     * @throws IOException if unable to write xml
     */
    private void writeStages( 
      final Writer writer, final StageDirective[] stages, String pad )
      throws IOException
    {
        if( stages.length == 0 ) return;
        writer.write( "\n" + pad + "<stages>" );
        final String padding = pad + INDENT;
        for( int i=0; i<stages.length; i++ )
        {
            writeStage( writer, stages[i], padding );
        }
        writer.write( "\n" + pad + "</stages>" );
    }

    /**
     * Write out xml representation of a stage directive.
     *
     * @param writer the writer
     * @param stage the stage directive
     * @throws IOException if unable to write xml
     */
    private void writeStage( 
      final Writer writer, final StageDirective stage, String pad )
      throws IOException
    {
        writer.write( "\n" + pad + "<stage key=\"" + stage.getKey() + "\"" );
        if( stage.getSource() != null )
        {
            writer.write( " source=\"" + stage.getSource() + "\"/>" );
        }
        else
        {
            writer.write( ">" );
            SelectionDirective[] features = stage.getSelectionDirectives();
            final String padding = pad + INDENT;
            for( int i=0; i<features.length; i++ )
            {
                writeSelectionDirective( writer, features[i], padding );
            }
            writer.write( "\n" + pad + "</stage>" );
        }
    }

    /**
     * Write out xml representation of a selection directive.
     *
     * @param writer the writer
     * @param feature the dependency selection directive
     * @throws IOException if unable to write xml
     */
    private void writeSelectionDirective( 
      final Writer writer, final SelectionDirective feature, String pad )
      throws IOException
    {
        writer.write( "\n" + pad 
          + "<select feature=\"" + feature.getFeature() 
          + "\" value=\"" + feature.getValue() 
          + "\" match=\"" + feature.getCriteria() 
          + "\"" );
        if( feature.isOptional() )
        {
            writer.write( " optional=\"true\"/>" );
        }
        else
        {
            writer.write( "/>" );
        }
    }

    /**
     * Write out xml representation of the logging categories
     *
     * @param writer the writer
     * @param categories the logging categopries directive
     * @throws IOException if unable to write xml
     */
    protected void writeCategories( 
      final Writer writer, final CategoriesDirective categories, String pad )
      throws IOException
    {

        //
        // if the categoriy is null or empty then don't bother with it
        //

        if( categories == null ) return;
        if( ( categories.getPriority() == null ) 
          && ( categories.getTarget() == null )
          && ( categories.getCategories().length == 0 ) ) return;

        //
        // write out the categories directive
        //

        writer.write( "\n" + pad + "<categories" );
        if( categories.getPriority() != null )
        {
            writer.write( " priority=\"" + categories.getPriority() + "\"" );
        }
        if( categories.getTarget() != null )
        {
            writer.write( " target=\"" + categories.getTarget() + "\"" );
        }

        if( categories.getCategories().length > 0 )
        {
            writer.write( ">" );
            CategoryDirective[] cats = categories.getCategories();
            final String padding = pad + INDENT;
            for( int i=0; i<cats.length; i++ )
            {
                CategoryDirective cat = cats[i];
                if( cat instanceof CategoriesDirective )
                {
                    writeCategories( 
                      writer, (CategoriesDirective) cat, padding );
                }
                else
                {
                    writeCategoryDirective( writer, cat, padding );
                }
            }
            writer.write( "\n" + pad + "</categories>" );
        }
        else
        {
            writer.write( "/>" );
        }
    }


    /**
     * Write out xml representation of the logging categories
     *
     * @param writer the writer
     * @param category a logging category
     * @throws IOException if unable to write xml
     */
    private void writeCategoryDirective( 
      final Writer writer, final CategoryDirective category, String pad )
      throws IOException
    {
        writer.write( "\n" + pad + "<category name=\"" + category.getName() + "\"" );
        if( category.getPriority() != null )
        {
            writer.write( " priority=\"" + category.getPriority() + "\"" );
        }
        if( category.getTarget() != null )
        {
            writer.write( " target=\"" + category.getTarget() + "\"" );
        }
        writer.write( "/>" );
    }

    /**
     * Write out xml representation of the configuration
     *
     * @param writer the writer
     * @param config the type info descriptor
     * @throws IOException if unable to write xml
     */
    private void writeConfiguration( 
      final Writer writer, final Configuration config, String pad )
      throws IOException
    {
        if( !isEmptyConfiguration( config ) )
        {
            StringBuffer buffer = new StringBuffer();
            ConfigurationUtil.list( buffer, pad, config );
            writer.write( "\n" + buffer.toString() );
        }
    }

    /**
     * Write out xml representation of a context directive
     *
     * @param writer the writer
     * @param config the type info descriptor
     * @throws IOException if unable to write xml
     */
    private void writeContext( 
      final Writer writer, final ContextDirective context, String pad )
      throws IOException
    {
        if( context == null )
        {
            return;
        }

        writer.write( "\n" + pad + "<context" );
        if( context.getClassname() != null )
        {
            writer.write( " class=\"" + context.getClassname() + "\"");
        }

        EntryDirective[] entries = context.getEntryDirectives();

        if( entries.length == 0 )
        {
            writer.write( "/>" );
            return;
        }
        else
        {
            writer.write( ">");
            final String padding = pad + INDENT;

            //
            // write out the entry directives
            //

            for( int i=0; i<entries.length; i++ )
            {
                EntryDirective entry = entries[i];
                writeEntryDirective( writer, entry, padding );
            }
            writer.write( "\n" + pad + "</context>" );
        }
    }

    /**
     * Write out xml representation of a entry directive
     *
     * @param writer the writer
     * @param entry the entry directive
     * @throws IOException if unable to write xml
     */
    private void writeEntryDirective( 
      final Writer writer, final EntryDirective entry, String pad )
      throws IOException
    {
        final String padding = pad + INDENT;
        writer.write( "\n" + pad + "<entry" );
        writer.write( " key=\"" + entry.getKey() + "\">" );
        if( entry instanceof ImportDirective )
        {
            ImportDirective imp = (ImportDirective) entry;
            writer.write( "\n" + padding + "<import" );
            writer.write( " key=\"" + imp.getImportKey() + "\"" );
            writer.write( "/>" );
        }
        else if( entry instanceof ConstructorDirective  )
        {
            final String fill = padding + INDENT;
            ConstructorDirective cd = (ConstructorDirective) entry;
            writer.write( "\n" + padding + "<constructor" );
            if( !cd.getClassname().equals( "java.lang.String" ) )
            {
                writer.write( " class=\"" + cd.getClassname() + "\"" );
            }
            writer.write( ">" );
            if( cd.getParameters().length > 0 )
            {
                writeParams( writer, cd.getParameters(), fill );
                writer.write( "\n" + padding + "</constructor>" );
            }
            else
            {
                writer.write( cd.getArgument() );
                writer.write( "</constructor>" );
            }
        }
        writer.write( "\n" + pad + "</entry>" );
    }

    /**
     * Write out xml representation of a set of parameters.
     *
     * @param writer the writer
     * @param params the set of parameters
     * @throws IOException if unable to write xml
     */
    private void writeParams( 
      final Writer writer, final Parameter[] params, String pad )
      throws IOException
    {
        for( int i=0; i<params.length; i++ )
        {
            writeParam( writer, params[i], pad );
        }
    }

    /**
     * Write out xml representation of a set of a single parameter.
     *
     * @param writer the writer
     * @param param the parameter
     * @throws IOException if unable to write xml
     */
    private void writeParam( 
      final Writer writer, final Parameter param, String pad )
      throws IOException
    {
        writer.write( "\n" + pad + "<param" );
        if( param.getClassname() != null )
        {
            if( !param.getClassname().equals( "java.lang.String" ) )
            {
                writer.write( " class=\"" + param.getClassname() + "\"" );
            }
        }
        writer.write( ">" );
        Parameter[] parameters = param.getParameters();
        if( parameters.length > 0 )
        {
            final String padding = pad + INDENT;
            writeParams( writer, parameters, padding );
            writer.write( "\n" + pad + "</param>" );
        }
        else
        {
            writer.write( param.getArgument() + "</param>" );
        }
    }

    /**
     * Write out xml representation of a set of parameters
     *
     * @param writer the writer
     * @param params the parameters
     * @throws IOException if unable to write xml
     */
    private void writeParameters( 
      final Writer writer, final Parameters params, String pad )
      throws IOException, ParameterException
    {
        if( params == null )
        {
            return;
        }
        String[] names = params.getNames();
        final String padding = pad + INDENT;
        writer.write( "\n" + pad + "<parameters>" );
        for( int i=0; i<names.length; i++ )
        {
            String name = names[i];
            String value = params.getParameter( name );
            writer.write( "\n" + padding + "<parameter" );
            writer.write( " name=\"" + name + "\"" );
            writer.write( " value=\"" + value + "\"/>" );
        }
        writer.write( "\n" + pad + "</parameters>" );
    }
}
