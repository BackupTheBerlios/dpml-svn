/*
 * Copyright 2004-2005 Stephen J. McConnell.
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

package net.dpml.metro.info.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.beans.Encoder;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.beans.ExceptionListener;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.beans.DefaultPersistenceDelegate;

import junit.framework.TestCase;

import net.dpml.metro.info.PartReference;
import net.dpml.metro.data.ValueDirective;

/**
 * EntryDescriptorTestCase
 *
 * @author <a href="mailto:dev-dpml@lists.ibiblio.org">The Digital Product Meta Library</a>
 * @version $Id: EntryDescriptorTestCase.java 2387 2005-04-23 19:12:58Z mcconnell@dpml.net $
 */
public class PartReferenceTestCase extends AbstractEncodingTestCase
{
    private final String m_key = "key";
    private ValueDirective m_directive;
    
    private PartReference m_reference;
    
    public void setUp() throws Exception
    {
        m_directive = new ValueDirective( "test" );
        m_reference = new PartReference( m_key, m_directive );
    }
    
    public void testEncoding() throws Exception
    {
        PartReference ref = new PartReference( m_key, m_directive );
        executeEncodingTest( ref, "ref.xml" );
    }
    
}