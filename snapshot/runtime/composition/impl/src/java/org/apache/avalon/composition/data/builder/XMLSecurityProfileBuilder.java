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

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.avalon.composition.data.SecurityProfile;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.avalon.meta.info.PermissionDescriptor;

/**
 * <p>Implementation of the default security builder.</p>
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: XMLSecurityProfileBuilder.java 30977 2004-07-30 08:57:54Z niclas $
 */
public final class XMLSecurityProfileBuilder
{
    //-------------------------------------------------------------------
    // SecurityProfileBuilder
    //-------------------------------------------------------------------

   /**
    * Utility method to construct set of {@link SecurityProfile} instances from a 
    * supplied &lt;security&gt; configuration.  The security configuration may 
    * contain one or more named security profiles. 
    * <pre>
    *   &lt;security&gt;
    *     &lt;profile name="default"&gt;
    *       &lt;permissions&gt;
    *         &lt;permission class="java.util.PropertyPermission"
    *            name="user.home"&gt;
    *           &lt;action>read&lt;/action&gt;
    *           &lt;action>write&lt;/action&gt;
    *         &lt;/permission&gt;
    *       &lt;/permissions&gt;
    *     &lt;/profile&gt;
    *   &lt;/security&gt;
    * </pre>
    *
    * @param config the security manager configuration
    * @return a new security manager
    */
    public SecurityProfile[] createSecurityProfiles( Configuration config )
      throws Exception
    {
        Configuration[] configs = config.getChildren( "profile" );
        SecurityProfile[] profiles = new SecurityProfile[ configs.length ]; 
        for( int i=0; i<configs.length; i++ )
        {
            Configuration conf = configs[i];
            SecurityProfile profile = createSecurityProfile( conf );
            profiles[i] = profile;
        }
        return profiles;
    }

   /**
    * Creation of a single security profile from a configuration.
    * @param config the profile configuration
    * @return the security profile
    */
    public SecurityProfile createSecurityProfile( Configuration config )
      throws Exception
    {
        final String name = config.getAttribute( "name" );
        Configuration conf = config.getChild( "permissions" );
        PermissionDescriptor[] permissions = buildPermissions( conf );
        return new SecurityProfile( name, permissions );
    }

    //-------------------------------------------------------------------
    // implementation
    //-------------------------------------------------------------------

   /**
    * Creation of set of permission descriptors from a configuration.
    * @param config the configuration fragment holding a set of child
    *   permission elements
    * @return a set of permission descriptors
    */
    private PermissionDescriptor[] buildPermissions( final Configuration config )
      throws ConfigurationException
    {
        ArrayList list = new ArrayList();
        Configuration[] children = config.getChildren( "permission" );
        PermissionDescriptor[] permissions = 
          new PermissionDescriptor[ children.length ];
        for( int i = 0; i < children.length; i++ )
        {			
            PermissionDescriptor permission = buildPermission( children[i] );
            permissions[i] = permission;
        }
        return permissions;
    }

   /**
    * Creation of a single permission descriptor from a configuration.
    * @param config a confuragion element descibing a permission
    * @return the permission descriptor
    */
    private PermissionDescriptor buildPermission( final Configuration config ) 
      throws ConfigurationException
    {
        final String classname = config.getAttribute( "class" );
        final String name = config.getAttribute( "name", null );
        ArrayList list = new ArrayList();
        Configuration[] children = config.getChildren( "action" );
        String[] actions = new String[ children.length ];
        for( int i=0; i<children.length; i++ )
        {
            Configuration child = children[i];
            String action = child.getValue();
            actions[i] = action;
        }
        return new PermissionDescriptor( classname, name, actions );
    }

    private Certificate[] createCertificates( Configuration config ) 
      throws Exception
    {
        ArrayList list = new ArrayList();
        Configuration[] children = config.getChildren();
        for( int i=0; i<children.length; i++ )
        {
            Configuration child = children[i];
            String name = child.getName();
            if( name.equals( "pkcs7" ) )
            {
                Certificate[] certs = 
                  createPKCS7( child );
                for( int j=0; j<certs.length; j++ )
                {
                    list.add( certs[j] );
                }
            }
            else if( name.equals( "x509" ) )
            {
                Certificate[] certs = 
                  createX509( child );
                for( int j=0; j<certs.length; j++ )
                {
                    list.add( certs[j] );
                }
            }
            else
            {
                final String error =
                  "Unrecognized certificate type [" + name + "].";
                throw new ConfigurationException( error );
            }
        }
        return (Certificate[]) list.toArray( new Certificate[0] );
    }

   /**
    * Utility method to construct a PKCS7 certificate set from a 
    * supplied configuration.
    *
    * @param config a configuration describing the PKCS7 certificate
    */
    private Certificate[] createPKCS7( Configuration config ) throws Exception
    {
        String href = config.getAttribute( "href" );
        InputStream in = null;
        try
        {
            URL url = new URL( href );
            in = url.openStream();

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Collection certs = cf.generateCertificates(in);
            Certificate[] certificates = new Certificate[ certs.size() ];
            return (Certificate[]) certs.toArray( certificates );
        } 
        finally
        {
            if( in != null ) in.close();
        }
    }

   /**
    * Utility method to construct a X509 certificate set for a 
    * supplied configuration.
    *
    * @param config a configuration describing the PKCS7 certificate
    */
    private Certificate[] createX509( Configuration config ) 
      throws ConfigurationException, CertificateException, IOException
    {
        String href = config.getAttribute( "href", "" );
        String data = config.getValue();

        InputStream in = null;
        try
        {
            if( href == null || "".equals( href ) )
            {
                in = new ByteArrayInputStream( data.getBytes("UTF-8") );
            }
            else
            {
                URL url = new URL( href );
                in = url.openStream();
            }
            CertificateFactory cf = CertificateFactory.getInstance( "X.509" );
            Collection certs = cf.generateCertificates( in );
            Certificate[] certificates = new Certificate[ certs.size() ];
            return (Certificate[]) certs.toArray( certificates );
        } 
        finally
        {
            if( in != null ) in.close();
        }
    }
}
