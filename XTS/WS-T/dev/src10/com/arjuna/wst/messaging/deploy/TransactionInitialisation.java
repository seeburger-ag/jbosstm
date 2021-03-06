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
package com.arjuna.wst.messaging.deploy;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.arjuna.webservices.wsarjtx.processors.TerminationCoordinatorProcessor;
import com.arjuna.webservices.wsat.processors.CompletionCoordinatorProcessor;
import com.arjuna.webservices.wsat.processors.CoordinatorProcessor;
import com.arjuna.webservices.wsat.processors.ParticipantProcessor;
import com.arjuna.webservices.wsba.processors.CoordinatorCompletionCoordinatorProcessor;
import com.arjuna.webservices.wsba.processors.CoordinatorCompletionParticipantProcessor;
import com.arjuna.webservices.wsba.processors.ParticipantCompletionCoordinatorProcessor;
import com.arjuna.webservices.wsba.processors.ParticipantCompletionParticipantProcessor;
import com.arjuna.wst.messaging.CompletionCoordinatorProcessorImpl;
import com.arjuna.wst.messaging.CoordinatorCompletionCoordinatorProcessorImpl;
import com.arjuna.wst.messaging.CoordinatorCompletionParticipantProcessorImpl;
import com.arjuna.wst.messaging.CoordinatorProcessorImpl;
import com.arjuna.wst.messaging.ParticipantCompletionCoordinatorProcessorImpl;
import com.arjuna.wst.messaging.ParticipantCompletionParticipantProcessorImpl;
import com.arjuna.wst.messaging.ParticipantProcessorImpl;
import com.arjuna.wst.messaging.TerminatorParticipantProcessorImpl;
import com.arjuna.services.framework.startup.Sequencer;

/**
 * Initialise the transaction services.
 * @author kevin
 */
public class TransactionInitialisation implements ServletContextListener
{
    /**
     * The context has been initialized.
     * @param servletContextEvent The servlet context event.
     */
    public void contextInitialized(final ServletContextEvent servletContextEvent)
    {
        Sequencer.Callback callback = new Sequencer.Callback(Sequencer.SEQUENCE_WSCOOR10, Sequencer.WEBAPP_WST10) {
           public void run() {
               CompletionCoordinatorProcessor.setProcessor(new CompletionCoordinatorProcessorImpl()) ;
               ParticipantProcessor.setProcessor(new ParticipantProcessorImpl()) ;
               CoordinatorProcessor.setProcessor(new CoordinatorProcessorImpl()) ;
               TerminationCoordinatorProcessor.setProcessor(new TerminatorParticipantProcessorImpl()) ;
               CoordinatorCompletionParticipantProcessor.setProcessor(new CoordinatorCompletionParticipantProcessorImpl()) ;
               ParticipantCompletionParticipantProcessor.setProcessor(new ParticipantCompletionParticipantProcessorImpl()) ;
               CoordinatorCompletionCoordinatorProcessor.setProcessor(new CoordinatorCompletionCoordinatorProcessorImpl()) ;
               ParticipantCompletionCoordinatorProcessor.setProcessor(new ParticipantCompletionCoordinatorProcessorImpl()) ;
           }
        };
        // this is the last WST callback to be initialised so close the list
        Sequencer.close(Sequencer.SEQUENCE_WSCOOR10, Sequencer.WEBAPP_WST10);
    }

    /**
     * The context is about to be destroyed.
     * @param servletContextEvent The servlet context event.
     */
    public void contextDestroyed(final ServletContextEvent servletContextEvent)
    {
    }
}
