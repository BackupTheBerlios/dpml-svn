/*
 * Copyright 2005 Stephen J. McConnell
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

package net.dpml.tools.info;

/**
 * Test artifact directive for general integrity.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public final class ProductionDirectiveTestCase extends AbstractTestCase
{
    public void testNullName()
    {
        try
        {
            ProductionDirective type = new ProductionDirective( null, true, PROPERTIES );
            fail( "no-NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testArtifactName()
    {
        ProductionDirective artifact = new ProductionDirective( "abc", true, PROPERTIES );
        assertEquals( "type", "abc", artifact.getType() );
    }
    
    public void testAlias()
    {
        ProductionDirective type = new ProductionDirective( "abc", true, PROPERTIES );
        assertTrue( "alias", type.getAlias() );
        type = new ProductionDirective( "abc", false, PROPERTIES );
        assertFalse( "alias", type.getAlias() );
    }
    
    public void testSerialization() throws Exception
    {
        ProductionDirective artifact = new ProductionDirective( "abc", true, PROPERTIES );
        doSerializationTest( artifact );
    }

    public void testXMLEncoding() throws Exception
    {
        ProductionDirective artifact = new ProductionDirective( "abc", false, PROPERTIES );
        doEncodingTest( artifact, "artifact-descriptor-encoded.xml" );
    }
}