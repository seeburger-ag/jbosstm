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
package com.arjuna.wsc.messaging;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFault10;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.logging.WSCLogger;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wscoor.CoordinationConstants;
import com.arjuna.webservices.wscoor.RegisterType;
import com.arjuna.webservices.wscoor.client.RegistrationRequesterClient;
import com.arjuna.webservices.wscoor.processors.RegistrationCoordinatorProcessor;
import com.arjuna.wsc.AlreadyRegisteredException;
import com.arjuna.wsc.InvalidProtocolException;
import com.arjuna.wsc.InvalidStateException;
import com.arjuna.wsc.NoActivityException;
import com.arjuna.wsc.Registrar;
import com.arjuna.wsc.RegistrarMapper;


/**
 * The Registration Coordinator processor.
 * @author kevin
 */
public class RegistrationCoordinatorProcessorImpl extends RegistrationCoordinatorProcessor
{
    /**
     * Register the participant in the protocol.
     * @param register The register request.
     * @param addressingContext The addressing context.
     * @param arjunaContext The arjuna context.
     */
    public void register(final RegisterType register, final AddressingContext addressingContext,
        final ArjunaContext arjunaContext)
    {
        final RegistrarMapper registrarMapper = RegistrarMapper.getFactory() ;
        try
        {
            final String protocolIdentifier = register.getProtocolIdentifier().getValue() ;
            final Registrar registrar = registrarMapper.getRegistrar(protocolIdentifier) ;
            
            if (registrar != null)
            {
                final EndpointReferenceType coordinationProtocolService ;
                final AddressingContext responseAddressingContext ;
                try
                {
                    final EndpointReferenceType participantProtocolService = register.getParticipantProtocolService() ;
                    final InstanceIdentifier instanceIdentifier = arjunaContext.getInstanceIdentifier();
                    coordinationProtocolService = registrar.register(participantProtocolService, protocolIdentifier, instanceIdentifier) ;
                    
                    responseAddressingContext = AddressingContext.createResponseContext(addressingContext, MessageId.getMessageId()) ;
                }
                catch (final AlreadyRegisteredException alreadyRegisteredException)
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                    final SoapFault soapFault = new SoapFault10(SoapFaultType.FAULT_SENDER, CoordinationConstants.WSCOOR_ERROR_CODE_ALREADY_REGISTERED_QNAME,
                            WSCLogger.i18NLogger.get_messaging_RegistrationCoordinatorProcessorImpl_1()) ;
                    RegistrationRequesterClient.getClient().sendSoapFault(faultAddressingContext, soapFault) ;
                    return ;
                }
                catch (final InvalidProtocolException invalidProtocolException)
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                    final SoapFault soapFault = new SoapFault10(SoapFaultType.FAULT_SENDER, CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_PROTOCOL_QNAME,
                            WSCLogger.i18NLogger.get_messaging_RegistrationCoordinatorProcessorImpl_2()) ;
                    RegistrationRequesterClient.getClient().sendSoapFault(faultAddressingContext, soapFault) ;
                    return ;
                }
                catch (final InvalidStateException InvalidStateException)
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                    final SoapFault soapFault = new SoapFault10(SoapFaultType.FAULT_SENDER, CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_STATE_QNAME,
                            WSCLogger.i18NLogger.get_messaging_RegistrationCoordinatorProcessorImpl_3()) ;
                    RegistrationRequesterClient.getClient().sendSoapFault(faultAddressingContext, soapFault) ;
                    return ;
                }
                catch (final NoActivityException noActivityException)
                {
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                    final SoapFault soapFault = new SoapFault10(SoapFaultType.FAULT_SENDER, CoordinationConstants.WSCOOR_ERROR_CODE_NO_ACTIVITY_QNAME,
                            WSCLogger.i18NLogger.get_messaging_RegistrationCoordinatorProcessorImpl_4()) ;
                    RegistrationRequesterClient.getClient().sendSoapFault(faultAddressingContext, soapFault) ;
                    return ;
                }
                catch (final Throwable th)
                {
                    if (WSCLogger.logger.isTraceEnabled())
                    {
                        WSCLogger.logger.tracev("Unexpected exception thrown from create:", th) ;
                    }
                    final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                    final SoapFault soapFault = new SoapFault10(th) ;
                    RegistrationRequesterClient.getClient().sendSoapFault(faultAddressingContext, soapFault) ;
                    return ;
                }
                RegistrationRequesterClient.getClient().sendRegisterResponse(responseAddressingContext, coordinationProtocolService) ;
            }
            else
            {
                if (WSCLogger.logger.isTraceEnabled())
                {
                    WSCLogger.logger.tracev("Register called for unknown protocol identifier: {0}", new Object[] {protocolIdentifier}) ;
                }
                final AddressingContext faultAddressingContext = AddressingContext.createFaultContext(addressingContext, MessageId.getMessageId()) ;
                final SoapFault soapFault = new SoapFault10(SoapFaultType.FAULT_SENDER, CoordinationConstants.WSCOOR_ERROR_CODE_INVALID_PROTOCOL_QNAME,
                        WSCLogger.i18NLogger.get_messaging_RegistrationCoordinatorProcessorImpl_2()) ;
                RegistrationRequesterClient.getClient().sendSoapFault(faultAddressingContext, soapFault) ;
            }
        }
        catch (Throwable throwable)
        {
            throwable.printStackTrace(System.err);
        }
    }
}
