/*
 *     Copyright 2004. The Apache Software Foundation.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
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
package org.apache.metro.studio.eclipse.ui.tools;

import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import org.eclipse.jface.text.contentassist.IContextInformation;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * @author Andreas Develop
 * @author <a href="mailto:dev@avalon.apache.org">Avalon Development Team</a>
 *
 */
public class AvalonProposals 
    implements IJavaCompletionProposal
{
    private int p_offset;
    private int p_length;
    private String key;
    private String proposal;
    private String tag;
    
    public AvalonProposals()
    {
        super();
    }
    public AvalonProposals( int off, int len )
    {
        p_offset = off;
        p_length = len;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jdt.ui.text.java.IJavaCompletionProposal#getRelevance()
     */
    public int getRelevance()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#apply(org.eclipse.jface.text.IDocument)
     */
    public void apply( IDocument pDocument )
    {
        try
        {
            pDocument.replace( p_offset, 0, tag );
        } catch( BadLocationException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getSelection(org.eclipse.jface.text.IDocument)
     */
    public Point getSelection( IDocument pDocument )
    {
 
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getAdditionalProposalInfo()
     */
    public String getAdditionalProposalInfo()
    {
        return proposal;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getDisplayString()
     */
    public String getDisplayString()
    {
        return key;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getImage()
     */
    public Image getImage()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getContextInformation()
     */
    public IContextInformation getContextInformation()
    {
        // TODO Auto-generated method stub
        return null;
    }
    /**
     * @param pString
     */
    public void setKey( String pString )
    {
        key = pString;
    }
    /**
     * @param pString
     */
    public void setDescription( String pString )
    {
        proposal = pString;
    }
    /**
     * @param pString
     */
    public void setTag( String pString )
    {
        tag = pString;
    }
}
