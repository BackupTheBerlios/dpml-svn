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

/**
 * An example of a multi-parameter class used as a constructed context entry.
 * The class takes two int values as constructed arguments and proves a service 
 * of multiplying the two numbers together. The purpose of this class is simply
 * to demonstrate how foreign classes can be easily included into a component 
 * context.
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 */
public class DimensionValue implements Dimension
{
    private int m_x;
    private int m_y;

    public DimensionValue( int x, int y )
    {
        m_x = x;
        m_y = y;
    }

    public int getWidth()
    {
        return m_x;
    }

    public int getHeight()
    {
        return m_y;
    }

    public int getSize()
    {
        return multiply( m_x, m_y );
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
}
