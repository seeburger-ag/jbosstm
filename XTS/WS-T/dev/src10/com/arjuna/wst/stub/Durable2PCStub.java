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
 * Copyright (c) 2003, 2004,
 *
 * Arjuna Technologies Limited.
 *
 * $Id: Durable2PCStub.java,v 1.1.2.3 2004/06/18 15:06:09 nmcl Exp $
 */

package com.arjuna.wst.stub;

import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.wst.Durable2PCParticipant;

public class Durable2PCStub extends ParticipantStub implements Durable2PCParticipant
{
    // default ctor for crash recovery
    public Durable2PCStub() throws Exception {
        super(null, true, null);
    }

    public Durable2PCStub(final String id, final EndpointReferenceType twoPCParticipant)
        throws Exception
    {
        super(id, true, twoPCParticipant) ;
    }
}
