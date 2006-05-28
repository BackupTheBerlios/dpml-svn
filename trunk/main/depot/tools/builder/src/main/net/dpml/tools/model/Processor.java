/*
 * Copyright 2005-2006 Stephen J. McConnell
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

package net.dpml.tools.model;

/**
 * The Processor interface is implemented by phase-aware build processors.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public interface Processor
{
   /**
    * Handle cleanup.
    * @param context the working context
    */
    void clean( Context context );
    
   /**
    * Handle initialization.
    * @param context the working context
    */
    void initialize( Context context );
    
   /**
    * Handle supplimentary codebase preparation.
    * @param context the working context
    */
    void prepare( Context context );
    
   /**
    * Handle type-specific construction in preparation for 
    * data packaging.
    * @param context the working context
    */
    void build( Context context );
    
   /**
    * Packaging of type-specific data.
    * @param context the working context
    */
    void pack( Context context );
    
   /**
    * Datatype validation.
    * @param context the working context
    */
    void validate( Context context );
    
   /**
    * Post installation actions.
    * @param context the working context
    */
    void install( Context context );
}
