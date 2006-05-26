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
package com.arjuna.webservices.wsba.processors;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.base.processors.Callback;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsba.ExceptionType;
import com.arjuna.webservices.wsba.NotificationType;
import com.arjuna.webservices.wsba.StatusType;


/**
 * The Coordinator Completion Coordinator callback.
 * @author kevin
 */
public abstract class CoordinatorCompletionCoordinatorCallback extends Callback
{
    /**
     * A cancelled response.
     * @param cancelled The cancelled notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void cancelled(final NotificationType cancelled, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;
    
    /**
     * A closed response.
     * @param closed The closed notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void closed(final NotificationType closed, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;
    
    /**
     * A compensated response.
     * @param compensated The compensated notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void compensated(final NotificationType compensated, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;

    /**
     * A completed response.
     * @param completed The completed notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void completed(final NotificationType completed, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext) ;
    
    /**
     * A Status.
     * @param status The status notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void status(final StatusType status, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;
    
    /**
     * A fault response.
     * @param fault The fault notification.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void fault(final ExceptionType fault, final AddressingContext addressingContext, final ArjunaContext arjunaContext) ;
    
    /**
     * A SOAP fault response.
     * @param soapFault The SOAP fault.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public abstract void soapFault(final SoapFault soapFault, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext) ;
}
