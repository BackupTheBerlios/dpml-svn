/*
 * Copyright  2002-2004 The Apache Software Foundation
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

package org.apache.avalon.composition.model.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.avalon.composition.data.ExcludeDirective;
import org.apache.avalon.composition.data.IncludeDirective;
import org.apache.avalon.composition.model.FilesetModel;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;


/**
 * Implementation of a <code>FilesetModel</code> in which a set
 * of <code>IncludeDirective</code> objects, a set of
 * <code>ExcludeDirective</code> objects, a set of default
 * includes and excludes, and a base directory anchor are used
 * to resolve and build a set of files specified by a
 * <code>FilesetDirective</code>.
 * 
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 * @version $Id: DefaultFilesetModel.java 30977 2004-07-30 08:57:54Z niclas $
 */
public class DefaultFilesetModel extends AbstractLogEnabled
    implements FilesetModel
{
    /**
     * Base directory anchor from which to begin filset resolution.
     */
    private File m_anchor = null;
    /**
     * Array of <code>IncludeDirective</code> objects to use during
     * fileset resolution.
     */
    private IncludeDirective[] m_includes = null;
    /**
     * Array of <code>ExcludeDirective</code> objects to use during
     * fileset resolution.
     */
    private ExcludeDirective[] m_excludes = null;
    /**
     * Array of <code>String</code> objects to use as a default set
     * of fileset includes.
     */
    private String[] m_defaultIncludes = null;
    /**
     * Array of <code>String</code> objects to use as a default set
     * of fileset excludes.
     */
    private String[] m_defaultExcludes = null;
    /**
     * Array of <code>File</code> objects representing the results
     * of fileset resolution.
     */
    private ArrayList m_list = null;
    /**
     * Container-supplied logger instance.
     */
    private final Logger m_logger;
    
    /**
     * Constructs a new default fileset model.
     * 
     * @param anchor base directory anchor from which to begin
     * fileset resolution
     * @param includes array of <code>IncludeDirective</code> objects
     * to use during fileset resolution
     * @param excludes array of <code>ExcludeDirective</code> objects
     * to use during fileset resolution
     * @param defaultIncludes array of <code>String</code> objects
     * to use as a default set of fileset includes
     * @param defaultExcludes array of <code>String</code> objects
     * to use as a default set of fileset excludes
     * @param logger <code>Logger</code> for the fileset model to use
     */
    public DefaultFilesetModel(File anchor, IncludeDirective[] includes,
            ExcludeDirective[] excludes, String[] defaultIncludes,
            String[] defaultExcludes, Logger logger)
    {
        m_logger = logger;
        m_list = new ArrayList();
        setBaseDirectory( anchor );
        setIncludeDirectives( includes );
        setExcludeDirectives( excludes );
        setDefaultIncludes( defaultIncludes );
        setDefaultExcludes( defaultExcludes );
    }
    
    /**
     * Establishes the base directory anchor for the fileset resolution.
     * 
     * @param anchor the base directory anchor
     */
    private void setBaseDirectory(File anchor) {
        m_anchor = anchor;
    }

    /**
     * Establishes the set of <code>IncludeDirective</code> objects
     * to use during fileset resolution.
     * 
     * @param includes array of <code>IncludeDirective</code> objects
     */
    private void setIncludeDirectives(IncludeDirective[] includes) {
        m_includes = includes;
    }

    /**
     * Establishes the set of <code>ExcludeDirective</code> objects
     * to use during fileset resolution.
     * 
     * @param excludes array of <code>ExcludeDirectives</code>
     */
    private void setExcludeDirectives(ExcludeDirective[] excludes) {
        m_excludes = excludes;
    }

    /**
     * Establishes a set of default includes to use during fileset
     * resolution in lieu of an explicit specification of a set
     * of <code>IncludeDirective</code> objects.
     * 
     * @param defaultIncludes array of <code>String</code> objects
     * representing a set of default fileset includes
     */
    private void setDefaultIncludes(String[] defaultIncludes) {
        if (defaultIncludes == null) {
            //m_defaultIncludes = new String[1];
            //m_defaultIncludes[0] = "*.jar";
            m_defaultIncludes = new String[0];
        } else {
            m_defaultIncludes = defaultIncludes;
        }
    }

    /**
     * Establishes a set of default excludes to use during fileset
     * resolution in lieu of an explicit specification of a set
     * of <code>ExcludeDirective</code> objects.
     * 
     * @param defaultExcludes array of <code>String</code> objects
     * representing a set of default fileset excludes
     */
    private void setDefaultExcludes(String[] defaultExcludes) {
        if (defaultExcludes == null) {
            m_defaultExcludes = new String[0];
        } else {
            m_defaultExcludes = defaultExcludes;
        }
    }

    /**
     * Returns a set of <code>File</code> objects representing the
     * results of the fileset resolution.  This array will contain
     * fully qualified filenames based on the base directory anchor.
     * 
     * @return an array of files to include in the classpath
     */
    public ArrayList getIncludes() {
        return m_list;
    }

    /**
     * Resolves the specified include and exclude directives from
     * the base directory anchor and produces an array of files
     * to include in the classpath.
     */
    public void resolveFileset() throws IOException, IllegalStateException {
        // sanity check...
        if (m_anchor == null) {
            throw new IllegalStateException("No basedir set");
        }
        if (!m_anchor.exists()) {
            throw new IllegalStateException("basedir " + m_anchor
                                            + " does not exist");
        }
        
        if (!m_anchor.isDirectory()) {
            throw new IllegalStateException("basedir " + m_anchor
                                            + " is not a directory");
        }
        
        // Return the directory attribute as a classpath if there are no
        // includes
        if ( m_includes.length == 0 && m_defaultIncludes.length == 0 ) {
            m_list.add( m_anchor );
            m_logger.debug("candidates=[" + m_anchor + "]");
            return;
        }
        
        // create a directory scanner
        DirectoryScanner ds = new DirectoryScanner();
        ds.setLogger(m_logger);

        // Supply the directory scanner with our base directory anchor
        ds.setBasedir( m_anchor );
        m_logger.debug( "ds.basedir=[" + ds.getBasedir() + "]" );

        // Any default excludes to add?
        for (int i = 0; i < m_defaultExcludes.length; i++ )
        {
            m_logger.debug("m_defaultExcludes[" + i + "]=[" + m_defaultExcludes[i] + "]");
            ds.addDefaultExclude( m_defaultExcludes[i] );
        }

        // Supply the directory scanner with our set of includes.
        // The scanner wants the includes in the form of String[],
        // but we have them in the form of IncludeDirective[].
        // So.. we need to first convert...
        String[] includes = new String[ m_includes.length ];
        if (m_includes.length == 0)
        {
            for (int i = 0; i < m_defaultIncludes.length; i++)
            {
                includes[i] = m_defaultIncludes[i];
                m_logger.debug("includes[" + i + "]=[" + includes[i] + "]");
            }
        }
        else
        {
            for (int i = 0; i < m_includes.length; i++ )
            {
                includes[i] = m_includes[i].getPath();
                m_logger.debug("includes[" + i + "]=[" + includes[i] + "]");
            }
        }
        ds.setIncludes( includes );

        // Same thing for the set of excludes...
        String[] excludes = new String[ m_excludes.length ];
        for (int i = 0; i < m_excludes.length; i++ )
        {
            excludes[i] = m_excludes[i].getPath();
            m_logger.debug("excludes[" + i + "]=[" + excludes[i] + "]");
        }
        ds.setExcludes( excludes );


        // Make the scanner pay attention to filesystem case sensitivity
        ds.setCaseSensitive( true );

        // Scan the directory (which doesn't do much right now) and output
        // some stuff to debug
        ds.scan();
        m_logger.debug( ds.toString() );
        String[] candidates = ds.getIncludedFiles();
        if ( candidates.length > 0 ) {
            for (int i = 0; i < candidates.length; i++) {
                File file = new File( m_anchor, candidates[i] );
                m_logger.debug("candidates[" + i + "]=[" + file.getAbsolutePath() + "]");
                m_list.add( file );
            }
        } else {
            m_list.add( m_anchor );
            m_logger.debug("candidates=[" + m_anchor + "]");
        }
    }
}
