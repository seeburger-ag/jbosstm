/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (c) 2002, 2003, Arjuna Technologies Limited.
 *
 * TestRegistrar.java
 */

package com.arjuna.wsc.tests;

import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.wsc.AlreadyRegisteredException;
import com.arjuna.wsc.InvalidProtocolException;
import com.arjuna.wsc.InvalidStateException;
import com.arjuna.wsc.NoActivityException;
import com.arjuna.wsc.Registrar;

public class TestRegistrar implements Registrar
{
    public void install(String protocolIdentifier)
    {
    }

    public EndpointReferenceType register(EndpointReferenceType participantProtocolService, String protocolIdentifier, InstanceIdentifier instanceIdentifier)
        throws AlreadyRegisteredException, InvalidProtocolException, InvalidStateException, NoActivityException
    {
        if (protocolIdentifier.equals(TestUtil.ALREADY_REGISTERED_PROTOCOL_IDENTIFIER))
            throw new AlreadyRegisteredException();
        else if (protocolIdentifier.equals(TestUtil.INVALID_PROTOCOL_PROTOCOL_IDENTIFIER))
            throw new InvalidProtocolException();
        else if (protocolIdentifier.equals(TestUtil.INVALID_STATE_PROTOCOL_IDENTIFIER))
            throw new InvalidStateException();
        else if (protocolIdentifier.equals(TestUtil.NO_ACTIVITY_PROTOCOL_IDENTIFIER))
            throw new NoActivityException();

        return new EndpointReferenceType(new AttributedURIType(TestUtil.PROTOCOL_COORDINATOR_SERVICE)) ;
    }

    public void uninstall(String protocolIdentifier)
    {
    }
}
