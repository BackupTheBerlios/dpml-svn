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

package org.apache.avalon.meta.info.builder.tags;

import java.util.Properties;

import org.apache.avalon.framework.Version;
import org.apache.avalon.meta.info.ContextDescriptor;
import org.apache.avalon.meta.info.DependencyDescriptor;
import org.apache.avalon.meta.info.ExtensionDescriptor;
import org.apache.avalon.meta.info.InfoDescriptor;
import org.apache.avalon.meta.info.CategoryDescriptor;
import org.apache.avalon.meta.info.ServiceDescriptor;
import org.apache.avalon.meta.info.StageDescriptor;
import org.apache.avalon.meta.info.SecurityDescriptor;
import org.apache.avalon.meta.info.Type;

import com.thoughtworks.qdox.model.DocletTag;
import com.thoughtworks.qdox.model.JavaClass;

/**
 * <p>A doclet tag the declares a a {@link Type} descriptor using the
 * <code>&amp;avalon.type</code> or <code>&amp;avalon.component</code>
 * javadoc tag.  The &amp;avalon.component is senonomouse with &amp;avalon.type.
 * The &amp;avalon.type tag recognizes the following attributes:<p>
 *
 * <table border="1" cellpadding="3" cellspacing="0" width="100%">
 * <tr bgcolor="#CCCCFF" class="TableHeadingColor">
 *   <td colspan="3"><p><b>Tag Attributes</b></p></td>
 * </tr>
 * <tr bgcolor="white" class="TableRowColor">
 *   <td>name</td><td>required</td>
 *   <td>The name of the component type.</td></tr>
 * <tr bgcolor="white" class="TableRowColor">
 *   <td>version</td><td>optional</td>
 *   <td>A version identifier in the format [major].[minor].[micro].
 *       If not supplied the version 1.0.0 will be assumed.</td></tr>
 * <tr bgcolor="white" class="TableRowColor">
 *   <td>lifestyle</td><td>optional</td>
 *   <td>The component implementation lifestyle - one of "singleton",
 *       "thread", "pooled", or "transient".  If not supplied "transient"
 *       is assumed.</td></tr>
 * </table>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: TypeTag.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class TypeTag extends AbstractTag
{
   /**
    * Javadoc tag key for the type tag.
    */
    public static final String KEY = "component";

   /**
    * The name parameter
    */
    public static final String NAME_PARAM = "name";

   /**
    * The version parameter
    */
    public static final String VERSION_PARAM = "version";

   /**
    * The lifestyle parameter
    */
    public static final String LIFESTYLE_PARAM = "lifestyle";

   /**
    * The lifestyle collection policy parameter
    */
    public static final String LIFESTYLE_COLLECTION_PARAM = "collection";

   /**
    * Type tag constructor.
    * @param clazz the javadoc class descriptor
    */
    public TypeTag( final JavaClass clazz )
    {
        super( clazz );
    }

   /**
    * Return the value of the Avalon 'component' tag.
    * @return the name of the component type
    * @exception IllegalArgumentException if the name tag does not contain a value
    */
    public Type getType()
    {
        DocletTag tag = getJavaClass().getTagByName( getNS() + Tags.DELIMITER + KEY );
        if( null == tag )
        {
            return null;
        }

        final String name = getName( tag );
        final Version version = getVersion( tag );
        final String lifestyle = getLifestyle( tag );
        final String collection = getLifestyleCollectionPolicy( tag );
        final String type = getJavaClass().getFullyQualifiedName();
        final Properties properties = new AttributeTag( getJavaClass() ).getProperties();
        final String schema = new SchemaTag( getJavaClass() ).getConfigurationSchema();
        final InfoDescriptor info = 
          new InfoDescriptor( 
            name, type, version, lifestyle, collection, schema, properties );
        final SecurityDescriptor security = 
          new SecurityTag( getJavaClass() ).getSecurityDescriptor();
        final ServiceDescriptor[] services = new ServicesTag( getJavaClass() ).getServices();
        final CategoryDescriptor[] loggers = new LoggerTag( getJavaClass() ).getCategories();
        final DependencyDescriptor[] dependencies =
          new DependencyTag( getJavaClass() ).getDependencies();
        final StageDescriptor[] stages = new StageTag( getJavaClass() ).getStages();
        final ExtensionDescriptor[] extensions = new ExtensionTag( getJavaClass() ).getExtensions();
        final ContextDescriptor context = new ContextTag( getJavaClass() ).getContext();

        return new Type( 
          info, security, loggers, context, services, dependencies, 
          stages, extensions, null );
    }

    private String getName(DocletTag tag)
    {
        return getNamedParameter( tag, NAME_PARAM );
    }

    private String getLifestyle(DocletTag tag)
    {
        return getNamedParameter( tag, LIFESTYLE_PARAM, null );
    }

    private String getLifestyleCollectionPolicy(DocletTag tag)
    {
        return getNamedParameter( tag, LIFESTYLE_COLLECTION_PARAM, null );
    }

    private Version getVersion(DocletTag tag)
    {
        return Version.getVersion( getNamedParameter( tag, VERSION_PARAM, "1.0" ) );
    }
}
