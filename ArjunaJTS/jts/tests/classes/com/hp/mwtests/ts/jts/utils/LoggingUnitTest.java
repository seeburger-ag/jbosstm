/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
 * as indicated by the @author tags. 
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
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Ltd,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: xidcheck.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.utils;

import org.junit.Test;

import com.arjuna.ats.jts.logging.FacilityCode;
import com.hp.mwtests.ts.jts.resources.TestBase;

import static org.junit.Assert.*;

public class LoggingUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        FacilityCode fc = new FacilityCode();
        
        assertEquals(fc.getLevel("FAC_OTS"), FacilityCode.FAC_OTS);
        assertEquals(fc.getLevel("FAC_INTERCEPTOR"), FacilityCode.FAC_INTERCEPTOR);
        assertEquals(fc.getLevel("foobar"), FacilityCode.FAC_NONE);
        
        assertEquals(fc.printString(FacilityCode.FAC_OTS), "FAC_OTS");
        assertEquals(fc.printString(FacilityCode.FAC_INTERCEPTOR), "FAC_INTERCEPTOR");
    }
}
