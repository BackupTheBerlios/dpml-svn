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

package net.dpml.runtime.composition;

import net.dpml.runtime.Provider;
import net.dpml.runtime.Component;

import net.dpml.runtime.AbstractTestCase;

import org.acme.SelectComponent;
import org.acme.Widget;
import org.acme.Gizmo;

/**
 * Select testcase.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class SelectTestCase extends AbstractTestCase
{
   /**
    * Validate part selection by return type.
    * @exception Exception if an error occurs
    */
    public void testSelectionByServiceReturnType() throws Exception
    {
        SelectComponent.Parts parts = getParts();
        Widget[] widgets = parts.getWidgets();
        assertEquals( "size-widget", 2, widgets.length );
    }
    
   /**
    * Validate part selection by type argument.
    * @exception Exception if an error occurs
    */
    public void testSelectionByServiceCriteria() throws Exception
    {
        SelectComponent.Parts parts = getParts();
        Provider[] providers = parts.getProviders( Widget.class );
        assertEquals( "size-providers", 2, providers.length );
        Component[] components = parts.getComponents( Widget.class );
        assertEquals( "size-providers", 2, components.length );
        Component[] gizmos = parts.getComponents( Gizmo.class );
        assertEquals( "gizmos", 1, gizmos.length );
        Provider[] gizmoProviders = parts.getProviders( Gizmo.class );
        assertEquals( "gizmo-providers", 1, gizmoProviders.length );
    }
    
   /**
    * Validate part selection of all components.
    * @exception Exception if an error occurs
    */
    public void testAllSelectionCriteria() throws Exception
    {
        SelectComponent.Parts parts = getParts();
        Component[] all = parts.getAllComponents();
        assertEquals( "size-all", 3, all.length );
    }
    
    private SelectComponent.Parts getParts() throws Exception
    {
        SelectComponent component = 
          load( SelectComponent.class, "select.xml", "select" );
        return component.getParts();
    }
}
