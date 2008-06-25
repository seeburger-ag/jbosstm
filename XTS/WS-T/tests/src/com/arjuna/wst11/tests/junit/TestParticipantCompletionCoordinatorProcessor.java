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
package com.arjuna.wst11.tests.junit;

import java.util.HashMap;
import java.util.Map;

import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsba.ParticipantCompletionCoordinatorInboundEvents;
import com.arjuna.webservices11.wsba.processors.ParticipantCompletionCoordinatorProcessor;
import com.arjuna.webservices.SoapFault;
import org.oasis_open.docs.ws_tx.wsba._2006._06.NotificationType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.ExceptionType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.StatusType;

import javax.xml.ws.addressing.AddressingProperties;


public class TestParticipantCompletionCoordinatorProcessor extends ParticipantCompletionCoordinatorProcessor
{
    private Map messageIdMap = new HashMap() ;

    public ParticipantCompletionCoordinatorDetails getParticipantCompletionCoordinatorDetails(final String messageId, final long timeout)
    {
        final long endTime = System.currentTimeMillis() + timeout ;
        synchronized(messageIdMap)
        {
            long now = System.currentTimeMillis() ;
            while(now < endTime)
            {
                final ParticipantCompletionCoordinatorDetails details = (ParticipantCompletionCoordinatorDetails)messageIdMap.remove(messageId) ;
                if (details != null)
                {
                    return details ;
                }
                try
                {
                    messageIdMap.wait(endTime - now) ;
                }
                catch (final InterruptedException ie) {} // ignore
                now = System.currentTimeMillis() ;
            }
            final ParticipantCompletionCoordinatorDetails details = (ParticipantCompletionCoordinatorDetails)messageIdMap.remove(messageId) ;
            if (details != null)
            {
                return details ;
            }
        }
        throw new NullPointerException("Timeout occurred waiting for id: " + messageId) ;
    }

    /**
     * Activate the coordinator.
     *
     * @param coordinator The coordinator.
     * @param identifier       The identifier.
     */
    public void activateCoordinator(ParticipantCompletionCoordinatorInboundEvents coordinator, String identifier) {
    }

    /**
     * Deactivate the coordinator.
     *
     * @param coordinator The coordinator.
     */
    public void deactivateCoordinator(ParticipantCompletionCoordinatorInboundEvents coordinator) {
    }

    /**
     * Cancelled.
     *
     * @param cancelled         The cancelled notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext     The arjuna context.
     */
    public void cancelled(NotificationType cancelled, AddressingProperties addressingProperties, ArjunaContext arjunaContext) {
        final String messageId = addressingProperties.getMessageID().getURI().toString() ;
        final ParticipantCompletionCoordinatorDetails details = new ParticipantCompletionCoordinatorDetails(addressingProperties, arjunaContext) ;
        details.setCancelled(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    /**
     * Closed.
     *
     * @param closed            The closed notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext     The arjuna context.
     */
    public void closed(NotificationType closed, AddressingProperties addressingProperties, ArjunaContext arjunaContext) {
        final String messageId = addressingProperties.getMessageID().getURI().toString() ;
        final ParticipantCompletionCoordinatorDetails details = new ParticipantCompletionCoordinatorDetails(addressingProperties, arjunaContext) ;
        details.setClosed(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    /**
     * Compensated.
     *
     * @param compensated       The compensated notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext     The arjuna context.
     */
    public void compensated(NotificationType compensated, AddressingProperties addressingProperties, ArjunaContext arjunaContext) {
        final String messageId = addressingProperties.getMessageID().getURI().toString() ;
        final ParticipantCompletionCoordinatorDetails details = new ParticipantCompletionCoordinatorDetails(addressingProperties, arjunaContext) ;
        details.setCompensated(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    /**
     * Completed.
     *
     * @param completed         The completed notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext     The arjuna context.
     */
    public void completed(NotificationType completed, AddressingProperties addressingProperties, ArjunaContext arjunaContext) {
        final String messageId = addressingProperties.getMessageID().getURI().toString() ;
        final ParticipantCompletionCoordinatorDetails details = new ParticipantCompletionCoordinatorDetails(addressingProperties, arjunaContext) ;
        details.setCompleted(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    /**
     * CannotComplete.
     *
     * @param cannotComplete       The cannot complete notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext        The arjuna context.
     */
    public void cannotComplete(NotificationType cannotComplete, AddressingProperties addressingProperties, ArjunaContext arjunaContext)
    {
        final String messageId = addressingProperties.getMessageID().getURI().toString() ;
        final ParticipantCompletionCoordinatorDetails details = new ParticipantCompletionCoordinatorDetails(addressingProperties, arjunaContext) ;
        details.setCannotComplete(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    /**
     * exit
     * @param exit                 The exit notification.
     * @param addressingProperties The addressing context.
     * @param arjunaContext        The arjuna context.
     */
    public void exit(NotificationType exit, AddressingProperties addressingProperties, ArjunaContext arjunaContext)
    {
        final String messageId = addressingProperties.getMessageID().getURI().toString() ;
        final ParticipantCompletionCoordinatorDetails details = new ParticipantCompletionCoordinatorDetails(addressingProperties, arjunaContext) ;
        details.setExit(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    /**
     * Fault.
     *
     * @param fault             The fault exception.
     * @param addressingProperties The addressing context.
     * @param arjunaContext     The arjuna context.
     */
    public void fail(ExceptionType fault, AddressingProperties addressingProperties, ArjunaContext arjunaContext) {
        final String messageId = addressingProperties.getMessageID().getURI().toString() ;
        final ParticipantCompletionCoordinatorDetails details = new ParticipantCompletionCoordinatorDetails(addressingProperties, arjunaContext) ;
        details.setFault(fault) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public void getStatus(NotificationType getStatus, AddressingProperties addressingProperties, ArjunaContext arjunaContext)
    {
        final String messageId = addressingProperties.getMessageID().getURI().toString() ;
        final ParticipantCompletionCoordinatorDetails details = new ParticipantCompletionCoordinatorDetails(addressingProperties, arjunaContext) ;
        details.setGetStatus(true) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    /**
     * Status.
     *
     * @param status            The status.
     * @param addressingProperties The addressing context.
     * @param arjunaContext     The arjuna context.
     */
    public void status(StatusType status, AddressingProperties addressingProperties, ArjunaContext arjunaContext) {
        final String messageId = addressingProperties.getMessageID().getURI().toString() ;
        final ParticipantCompletionCoordinatorDetails details = new ParticipantCompletionCoordinatorDetails(addressingProperties, arjunaContext) ;
        details.setStatus(status) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    /**
     * SOAP fault.
     *
     * @param soapFault         The SOAP fault.
     * @param addressingProperties The addressing context.
     * @param arjunaContext     The arjuna context.
     */
    public void soapFault(SoapFault soapFault, AddressingProperties addressingProperties, ArjunaContext arjunaContext) {
        final String messageId = addressingProperties.getMessageID().getURI().toString() ;
        final ParticipantCompletionCoordinatorDetails details = new ParticipantCompletionCoordinatorDetails(addressingProperties, arjunaContext) ;
        details.setSoapFault(soapFault) ;

        synchronized(messageIdMap)
        {
            messageIdMap.put(messageId, details) ;
            messageIdMap.notifyAll() ;
        }
    }

    public static class ParticipantCompletionCoordinatorDetails
    {
        private final AddressingProperties addressingProperties ;
        private final ArjunaContext arjunaContext ;
        private boolean exit ;
        private boolean getStatus ;
        private boolean cancelled;
        private boolean closed;
        private boolean compensated;
        private boolean completed;
        private boolean cannotComplete;
        private ExceptionType fault;
        private StatusType status;
        private SoapFault soapFault;

        ParticipantCompletionCoordinatorDetails(final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
        {
            this.addressingProperties = addressingProperties ;
            this.arjunaContext = arjunaContext ;
        }

        public AddressingProperties getAddressingProperties()
        {
            return addressingProperties ;
        }

        public ArjunaContext getArjunaContext()
        {
            return arjunaContext ;
        }

        public boolean hasCannotComplete()
        {
            return cannotComplete ;
        }

        void setCannotComplete(final boolean cannotComplete)
        {
            this.cannotComplete = cannotComplete ;
        }

        public boolean hasExit()
        {
            return exit ;
        }

        void setExit(final boolean exit)
        {
            this.exit = exit ;
        }

        public boolean hasGetStatus()
        {
            return getStatus ;
        }

        void setGetStatus(final boolean getStatus)
        {
            this.getStatus = getStatus ;
        }

        public boolean hasCancelled() {
            return cancelled;
        }

        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }

        public boolean hasClosed() {
            return closed;
        }

        public void setClosed(boolean closed) {
            this.closed = closed;
        }

        public boolean hasCompensated() {
            return compensated;
        }

        public void setCompensated(boolean compensated) {
            this.compensated = compensated;
        }

        public boolean hasCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }

        public ExceptionType hasFault() {
            return fault;
        }

        public void setFault(ExceptionType fault) {
            this.fault = fault;
        }

        public StatusType hasStatus() {
            return status;
        }

        public void setStatus(StatusType status) {
            this.status = status;
        }

        public SoapFault hasSoapFault() {
            return soapFault;
        }

        public void setSoapFault(SoapFault soapFault) {
            this.soapFault = soapFault;
        }
    }
}