/*
 * Copyright 2003-2005 The Apache Software Foundation
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
package dpml.cli.validation;

import java.io.File;
import java.util.List;
import java.util.ListIterator;

/**
 * The <code>FileValidator</code> validates the string argument
 * values are files.  If the value is a file, the string value in
 * the {@link java.util.List} of values is replaced with the
 * {@link java.io.File} instance.
 *
 * The following attributes can also be specified using the 
 * appropriate settors:
 * <ul>
 *  <li>existing</li>
 *  <li>is a file</li>
 *  <li>is a directory</li>
 * </ul>
 *
 * The following example shows how to limit the valid values
 * for the config attribute to files that exist.
 *
 * <pre>
 * ...
 * ArgumentBuilder builder = new ArgumentBuilder();
 * FileValidator validator = FileValidator.getExistingFileInstance();
 * Argument age = 
 *     builder.withName("config");
 *            .withValidator(validator);
 * </pre>
 * 
 * @author <a href="@PUBLISHER-URL@">@PUBLISHER-NAME@</a>
 * @version @PROJECT-VERSION@
 */
public class FileValidator implements Validator
{
    /**
     * Returns a <code>FileValidator</code> for existing files/directories.
     *
     * @return a <code>FileValidator</code> for existing files/directories.
     */
    public static FileValidator getExistingInstance() 
    {
        final FileValidator validator = new FileValidator();
        validator.setExisting( true );
        return validator;
    }

    /**
     * Returns a <code>FileValidator</code> for existing files.
     *
     * @return a <code>FileValidator</code> for existing files.
     */
    public static FileValidator getExistingFileInstance()
    {
        final FileValidator validator = new FileValidator();
        validator.setExisting( true );
        validator.setFile( true );
        return validator;
    }

    /**
     * Returns a <code>FileValidator</code> for existing directories.
     *
     * @return a <code>FileValidator</code> for existing directories.
     */
    public static FileValidator getExistingDirectoryInstance()
    {
        final FileValidator validator = new FileValidator();
        validator.setExisting( true );
        validator.setDirectory( true );
        return validator;
    }

    /** whether the argument value exists */
    private boolean m_existing = false;
    
    /** whether the argument value is a directory */
    private boolean m_directory = false;
    
    /** whether the argument value is a file */
    private boolean m_file = false;

   /**
    * Validate the list of values against the list of permitted values.
    * If a value is valid, replace the string in the <code>values</code>
    * {@link java.util.List} with the {@link java.io.File} instance.
    * 
    * @param values the list of values to validate 
    * @exception InvalidArgumentException if a value is invalid
    * @see dpml.cli.validation.Validator#validate(java.util.List)
    */
    public void validate( final List values ) throws InvalidArgumentException 
    {
        for( final ListIterator i = values.listIterator(); i.hasNext();) 
        {
            final Object next = i.next();
            if( next instanceof File )
            {
                return;
            }
            final String name = (String) next;
            final File f = new File( name );
            if( ( m_existing && !f.exists() )
              || ( m_file && !f.isFile() )
              || ( m_directory && !f.isDirectory() ) )
            {
                throw new InvalidArgumentException( name );
            }
            i.set( f );
        }
    }

    /**
     * Returns whether the argument values must represent directories.
     *
     * @return whether the argument values must represent directories.
     */
    public boolean isDirectory()
    {
        return m_directory;
    }

    /**
     * Specifies whether the argument values must represent directories.
     *
     * @param directory specifies whether the argument values must 
     * represent directories.
     */
    public void setDirectory( boolean directory )
    {
        m_directory = directory;
    }

    /**
     * Returns whether the argument values must represent existing 
     * files/directories.
     *
     * @return whether the argument values must represent existing 
     * files/directories.
     */
    public boolean isExisting()
    {
        return m_existing;
    }

    /**
     * Specifies whether the argument values must represent existing 
     * files/directories.
     *
     * @param existing specifies whether the argument values must 
     * represent existing files/directories.
     */
    public void setExisting( boolean existing )
    {
        m_existing = existing;
    }

    /**
     * Returns whether the argument values must represent directories.
     *
     * @return whether the argument values must represent directories.
     */
    public boolean isFile()
    {
        return m_file;
    }

    /**
     * Specifies whether the argument values must represent files.
     *
     * @param file specifies whether the argument values must 
     * represent files.
     */
    public void setFile( boolean file )
    {
        m_file = file;
    }
}
