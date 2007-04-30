/*
 * Copyright 2006 Stephen J. McConnell.
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

package net.dpml.lang;

import dpml.lang.ValueDecoder;
import dpml.lang.Classpath;
import dpml.util.StandardClassLoader;
import dpml.util.Category;
import dpml.util.DefaultLogger;

import java.io.IOException;
import java.net.URI;

import net.dpml.util.Logger;

/**
 * Interace implemented by part strategy handlers.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
final class ClassLoaderHelper
{
    private ClassLoaderHelper()
    {
        // disabled
    }
    
    private static final ValueDecoder VALUE_DECODER = new ValueDecoder();
    
    private static final Logger LOGGER = new DefaultLogger( "dpml.lang" );
    
   /**
    * Create a classloader given a classpath defintion.
    * @param anchor the parent classloader
    * @param classpath the classpath definition
    * @return the new classloader
    */
    public static ClassLoader newClassLoader( ClassLoader anchor, URI codebase, Classpath classpath ) throws IOException
    {
        if( null == anchor )
        {
            throw new NullPointerException( "anchor" );
        }
        //ClassLoader anchor = getAnchorClassLoader( parent );
        String spec = codebase.toASCIIString();
        return newClassLoader( anchor, classpath, spec, true );
    }
    
    private static ClassLoader newClassLoader( 
      ClassLoader base, Classpath classpath, String spec, boolean expand ) throws IOException
    {
        if( expand )
        {
            Classpath cp = classpath.getBaseClasspath();
            if( null != cp )
            {
                String label = spec + " (base)";
                ClassLoader cl = newClassLoader( base, cp, label, true );
                return newClassLoader( cl, classpath, spec, false );
            }
        }
        
        URI[] uris = classpath.getDependencies( Category.SYSTEM );
        if( uris.length > 0 )
        {
            for( URI uri : uris )
            {
                LOGGER.warn( "Ignoring system reference: " + uri );
            }
            //updateSystemClassLoader( uris );
        }
        
        URI[] apis = classpath.getDependencies( Category.PUBLIC );
        ClassLoader api = 
          StandardClassLoader.buildClassLoader( LOGGER, spec, Category.PUBLIC, base, apis );
        URI[] spis = classpath.getDependencies( Category.PROTECTED );
        ClassLoader spi = 
          StandardClassLoader.buildClassLoader( LOGGER, spec, Category.PROTECTED, api, spis );
        URI[] imps = classpath.getDependencies( Category.PRIVATE );
        return StandardClassLoader.buildClassLoader( LOGGER, spec, Category.PRIVATE, spi, imps );
    }

    /*
    private static void updateSystemClassLoader( URI[] uris ) throws IOException
    {
        ClassLoader parent = ClassLoader.getSystemClassLoader();
        synchronized( parent )
        {
            if( parent instanceof SystemClassLoader )
            {
                SystemClassLoader loader = (SystemClassLoader) parent;
                loader.addDelegates( uris );
            }
            else
            {
                final String message =
                  "Cannot load [" 
                  + uris.length 
                  + "] system artifacts into a foreign system classloader.";
                LOGGER.trace( message );
            }
        }
    }
    */
    
    /*
    private static ClassLoader getAnchorClassLoader( ClassLoader parent )
    {
        if( null != parent )
        {
            return parent;
        }
        else
        {
            ClassLoader current = Thread.currentThread().getContextClassLoader();
            if( null != current )
            {
                return current;
            }
            else
            {
                return ClassLoaderHelper.class.getClassLoader();
            }
        }
    }
    */
}
