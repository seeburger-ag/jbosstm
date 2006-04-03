/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
 * by the @authors tag. All rights reserved. 
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: LockResult.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.txoj;

import java.io.PrintWriter;

/**
 * The various results which can occur when setting a lock.
 *
 * @author Mark Little (mark@arjuna.com)
 * @version $Id: LockResult.java 2342 2006-03-30 13:06:17Z  $
 * @since JTS 1.0.
 */

public class LockResult
{
    
    public static final int GRANTED = 0;
    public static final int REFUSED = 1;
    public static final int RELEASED = 2;

    public static String stringForm (int l)
    {
	switch (l)
	{
	case GRANTED:
	    return "LockResult.GRANTED";
	case REFUSED:
	    return "LockResult.REFUSED";
	case RELEASED:
	    return "LockResult.RELEASED";
	default:
	    return "Unknown";
	}
    }
    
    /**
     * Print a human-readable form of the lock result.
     */

    public static void print (PrintWriter strm, int l)
    {
	strm.print(stringForm(l));
    }
	
}
