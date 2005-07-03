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

package net.dpml.test.acme;

import java.util.logging.Logger;

/**
 * An example of component based implementation of a Dimension.  Unlike
 * the DimensionValue, this implementation resolves width and height parameters
 * form the supplied context.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class DimensionComponent implements Dimension
{
    //------------------------------------------------------------------
    // state
    //------------------------------------------------------------------

    private final Logger m_logger;
    private final Context m_context;

    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------

    public DimensionComponent( Logger logger, Context context )
    {
        m_context = context;
        m_logger = logger;
        m_logger.fine( "available" );
    }

    //------------------------------------------------------------------
    // Dimension
    //------------------------------------------------------------------

    public int getWidth()
    {
        return m_context.getWidth( 1024 );
    }

    public int getHeight()
    {
        return m_context.getHeight( 512 );
    }

    public int getSize()
    {
        synchronized( m_context )
        {
            int x = getWidth();
            int y = getHeight();
            int result = multiply( x, y );
            m_logger.fine( "" + x + " x " + y + " = " + result );
           return result;
        }
    }

    private int multiply( int x, int y )
    {
        int result = 0; 
        if( 1 == x )
        { 
           return y;
        }
        else if( 1 == y )
        {
           return x;
        }
        else if( ( x % 2) == 0 ) 
        {
           // even number
           int m = x/2; 
           int subtotal = multiply( m, y ); 
           return subtotal + subtotal;
        } 
        else 
        { 
           // odd number
           return y + multiply( x-1, y );
        }        
    }

    public interface Context extends DimensionalContext
    {
    }
}
