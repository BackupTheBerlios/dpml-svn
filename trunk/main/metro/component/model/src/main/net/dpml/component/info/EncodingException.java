/*
 * Copyright 2005 Stephen J. McConnell.
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
package net.dpml.component.info;

/**
 * Exception throw if an error occurs during the encoding of a component type.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class EncodingException extends Exception
{
    private final Exception[] m_errors;
    
    public EncodingException( String message, Exception[] errors )
    {
        super( message );
        
        m_errors = errors;
    }
    
    public Exception[] getExceptions()
    {
        return m_errors;
    }
}
