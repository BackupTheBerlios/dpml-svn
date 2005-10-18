/*
 * Copyright 2004 Stephen J. McConnell.
 * Copyright 1999-2004 The Apache Software Foundation
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

package net.dpml.tools.ant;

import java.beans.Expression;
import java.beans.BeanDescriptor;
import java.beans.PersistenceDelegate;
import java.beans.DefaultPersistenceDelegate;
import java.beans.SimpleBeanInfo;
import java.beans.Encoder;

import net.dpml.transit.util.Enum;

/**
 * Lifestyle policy enumeration.
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: InfoDescriptor.java 2522 2005-05-12 11:23:50Z mcconnell@dpml.net $
 */
public final class Phase extends Enum
{
    static final long serialVersionUID = 1L;

   /**
    * System scope.
    */
    public static final Phase PREPARE = new Phase( "prepare" );

   /**
    * Build scope.
    */
    public static final Phase BUILD = new Phase( "build" );

   /**
    * Test scope.
    */
    public static final Phase PACKAGE = new Phase( "package" );

   /**
    * Runtime scope.
    */
    public static final Phase VALIDATE = new Phase( "validate" );

   /**
    * Runtime scope.
    */
    public static final Phase INSTALL = new Phase( "install" );

   /**
    * Array of scope enumeration values.
    */
    private static final Phase[] ENUM_VALUES = new Phase[]{ PREPARE, BUILD, PACKAGE, VALIDATE, INSTALL };

   /**
    * Returns an array of activation enum values.
    * @return the activation policies array
    */
    public static Phase[] values()
    {
        return ENUM_VALUES;
    }
    
   /**
    * Internal constructor.
    * @param label the enumeration label.
    * @param index the enumeration index.
    * @param map the set of constructed enumerations.
    */
    private Phase( String label )
    {
        super( label );
    }
    
    public static Phase parse( String value )
    {
        if( value.equalsIgnoreCase( "prepare" ) )
        {
            return PREPARE;
        }
        else if( value.equalsIgnoreCase( "build" ) )
        {
            return BUILD;
        }
        else if( value.equalsIgnoreCase( "package" ) )
        {
            return PACKAGE;
        }
        else if( value.equalsIgnoreCase( "validate" ))
        {
            return VALIDATE;
        }
        else if( value.equalsIgnoreCase( "install" ))
        {
            return INSTALL;
        }
        else
        {
            final String error =
              "Unrecognized phase argument [" + value + "]";
            throw new IllegalArgumentException( error );
        }
    }
}

