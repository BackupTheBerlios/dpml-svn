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

import org.apache.avalon.composition.data.Targets;
import org.apache.avalon.composition.data.builder.TargetsCreator;

import org.apache.avalon.framework.configuration.Configuration;

import org.apache.avalon.meta.ConfigurationBuilder;

import org.apache.avalon.util.i18n.ResourceManager;
import org.apache.avalon.util.i18n.Resources;

import org.xml.sax.InputSource;


/**
 * A TargetsBuilderis responsible for building {@link Targets}
 * objects from a configuration object.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: TargetsBuilder.java 30977 2004-07-30 08:57:54Z niclas $
 */
public final class TargetsBuilder implements TargetsCreator
{

    private static final Resources REZ =
        ResourceManager.getPackageResources( TargetsBuilder.class );

    private XMLTargetsCreator m_xml = 
      new XMLTargetsCreator( );

    private final SerializedTargetsCreator m_serial =
      new SerializedTargetsCreator();


    /**
     * Create a {@link Targets} instance from a stream.
     *
     * @param inputStream the stream that the resource is loaded from
     * @return the target directive
     * @exception Exception if a error occurs during directive creation
     */
    public Targets createTargets( InputStream inputStream )
        throws Exception
    {
        try
        {
            final Targets directive = buildFromSerDescriptor( inputStream);
            if( null != directive )
            {
                return directive;
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
        }

        return buildFromXMLDescriptor( inputStream );
    }

    /**
     * Build Targets from the serialized format.
     *
     * @throws Exception if an error occurs
     */
    private Targets buildFromSerDescriptor( InputStream inputStream )
        throws Exception
    {
        return m_serial.createTargets( inputStream );
    }

    /**
     * Build Targets from an XML descriptor.
     *
     * @throws Exception if an error occurs
     */
    private Targets buildFromXMLDescriptor( InputStream inputStream )
        throws Exception
    {
        final InputSource inputSource = new InputSource( inputStream );
        Configuration config = ConfigurationBuilder.build( inputSource );
        return m_xml.createTargets( config );
    }
}
