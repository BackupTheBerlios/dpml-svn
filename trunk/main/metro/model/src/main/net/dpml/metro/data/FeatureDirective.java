/*
 * Copyright 2004 Stephen J. McConnell.
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

package net.dpml.metro.data;

import java.net.URI;

import net.dpml.part.Directive;

/**
 * A <code>FeatureDirective</code> declares a context entry that is itself a feature
 * of the component including the component uri, name, working directory, or temporary 
 * directory.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class FeatureDirective extends AbstractDirective implements Directive
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

   /**
    * Constant identifier for the component name feature.
    */
    public static final int NAME = 0;

   /**
    * Constant identifier for the component uri feature.
    */
    public static final int URI = 1;

   /**
    * Constant identifier for the component working directory feature.
    */
    public static final int WORK = 2;

   /**
    * Constant identifier for the component temporary directory feature.
    */
    public static final int TEMP = 3;

   /**
    * Return the feature id givcen a supplied name.
    * @param value the feature name
    * @return the feature id
    */
    public static int getFeatureForName( String value )
    { 
        if( "name".equals( value ) )
        {
            return NAME;
        }
        else if( "uri".equals( value ) )
        {
            return URI;
        }
        else if( "work".equals( value ) )
        {
            return WORK;
        }
        else if( "temp".equals( value ) )
        {
            return TEMP;
        }
        else
        {
            final String error = 
              "Feature not recognized [" + value + "].";
            throw new IllegalArgumentException( error );
        }
    }

    //--------------------------------------------------------------------------
    // state
    //--------------------------------------------------------------------------

    private final String m_key;
    private final int m_feature;

    //--------------------------------------------------------------------------
    // constructors
    //--------------------------------------------------------------------------

   /**
    * Create a new feature directive.
    * @param key the feature name
    * @param feature the feasture id
    */
    public FeatureDirective( String key, int feature )
    {
        if( null == key )
        {
            throw new NullPointerException( "key" );
        }
        m_key = key;
        m_feature = feature;
    }

    //--------------------------------------------------------------------------
    // FeatureDirective
    //--------------------------------------------------------------------------

   /**
    * Return the feature key.
    * @return the feature key
    */
    public String getKey()
    {
        return m_key;
    }

   /**
    * Return the feature id.
    * @return the feature id
    */
    public int getFeature()
    {
        return m_feature;
    }

   /**
    * Test if the supplied object is equal to this object.
    * @param other the object to compare with this instance
    * @return TRUE if the supplied object is equal to this object
    */
    public boolean equals( Object other )
    {
        if( null == other )
        { 
            return false;
        }
        else
        {
            FeatureDirective feature = (FeatureDirective) other;
            if( !m_key.equals( feature.getKey() ) )
            {
                return false;
            }
            else
            {
                return m_feature == feature.getFeature();
            }
        }
    }

   /**
    * Return the hashcode for the instance.
    * @return the instance hashcode
    */
    public int hashCode()
    {
        int hash = m_key.hashCode();
        hash ^= m_feature;
        return hash;
    }
}
