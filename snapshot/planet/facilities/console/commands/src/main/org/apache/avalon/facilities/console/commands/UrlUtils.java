/*
 * Copyright 1997-2004 Apache Software Foundation
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
package org.apache.avalon.facilities.console.commands;

import java.io.File;
import java.io.IOException;

import java.net.URL;
import java.net.MalformedURLException;

import org.apache.avalon.facilities.console.CommandException;

import org.apache.avalon.repository.ArtifactHandler;
import org.apache.avalon.repository.BlockHandler;

public class UrlUtils
{
    static URL resolveURL( File base, String value )
        throws Exception
    {
        if( value.startsWith( "block:" ) )
        {
            return blockSpecToURL( value );
        }
        else if( value.startsWith( "artifact:" ) )
        {
            return artifactSpecToURL( value );
        }

        try
        {
            return new URL( value );
        }
        catch( Exception e )
        {
            File target = new File( value );
            if( target.exists() )
            {
                return toURL( target );
            }
            else
            {
                target = new File( base, value );
                if( target.exists() )
                {
                    return toURL( target );
                }
                else
                {
                    throw new CommandException( "Unable to resolve URL: " + value );
                }
            }
        }
    }

    static URL toURL( File file )
        throws MalformedURLException, IOException
    {
        return file.getCanonicalFile().toURL();
    }

    static URL blockSpecToURL( String spec )   
        throws MalformedURLException
    {
        return new URL( null, spec, new BlockHandler() );
    }

    static URL artifactSpecToURL( String spec )
        throws MalformedURLException
    {
        return new URL( null, spec, new ArtifactHandler() );
    }
}
