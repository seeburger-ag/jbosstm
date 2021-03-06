/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2007,
 * @author JBoss Inc.
 */
package com.arjuna.mw.wst.service;

import com.arjuna.webservices.wscoor.CoordinationConstants;
import com.arjuna.webservices.wscoor.CoordinationContextType;
import com.arjuna.webservices.wsat.AtomicTransactionConstants;
import com.arjuna.webservices.wsba.BusinessActivityConstants;
import com.arjuna.mw.wst.common.CoordinationContextHelper;
import com.arjuna.mw.wst.common.SOAPUtil;
import com.arjuna.mw.wst.*;
import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.mwlabs.wst.at.SubordinateImporter;

import javax.xml.soap.*;
import java.util.Iterator;

/**
 * Common base class for classes used to perform
 * WS-Transaction context manipulation on SOAP messages.
 *
 */
public class JaxBaseHeaderContextProcessor
{
    /**
     * Handle the request.
     * @param soapMessage The current message context.
     */
    protected boolean handleInboundMessage(final SOAPMessage soapMessage)
    {
        return handleInboundMessage(soapMessage, false);
    }

    /**
     * Handle the request.
     * @param soapMessage The current message context.
     * @param installSubordinateTx true if a subordinate transaction should be interposed and false
     * if the handler should just resume the incoming transaction. currently only works for AT
     * transactions but will eventually be extended to work for BA transactions too.
     */
    protected boolean handleInboundMessage(final SOAPMessage soapMessage, boolean installSubordinateTx)
    {
        if (soapMessage != null)
        {
            try
            {
                final SOAPEnvelope soapEnvelope = soapMessage.getSOAPPart().getEnvelope() ;
                final SOAPHeader soapHeader = soapEnvelope.getHeader() ;
                final SOAPHeaderElement soapHeaderElement = getHeaderElement(soapHeader, CoordinationConstants.WSCOOR_NAMESPACE, CoordinationConstants.WSCOOR_ELEMENT_COORDINATION_CONTEXT) ;

                if (soapHeaderElement != null)
                {
                    final CoordinationContextType cc = CoordinationContextHelper.deserialise(soapEnvelope, soapHeaderElement) ;
                    final String coordinationType = cc.getCoordinationType().getValue() ;
                    if (AtomicTransactionConstants.WSAT_PROTOCOL.equals(coordinationType))
                    {
                        clearMustUnderstand(soapHeader, soapHeaderElement) ;
                        TxContext txContext = new com.arjuna.mwlabs.wst.at.context.TxContextImple(cc) ;
                        if (installSubordinateTx) {
                            txContext = SubordinateImporter.importContext(cc);
                        }
                        TransactionManagerFactory.transactionManager().resume(txContext) ;
                    }
                    else if (BusinessActivityConstants.WSBA_PROTOCOL_ATOMIC_OUTCOME.equals(coordinationType))
                    {
                        // interposition is not yet implemented for business activities
                        clearMustUnderstand(soapHeader, soapHeaderElement) ;
                        if (installSubordinateTx) {
                            // throw an exception to force logging of a warning below
                            throw new Exception("com.arjuna.mw.wst11.service.JaxBaseHeaderContextProcessor : interposition is not yet implemented for the WSBA 1.0 protocol");
                        }
                        final TxContext txContext = new com.arjuna.mwlabs.wst.ba.context.TxContextImple(cc);
                        BusinessActivityManagerFactory.businessActivityManager().resume(txContext) ;
                    }
                    else {
                        wstxLogger.i18NLogger.warn_mw_wst_service_JaxHCP_2("com.arjuna.mw.wst.service.JaxRPCHeaderContextProcessor.handleRequest(MessageContext context)", coordinationType);
                    }
                }
            }
            catch (final Throwable th) {
                wstxLogger.i18NLogger.warn_mw_wst_service_JaxHCP_1("com.arjuna.mw.wst.service.JaxRPCHeaderContextProcessor.handleRequest(MessageContext context)", th);
            }
        }
        return true ;
    }

    /**
     * Suspend the current transaction.
     */
    protected void suspendTransaction()
    {
        try
        {
            /*
             * There should either be an Atomic Transaction *or* a Business Activity
             * associated with the thread.
             */
            final TransactionManager transactionManager = TransactionManagerFactory.transactionManager() ;
            final BusinessActivityManager businessActivityManager = BusinessActivityManagerFactory.businessActivityManager() ;

            if (transactionManager != null)
            {
                transactionManager.suspend() ;
            }

            if (businessActivityManager != null)
            {
                businessActivityManager.suspend() ;
            }
        }
        catch (final Throwable th) {
            wstxLogger.i18NLogger.warn_mw_wst_service_JaxHCP_1("com.arjuna.mw.wst.service.JaxRPCHeaderContextProcessor.suspendTransaction()", th);
        }
    }

    /**
     * Clear the soap MustUnderstand.
     * @param soapHeader The SOAP header.
     * @param soapHeaderElement The SOAP header element.
     */
    private void clearMustUnderstand(final SOAPHeader soapHeader, final SOAPHeaderElement soapHeaderElement)
            throws SOAPException
    {
        final Name headerName = soapHeader.getElementName() ;

        final SOAPFactory factory = SOAPFactory.newInstance() ;
        final Name attributeName = factory.createName("mustUnderstand", headerName.getPrefix(), headerName.getURI()) ;

        soapHeaderElement.removeAttribute(attributeName) ;
    }

    private SOAPHeaderElement getHeaderElement(final SOAPHeader soapHeader, final String uri, final String name)
            throws SOAPException
    {
        if (soapHeader != null)
        {
            final Iterator headerIter = SOAPUtil.getChildElements(soapHeader) ;
            while(headerIter.hasNext())
            {
                final SOAPHeaderElement current = (SOAPHeaderElement)headerIter.next() ;
                final Name currentName = current.getElementName() ;
                if ((currentName != null) &&
                        match(name, currentName.getLocalName()) &&
                        match(uri, currentName.getURI()))
                {
                    return current ;
                }
            }
        }
        return null ;
    }

    /**
     * Do the two references match?
     * @param lhs The first reference.
     * @param rhs The second reference.
     * @return true if the references are both null or if they are equal.
     */
    private boolean match(final Object lhs, final Object rhs)
    {
        if (lhs == null)
        {
            return (rhs == null) ;
        }
        else
        {
            return lhs.equals(rhs) ;
        }
    }
}
