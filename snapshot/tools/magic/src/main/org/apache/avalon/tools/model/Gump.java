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

package org.apache.avalon.tools.model;

/**
 * An immutable data object holding supplimentary information required for
 * the creation fo gump project defintions.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Revision: 1.2 $ $Date: 2004/03/17 10:30:09 $
 */
public class Gump 
{
    public static final Gump NULL_GUMP = new Gump( null, null, false, false );

    private String m_alias;
    private String m_id;
    private boolean m_ignore;
    private boolean m_classpath;

   /**
    * Creation of a new gump supplementation definition.
    * @param alias a name used by gump when referring to a magic key
    * @param id a buuild id used by gump when referencing projects that declare 
    *    multiple jar file deliverables
    * @param classpath if TRUE then the defintion to which this object is associated will
    *   referenced as a gump classpath reference
    * @param ignore if TRUE magic can ignore this project when generating a gump project 
    *   descriptor
    */
    public Gump( final String alias, final String id, boolean classpath, boolean ignore )
    {
        m_alias = alias;
        m_id = id;
        m_ignore = ignore;
        m_classpath = classpath;
    }

   /**
    * Gump alias name.
    * @return the alias name
    */
    public String getAlias()
    {
        return m_alias;
    }

   /**
    * Gump project jar id.
    * @return the gump jar id
    */
    public String getId()
    {
        return m_id;
    }

   /**
    * Return true if this defintion can be ignored when 
    * building a gump project dependency.
    * @return the ignorable status
    */
    public boolean isIgnorable()
    {
        return m_ignore;
    }

   /**
    * Return true if this defintion is required as part of the 
    * classpath established by gump.
    * @return TRUE if this is a classpath entry
    */
    public boolean isClasspathEntry()
    {
        return m_classpath;
    }
}
