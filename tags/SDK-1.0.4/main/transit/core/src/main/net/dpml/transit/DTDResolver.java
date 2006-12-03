/* 
 * Copyright 2004-2006 Stephen J. McConnell.
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

package net.dpml.transit;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import net.dpml.lang.StandardClassLoader;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * A Class to help to resolve Entitys for items such as DTDs or
 * Schemas.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
class DTDResolver implements EntityResolver
{
    /**
     * The list of DTDs that can be resolved by this class.
     */
    private final DTD[] m_dtds;

    /**
     * The ClassLoader to use when loading resources for DTDs.
     */
    private final ClassLoader m_classloader;

    /**
     * Construct a resolver using specified DTDInfos where resources are loaded
     * from specified ClassLoader.
     */
    public DTDResolver( final DTD[] dtds, final ClassLoader classLoader )
    {
        m_dtds = dtds;
        m_classloader = classLoader;
        
        /*
        final ClassLoader loader = getClassLoader();
        for( int i=0; i<dtds.length; i++ )
        {
            DTD dtd = dtds[i];
            final String path = dtd.getResource();
            URL url = loader.getResource( path );
            if( null == url )
            {
                if( null == ClassLoader.getSystemClassLoader().getResource( path ) )
                {
                    String stack = StandardClassLoader.toString( loader, null );
                    String sysLoaderProp = System.getProperty( "java.system.class.loader" );
                    ClassLoader sysLoader = ClassLoader.getSystemClassLoader();
                    final String error = 
                      "Unresolvable DTD resource [" 
                      + path 
                      + "]"
                      + "\nPROP: " + sysLoaderProp
                      + "\nSYSTEM: " + sysLoader
                      + "\nCCL-STACK: " + stack
                      + "\nSYS-STACK: " + StandardClassLoader.toString( sysLoader, null );
                    throw new TransitError( error );
                }
            }
        }
        */
    }

    /**
     * Resolve an entity in the XML file.
     * Called by parser to resolve DTDs.
     */
    public InputSource resolveEntity( final String publicId, final String systemId )
        throws IOException, SAXException
    {
        //System.out.println( "# PUBLIC: " + publicId );
        //System.out.println( "# SYSTEM: " + systemId );
        
        for( int i=0; i<m_dtds.length; i++ )
        {
            final DTD info = m_dtds[i];

            if( ( publicId != null && publicId.equals( info.getPublicId() ) ) 
               || ( systemId != null && systemId.equals( info.getSystemId() ) ) )
            {
                final String path = info.getResource();
                final InputStream inputStream = resolveInputStream( path );
                if( null == inputStream )
                {
                    final String error = 
                      "Matched DTD identifier returned an unresolvable resource path."
                      + "\nPublic ID: " + info.getPublicId()
                      + "\nSystem ID: " + info.getSystemId()
                      + "\nResource: '" + path + "'";
                    throw new TransitError( error );
                }
                else
                {
                    return new InputSource( inputStream );
                }
            }
        }
        return null;
    }
    
    private InputStream resolveInputStream( String path )
    {
        InputStream input = ClassLoader.getSystemClassLoader().getResourceAsStream( path );
        if( null != input )
        {
            return input;
        }
        ClassLoader context = Thread.currentThread().getContextClassLoader();
        if( null != context )
        {
            input = context.getResourceAsStream( path );
            if( null != input )
            {
                return input;
            }
        }
        if( null != m_classloader )
        {
            input = m_classloader.getResourceAsStream( path );
            if( null != input )
            {
                return input;
            }
        }
        return null;
    }

    /**
     * Return CLassLoader to load resource from.
     * If a ClassLoader is specified in the constructor use that,
     * else use ContextClassLoader unless that is null in which case
     * use the current classes ClassLoader.
     */
    private ClassLoader getClassLoader()
    {
        ClassLoader classLoader = m_classloader;
        if( null == classLoader )
        {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        if( null == classLoader )
        {
            classLoader = getClass().getClassLoader();
        }
        return classLoader;
    }
}
