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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.jboss.transaction.wstf.webservices.sc007.participant;

import com.arjuna.ats.arjuna.common.Uid;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import com.arjuna.wst.ReadOnly;
import com.arjuna.wst.SystemException;
import com.arjuna.wst.Volatile2PCParticipant;
import com.arjuna.wst.Vote;
import com.arjuna.wst.WrongStateException;
import com.jboss.transaction.wstf.webservices.sc007.InteropUtil;

/**
 * The VolatileAndDurable volatile 2PC participant
 */
public class VolatileAndDurableVolatile2PCParticipant extends ParticipantAdapter implements Volatile2PCParticipant
{
    /**
     * The current coordination context.
     */
    private final CoordinationContextType coordinationContext ;
    
    /**
     * Construct the participant.
     * @param coordinationContext The coordination context.
     */
    public VolatileAndDurableVolatile2PCParticipant(final CoordinationContextType coordinationContext)
    {
        this.coordinationContext = coordinationContext ;
    }
    
    /**
     * Vote to prepare.
     */
    public Vote prepare()
        throws WrongStateException, SystemException
    {
        try
        {
            InteropUtil.registerDurable2PC(coordinationContext, new VolatileAndDurableDurable2PCParticipant(), new Uid().toString()) ;
        }
        catch (final Throwable th)
        {
            throw new SystemException(th.getMessage()) ;
        }
        return new ReadOnly() ;
    }    
}