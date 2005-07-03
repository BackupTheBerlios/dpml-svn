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

package net.dpml.magic.tasks;

import java.io.File;
import java.net.URL;

import net.dpml.transit.artifact.Handler;

import org.apache.tools.ant.taskdefs.XSLTProcess;
import org.apache.tools.ant.BuildException;


public class StyleTask extends XSLTProcess
{
    public void setStyle( String uri )
    {
        if( !uri.startsWith( "artifact:" ) )
        {
            super.setStyle( uri );
        }

        try
        {
            URL url = new URL( null, uri, new Handler() );
            File local = (File) url.getContent( new Class[]{File.class} );
            super.setStyle( local.getAbsolutePath() );
        }
        catch( Throwable e )
        {
            final String error = 
              "Could not resolve the uri ["
              + uri 
              + "]";
            throw new BuildException( error, e );
        }
    }
}