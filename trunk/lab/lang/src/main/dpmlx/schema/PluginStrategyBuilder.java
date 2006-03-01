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

package dpmlx.schema;

import java.io.Serializable;
import java.net.URI;

import dpmlx.lang.Strategy;
import dpmlx.lang.StrategyBuilder;

import net.dpml.transit.util.ElementHelper;

import org.w3c.dom.Element;

/**
 * Utility used to build a plugin strategy from a DOM element.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class PluginStrategyBuilder implements StrategyBuilder
{
    public Strategy buildStrategy( Element element ) throws Exception
    {
        String name = element.getTagName();
        if( "class".equals( name ) )
        {
            String classname = ElementHelper.getValue( element );
            return new ClassStrategy( classname );
        }
        else if( "resource".equals( name ) )
        {
            Element urnElement = ElementHelper.getChild( element, "urn" );
            Element pathElement = ElementHelper.getChild( element, "path" );
            String urn = ElementHelper.getValue( urnElement );
            String path = ElementHelper.getValue( pathElement );
            Resource resource = new Resource( urn, path );
            return new ResourceStrategy( resource );
        }
        else
        {
            final String error = 
              "Element [" + name + "] is not recognized.";
            throw new IllegalArgumentException( error );
        }
    }
    
   /**
    * Handle the deployment of a part.
    *
    * @param classloader the classloader
    * @param args supplimentary deployment arguments
    * @exception Exception if an error occurs
    */
    /*
    public Object getInstance( Object[] args ) throws Exception
    {
        if( m_strategy instanceof ClassStrategy )
        {
            ClassStrategy strategy = (ClassStrategy) m_strategy;
            Class c = strategy.getPluginClass( m_classloader );
            Repository repository = Transit.getInstance().getRepository();
            repository.instantiate( c, args );
        }
        else
        {
            final String error = 
              "Resource instantiation not supported.";
            throw new UnsupportedOperationException( error );
        }
    }
    */
    
    private abstract static class PluginStrategy extends Strategy
    {
        PluginStrategy( URI uri, Serializable data ) throws Exception
        {
            super( uri, data );
        }
    }
    
    private static class ClassStrategy extends PluginStrategy
    {
        ClassStrategy( String classname ) throws Exception
        {
            super( new URI( "plugin:class" ), classname );
        }
        
        public String getClassname()
        {
            return (String) super.getDeploymentData();
        }
        
        public Class getPluginClass( ClassLoader classloader ) throws ClassNotFoundException
        {
            String classname = getClassname();
            return classloader.loadClass( classname );
        }
    }
    
    private static class ResourceStrategy extends PluginStrategy
    {
        ResourceStrategy( Resource resource ) throws Exception
        {
            super( new URI( "plugin:resource" ), resource );
        }
        
        public Resource getResource()
        {
            return (Resource) super.getDeploymentData();
        }
    }
    
    private static class Resource implements Serializable
    {
        private final String m_urn;
        private final String m_path;
        
        public Resource( String urn, String path )
        {
            m_urn = urn;
            m_path = path;
        }
        
        public boolean equals( Object other )
        {
            if( null == other )
            {
                return false;
            }
            else if( other instanceof Resource )
            {
                Resource resource = (Resource) other;
                if( !m_urn.equals( resource.m_urn ) )
                {
                    return false;
                }
                else
                {
                    return m_path.equals( resource.m_path );
                }
            }
            else
            {
                return false;
            }
        }
        
        public int hashCode()
        {
            int hash = m_urn.hashCode();
            hash ^= m_path.hashCode();
            return hash;
        }
    }
}
