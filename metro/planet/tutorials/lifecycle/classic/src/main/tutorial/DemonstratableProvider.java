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

package tutorial;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.lifecycle.Accessor;
import org.apache.avalon.lifecycle.Creator;

/**
 * Definition of an extension type that logs messages related to
 * all lifestyle stages.
 *
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @avalon.component name="extension" version="1.0" lifestyle="singleton"
 * @avalon.extension id="urn:demo:demonstratable"
 */
public class DemonstratableProvider extends AbstractLogEnabled
        implements Creator, Accessor, Initializable, Disposable
{

    //=======================================================================
    // Initializable
    //=======================================================================

    /**
     * Initialization of the component. Simply demonstrates that this is
     * a real componet that can implement any lifcycle stage in the role of
     * classic component.
     */
    public void initialize()
    {
        getLogger().info( "initialize" );
    }

    //=======================================================================
    // Creator
    //=======================================================================

    /**
     * Invoked by a container to handle a create stage action.
     * @param target the object to handle
     * @param context the context supplied to the extension by container
     *   corresponding to its extension context criteria
     * @exception Exception if an error occurs
     */
    public void create( Object target, Context context ) throws Exception
    {
        getLogger().info( "invoking create on target" );
        if( target instanceof Demonstratable )
        {
            ((Demonstratable) target).demo( "create id: "
                    + System.identityHashCode( this )
                    + ", " + Thread.currentThread() );
        }
    }

    /**
     * Invoked by a container to handle a destroy stage action.
     * @param target the object to handle
     * @param context the context supplied to the extension by container
     *   corresponding to its extension context criteria
     */
    public void destroy( Object target, Context context )
    {
        getLogger().info( "invoking destroy on target" );
        if( target instanceof Demonstratable )
        {
            ((Demonstratable) target).demo( "destroy id: "
                    + System.identityHashCode( this )
                    + ", " + Thread.currentThread() );
        }
    }

    /**
     * Invoked by a container to handle a access stage action.
     * @param target the object to handle
     * @param context the context supplied to the extension by container
     *   corresponding to its extension context criteria
     * @exception Exception if an error occurs
     */
    public void access( Object target, Context context ) throws Exception
    {
        getLogger().info( "invoking access on target" );
        if( target instanceof Demonstratable )
        {
            ((Demonstratable) target).demo( "access id: "
                    + System.identityHashCode( this )
                    + ", " + Thread.currentThread() );
        }
    }

    /**
     * Invoked by a container to handle a release stage action.
     * @param target the object to handle
     * @param context the context supplied to the extension by container
     *   corresponding to its extension context criteria
     */
    public void release( Object target, Context context )
    {
        getLogger().info( "invoking release on target" );
        if( target instanceof Demonstratable )
        {
            ((Demonstratable) target).demo( "release id: "
                    + System.identityHashCode( this )
                    + ", " + Thread.currentThread() );
        }
    }

    //=======================================================================
    // Disposable
    //=======================================================================

    /**
     * Disposal of the component.
     */
    public void dispose()
    {
        if( getLogger().isInfoEnabled() )
        {
            getLogger().info( "dispose" );
        }
    }
}
