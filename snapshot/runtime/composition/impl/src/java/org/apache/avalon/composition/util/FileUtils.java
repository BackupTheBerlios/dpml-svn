/*
 * Copyright  2000-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.avalon.composition.util;

import java.io.File;
import java.io.IOException;
import java.util.Stack;
import java.util.StringTokenizer;

import org.apache.avalon.util.env.Env;

/**
 * This class provides some common file utilities used by the
 * composition implementation, and is borrowed from the Apache
 * Ant development team.
 *
 * @author Apache Ant Development Team (MacNeill, Umasankar, and Bodewig)
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: FileUtils.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class FileUtils {

    private boolean onNetWare = Env.isNetWare();
    
    /**
     * Factory method.
     *
     * @return a new instance of FileUtils.
     */
    public static FileUtils newFileUtils() {
        return new FileUtils();
    }

    /**
     * Empty constructor.
     */
    protected FileUtils() {
    }

    /**
     * Checks whether a given file is a symbolic link.
     *
     * <p>It doesn't really test for symbolic links but whether the
     * canonical and absolute paths of the file are identical - this
     * may lead to false positives on some platforms.</p>
     *
     * @param parent the parent directory of the file to test
     * @param name the name of the file to test.
     * @return true if the file is a symbolic link.
     */
    public boolean isSymbolicLink(File parent, String name)
        throws IOException
    {
        File resolvedParent = new File( parent.getCanonicalPath() );
        File toTest = new File( resolvedParent, name );
        return !toTest.getAbsolutePath().equals( toTest.getCanonicalPath() );
    }

    /**
     * Removes a leading path from a second path.
     *
     * @param leading The leading path, must not be null, must be absolute.
     * @param path The path to remove from, must not be null, must be absolute.
     *
     * @return path's normalized absolute if it doesn't start with
     * leading, path's path with leading's path removed otherwise.
     */
    public String removeLeadingPath(File leading, File path) throws IOException {
        String l = normalize(leading.getAbsolutePath()).getAbsolutePath();
        String p = normalize(path.getAbsolutePath()).getAbsolutePath();
        if (l.equals(p)) {
            return "";
        }

        // ensure that l ends with a /
        // so we never think /foo was a parent directory of /foobar
        if (!l.endsWith(File.separator)) {
            l += File.separator;
        }

        if (p.startsWith(l)) {
            return p.substring(l.length());
        } else {
            return p;
        }
    }

    /**
     * &quot;normalize&quot; the given absolute path.
     *
     * <p>This includes:
     * <ul>
     *   <li>Uppercase the drive letter if there is one.</li>
     *   <li>Remove redundant slashes after the drive spec.</li>
     *   <li>resolve all ./, .\, ../ and ..\ sequences.</li>
     *   <li>DOS style paths that start with a drive letter will have
     *     \ as the separator.</li>
     * </ul>
     * Unlike <code>File#getCanonicalPath()</code> it specifically doesn't
     * resolve symbolic links.
     *
     * @param path the path to be normalized
     * @return the normalized version of the path.
     *
     * @throws java.lang.NullPointerException if the file path is
     * equal to null.
     */
    public File normalize(String path) throws IOException {
        String orig = path;

        path = path.replace('/', File.separatorChar)
            .replace('\\', File.separatorChar);

        // make sure we are dealing with an absolute path
        int colon = path.indexOf(":");

        if (!onNetWare) {
            if (!path.startsWith(File.separator)
                && !(path.length() >= 2
                    && Character.isLetter(path.charAt(0))
                    && colon == 1)) {
                String msg = path + " is not an absolute path";
                throw new IOException(msg);
            }
        } else {
            if (!path.startsWith(File.separator)
                && (colon == -1)) {
                String msg = path + " is not an absolute path";
                throw new IOException(msg);
            }
        }

        boolean dosWithDrive = false;
        String root = null;
        // Eliminate consecutive slashes after the drive spec
        if ((!onNetWare && path.length() >= 2
                && Character.isLetter(path.charAt(0))
                && path.charAt(1) == ':')
            || (onNetWare && colon > -1)) {

            dosWithDrive = true;

            char[] ca = path.replace('/', '\\').toCharArray();
            StringBuffer sbRoot = new StringBuffer();
            for (int i = 0; i < colon; i++) {
                sbRoot.append(Character.toUpperCase(ca[i]));
            }
            sbRoot.append(':');
            if (colon + 1 < path.length()) {
                sbRoot.append(File.separatorChar);
            }
            root = sbRoot.toString();

            // Eliminate consecutive slashes after the drive spec
            StringBuffer sbPath = new StringBuffer();
            for (int i = colon + 1; i < ca.length; i++) {
                if ((ca[i] != '\\')
                    || (ca[i] == '\\' && ca[i - 1] != '\\')) {
                    sbPath.append(ca[i]);
                }
            }
            path = sbPath.toString().replace('\\', File.separatorChar);

        } else {
            if (path.length() == 1) {
                root = File.separator;
                path = "";
            } else if (path.charAt(1) == File.separatorChar) {
                // UNC drive
                root = File.separator + File.separator;
                path = path.substring(2);
            } else {
                root = File.separator;
                path = path.substring(1);
            }
        }

        Stack s = new Stack();
        s.push(root);
        StringTokenizer tok = new StringTokenizer(path, File.separator);
        while (tok.hasMoreTokens()) {
            String thisToken = tok.nextToken();
            if (".".equals(thisToken)) {
                continue;
            } else if ("..".equals(thisToken)) {
                if (s.size() < 2) {
                    throw new IOException("Cannot resolve path " + orig);
                } else {
                    s.pop();
                }
            } else { // plain component
                s.push(thisToken);
            }
        }

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.size(); i++) {
            if (i > 1) {
                // not before the filesystem root and not after it, since root
                // already contains one
                sb.append(File.separatorChar);
            }
            sb.append(s.elementAt(i));
        }


        path = sb.toString();
        if (dosWithDrive) {
            path = path.replace('/', '\\');
        }
        return new File(path);
    }

}
