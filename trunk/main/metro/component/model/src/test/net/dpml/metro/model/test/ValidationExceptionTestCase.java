/* 
 * Copyright 2004 Stephen J. McConnell.
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

package net.dpml.metro.model.test;

import net.dpml.metro.model.ValidationException;
import net.dpml.metro.model.ValidationException.Issue;

import junit.framework.TestCase;

/**
 * Validation exception testcase verifies the ValidationException and 
 * ValidationException#Issue class implementations.
 *
 * @author <a href="http://www.dpml.net">The Digital Product Meta Library</a>
 */
public class ValidationExceptionTestCase extends TestCase
{
    public void testIssueNullKey() throws Exception
    {
        try
        {
            Issue issue = new Issue( null, "whatever" );
            fail( "did-not-throw-NPE-on-null-key" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testIssueNullMessage() throws Exception
    {
        try
        {
            Issue issue = new Issue( "key", null );
            fail( "did-not-throw-NPE-on-null-message" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testIssueAccessors() throws Exception
    {
        Issue issue = new Issue( "key", "message" );
        assertEquals( "issue-key", "key", issue.getKey() );
        assertEquals( "issue-message", "message", issue.getMessage() );
    }
    
    public void testExceptionAccessors() throws Exception
    {
        Issue[] issues = new Issue[3];
        issues[0] = new Issue( "aaa", "aaa-issue" );
        issues[1] = new Issue( "bbb", "bbb-issue" );
        issues[2] = new Issue( "ccc", "ccc-issue" );
        ValidationException exception = new ValidationException( this, issues );
        assertEquals( "issues-length", 3, exception.getIssues().length );
        assertEquals( "issues-source", this, exception.getSource() );
    }
    
    public void testValidationNullSource() throws Exception
    {
        try
        {
            ValidationException exception = new ValidationException( null, new Issue[0] );
            fail( "did-not-throw-NPE-on-null-source" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
    
    public void testValidationNullIssues() throws Exception
    {
        try
        {
            ValidationException exception = new ValidationException( this, null );
            fail( "did-not-throw-NPE-on-null-issues" );
        }
        catch( NullPointerException e )
        {
            // success
        }
    }
}
