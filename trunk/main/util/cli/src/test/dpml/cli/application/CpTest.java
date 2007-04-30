/**
 * Copyright 2003-2004 The Apache Software Foundation
 * Copyright 2005-2007 Stephen McConnell
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
package dpml.cli.application;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import dpml.cli.Argument;
import dpml.cli.CommandLine;
import dpml.cli.Group;
import dpml.cli.Option;
import dpml.cli.OptionException;
import dpml.cli.builder.ArgumentBuilder;
import dpml.cli.builder.DefaultOptionBuilder;
import dpml.cli.builder.GroupBuilder;
import dpml.cli.commandline.Parser;
import dpml.cli.option.ArgumentImpl;
import dpml.cli.option.SourceDestArgument;
import dpml.cli.util.HelpFormatter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * <p>Test the <code>cp</code> command. Duplicated Option types are not
 * tested e.g. -a and -d are the same Option type.</p>
 *
 * <p>The following is the man output for 'cp'. See
 * http://www.rt.com/man/cp.1.html.</p>
 *
 * <pre>
 *  CP(1) FSF CP(1)
 *
 *  NAME cp - copy files and directories
 *
 *  SYNOPSIS cp [OPTION]... SOURCE DEST cp [OPTION]... SOURCE... DIRECTORY
 *
 *  DESCRIPTION Copy SOURCE to DEST, or multiple SOURCE(s) to DIRECTORY.
 *
 *  -a, --archive same as -dpR
 *
 *  -b, --backup make backup before removal
 *
 *  -d, --no-dereference preserve links
 *
 *  -f, --force remove existing destinations, never prompt
 *
 *  -i, --interactive prompt before overwrite
 *
 *  -l, --link link files instead of copying
 *
 *  -p, --preserve preserve file attributes if possible
 *
 *  -P, --parents append source path to DIRECTORY
 * -r copy recursively, non-directories as files
 *
 *  --sparse=WHEN control creation of sparse files
 *
 *  -R, --recursive copy directories recursively
 *
 *  -s, --symbolic-link make symbolic links instead of copying
 *
 *  -S, --suffix=SUFFIX override the usual backup suffix
 *
 *  -u, --update copy only when the SOURCE file is newer than the destination file or when the destination file is missing
 *
 *  -v, --verbose explain what is being done
 *
 *  -V, --version-control=WORD override the usual version control
 *
 *  -x, --one-file-system stay on this file system
 *
 *  --help display this help and exit
 *
 *  --version output version information and exit
 *
 *  By default, sparse SOURCE files are detected by a crude heuristic and the corresponding DEST file is made sparse as well. That is the behavior selected by --sparse=auto. Specify --sparse=always to create a sparse DEST file when- ever the SOURCE file contains a long enough sequence of zero bytes. Use --sparse=never to inhibit creation of sparse files.
 *
 *  The backup suffix is ~, unless set with SIMPLE_BACKUP_SUF- FIX. The version control may be set with VERSION_CONTROL, values are:
 * t, numbered make numbered backups
 *
 *  nil, existing numbered if numbered backups exist, simple other- wise
 *
 *  never, simple always make simple backups
 *
 *  As a special case, cp makes a backup of SOURCE when the force and backup options are given and SOURCE and DEST are the same name for an existing, regular file. * </pre>
 * </pre>
 *
 * @author Rob Oxspring
 * @author John Keyes
 */
public class CpTest extends TestCase
{
    /** Option Builder */
    private static final DefaultOptionBuilder OPTION_BUILDER = new DefaultOptionBuilder(  );

    /** Argument Builder */
    private static final ArgumentBuilder ARGUMENT_BUILDER = new ArgumentBuilder(  );

    /** Group Builder */
    private static final GroupBuilder GROUP_BUILDER = new GroupBuilder(  );
    
    private Group m_options;
    private ArgumentImpl m_source;
    private ArgumentImpl m_dest;
    private Argument m_targets;
    private Option m_archive;
    private Option m_backup;
    private Option m_noDereference;
    private Option m_force;
    private Option m_interactive;
    private Option m_link;
    private Option m_preserve;
    private Option m_parents;
    private Option m_recursive1;
    private Option m_sparse;
    private Option m_recursive2;
    private Option m_symbolicLink;
    private Option m_suffix;
    private Option m_update;
    private Option m_verbose;
    private Option m_versionControl;
    private Option m_oneFileSystem;
    private Option m_help;
    private Option m_version;

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Test suite(  )
    {
        return new TestSuite( CpTest.class );
    }

    /**
     * DOCUMENT ME!
     */
    public void setUp(  )
    {
        m_source = (ArgumentImpl) ARGUMENT_BUILDER.withName( "SOURCE" ).withMinimum( 1 )
                                        .create(  );
        m_dest = (ArgumentImpl) ARGUMENT_BUILDER.withName( "DEST" ).withMinimum( 1 )
                                      .withMaximum( 1 ).create(  );
        m_targets = new SourceDestArgument( m_source, m_dest );

        m_archive = OPTION_BUILDER.withShortName( "a" ).withLongName( "archive" )
                          .withDescription( "same as -dpR" ).create(  );

        m_backup = OPTION_BUILDER.withShortName( "b" ).withLongName( "backup" )
                         .withDescription( "make backup before removal" )
                         .create(  );

        m_noDereference = OPTION_BUILDER.withShortName( "d" )
                                .withLongName( "no-dereference" )
                                .withDescription( "preserve links" ).create(  );

        m_force = OPTION_BUILDER.withShortName( "f" ).withLongName( "force" )
                        .withDescription( "remove existing destinations, never prompt" )
                        .create(  );

        m_interactive = OPTION_BUILDER.withShortName( "i" ).withLongName( "interactive" )
                              .withDescription( "prompt before overwrite" )
                              .create(  );

        m_link = OPTION_BUILDER.withShortName( "l" ).withLongName( "link" )
                       .withDescription( "link files instead of copying" )
                       .create(  );

        m_preserve = OPTION_BUILDER.withShortName( "p" ).withLongName( "preserve" )
                           .withDescription( "preserve file attributes if possible" )
                           .create(  );

        m_parents = OPTION_BUILDER.withShortName( "P" ).withLongName( "parents" )
                          .withDescription( "append source path to DIRECTORY" )
                          .create(  );

        m_recursive1 = OPTION_BUILDER.withShortName( "r" )
                             .withDescription( "copy recursively, non-directories as files" )
                             .create(  );

        m_sparse = OPTION_BUILDER.withLongName( "sparse" )
                         .withDescription( "control creation of sparse files" )
                         .withArgument( ARGUMENT_BUILDER.withName( "WHEN" )
                                                .withMinimum( 1 ).withMaximum( 1 )
                                                .withInitialSeparator( '=' )
                                                .create(  ) ).create(  );

        m_recursive2 = OPTION_BUILDER.withShortName( "R" ).withLongName( "recursive" )
                             .withDescription( "copy directories recursively" )
                             .create(  );

        m_symbolicLink = OPTION_BUILDER.withShortName( "s" )
                               .withLongName( "symbolic-link" )
                               .withDescription( "make symbolic links instead of copying" )
                               .create(  );

        m_suffix = OPTION_BUILDER.withShortName( "S" ).withLongName( "suffix" )
                         .withDescription( "override the usual backup suffix" )
                         .withArgument( ARGUMENT_BUILDER.withName( "SUFFIX" )
                                                .withMinimum( 1 ).withMaximum( 1 )
                                                .create(  ) ).create(  );

        m_update = OPTION_BUILDER.withShortName( "u" ).withLongName( "update" )
                         .withDescription( "copy only when the SOURCE file is newer than the destination file or when the destination file is missing" )
                         .create(  );

        m_verbose = OPTION_BUILDER.withShortName( "v" ).withLongName( "verbose" )
                          .withDescription( "explain what is being done" )
                          .create(  );

        m_versionControl = OPTION_BUILDER.withShortName( "V" )
                                 .withLongName( "version-contol" )
                                 .withDescription( "explain what is being done" )
                                 .withArgument( ARGUMENT_BUILDER.withName( "WORD" )
                                                        .withInitialSeparator( '=' )
                                                        .withMinimum( 1 )
                                                        .withMaximum( 1 )
                                                        .create(  ) ).create(  );

        m_oneFileSystem = OPTION_BUILDER.withShortName( "x" )
                                .withLongName( "one-file-system" )
                                .withDescription( "stay on this file system" )
                                .create(  );

        m_help = OPTION_BUILDER.withLongName( "help" )
                       .withDescription( "display this help and exit" ).create(  );

        m_version = OPTION_BUILDER.withLongName( "version" )
                          .withDescription( "output version information and exit" )
                          .create(  );

        m_options = GROUP_BUILDER.withOption( m_archive ).withOption( m_backup )
                          .withOption( m_noDereference ).withOption( m_force )
                          .withOption( m_interactive ).withOption( m_link )
                          .withOption( m_preserve ).withOption( m_parents )
                          .withOption( m_recursive1 ).withOption( m_sparse )
                          .withOption( m_recursive2 ).withOption( m_symbolicLink )
                          .withOption( m_suffix ).withOption( m_update )
                          .withOption( m_verbose ).withOption( m_versionControl )
                          .withOption( m_oneFileSystem ).withOption( m_help )
                          .withOption( m_version ).withOption( m_targets )
                          .withName( "OPTIONS" ).create(  );
    }

    /**
     * DOCUMENT ME!
     */
    public void testNoSource(  )
    {
        Parser parser = new Parser(  );
        parser.setGroup( m_options );

        try
        {
            parser.parse( new String[0] );
        }
        catch( OptionException mve )
        {
            assertEquals( "Missing value(s) SOURCE [SOURCE ...]",
                mve.getMessage(  ) );
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testOneSource(  ) throws OptionException
    {
        final String[] args = new String[]{"source1", "dest1"};
        final Parser parser = new Parser(  );
        parser.setGroup( m_options );

        final CommandLine commandLine = parser.parse( args );

        assertTrue( commandLine.getValues( m_source ).contains( "source1" ) );
        assertEquals( 1, commandLine.getValues( m_source ).size(  ) );
        assertTrue( commandLine.getValues( m_dest ).contains( "dest1" ) );
        assertEquals( 1, commandLine.getValues( m_dest ).size(  ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testMultiSource(  ) throws OptionException
    {
        final String[] args = new String[]
            {
                "source1", "source2", "source3", "dest1"
            };
        final Parser parser = new Parser(  );
        parser.setGroup( m_options );

        final CommandLine commandLine = parser.parse( args );

        assertTrue( commandLine.getValues( m_source ).contains( "source1" ) );
        assertTrue( commandLine.getValues( m_source ).contains( "source2" ) );
        assertTrue( commandLine.getValues( m_source ).contains( "source3" ) );
        assertEquals( 3, commandLine.getValues( m_source ).size(  ) );

        assertTrue( commandLine.getValues( m_dest ).contains( "dest1" ) );
        assertEquals( 1, commandLine.getValues( m_dest ).size(  ) );
    }

    /**
     * DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public void testHelp(  ) throws IOException
    {
        final StringWriter out = new StringWriter(  );
        final HelpFormatter helpFormatter = new HelpFormatter(  );
        helpFormatter.setGroup( m_options );
        helpFormatter.setPrintWriter( new PrintWriter( out ) );
        helpFormatter.print(  );

        final BufferedReader in = new BufferedReader( new StringReader( 
                    out.toString(  ) ) );
        assertEquals( "Usage:                                                                          ",
            in.readLine(  ) );
        assertEquals( " [-a -b -d -f -i -l -p -P -r --sparse <WHEN> -R -s -S <SUFFIX> -u -v -V <WORD>  ",
            in.readLine(  ) );
        assertEquals( "-x --help --version] <SOURCE1> [<SOURCE2> ...] <DEST>                           ",
            in.readLine(  ) );
        assertEquals( "OPTIONS                                                                         ",
            in.readLine(  ) );
        assertEquals( "  -a (--archive)                same as -dpR                                    ",
            in.readLine(  ) );
        assertEquals( "  -b (--backup)                 make backup before removal                      ",
            in.readLine(  ) );
        assertEquals( "  -d (--no-dereference)         preserve links                                  ",
            in.readLine(  ) );
        assertEquals( "  -f (--force)                  remove existing destinations, never prompt      ",
            in.readLine(  ) );
        assertEquals( "  -i (--interactive)            prompt before overwrite                         ",
            in.readLine(  ) );
        assertEquals( "  -l (--link)                   link files instead of copying                   ",
            in.readLine(  ) );
        assertEquals( "  -p (--preserve)               preserve file attributes if possible            ",
            in.readLine(  ) );
        assertEquals( "  -P (--parents)                append source path to DIRECTORY                 ",
            in.readLine(  ) );
        assertEquals( "  -r                            copy recursively, non-directories as files      ",
            in.readLine(  ) );
        assertEquals( "  --sparse WHEN                 control creation of sparse files                ",
            in.readLine(  ) );
        assertEquals( "  -R (--recursive)              copy directories recursively                    ",
            in.readLine(  ) );
        assertEquals( "  -s (--symbolic-link)          make symbolic links instead of copying          ",
            in.readLine(  ) );
        assertEquals( "  -S (--suffix) SUFFIX          override the usual backup suffix                ",
            in.readLine(  ) );
        assertEquals( "  -u (--update)                 copy only when the SOURCE file is newer than    ",
            in.readLine(  ) );
        assertEquals( "                                the destination file or when the destination    ",
            in.readLine(  ) );
        assertEquals( "                                file is missing                                 ",
            in.readLine(  ) );
        assertEquals( "  -v (--verbose)                explain what is being done                      ",
            in.readLine(  ) );
        assertEquals( "  -V (--version-contol) WORD    explain what is being done                      ",
            in.readLine(  ) );
        assertEquals( "  -x (--one-file-system)        stay on this file system                        ",
            in.readLine(  ) );
        assertEquals( "  --help                        display this help and exit                      ",
            in.readLine(  ) );
        assertEquals( "  --version                     output version information and exit             ",
            in.readLine(  ) );
        assertEquals( "  SOURCE [SOURCE ...]                                                           ",
            in.readLine(  ) );
        assertEquals( "  DEST                                                                          ",
            in.readLine(  ) );
        assertNull( in.readLine(  ) );
    }
}
