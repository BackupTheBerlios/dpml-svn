/*
 * Copyright (c) 1996-2003 Bali Automation. All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software
 * and its documentation for NON-COMMERCIAL purposes and without
 * fee is hereby granted provided that this copyright notice
 * appears in all copies. Please refer to the file "copyright.html"
 * for further important copyright and licensing information.
 *
 * BALI AUTOMATION MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE 
 * SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING 
 * BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. BALI AUTOMATION
 * SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A 
 * RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS 
 * DERIVATIVES.
 */
package org.apache.metro.facilities.reflector;

public class ReflectionException extends Exception
{
    static final long serialVersionUID = 1L;

    public ReflectionException()
    {
        super();
    }

    public ReflectionException( String message )
    {
        super(message);
    }

    public ReflectionException( String message, Exception exception )
    {
        super(message, exception);
    }
}
