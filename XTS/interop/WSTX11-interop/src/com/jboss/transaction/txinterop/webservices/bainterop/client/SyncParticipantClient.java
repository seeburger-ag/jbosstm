package com.jboss.transaction.txinterop.webservices.bainterop.client;

import java.io.IOException;

import javax.xml.ws.addressing.AddressingProperties;

import com.arjuna.webservices.SoapFault;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import com.jboss.transaction.txinterop.webservices.bainterop.BAInteropConstants;

/**
 * The participant client.
 * @author kevin
 */
public class SyncParticipantClient
{
    /**
     * The client singleton.
     */
    private static final SyncParticipantClient CLIENT = new SyncParticipantClient() ;
    
    /**
     * The cancel action.
     */
    private static final String cancelAction = BAInteropConstants.INTEROP_ACTION_CANCEL ;
    /**
     * The exit action.
     */
    private static final String exitAction = BAInteropConstants.INTEROP_ACTION_EXIT ;
    /**
     * The fail action.
     */
    private static final String failAction = BAInteropConstants.INTEROP_ACTION_FAIL ;
    /**
     * The cannot complete action.
     */
    private static final String cannotCompleteAction = BAInteropConstants.INTEROP_ACTION_CANNOT_COMPLETE ;
    /**
     * The participant complete close action.
     */
    private static final String participantCompleteCloseAction = BAInteropConstants.INTEROP_ACTION_PARTICIPANT_COMPLETE_CLOSE ;
    /**
     * The coordinator complete close action.
     */
    private static final String coordinatorCompleteCloseAction = BAInteropConstants.INTEROP_ACTION_COORDINATOR_COMPLETE_CLOSE ;
    /**
     * The unsolicited complete action.
     */
    private static final String unsolicitedCompleteAction = BAInteropConstants.INTEROP_ACTION_UNSOLICITED_COMPLETE ;
    /**
     * The compensate action.
     */
    private static final String compensateAction = BAInteropConstants.INTEROP_ACTION_COMPENSATE ;
    /**
     * The compensation fail action.
     */
    private static final String participantCompensationFailAction = BAInteropConstants.INTEROP_ACTION_COMPENSATION_FAIL ;
    /**
     * The participant cancel completed race action.
     */
    private static final String participantCancelCompletedRaceAction = BAInteropConstants.INTEROP_ACTION_PARTICIPANT_CANCEL_COMPLETED_RACE ;
    /**
     * The message loss and recovery action.
     */
    private static final String messageLossAndRecoveryAction = BAInteropConstants.INTEROP_ACTION_MESSAGE_LOSS_AND_RECOVERY ;
    /**
     * The mixed outcome action.
     */
    private static final String mixedOutcomeAction = BAInteropConstants.INTEROP_ACTION_MIXED_OUTCOME ;
    
    /**
     * Construct the interop synch client.
     */
    private SyncParticipantClient()
    {
    }
    
    /**
     * Send a cancel request.
     * @param coordinationContext The coordination context.
     * @param addressingProperties The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendCancel(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault, IOException
    {
    }
    
    /**
     * Send a exit request.
     * @param coordinationContext The coordination context.
     * @param addressingProperties The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendExit(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault, IOException
    {
    }
    
    /**
     * Send a fail request.
     * @param coordinationContext The coordination context.
     * @param addressingProperties The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendFail(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault, IOException
    {
    }
    
    /**
     * Send a cannot complete request.
     * @param coordinationContext The coordination context.
     * @param addressingProperties The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendCannotComplete(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault, IOException
    {
    }
    
    /**
     * Send a participant complete close request.
     * @param coordinationContext The coordination context.
     * @param addressingProperties The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendParticipantCompleteClose(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault, IOException
    {
    }
    
    /**
     * Send a coordinator complete close request.
     * @param coordinationContext The coordination context.
     * @param addressingProperties The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendCoordinatorCompleteClose(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault, IOException
    {
    }
    
    /**
     * Send a unsolicited complete request.
     * @param coordinationContext The coordination context.
     * @param addressingProperties The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendUnsolicitedComplete(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault, IOException
    {
    }
    
    /**
     * Send a compensate request.
     * @param coordinationContext The coordination context.
     * @param addressingProperties The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendCompensate(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault, IOException
    {
    }
    
    /**
     * Send a compensation fail request.
     * @param coordinationContext The coordination context.
     * @param addressingProperties The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendCompensationFail(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault, IOException
    {
    }
    
    /**
     * Send a participant cancel completed race request.
     * @param coordinationContext The coordination context.
     * @param addressingProperties The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendParticipantCancelCompletedRace(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault, IOException
    {
    }
    
    /**
     * Send a message loss and recovery request.
     * @param coordinationContext The coordination context.
     * @param addressingProperties The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendMessageLossAndRecovery(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault, IOException
    {
    }
    
    /**
     * Send a mixed outcome request.
     * @param coordinationContext The coordination context.
     * @param addressingProperties The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendMixedOutcome(final CoordinationContextType coordinationContext, final AddressingProperties addressingProperties)
        throws SoapFault, IOException
    {
    }
    
    /**
     * Get the Interop client singleton.
     * @return The Interop client singleton.
     */
    public static SyncParticipantClient getClient()
    {
        return CLIENT ;
    }
}
