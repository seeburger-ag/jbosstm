package com.jboss.transaction.txinterop.webservices.bainterop.client;

import java.io.IOException;
import java.net.URISyntaxException;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.wsaddr.client.SoapFaultClient;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.SoapFault11;
import com.jboss.transaction.txinterop.webservices.bainterop.BAInteropConstants;
import com.jboss.transaction.txinterop.webservices.bainterop.generated.InitiatorPortType;

import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.AttributedURI;
import javax.xml.ws.addressing.AddressingBuilder;

/**
 * The initiator client.
 * @author kevin
 */
public class InitiatorClient
{
    /**
     * The client singleton.
     */
    private static final InitiatorClient CLIENT = new InitiatorClient() ;
    
    /**
     * The response action.
     */
    private static final String responseAction = BAInteropConstants.INTEROP_ACTION_RESPONSE ;
    
    /**
     * Construct the interop synch client.
     */
    private InitiatorClient()
    {
        // final HandlerRegistry handlerRegistry = new HandlerRegistry() ;
        
        // Add WS-Addressing
        // AddressingPolicy.register(handlerRegistry) ;
        // Add client policies
        // ClientPolicy.register(handlerRegistry) ;
        
        // soapService = new SoapService(handlerRegistry) ;
    }

    /**
     * Send a response.
     * @param addressingProperties The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendResponse(final AddressingProperties addressingProperties)
        throws SoapFault, IOException
    {
        InitiatorPortType port = BAInteropClient.getInitiatorPort(addressingProperties, responseAction);
        port.response();
    }

    /**
     * Send a fault.
     * @param addressingProperties The addressing context.
     * @param soapFault The SOAP fault.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendSoapFault(final AddressingProperties addressingProperties, final SoapFault11 soapFault)
        throws SoapFault, IOException
    {
        String soapFaultAction = soapFault.getAction() ;
        AttributedURI actionURI = null;
        if (soapFaultAction == null)
        {
            soapFaultAction = faultAction;
        }
        try {
            actionURI = builder.newURI(soapFaultAction);
        } catch (URISyntaxException e) {
            // TODO log error here
        }

        AddressingHelper.installNoneReplyTo(addressingProperties);
        SoapFaultClient.sendSoapFault(soapFault, addressingProperties, actionURI);
    }

    /**
     * Get the Interop client singleton.
     * @return The Interop client singleton.
     */
    public static InitiatorClient getClient()
    {
        return CLIENT ;
    }

    private static final String faultAction = "http://fabrikam123.com/SoapFault";
    private static final AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
}
