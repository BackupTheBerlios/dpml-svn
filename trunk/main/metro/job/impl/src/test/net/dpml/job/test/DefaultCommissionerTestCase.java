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

package net.dpml.job.test;

import java.lang.reflect.InvocationTargetException;

import net.dpml.util.Logger;

import net.dpml.transit.monitor.LoggingAdapter;

import net.dpml.job.Commissionable;
import net.dpml.job.CommissionerController;
import net.dpml.job.CommissionerEvent;
import net.dpml.job.TimeoutException;
import net.dpml.job.TimeoutError;

import net.dpml.job.impl.DefaultCommissioner;

import junit.framework.TestCase;

/**
 * Validation exception testcase verifies the ValidationException and 
 * ValidationException#Issue class implementations.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class DefaultCommissionerTestCase extends TestCase
{
    static
    {
        System.setProperty( 
          "java.util.logging.config.class", 
          "net.dpml.util.ConfigurationHandler" );
        System.setProperty( 
          "dpml.logging.config",
          "file:" + System.getProperty( "project.test.dir" ) + "/logging.properties" );
    }
    
    private static final Logger LOGGER = new LoggingAdapter( "test" );
    
   /**
    * Test the commissioner implementation.
    * @exception Exception if an error occurs
    */
    public void testCommission() throws Exception
    {
        DefaultCommissioner queue = 
          new DefaultCommissioner( "demo", true, new DefaultCommissionerController() );
        try
        {
            Widget widget = new Widget();
            long timeout = 200;
            queue.add( widget, timeout );
        }
        finally
        {
            queue.dispose();
        }
    }
    
   /**
    * Test the commissioner implementation.
    * @exception Exception if an error occurs
    */
    public void testDecommission() throws Exception
    {
        DefaultCommissioner queue = 
          new DefaultCommissioner( "demo", false, new DefaultCommissionerController() );
        try
        {
            Widget widget = new Widget();
            long timeout = 200;
            queue.add( widget, timeout );
        }
        finally
        {
            queue.dispose();
        }
    }
    
   /**
    * Test the commissioner implementation.
    * @exception Exception if an error occurs
    */
    public void testCommissionWithTimeout() throws Exception
    {
        DefaultCommissioner queue = 
          new DefaultCommissioner( "demo", true, new DefaultCommissionerController() );
        try
        {
            Widget widget = new Widget();
            long timeout = 10;
            queue.add( widget, timeout );
            fail( "did not timeout" );
        }
        catch( TimeoutException e )
        {
            // success
        }
        finally
        {
            queue.dispose();
        }
    }
    
   /**
    * Test the commissioner implementation.
    * @exception Exception if an error occurs
    */
    public void testDecommissionWithTimeout() throws Exception
    {
        DefaultCommissioner queue = 
          new DefaultCommissioner( "demo", false, new DefaultCommissionerController() );
        try
        {
            Widget widget = new Widget();
            long timeout = 10;
            queue.add( widget, timeout );
            fail( "did not timeout" );
        }
        catch( TimeoutException e )
        {
            // success
        }
        finally
        {
            queue.dispose();
        }
    }
    
   /**
    * Test the commissioner implementation.
    * @exception Exception if an error occurs
    */
    public void testCommissionWithTimeoutAndNoResponse() throws Exception
    {
        DefaultCommissioner queue = 
          new DefaultCommissioner( "demo", true, new DefaultCommissionerController() );
        try
        {
            Gizmo gizmo = new Gizmo();
            long timeout = 100;
            queue.add( gizmo, timeout );
            fail( "did not timeout" );
        }
        catch( TimeoutError e )
        {
            // success
        }
        finally
        {
            queue.dispose();
        }
    }
    
   /**
    * Test the commissioner implementation.
    * @exception Exception if an error occurs
    */
    public void testClientError() throws Exception
    {
        DefaultCommissioner queue = 
          new DefaultCommissioner( "demo", false, new DefaultCommissionerController() );
        try
        {
            Gizmo gizmo = new Gizmo();
            long timeout = 100;
            queue.add( gizmo, timeout );
            fail( "did not throw client exception" );
        }
        catch( InvocationTargetException e )
        {
            // success
        }
        finally
        {
            queue.dispose();
        }
    }
    
   /**
    * Test object.
    */
    private class Widget implements Commissionable
    {
       /**
        * Commission the widget.
        * @throws Exception if a error occurs
        */
        public void commission() throws Exception
        {
            try
            {
                Thread.currentThread().sleep( 100 );
            }
            catch( Exception e )
            {
            }
        }
    
       /**
        * Decommission the widget.
        */
        public void decommission()
        {
            try
            {
                Thread.currentThread().sleep( 100 );
            }
            catch( Exception e )
            {
            }
        }
        
       /**
        * Return the instance as a string.
        * @return the string
        */
        public String toString()
        {
            return "widget";
        }
    }

   /**
    * Test object.
    */
    private class Gizmo implements Commissionable
    {
       /**
        * Commission the gizmo.
        * @throws Exception if a error occurs
        */
        public void commission() throws Exception
        {
            while( true )
            {
                try
                {
                    Thread.currentThread().sleep( 100 );
                }
                catch( Exception e )
                {
                }
            }
        }
    
       /**
        * Decommission the widget.
        */
        public void decommission()
        {
            throw new UnsupportedOperationException( "I'm bad!" );
        }
        
       /**
        * Return the instance as a string.
        * @return the string
        */
        public String toString()
        {
            return "gizmo";
        }
    }
    
   /**
    * Test controller.
    */
    public class DefaultCommissionerController implements CommissionerController
    {
       /**
        * Notification that a commissioning or decommissioning 
        * process has commenced.
        * @param event the commissioner event
        */
        public void started( CommissionerEvent event )
        {
            String message = 
              getAction( event )
              + "[" 
              + event.getSource() 
              + "]";
            LOGGER.debug( message );
        }
        
       /**
        * Notification that a commissioning or decommissioning 
        * process has completed.
        * @param event the commissioner event
        */
        public void completed( CommissionerEvent event )
        {
            String message = 
              getAction( event )
              + "[" 
              + event.getSource() 
              + "] completed in "
              + event.getDuration() 
              + " milliseconds";
            LOGGER.debug( message );
        }
    
       /**
        * Notification that a commissioning or decommissioning 
        * process has been interrupted.
        * @param event the commissioner event
        * @exception TimeoutException thrown ofter logging event
        */
        public void interrupted( CommissionerEvent event ) throws TimeoutException
        {
            String message = 
              getAction( event )
              + "of [" 
              + event.getSource() 
              + "] interrupted after "
              + event.getDuration() 
              + " milliseconds";
            LOGGER.debug( message );
            throw new TimeoutException( event.getDuration() );
        }
    
       /**
        * Notification that a commissioning or decommissioning 
        * process has been terminated.
        * @param event the commissioner event
        * @exception TimeoutError thrown ofter logging event
        */
        public void terminated( CommissionerEvent event ) throws TimeoutError
        {
            String message = 
              getAction( event )
              + "of [" 
              + event.getSource() 
              + "] terminated after "
              + event.getDuration() 
              + " milliseconds";
            LOGGER.debug( message );
            throw new TimeoutError( event.getDuration() );
        }
        
       /**
        * Notification that a commissioning or decommissioning 
        * process failed.
        * @param event the commissioner event
        * @param cause the causal exception
        * @exception InvocationTargetException throw after logging event
        */
        public void failed( CommissionerEvent event, Throwable cause ) throws InvocationTargetException
        {
            String message = 
              getAction( event )
              + "of [" 
              + event.getSource() 
              + "] failed due ["
              + cause.getClass().getName()
              + "]";
            LOGGER.error( message );
            if( cause instanceof InvocationTargetException )
            {
                throw (InvocationTargetException) cause;
            }
            else
            {
                throw new InvocationTargetException( cause );
            }
        }
        
        private String getAction( CommissionerEvent event )
        {
            if( event.isCommissioning() )
            {
                return "commissioning ";
            }
            else
            {
                return "decommissioning ";
            }
        }
    }
}
