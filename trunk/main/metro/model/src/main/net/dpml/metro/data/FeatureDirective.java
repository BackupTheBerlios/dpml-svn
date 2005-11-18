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

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

import net.dpml.metro.part.Part;

/**
 * A <code>FeatureDirective</code> declares a context entry that is itself a feature
 * of the component including the component uri, name, working directory, or temporary 
 * directory.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class FeatureDirective extends AbstractDirective
{
   /**
    * Serial version identifier.
    */
    static final long serialVersionUID = 1L;

    public static int NAME = 0;
    public static int URI = 1;
    public static int WORK = 2;
    public static int TEMP = 3;

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

    public String getKey()
    {
        return m_key;
    }

    public int getFeature()
    {
        return m_feature;
    }

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

    public int hashCode()
    {
        int hash = m_key.hashCode();
        hash ^= m_feature;
        return hash;
    }
}
