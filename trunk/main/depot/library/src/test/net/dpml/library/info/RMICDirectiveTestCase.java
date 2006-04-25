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

package net.dpml.library.info;

/**
 * Test and validate the RMICDirective class.
 *
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public final class  RMICDirectiveTestCase extends AbstractTestCase
{
    static final PatternDirective[] PATTERNS = 
      new PatternDirective[]
      {
          new IncludePatternDirective( "foo" ),
          new ExcludePatternDirective( "bar" )
      };
    
    static final RMICDirective RMIC = new RMICDirective( PATTERNS );

   /**
    * Test null argument.
    */
    public void testNullArgs()
    {
        try
        {
            RMICDirective rmic = new RMICDirective( null );
            fail( "Did not thow a NPE" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
   /**
    * Test accessor.
    */
    public void testPatternAccessor()
    {
        assertEquals( "patterns", PATTERNS, RMIC.getPatternDirectives() );
    }
    
   /**
    * Test directive serialization.
    * @exception Exception if an error occurs
    */
    public void testSerialization() throws Exception
    {
        doSerializationTest( RMIC );
    }

}
