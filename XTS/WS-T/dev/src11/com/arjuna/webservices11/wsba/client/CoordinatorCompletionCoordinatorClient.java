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
package com.arjuna.webservices11.wsba.client;

import com.arjuna.webservices11.wsba.BusinessActivityConstants;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.wsba.client.WSBAClient;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.oasis_open.docs.ws_tx.wsba._2006._06.BusinessAgreementWithCoordinatorCompletionCoordinatorPortType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.ExceptionType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.NotificationType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.StatusType;

import javax.xml.namespace.QName;
import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.AttributedURI;
import javax.xml.ws.addressing.EndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URI;

/**
 * The Client side of the Coordinator Completion Coordinator.
 * @author kevin
 */
public class CoordinatorCompletionCoordinatorClient
{
    /**
     * The client singleton.
     */
    private static final CoordinatorCompletionCoordinatorClient CLIENT = new CoordinatorCompletionCoordinatorClient() ;

    /**
     * The completed action.
     */
    private AttributedURI completedAction = null;
    /**
     * The fault action.
     */
    private AttributedURI failAction = null;
    /**
     * The compensated action.
     */
    private AttributedURI compensatedAction = null;
    /**
     * The closed action.
     */
    private AttributedURI closedAction = null;
    /**
     * The cancelled action.
     */
    private AttributedURI cancelledAction = null;
    /**
     * The exit action.
     */
    private AttributedURI cannotCompleteAction = null;
    /**
     * The exit action.
     */
    private AttributedURI exitAction = null;
    /**
     * The get status action.
     */
    private AttributedURI getStatusAction = null;
    /**
     * The status action.
     */
    private AttributedURI statusAction = null;

    /**
     * The coordinator completion participant URI for replies.
     */
    private EndpointReference coordinatorCompletionParticipant = null;

    /**
     * The coordinator completion participant URI for replies.
     */
    private EndpointReference secureCoordinatorCompletionParticipant = null;

    /**
     * Construct the participant completion coordinator client.
     */
    private CoordinatorCompletionCoordinatorClient()
    {
        final AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
        try {
            completedAction = builder.newURI(BusinessActivityConstants.WSBA_ACTION_COMPLETED);
            failAction = builder.newURI(BusinessActivityConstants.WSBA_ACTION_FAIL);
            compensatedAction = builder.newURI(BusinessActivityConstants.WSBA_ACTION_COMPENSATED);
            closedAction = builder.newURI(BusinessActivityConstants.WSBA_ACTION_CLOSED);
            cancelledAction = builder.newURI(BusinessActivityConstants.WSBA_ACTION_CANCELLED);
            exitAction = builder.newURI(BusinessActivityConstants.WSBA_ACTION_EXIT);
            cannotCompleteAction = builder.newURI(BusinessActivityConstants.WSBA_ACTION_CANNOT_COMPLETE);
            getStatusAction = builder.newURI(BusinessActivityConstants.WSBA_ACTION_GET_STATUS);
            statusAction = builder.newURI(BusinessActivityConstants.WSBA_ACTION_STATUS);
        } catch (URISyntaxException use) {
            // TODO - log fault and throw exception
        }
        //final HandlerRegistry handlerRegistry = new HandlerRegistry() ;

        // Add WS-Addressing
        //AddressingPolicy.register(handlerRegistry) ;
        // Add client policies
        //ClientPolicy.register(handlerRegistry) ;

        final String coordinatorCompletionParticipantURIString =
            ServiceRegistry.getRegistry().getServiceURI(BusinessActivityConstants.COORDINATOR_COMPLETION_PARTICIPANT_SERVICE_NAME, false) ;
        final String secureCoordinatorCompletionParticipantURIString =
            ServiceRegistry.getRegistry().getServiceURI(BusinessActivityConstants.COORDINATOR_COMPLETION_PARTICIPANT_SERVICE_NAME, true) ;
        try {
            URI coordinatorCompletionParticipantURI = new URI(coordinatorCompletionParticipantURIString) ;
            coordinatorCompletionParticipant = builder.newEndpointReference(coordinatorCompletionParticipantURI);
        } catch (URISyntaxException use) {
            // TODO - log fault and throw exception
        }
        try {
            URI secureCoordinatorCompletionParticipantURI = new URI(secureCoordinatorCompletionParticipantURIString) ;
            secureCoordinatorCompletionParticipant = builder.newEndpointReference(secureCoordinatorCompletionParticipantURI);
        } catch (URISyntaxException use) {
            // TODO - log fault and throw exception
        }
    }

    /**
     * Send a completed request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendCompleted(W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        EndpointReference participant = getParticipant(endpoint, addressingProperties);
        AddressingHelper.installFromFaultTo(addressingProperties, participant, identifier);
        BusinessAgreementWithCoordinatorCompletionCoordinatorPortType port;
        port = getPort(endpoint, addressingProperties, completedAction);
        NotificationType completed = new NotificationType();

        port.completedOperation(completed);
    }

    /**                                                                 Address
     * Send a fail request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendFail(W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final InstanceIdentifier identifier,
        final QName exceptionIdentifier)
        throws SoapFault, IOException
    {
        EndpointReference participant = getParticipant(endpoint, addressingProperties);
        AddressingHelper.installFromFaultTo(addressingProperties, participant, identifier);
        BusinessAgreementWithCoordinatorCompletionCoordinatorPortType port;
        port = getPort(endpoint, addressingProperties, failAction);
        ExceptionType fail = new ExceptionType();
        fail.setExceptionIdentifier(exceptionIdentifier);

        port.failOperation(fail);
    }

    /**
     * Send a compensated request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendCompensated(W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        EndpointReference participant = getParticipant(endpoint, addressingProperties);
        AddressingHelper.installFaultTo(addressingProperties, participant, identifier);
        BusinessAgreementWithCoordinatorCompletionCoordinatorPortType port;
        port = getPort(endpoint, addressingProperties, compensatedAction);
        NotificationType compensated = new NotificationType();

        port.compensatedOperation(compensated);
    }

    /**
     * Send a closed request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendClosed(W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        EndpointReference participant = getParticipant(endpoint, addressingProperties);
        AddressingHelper.installFaultTo(addressingProperties, participant, identifier);
        BusinessAgreementWithCoordinatorCompletionCoordinatorPortType port;
        port = getPort(endpoint, addressingProperties, closedAction);
        NotificationType closed = new NotificationType();

        port.closedOperation(closed);
    }

    /**
     * Send a cancelled request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendCancelled(W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        EndpointReference participant = getParticipant(endpoint, addressingProperties);
        AddressingHelper.installFaultTo(addressingProperties, participant, identifier);
        BusinessAgreementWithCoordinatorCompletionCoordinatorPortType port;
        port = getPort(endpoint, addressingProperties, cancelledAction);
        NotificationType camcelled = new NotificationType();

        port.canceledOperation(camcelled);
    }

    /**
     * Send an exit request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendExit(W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        EndpointReference participant = getParticipant(endpoint, addressingProperties);
        AddressingHelper.installFromFaultTo(addressingProperties, participant, identifier);
        BusinessAgreementWithCoordinatorCompletionCoordinatorPortType port;
        port = getPort(endpoint, addressingProperties, exitAction);
        NotificationType exited = new NotificationType();

        port.exitOperation(exited);
    }

    /**
     * Send a cannot complete request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendCannotComplete(W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        EndpointReference participant = getParticipant(endpoint, addressingProperties);
        AddressingHelper.installFromFaultTo(addressingProperties, participant, identifier);
        BusinessAgreementWithCoordinatorCompletionCoordinatorPortType port;
        port = getPort(endpoint, addressingProperties, cannotCompleteAction);
        NotificationType cannotComplete = new NotificationType();

        port.cannotComplete(cannotComplete);
    }

    /**
     * Send a get status request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendGetStatus(W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        EndpointReference participant = getParticipant(endpoint, addressingProperties);
        AddressingHelper.installFromFaultTo(addressingProperties, participant, identifier);
        BusinessAgreementWithCoordinatorCompletionCoordinatorPortType port;
        port = getPort(endpoint, addressingProperties, getStatusAction);
        NotificationType getStatus = new NotificationType();

        port.getStatusOperation(getStatus);
    }

    /**
     * Send a status request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendStatus(W3CEndpointReference endpoint, final AddressingProperties addressingProperties, final InstanceIdentifier identifier,
        final QName state)
        throws SoapFault, IOException
    {
        EndpointReference participant = getParticipant(endpoint, addressingProperties);
        AddressingHelper.installFromFaultTo(addressingProperties, participant, identifier);
        BusinessAgreementWithCoordinatorCompletionCoordinatorPortType port;
        port = getPort(endpoint, addressingProperties, statusAction);
        StatusType status = new StatusType();
        status.setState(state);

        port.statusOperation(status);
    }

    /**
     * return a participant endpoint appropriate to the type of coordinator
     * @param endpoint
     * @return either the secure participant endpoint or the non-secure endpoint
     */
    EndpointReference getParticipant(W3CEndpointReference endpoint, AddressingProperties addressingProperties)
    {
        String address;
        if (endpoint != null) {
            address = endpoint.getAddress();
        } else {
            address = addressingProperties.getTo().getURI().toString();
        }

        if (address.startsWith("https")) {
            return secureCoordinatorCompletionParticipant;
        } else {
            return coordinatorCompletionParticipant;
        }
    }

    /**
     * Get the Completion Coordinator client singleton.
     * @return The Completion Coordinator client singleton.
     */
    public static CoordinatorCompletionCoordinatorClient getClient()
    {
        return CLIENT ;
    }

    /**
     * obtain a port from the coordinator endpoint configured with the instance identifier handler and the supplied
     * addressing properties supplemented with the given action
     * @param participant
     * @param addressingProperties
     * @param action
     * @return
     */
    private BusinessAgreementWithCoordinatorCompletionCoordinatorPortType
    getPort(final W3CEndpointReference participant, final AddressingProperties addressingProperties, final AttributedURI action)
    {
        AddressingHelper.installNoneReplyTo(addressingProperties);
        if (participant != null) {
            return WSBAClient.getCoordinatorCompletionCoordinatorPort(participant, action, addressingProperties);
        } else {
            return WSBAClient.getCoordinatorCompletionCoordinatorPort(action, addressingProperties);
        }
    }
}