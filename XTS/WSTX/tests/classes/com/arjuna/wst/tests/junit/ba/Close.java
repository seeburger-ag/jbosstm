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
 * $Id: Close.java,v 1.3.8.1 2005/11/22 10:36:17 kconner Exp $
 */

package com.arjuna.wst.tests.junit.ba;

import com.arjuna.mw.wst.BusinessActivityManager;
import com.arjuna.mw.wst.UserBusinessActivity;
import com.arjuna.wst.tests.common.DemoBusinessParticipant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: Close.java,v 1.3.8.1 2005/11/22 10:36:17 kconner Exp $
 * @since 1.0.
 */

public class Close
{
    @Test
    public void testClose()
            throws Exception
    {
	    UserBusinessActivity uba = UserBusinessActivity.getUserBusinessActivity();
        BusinessActivityManager bam = BusinessActivityManager.getBusinessActivityManager();
	    com.arjuna.wst.BAParticipantManager bpm = null;
        DemoBusinessParticipant p = new DemoBusinessParticipant(DemoBusinessParticipant.CLOSE, "1235");
	    try {
	    uba.begin();
	    
	    bpm = bam.enlistForBusinessAgreementWithParticipantCompletion(p, p.identifier());
        bpm.completed();
        } catch (Exception eouter) {
            try {
                uba.cancel();
            } catch(Exception einner) {
            }
            throw eouter;
        }

	    uba.close();

	    assertTrue(p.passed());
    }
}
