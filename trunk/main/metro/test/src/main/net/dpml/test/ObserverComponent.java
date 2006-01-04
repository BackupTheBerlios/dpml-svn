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
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.LinkedList;

import net.dpml.logging.Logger;

/**
 * Component implementation that demonstrates the use of a context inner-class. 
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class ObserverComponent implements ColorManager, PropertyChangeListener
{
    //------------------------------------------------------------------
    // concerns
    //------------------------------------------------------------------
    
   /**
    * Component driven context criteria specification.
    */
    public interface Context
    {
       /**
        * Return a non-optional color value.
        * @return the color
        */
        Color getColor();
        
       /**
        * Add a property change listener to the context object.
        * @param listener the property change listener
        */
        void addPropertyChangeListener( PropertyChangeListener listener );
        
       /**
        * Remove a property change listener from the context object.
        * @param listener the property change listener
        */
        void removePropertyChangeListener( PropertyChangeListener listener );
    }

    //------------------------------------------------------------------
    // state
    //------------------------------------------------------------------

   /**
    * The logging channel.
    */
    private final Logger m_logger;

   /**
    * The assigned context instance.
    */
    private final Context m_context;

   /**
    * Internal list of events we receive.
    */
    private final List m_list = new LinkedList();

    //------------------------------------------------------------------
    // constructor
    //------------------------------------------------------------------

   /**
    * Creation of a new <tt>ExampleComponent</tt> using a supplied 
    * logging channel and context.
    * 
    * @param logger the assigned logging channel
    * @param context the assigned context
    */
    public ObserverComponent( final Logger logger, final Context context )
    {
        m_context = context;
        m_logger = logger;
        
        m_context.addPropertyChangeListener( this );
        
        getLogger().debug( "example component created" );
    }
    
    //------------------------------------------------------------------
    // PropertyChangeListener
    //------------------------------------------------------------------

    public void propertyChange( PropertyChangeEvent event )
    {
        m_list.add( event );
        String name = event.getPropertyName();
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        getLogger().info( 
          "property [" + name + "] changed." );
    }
    
    //------------------------------------------------------------------
    // Testcase hook
    //------------------------------------------------------------------
    
    public List getEventList()
    {
        return m_list;
    }
    
    //------------------------------------------------------------------
    // Example
    //------------------------------------------------------------------
    
   /**
    * Return the color value from the supplied context.
    * @return the color value
    */
    public Color getColor()
    {
        return m_context.getColor();
    }
    
    //------------------------------------------------------------------
    // internal
    //------------------------------------------------------------------

   /**
    * Return the assigned logging channel.
    * @return the logging channel
    */
    private Logger getLogger()
    {
        return m_logger;
    }
}
