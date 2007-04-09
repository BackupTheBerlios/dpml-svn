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
    String name() default "";
    LifestylePolicy lifestyle() default LifestylePolicy.THREAD;
    CollectionPolicy collection() default CollectionPolicy.HARD;
    String lifecycle() default "";
    String handlerURI() default "link:part:dpml/metro/dpml-lang-component";
    String handlerClassname() default "net.dpml.runtime.ComponentStrategyHandler";
}
