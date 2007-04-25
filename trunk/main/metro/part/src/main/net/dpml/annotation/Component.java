/*
 * Copyright 2004-2006 Stephen J. McConnell.
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

package net.dpml.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Component annotation dealing with lifestyle policy, lifecycle model, and collection semantics.
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
 @Target( ElementType.TYPE )
 @Retention( RetentionPolicy.RUNTIME )
public @interface Component
{
   /**
    * Declaration of the component name. QAs a general rule it is recommended
    * that component names should be restricted to short names without periods or
    * spaces (such as 'widget', 'gizmo', etc.).  Component names are used in the 
    * construction of composite names that fully qualify the component within the 
    * runtime and serve as logging category names.  If undefined, a "" name value
    * is declared in which case a container may resolve a name based on the component
    * classname.
    */
    String name() default "";
    
   /**
    * Declaration of an explicit component lifestyle policy.  If undefined the 
    * default policy is LifestylePolicy.THREAD.
    */
    LifestylePolicy lifestyle() default LifestylePolicy.THREAD;
    
   /**
    * Declaration of an explicit component collection policy.  If undefined the 
    * default policy is CollectionPolicy.HARD.
    */
    CollectionPolicy collection() default CollectionPolicy.HARD;
    
   /**
    * Declaration of an alternative lifestyle graph via a URI.  If undefined the 
    * default semantics are derived from a graph definition colocated with the class
    * using the resource path <classname>.xgraph.
    */
    String lifecycle() default "";
    
   /**
    * Declaration of an alternative component runtime handler uri.
    */
    String handlerURI() default "link:part:dpml/metro/dpml-lang-component";
    
   /**
    * Declaration of an alternative component runtime strategy handler classname.
    */
    String handlerClassname() default "net.dpml.runtime.ComponentStrategyHandler";
}
