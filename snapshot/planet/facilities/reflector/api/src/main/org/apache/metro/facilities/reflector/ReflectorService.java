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

package org.apache.metro.facilities.reflector;

/**
 * @avalon.service name="ReflectorService"
 */
public interface ReflectorService
{
    String get( String object ) throws ReflectionException;
    Object getObject( String object ) throws ReflectionException;
    Object getObject( Object container, String memberName ) throws ReflectionException;

    void set( String object, String value ) throws ReflectionException;
    void setObject( String objectname, Object object ) throws ReflectionException;
    void setObject( Object container, String memberName, Object object ) throws ReflectionException;

    String[] getNames( String object ) throws ReflectionException;
    String[] getNames( Object object ) throws ReflectionException;
    
    Class getClass( String objectname ) throws ReflectionException;
    Class getClass( Object container, String memberName ) throws ReflectionException;
    
    String getClassName( String objectname ) throws ReflectionException;
    String getClassName( Object container, String memberName ) throws ReflectionException;
    
    boolean isSettable( String objectname ) throws ReflectionException;
    boolean isSettable( Object container, String member) throws ReflectionException;
    
    String getContainer( String objectname ) throws ReflectionException;
    String getMember( String objectname ) throws ReflectionException;

    void addRootObject( String name, Object object ) throws ReflectionException;
    void removeRootObject( String name ) throws ReflectionException;

}
