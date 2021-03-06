/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: SuspendResumeParticipants.java,v 1.6.8.1 2005/11/22 10:36:12 kconner Exp $
 */

package com.arjuna.wst11.tests.junit.basic;

import com.arjuna.mw.wst11.TransactionManager;
import com.arjuna.mw.wst.TxContext;
import com.arjuna.mw.wst11.UserTransaction;
import com.arjuna.wst.tests.common.DemoDurableParticipant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: SuspendResumeParticipants.java,v 1.6.8.1 2005/11/22 10:36:12 kconner Exp $
 * @since 1.0.
 */

public class SuspendResumeParticipants
{

    @Test
    public void testSuspendResumeParticipants()
            throws Exception
    {
	    UserTransaction ut = UserTransaction.getUserTransaction();
	    TransactionManager tm = TransactionManager.getTransactionManager();
	    DemoDurableParticipant p1 = new DemoDurableParticipant();
	    DemoDurableParticipant p2 = new DemoDurableParticipant();
	    DemoDurableParticipant p3 = new DemoDurableParticipant();
	    DemoDurableParticipant p4 = new DemoDurableParticipant();

	    System.out.println("Starting first transaction.\n");

	    ut.begin();
    try {
	    tm.enlistForDurableTwoPhase(p1, p1.identifier());
	    tm.enlistForDurableTwoPhase(p2, p2.identifier());
	    tm.enlistForDurableTwoPhase(p3, p3.identifier());
	    tm.enlistForDurableTwoPhase(p4, p4.identifier());

	    TxContext ctx = tm.suspend();

	    System.out.println("Suspended: "+ctx);

	    ut.begin();

	    System.out.println("\nStarted second transaction.");

	    tm.resume(ctx);

	    System.out.println("\nCommitting first transaction.\n");
    }  catch (Exception eouter) {
        try {
            ut.rollback();
        } catch(Exception einner) {
        }
        throw eouter;
    }
	    ut.commit();
	}
}