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

package org.apache.avalon.composition.data.builder;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.avalon.composition.data.ContainmentProfile;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.meta.ConfigurationBuilder;
import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

import org.xml.sax.InputSource;


/**
 * A ContainmentProfileBuilder is responsible for building {@link ContainmentProfile}
 * objects from a configuration object.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: ContainmentProfileBuilder.java 30977 2004-07-30 08:57:54Z niclas $
 */
public final class ContainmentProfileBuilder implements ContainmentProfileCreator
{

    private static final Resources REZ =
        ResourceManager.getPackageResources( ContainmentProfileBuilder.class );

    private XMLContainmentProfileCreator m_xml = 
      new XMLContainmentProfileCreator( );

    private final SerializedContainmentProfileCreator m_serial =
      new SerializedContainmentProfileCreator();

    /**
     * Create a {@link ContainmentProfile} from a stream.
     *
     * @param inputStream the stream that the resource is loaded from
     * @return the containment profile
     * @exception Exception if a error occurs during profile creation
     */
    public ContainmentProfile createContainmentProfile( InputStream inputStream )
        throws Exception
    {
        // we backup the inputstream content in a bytearray
        final byte[] buffer = new byte[1024];
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for( int read = 0; read >= 0; )
        {
            baos.write( buffer, 0, read );
            read = inputStream.read( buffer );
        }

        inputStream = new ByteArrayInputStream( baos.toByteArray() );
        
        try
        {
            final ContainmentProfile profile = 
              buildFromSerDescriptor( inputStream);
            if( null != profile )
            {
                return profile;
            }
        }
        catch( Throwable e )
        {
            // exception case here is not clear - basically we get a 
            // java.io.StreamCorruptedException if wthe input stream is 
            // referencing an XML stream - for the moment just go ahead
            // and see if we can resolve the source as an XML input but 
            // sooner or later we will need to update the serialized 
            // to return null if the source isn' a serialized source

            inputStream = new ByteArrayInputStream( baos.toByteArray() );

        }
        
        return buildFromXMLDescriptor( inputStream );
    }

    /**
     * Build ContainmentProfile from the serialized format.
     *
     * @throws Exception if an error occurs
     */
    private ContainmentProfile buildFromSerDescriptor( InputStream inputStream )
        throws Exception
    {
        return m_serial.createContainmentProfile( inputStream );
    }

    /**
     * Build ContainmentProfile from an XML descriptor.
     * @param stream the input stream
     * @throws Exception if an error occurs
     */
    private ContainmentProfile buildFromXMLDescriptor( InputStream stream )
        throws Exception
    {
        final InputSource source = new InputSource( stream );
        Configuration config = ConfigurationBuilder.build( source );
        return m_xml.createContainmentProfile( config );
    }

}
