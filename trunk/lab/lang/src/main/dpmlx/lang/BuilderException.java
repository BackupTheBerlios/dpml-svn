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

package dpmlx.lang;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class BuilderException extends Exception
{
    private final Element m_element;
    
    public BuilderException( Element element, String message )
    {
        super( message );
        m_element = element;
    }
    
    public Element getElement()
    {
        return m_element;
    }
    
    public String getMessage()
    {
        try
        {
            String message = super.getMessage();
            StringBuffer buffer = new StringBuffer( message );
            Element element = getElement();
            String tag = element.getTagName();
            buffer.append( "\nElement: <" );
            buffer.append( tag );
            buffer.append( " ...>" );
            Document document = element.getOwnerDocument();
            String uri = document.getDocumentURI();
            if( null != uri )
            {
                buffer.append( "\nDocument: " + uri );
            }
            return buffer.toString();
        }
        catch( Throwable e )
        {
            return super.getMessage();
        }
    }
}
