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
package org.apache.avalon.composition.data;

import java.io.Serializable;

/**
 * A SelectionDirectivecontains information describing how a 
 * depedency should be resolved relative to a set of supplied
 * features and qualified values.  
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: SelectionDirective.java 30977 2004-07-30 08:57:54Z niclas $
 */
public final class SelectionDirective implements Serializable
{
    public static final String EXISTS = "exists";
    public static final String EQUALS = "equals";
    public static final String INCLUDES = "includes";

   /**
    * The feature name.
    */
    private final String m_feature;

   /**
    * The value attributed to the feature selection criteria.
    */
    private final String m_value;

   /**
    * The criteria to be applied with respect to the feature criteria.
    */
    private final String m_criteria;

   /**
    * The optional status of the selection directive.
    */
    private final boolean m_optional;

   /**
    * Creation of a new dependency directive.
    * 
    * @param feature the selection feature
    * @param value the value to asses
    * @param criteria the selection criteria
    * @param optional the optional status
    */
    public SelectionDirective( String feature, String value, String criteria, boolean optional )
    {
        m_feature = feature;
        m_value = value;
        m_criteria = criteria;
        m_optional = optional;
    }

   /**
    * Return the feature name.
    * @return the name
    */
    public String getFeature()
    {
        return m_feature;
    }

   /**
    * Return the feature value.
    * @return the name
    */
    public String getValue()
    {
        return m_value;
    }

   /**
    * Return the feature selection criteria.
    * @return the criteria
    */
    public String getCriteria()
    {
        return m_criteria;
    }

   /**
    * Return the required status of this directive.
    * @return the required status
    */
    public boolean isRequired()
    {
        return !m_optional;
    }

   /**
    * Return the optional status of this directive. This 
    * is equivalent to !isRequired()
    * @return the optional status
    */
    public boolean isOptional()
    {
        return m_optional;
    }

}
