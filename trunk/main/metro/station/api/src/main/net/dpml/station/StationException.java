/*
 * Copyright 2005 Stephen McConnell
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dpml.station;

import java.rmi.ServerException;

/**
 * A station exception may be raised by the station if an establishment
 * failure occurs.
 */
public class StationException extends ServerException
{
   /**
    * Creation of a new station exception instance.
    * @param message the exception message
    */
    public StationException( final String message )
    {
        this( message, null );
    }
    
   /**
    * Creation of a new station exception instance.
    * @param message the exception message
    * @param cause the causal exception
    */
    public StationException( final String message, Exception cause )
    {
        super( message, cause );
    }
}
