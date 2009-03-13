/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2007,
 * @author JBoss, a division of Red Hat.
 */
package com.hp.mwtests.ts.arjuna.common;

import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;

import com.arjuna.ats.arjuna.coordinator.TransactionReaper;
import com.arjuna.ats.arjuna.coordinator.Reapable;
import com.arjuna.ats.arjuna.coordinator.TxControl;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.arjuna.coordinator.ReaperElement;
import com.hp.mwtests.ts.arjuna.resources.BasicObject;

import java.util.SortedSet;
import java.util.TreeSet;

public class PersistenceUnitTest extends TestCase
{
    public static Test suite ()
    {
        return new TestSuite(PersistenceUnitTest.class);
    }

    public void testSaveRestore () throws Exception
    {
        final BasicObject obj = new BasicObject();
        final Uid objUid = obj.get_uid();

        obj.set(1234);
        obj.deactivate();
        
        final BasicObject rec = new BasicObject(objUid);
        int res = rec.get();

        assertEquals(res, 1234);
    }
}
