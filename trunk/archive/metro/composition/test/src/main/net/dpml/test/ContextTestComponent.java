/*
 * Copyright 2005 Stephen J. McConnell.
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

package net.dpml.test;

import java.awt.Color;
import java.io.File;
import java.net.URI;

/**
 * Component used for context entry testing. 
 * 
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class ContextTestComponent
{
    //------------------------------------------------------------------
    // concerns
    //------------------------------------------------------------------

    public interface Context
    {
        //
        // constructed value
        //
        Color getColor();
        Color getOptionalColor( Color color );
        
        //
        // primitive value
        //
        
        int getInteger();
        int getOptionalInteger( int value );
        short getShort();
        short getOptionalShort( short value );
        long getLong();
        long getOptionalLong( long value );
        byte getByte();
        byte getOptionalByte( byte value );
        double getDouble();
        double getOptionalDouble( double value );
        float getFloat();
        float getOptionalFloat( float value );
        char getChar();
        char getOptionalChar( char value );
        boolean getBoolean();
        boolean getOptionalBoolean( boolean flag );
        
        //
        // symbolic values
        //
        
        File getFile();
        File getFile( File value );
        File getOptionalFile( File value );
        File getTempFile();
        
        URI getURI();
        URI getOptionalURI( URI value );
        String getName();
        String getPath();
    }

    private final Context m_context;
    
    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------

    public ContextTestComponent( final Context context ) throws Exception
    {
        m_context = context;
    }
    
    public Context getContext() // for testcase
    {
        return m_context;
    }
    
}
