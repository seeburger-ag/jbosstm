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
 * Copyright (C) 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: FdCache.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.objectstore;

import com.arjuna.ats.arjuna.common.Uid;
import java.io.*;

class CacheEntry
{
    
public void CacheEntry (File f)
    {
	_theFile = f;
    }
    
public File file ()
    {
	return _theFile;
    }
    
CacheEntry _next;
CacheEntry _prev;

private File _theFile;
    
}

class FdCache
{

public FdCache ()
    {
	this(FdCache.cacheSize);
    }

public FdCache (int size)
    {
	_head = _tail = null;
    }

/* XXX: Not yet finished
public synchronized File scanCache (Uid u)
    {
    }
    
public synchronized File enterIntoCache (Uid u)
    {
    }
    
public void purgeFromCache (Uid u)
    {
    }
    
private boolean compactCache ()
    {
    }
*/
    
private CacheEntry _head;
private CacheEntry _tail;
    
private static final int cacheSize = 256;

}
