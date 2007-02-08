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

/**
 * <p>Immutable datastructures reflecting a transit configuration.  The 
 * principal class {@link dpml.transit.info.TransitDirective} holds 
 * a single {@link dpml.transit.info.CacheDirective} and optional 
 * {@link dpml.transit.info.ProxyDirective}. The {@link dpml.transit.info.CacheDirective} 
 * holds 0..n {@link dpml.transit.info.HostDirective} values.
 * Collectively these descriptors define the Transit deployment configuration.
 * An instance of {@code TransitDirective} is supplied as an argument to 
 * {@link net.dpml.transit.Transit} via the system property 
 * {@code dpml.transit.profile} as a part of the 
 * {@link net.dpml.transit.Transit#getInstance()} invocation (typically
 * invoked by Transit subsystems when handling URL resolution).</p>
 *
 * <P><img src="doc-files/transit-directive-uml.png"/></P>
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
package dpml.transit.info;
