/**
 * Copyright 2003-2004 The Apache Software Foundation
 * Copyright 2005-2006 Stephen McConnell, The Digital Product Management Laboratory
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

import dpml.cli.CommandLine;
import dpml.cli.Group;
import dpml.cli.Option;
import dpml.cli.OptionException;
import dpml.cli.builder.ArgumentBuilder;
import dpml.cli.builder.DefaultOptionBuilder;
import dpml.cli.builder.GroupBuilder;
import dpml.cli.commandline.Parser;
import dpml.cli.validation.EnumValidator;

import java.util.HashSet;
import java.util.Set;

/**
 * <p>Test the <code>ls</code> command. Duplicated Option types are not
 * tested e.g. -a and -d are the same Option type.</p>
 *
 * <p>The following is the man output for 'ls'. See
 * http://www.rt.com/man/ls.1.html.</p>
 *
 * <pre>
 *  LS(1) FSF LS(1)
 *
 *  NAME ls - list directory contents
 *
 *  SYNOPSIS ls [OPTION]... [FILE]...
 *
 *  DESCRIPTION List information about the FILEs (the current directory by default). Sort entries alphabetically if none of -cftuSUX nor --sort.
 *
 *  -a, --all do not hide entries starting with .
 *
 *  -A, --almost-all do not list implied . and ..
 *
 *  -b, --escape print octal escapes for nongraphic characters
 *
 *  --block-size=SIZE use SIZE-byte blocks
 *
 *  -B, --ignore-backups do not list implied entries ending with ~ -c sort by change time; with -l: show ctime -C list entries by columns
 *
 *  --color[=WHEN] control whether color is used to distinguish file types. WHEN may be `never', `always', or `auto'
 *
 *  -d, --directory list directory entries instead of contents
 *
 *  -D, --dired generate output designed for Emacs' dired mode -f do not sort, enable -aU, disable -lst
 *
 *  -F, --classify append indicator (one of /=@|*) to entries
 *
 *  --format=WORD across -x, commas -m, horizontal -x, long -l, sin- gle-column -1, verbose -l, vertical -C
 *
 *  --full-time list both full date and full time -g (ignored)
 *
 *  -G, --no-group inhibit display of group information
 *
 *  -h, --human-readable print sizes in human readable format (e.g., 1K 234M 2G)
 *
 *  -H, --si likewise, but use powers of 1000 not 1024
 *
 *  --indicator-style=WORD append indicator with style WORD to entry names: none (default), classify (-F), file-type (-p)
 *
 *  -i, --inode print index number of each file
 *
 *  -I, --ignore=PATTERN do not list implied entries matching shell PATTERN
 *
 *  -k, --kilobytes like --block-size=1024 -l use a long listing format
 *
 *  -L, --dereference list entries pointed to by symbolic links -m fill width with a comma separated list of entries
 *
 *  -n, --numeric-uid-gid list numeric UIDs and GIDs instead of names
 *
 *  -N, --literal print raw entry names (don't treat e.g. control characters specially) -o use long listing format without group info
 *
 *  -p, --file-type append indicator (one of /=@|) to entries
 *
 *  -q, --hide-control-chars print ? instead of non graphic characters
 *
 *  --show-control-chars show non graphic characters as-is (default)
 *
 *  -Q, --quote-name enclose entry names in double quotes
 *
 *  --quoting-style=WORD use quoting style WORD for entry names: literal, shell, shell-always, c, escape
 *
 *  -r, --reverse reverse order while sorting
 *
 *  -R, --recursive list subdirectories recursively
 *
 *  -s, --size print size of each file, in blocks -S sort by file size
 *
 *  --sort=WORD extension -X, none -U, size -S, time -t, version -v status -c, time -t, atime -u, access -u, use -u
 *
 *  --time=WORD show time as WORD instead of modification time: atime, access, use, ctime or status; use specified time as sort key if --sort=time -t sort by modification time
 *
 *  -T, --tabsize=COLS assume tab stops at each COLS instead of 8 -u sort by last access time; with -l: show atime -U do not sort; list entries in directory order -v sort by version
 *
 *  -w, --width=COLS assume screen width instead of current value -x list entries by lines instead of by columns -X sort alphabetically by entry extension -1 list one file per line
 *
 *  --help display this help and exit
 *
 *  --version output version information and exit
 *
 *  By default, color is not used to distinguish types of files. That is equivalent to using --color=none. Using the --color option without the optional WHEN argument is equivalent to using --color=always. With --color=auto, color codes are output only if standard output is con- nected to a terminal (tty).
 * </pre>
 *
 * @author Rob Oxspring
 * @author John Keyes
 */
public class LsTest extends TestCase
{
    /** Option Builder */
    private static final DefaultOptionBuilder OPTION_BUILDER = new DefaultOptionBuilder(  );

    /** Argument Builder */
    private static final ArgumentBuilder ARGUMENT_BUILDER = new ArgumentBuilder(  );

    /** Group Builder */
    private static final GroupBuilder GROUP_BUILDER = new GroupBuilder(  );
    private static Group m_OPTIONS;

    /**
     * Required ctor.
     *
     * @param name
     *            the name of the TestCase
     */
    public LsTest( final String name )
    {
        super( name );
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static Test suite(  )
    {
        return new TestSuite( LsTest.class );
    }

    /**
     * DOCUMENT ME!
     */
    public void setUp(  )
    {
        if( LsTest.m_OPTIONS == null )
        {
            final Option a = OPTION_BUILDER.withShortName( "a" ).withLongName( "all" )
                                     .withDescription( "do not hide entries starting with ." )
                                     .create(  );

            final Option blockSize = OPTION_BUILDER.withLongName( "block-size" )
                                             .withRequired( false )
                                             .withDescription( "use SIZE-byte blocks" )
                                             .withArgument( ARGUMENT_BUILDER.withMaximum( 
                        1 ).withMinimum( 1 ).withInitialSeparator( '=' ).create(  ) )
                                             .create(  );

            final Option c = OPTION_BUILDER.withShortName( "c" ).withRequired( false )
                                     .withDescription( "with -lt: sort by, and show, ctime (time of last modification of file status information) with -l:show ctime and sort by name otherwise: sort by ctime" )
                                     .create(  );

            final Set colors = new HashSet(  );
            colors.add( "never" );
            colors.add( "always" );
            colors.add( "auto" );

            final Option color = OPTION_BUILDER.withLongName( "color" )
                                         .withRequired( false )
                                         .withDescription( "control  whether  color is used to distinguish file types.  WHEN may be `never', `always', or `auto'" )
                                         .withArgument( ARGUMENT_BUILDER.withMaximum( 1 )
                                                                .withMinimum( 1 )
                                                                .withInitialSeparator( '=' )
                                                                .withValidator( new EnumValidator( 
                            colors ) ).create(  ) ).create(  );

            LsTest.m_OPTIONS = GROUP_BUILDER.withOption( a ).withOption( blockSize )
                                     .withOption( c ).withOption( color )
                                     .create(  );
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @throws OptionException DOCUMENT ME!
     */
    public void testLs(  ) throws OptionException
    {
        // create the command line parser
        Parser parser = new Parser(  );
        parser.setGroup( m_OPTIONS );

        CommandLine line = parser.parse( new String[]
                {
                    "--block-size=10", "--color=never"
                } );

        assertTrue( line.hasOption( "--block-size" ) );
        assertEquals( line.getValue( "--block-size" ), "10" );
        assertFalse( line.hasOption( "--ignore-backups" ) );
    }
}
