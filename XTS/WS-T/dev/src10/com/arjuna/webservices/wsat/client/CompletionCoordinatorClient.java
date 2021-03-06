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
package com.arjuna.webservices.wsat.client;

import java.io.IOException;

import com.arjuna.webservices.HandlerRegistry;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.SoapService;
import com.arjuna.webservices.base.policy.ClientPolicy;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsaddr.client.BaseWSAddrClient;
import com.arjuna.webservices.wsaddr.policy.AddressingPolicy;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsat.AtomicTransactionConstants;
import com.arjuna.webservices.wsat.NotificationType;

/**
 * The Client side of the Completion Coordinator.
 * @author kevin
 */
public class CompletionCoordinatorClient extends BaseWSAddrClient
{
    /**
     * The client singleton.
     */
    private static final CompletionCoordinatorClient CLIENT = new CompletionCoordinatorClient() ;
    
    /**
     * The commit action.
     */
    private final AttributedURIType commitAction =
        new AttributedURIType(AtomicTransactionConstants.WSAT_ACTION_COMMIT) ;
    /**
     * The rollback action.
     */
    private final AttributedURIType rollbackAction =
        new AttributedURIType(AtomicTransactionConstants.WSAT_ACTION_ROLLBACK) ;
    
    /**
     * The SOAP service representing the client.
     */
    private final SoapService soapService ;
    /**
     * The completion initiator URI for replies.
     */
    private final AttributedURIType completionInitiator ;
    
    /**
     * Construct the completion coordinator client.
     */
    private CompletionCoordinatorClient()
    {
        final HandlerRegistry handlerRegistry = new HandlerRegistry() ;
        
        // Add WS-Addressing
        AddressingPolicy.register(handlerRegistry) ;
        // Add client policies
        ClientPolicy.register(handlerRegistry) ;
        
        soapService = new SoapService(handlerRegistry) ;
        final String completionInitiatorURI =
            SoapRegistry.getRegistry().getServiceURI(AtomicTransactionConstants.SERVICE_COMPLETION_INITIATOR) ;
        completionInitiator = new AttributedURIType(completionInitiatorURI) ;
    }
    
    /**
     * Send a commit request.
     * @param addressingContext addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendCommit(final AddressingContext addressingContext, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        final EndpointReferenceType endpointReference = getEndpointReference(identifier) ;
        sendOneWay(new NotificationType(), addressingContext, soapService, endpointReference,
            endpointReference, AtomicTransactionConstants.WSAT_ELEMENT_COMMIT_QNAME,
            commitAction) ;
    }
    
    /**
     * Send a rollback request.
     * @param addressingContext addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendRollback(final AddressingContext addressingContext, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        final EndpointReferenceType endpointReference = getEndpointReference(identifier) ;
        sendOneWay(new NotificationType(), addressingContext, soapService, endpointReference,
                endpointReference, AtomicTransactionConstants.WSAT_ELEMENT_ROLLBACK_QNAME,
            rollbackAction) ;
    }
    
    /**
     * Get the endpoint reference for the specified identifier.
     * @param identifier The endpoint reference identifier.
     * @return The endpoint reference.
     */
    private EndpointReferenceType getEndpointReference(final InstanceIdentifier identifier)
    {
        final EndpointReferenceType completionInitiatorEndpoint = new EndpointReferenceType(completionInitiator) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(completionInitiatorEndpoint, identifier) ;
        return completionInitiatorEndpoint ;
    }
    
    /**
     * Get the Completion Coordinator client singleton.
     * @return The Completion Coordinator client singleton.
     */
    public static CompletionCoordinatorClient getClient()
    {
        return CLIENT ;
    }
}
