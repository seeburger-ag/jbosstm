package com.jboss.transaction.wstf.webservices.sc007.client;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URI;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.ServiceRegistry;
import com.jboss.transaction.wstf.webservices.CoordinationContextManager;
import com.jboss.transaction.wstf.webservices.sc007.InteropConstants;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import com.jboss.transaction.wstf.webservices.sc007.generated.ParticipantPortType;

import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.EndpointReference;
import javax.xml.ws.addressing.AddressingBuilder;

/**
 * The participant client.
 * @author kevin
 */
public class AsyncParticipantClient
{
    /**
     * The client singleton.
     */
    private static final AsyncParticipantClient CLIENT = new AsyncParticipantClient() ;

    /**
     * The completion commit action.
     */
    private static final String completionCommitAction = InteropConstants.INTEROP_ACTION_COMPLETION_COMMIT ;
    /**
     * The completion rollback Action.
     */
    private static final String completionRollbackAction = InteropConstants.INTEROP_ACTION_COMPLETION_ROLLBACK ;
    /**
     * The commit Action.
     */
    private static final String commitAction = InteropConstants.INTEROP_ACTION_COMMIT ;
    /**
     * The rollback Action.
     */
    private static final String rollbackAction = InteropConstants.INTEROP_ACTION_ROLLBACK ;
    /**
     * The phase 2 rollback Action.
     */
    private static final String phase2RollbackAction = InteropConstants.INTEROP_ACTION_PHASE_2_ROLLBACK ;
    /**
     * The readonly Action.
     */
    private static final String readonlyAction = InteropConstants.INTEROP_ACTION_READONLY ;
    /**
     * The volatile and durable Action.
     */
    private static final String volatileAndDurableAction = InteropConstants.INTEROP_ACTION_VOLATILE_AND_DURABLE ;
    /**
     * The early readonly Action.
     */
    private static final String earlyReadonlyAction = InteropConstants.INTEROP_ACTION_EARLY_READONLY ;
    /**
     * The early aborted Action.
     */
    private static final String earlyAbortedAction = InteropConstants.INTEROP_ACTION_EARLY_ABORTED ;
    /**
     * The replay commit Action.
     */
    private static final String replayCommitAction = InteropConstants.INTEROP_ACTION_REPLAY_COMMIT ;
    /**
     * The retry prepared commit Action.
     */
    private static final String retryPreparedCommitAction = InteropConstants.INTEROP_ACTION_RETRY_PREPARED_COMMIT ;
    /**
     * The retry prepared abort Action.
     */
    private static final String retryPreparedAbortAction = InteropConstants.INTEROP_ACTION_RETRY_PREPARED_ABORT ;
    /**
     * The retry commit Action.
     */
    private static final String retryCommitAction = InteropConstants.INTEROP_ACTION_RETRY_COMMIT ;
    /**
     * The prepared after timeout Action.
     */
    private static final String preparedAfterTimeoutAction = InteropConstants.INTEROP_ACTION_PREPARED_AFTER_TIMEOUT ;
    /**
     * The lost committed Action.
     */
    private static final String lostCommittedAction = InteropConstants.INTEROP_ACTION_LOST_COMMITTED ;

    /**
     * The initiator URI for replies.
     */
    private EndpointReference initiator = null;

    /**
     * Construct the interop synch client.
     */
    private AsyncParticipantClient()
    {
        //final HandlerRegistry handlerRegistry = new HandlerRegistry() ;

        // Add WS-Addressing
        //AddressingPolicy.register(handlerRegistry) ;
        // Add coordination context
        //CoordinationContextPolicy.register(handlerRegistry) ;
        // Add client policies
        //ClientPolicy.register(handlerRegistry) ;

        //soapService = new SoapService(handlerRegistry) ;
        AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
        final String initiatorURIString = ServiceRegistry.getRegistry().getServiceURI(InteropConstants.SERVICE_INITIATOR) ;
        URI uri = null;
        try {
            uri = new URI(initiatorURIString);
            initiator = builder.newEndpointReference(uri);
        } catch (URISyntaxException e) {
            // TODO log error here
        }
    }
    /**
     * Send a completion commit request.
     * @param addressingProperties The addressing context initialised with to, message ID and relates to.
     * @param coordinatorURI The coordinator URI.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendCompletionCommit(final AddressingProperties addressingProperties, final String coordinatorURI)
        throws SoapFault, IOException
    {
        addressingProperties.setReplyTo(initiator) ;
        ParticipantPortType port = InteropClient.getParticipantPort(addressingProperties, completionCommitAction);
        port.completionCommit(coordinatorURI);
    }

    /**
     * Send a completion rollback request.
     * @param addressingProperties The addressing context initialised with to, message ID and relates to.
     * @param coordinatorURI The coordinator URI.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendCompletionRollback(final AddressingProperties addressingProperties, final String coordinatorURI)
        throws SoapFault, IOException
    {
        addressingProperties.setReplyTo(initiator) ;
        ParticipantPortType port = InteropClient.getParticipantPort(addressingProperties, completionRollbackAction);
        port.completionRollback(coordinatorURI);
    }

    /**
     * Send a commit request.
     * @param coordinationContext The coordination context.
     * @param addressingProperties The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendCommit(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault, IOException
    {
        addressingProperties.setReplyTo(initiator) ;
        ParticipantPortType port = InteropClient.getParticipantPort(addressingProperties, commitAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try
        {
            port.commit();
        }
        finally
        {
            CoordinationContextManager.setThreadContext(null) ;
        }
    }

    /**
     * Send a rollback request.
     * @param coordinationContext The coordination context.
     * @param addressingProperties The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendRollback(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault, IOException
    {
        addressingProperties.setReplyTo(initiator) ;
        ParticipantPortType port = InteropClient.getParticipantPort(addressingProperties, rollbackAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try
        {
            port.rollback();
        }
        finally
        {
            CoordinationContextManager.setThreadContext(null) ;
        }
    }

    /**
     * Send a phase2Rollback request.
     * @param coordinationContext The coordination context.
     * @param addressingProperties The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendPhase2Rollback(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault, IOException
    {
        addressingProperties.setReplyTo(initiator) ;
        ParticipantPortType port = InteropClient.getParticipantPort(addressingProperties, phase2RollbackAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try
        {
            port.phase2Rollback();
        }
        finally
        {
            CoordinationContextManager.setThreadContext(null) ;
        }
    }

    /**
     * Send a readonly request.
     * @param coordinationContext The coordination context.
     * @param addressingProperties The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendReadonly(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault, IOException
    {
        addressingProperties.setReplyTo(initiator) ;
        ParticipantPortType port = InteropClient.getParticipantPort(addressingProperties, readonlyAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try
        {
            port.readonly();
        }
        finally
        {
            CoordinationContextManager.setThreadContext(null) ;
        }
    }

    /**
     * Send a volatileAndDurable request.
     * @param coordinationContext The coordination context.
     * @param addressingProperties The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendVolatileAndDurable(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault, IOException
    {
        addressingProperties.setReplyTo(initiator) ;
        ParticipantPortType port = InteropClient.getParticipantPort(addressingProperties, volatileAndDurableAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try
        {
            port.volatileAndDurable();
        }
        finally
        {
            CoordinationContextManager.setThreadContext(null) ;
        }
    }

    /**
     * Send an earlyReadonly request.
     * @param coordinationContext The coordination context.
     * @param addressingProperties The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendEarlyReadonly(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault, IOException
    {
        addressingProperties.setReplyTo(initiator) ;
        ParticipantPortType port = InteropClient.getParticipantPort(addressingProperties, earlyReadonlyAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try
        {
            port.earlyReadonly();
        }
        finally
        {
            CoordinationContextManager.setThreadContext(null) ;
        }
    }

    /**
     * Send a earlyAborted request.
     * @param coordinationContext The coordination context.
     * @param addressingProperties The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendEarlyAborted(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault, IOException
    {
        addressingProperties.setReplyTo(initiator) ;
        ParticipantPortType port = InteropClient.getParticipantPort(addressingProperties, earlyAbortedAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try
        {
            port.earlyAborted();
        }
        finally
        {
            CoordinationContextManager.setThreadContext(null) ;
        }
    }

    /**
     * Send a replayCommit request.
     * @param coordinationContext The coordination context.
     * @param addressingProperties The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendReplayCommit(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault, IOException
    {
        addressingProperties.setReplyTo(initiator) ;
        ParticipantPortType port = InteropClient.getParticipantPort(addressingProperties, replayCommitAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try
        {
            port.replayCommit();
        }
        finally
        {
            CoordinationContextManager.setThreadContext(null) ;
        }
    }

    /**
     * Send a retryPreparedCommit request.
     * @param coordinationContext The coordination context.
     * @param addressingProperties The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendRetryPreparedCommit(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault, IOException
    {
        addressingProperties.setReplyTo(initiator) ;
        ParticipantPortType port = InteropClient.getParticipantPort(addressingProperties, retryPreparedCommitAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try
        {
            port.retryPreparedCommit();
        }
        finally
        {
            CoordinationContextManager.setThreadContext(null) ;
        }
    }

    /**
     * Send a retryPreparedAbort request.
     * @param coordinationContext The coordination context.
     * @param addressingProperties The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendRetryPreparedAbort(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault, IOException
    {
        addressingProperties.setReplyTo(initiator) ;
        ParticipantPortType port = InteropClient.getParticipantPort(addressingProperties, retryPreparedAbortAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try
        {
            port.retryPreparedAbort();
        }
        finally
        {
            CoordinationContextManager.setThreadContext(null) ;
        }
    }

    /**
     * Send a retryCommit request.
     * @param coordinationContext The coordination context.
     * @param addressingProperties The addressing context initialised with to, message ID and relates to.
     * @throws IOException for any transport errors.
     */
    public void sendRetryCommit(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault, IOException
    {
        addressingProperties.setReplyTo(initiator) ;
        ParticipantPortType port = InteropClient.getParticipantPort(addressingProperties, retryCommitAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try
        {
            port.retryCommit();
        }
        finally
        {
            CoordinationContextManager.setThreadContext(null) ;
        }
    }

    /**
     * Send a preparedAfterTimeout request.
     * @param coordinationContext The coordination context.
     * @param addressingProperties The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendPreparedAfterTimeout(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault, IOException
    {
        addressingProperties.setReplyTo(initiator) ;
        ParticipantPortType port = InteropClient.getParticipantPort(addressingProperties, preparedAfterTimeoutAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try
        {
            port.preparedAfterTimeout();
        }
        finally
        {
            CoordinationContextManager.setThreadContext(null) ;
        }
    }

    /**
     * Send a lostCommitted request.
     * @param coordinationContext The coordination context.
     * @param addressingProperties The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendLostCommitted(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault, IOException
    {
        addressingProperties.setReplyTo(initiator) ;
        ParticipantPortType port = InteropClient.getParticipantPort(addressingProperties, lostCommittedAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try
        {
            port.lostCommitted();
        }
        finally
        {
            CoordinationContextManager.setThreadContext(null) ;
        }
    }

    /**
     * Get the Interop client singleton.
     * @return The Interop client singleton.
     */
    public static AsyncParticipantClient getClient()
    {
        return CLIENT ;
    }
}
