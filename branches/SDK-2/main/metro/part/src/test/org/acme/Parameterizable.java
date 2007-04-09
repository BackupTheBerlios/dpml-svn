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

package org.acme;

import java.net.URI;
import java.awt.Color;

/**
 * Sample object used in test case.  Contains a number of inner context classes
 * used to evaluate strict and non-strict context evaluation strategies and 
 * general part instantiation.
 */
public class Parameterizable
{
    private String m_name;
    private int m_id;
    
    public Parameterizable( String name, int id )
    {
        m_name = name;
        m_id = id;
    }
    
    public String getName()
    {
        return m_name;
    }
    
    public int getID()
    {
        return m_id;
    }
}