/*
 *     Copyright 2004. The Apache Software Foundation.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 *  
 */
package org.apache.metro.studio.eclipse.core.environment;

import java.util.Hashtable;


/**
 * @author <a href="mailto:dev@avalon.apache.org">Metro Development Team</a>
 */
public class ModelObject
{

    private Hashtable table;
    
    public ModelObject()
    {
        super();
        table = new Hashtable();
    }
    
    public void put( Object key, Object value )
    {
        table.put( key, value );
    }
    
    public boolean hasOption( String option )
    {
        return table.containsKey( option );
    }
    
    public String getOptionValue( String option )
    {
        return (String) table.get( option );
    }
    
    public String[] getArgs()
    {
        return null;
    }
}
