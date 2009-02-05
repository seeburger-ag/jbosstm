/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.jbossts;

import org.jboss.logging.Logger;
import org.jboss.jbossts.xts.recovery.coordinator.at.ACCoordinatorRecoveryModule;
import org.jboss.jbossts.xts.recovery.coordinator.at.SubordinateCoordinatorRecoveryModule;
import org.jboss.jbossts.xts.recovery.coordinator.ba.BACoordinatorRecoveryModule;
import org.jboss.jbossts.xts.recovery.participant.at.ATParticipantRecoveryModule;
import org.jboss.jbossts.xts.recovery.participant.ba.BAParticipantRecoveryModule;

//import com.arjuna.mw.wst.deploy.WSTXInitialisation;
//import com.arjuna.mw.wst.UserTransaction;
//import com.arjuna.mw.wst.TransactionManager;
//import com.arjuna.mw.wst.UserBusinessActivity;
//import com.arjuna.mw.wst.BusinessActivityManager;
//import com.arjuna.wsc.ContextFactoryMapper;
//import com.arjuna.wsc.messaging.ActivationCoordinatorProcessorImpl;
//import com.arjuna.wsc.messaging.RegistrationCoordinatorProcessorImpl;
//import com.arjuna.mwlabs.wsc.ContextFactoryMapperImple;
import com.arjuna.services.framework.task.TaskManager;
import com.arjuna.services.framework.startup.Sequencer;
//import com.arjuna.webservices.HandlerRegistry;
//import com.arjuna.webservices.SoapRegistry;
//import com.arjuna.webservices.SoapService;
//import com.arjuna.webservices.SoapClient;
//import com.arjuna.webservices.util.ClassLoaderHelper;
//import com.arjuna.webservices.wsba.policy.CoordinatorCompletionCoordinatorPolicy;
//import com.arjuna.webservices.wsba.policy.CoordinatorCompletionParticipantPolicy;
//import com.arjuna.webservices.wsba.policy.ParticipantCompletionCoordinatorPolicy;
//import com.arjuna.webservices.wsba.policy.ParticipantCompletionParticipantPolicy;
//import com.arjuna.webservices.wsba.BusinessActivityConstants;
//import com.arjuna.webservices.wsba.processors.CoordinatorCompletionParticipantProcessor;
//import com.arjuna.webservices.wsba.processors.ParticipantCompletionParticipantProcessor;
//import com.arjuna.webservices.wsba.processors.CoordinatorCompletionCoordinatorProcessor;
//import com.arjuna.webservices.wsba.processors.ParticipantCompletionCoordinatorProcessor;
//import com.arjuna.webservices.wsat.policy.CompletionCoordinatorPolicy;
//import com.arjuna.webservices.wsat.policy.CompletionInitiatorPolicy;
//import com.arjuna.webservices.wsat.policy.CoordinatorPolicy;
//import com.arjuna.webservices.wsat.policy.ParticipantPolicy;
//import com.arjuna.webservices.wsat.AtomicTransactionConstants;
//import com.arjuna.webservices.wsat.processors.CompletionCoordinatorProcessor;
//import com.arjuna.webservices.wsat.processors.ParticipantProcessor;
//import com.arjuna.webservices.wsat.processors.CoordinatorProcessor;
//import com.arjuna.webservices.wsarjtx.policy.TerminationParticipantPolicy;
//import com.arjuna.webservices.wsarjtx.policy.TerminationCoordinatorPolicy;
//import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
//import com.arjuna.webservices.wsarjtx.processors.TerminationCoordinatorProcessor;
//import com.arjuna.webservices.transport.http.HttpClient;
//import com.arjuna.webservices.wsarj.policy.ArjunaPolicy;
//import com.arjuna.webservices.wscoor.policy.ActivationCoordinatorPolicy;
//import com.arjuna.webservices.wscoor.policy.ActivationRequesterPolicy;
//import com.arjuna.webservices.wscoor.policy.RegistrationCoordinatorPolicy;
//import com.arjuna.webservices.wscoor.policy.RegistrationRequesterPolicy;
//import com.arjuna.webservices.wscoor.CoordinationConstants;
//import com.arjuna.webservices.wscoor.processors.ActivationCoordinatorProcessor;
//import com.arjuna.webservices.wscoor.processors.RegistrationCoordinatorProcessor;
//import com.arjuna.webservices.wsaddr.policy.AddressingPolicy;
//import com.arjuna.wst.messaging.*;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;

import javax.management.MBeanServer;
import java.net.InetAddress;
//import com.arjuna.ats.arjuna.recovery.RecoveryModule;

/**
 * $Id$
 */
public class XTSService implements XTSServiceMBean {

/*
<?xml version="1.0" encoding="UTF-8"?>
<deployment xmlns="urn:jboss:bean-deployer:2.0">

    <bean name="XTSService" class="org.jboss.transactions.XTSService">
        <annotation>@org.jboss.aop.microcontainer.aspects.jmx.JMX(name="jboss.xts:service=XTSService", exposedInterface=org.jboss.transactions.XTSServiceMBean.class, registerDirectly=true)</annotation>

       <depends>jboss.web:service=WebServer</depends>
       <depends>jboss:service=TransactionManager</depends>
   </bean>

</deployment>

 */

    // TODO expose as bean properties
    private int taskManagerMinWorkerCount = 0;
    private int taskManagerMaxWorkerCount = 10;

    private final Logger log = org.jboss.logging.Logger.getLogger(XTSService.class);

    private ACCoordinatorRecoveryModule acCoordinatorRecoveryModule = null;
    private SubordinateCoordinatorRecoveryModule subordinateCoordinatorRecoveryModule = null;
    private ATParticipantRecoveryModule atParticipantRecoveryModule = null;

    private BACoordinatorRecoveryModule baCoordinatorRecoveryModule = null;
    private BAParticipantRecoveryModule baParticipantRecoveryModule = null;

    // TODO: how to use a (per application) remote coordinator?
    // does the http servlet param indicate its own location and the
    // coordinatorURL indicate the coord??

    // ./HashedActionStore/defaultStore/StateManager/BasicAction/AtomicAction/TwoPhaseCoordinator/TwoPhase/ACCoordinator/

//    	    <property
//	    name="com.arjuna.ats.arjuna.recovery.recoveryExtension1"
//	    value="com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule"/>
    // recovers /StateManager/BasicAction/TwoPhaseCoordinator/AtomicAction
    // not what we need.

    // TODO: new recovery mod, inheriting from existing base version e.g. AtomicActionRecoveryModule - change typex

    public XTSService() {}

    public void start() throws Exception
    {
        log.info("JBossTS XTS Transaction Service - starting");

        // read unified properties file (replaces wscf.xml and wstx.xml)
        // Configuration.initialise("/jbossxts.xml");

        // before we can allow the services to start up we need to identify the server
        // bind address and the web service port so they services register themselves
        // in the registry using the correct URL

        // the app server's MicroContainer and ServiceBindingManager should work together
        // to supply us the properties used by the web server. Check that it's done so:
        // if this blows up, changes are binding.xml or XTS's jboss-beans.xml is broken.
        if(httpBindInetAddress == null || (httpPort == 0 && httpsPort == 0)) {
            log.error("insufficient webserver address:port information available - unable to start XTS.");
            throw new Exception("insufficient webserver address:port information available - unable to start XTS.");
        }

        // The app servers gives us a InetAddress, but our config system is String based.
        // so we convert it here, then XTS internally convert it back later. sigh.
        // likewise for ints to Strings for hte port numbers.

        String bindAddress = httpBindInetAddress.getHostName();

        if (bindAddress == null || "".equals(bindAddress)) {
            // use the ip address instead of the name
            bindAddress= httpBindInetAddress.getHostAddress();
        }

        System.setProperty(com.arjuna.wsc.common.Environment.XTS_BIND_ADDRESS, bindAddress);
        System.setProperty(com.arjuna.wsc11.common.Environment.XTS_BIND_ADDRESS, bindAddress);

        if(httpPort != 0) {
            System.setProperty(com.arjuna.wsc.common.Environment.XTS_BIND_PORT, ""+httpPort);
            System.setProperty(com.arjuna.wsc11.common.Environment.XTS_BIND_PORT, ""+httpPort);
        }

        if(httpsPort != 0) {
            System.setProperty(com.arjuna.wsc.common.Environment.XTS_SECURE_BIND_PORT, ""+httpsPort);
            System.setProperty(com.arjuna.wsc11.common.Environment.XTS_SECURE_BIND_PORT, ""+httpsPort);
        }

        // see if the coordinatorURL or host/port has been specified on the command line
        // if so then we need to record that fact here so we override any value
        // supplied in the  config file
        // but we don't do this if we have already saved it and we are now reloading XTS
        // yeeurrch really need to stop using System properties

        if (System.getProperty(com.arjuna.wsc.common.Environment.XTS_COMMAND_LINE_COORDINATOR_URL) == null) {
            String coordinatorURL = System.getProperty(com.arjuna.mw.wst.common.Environment.COORDINATOR_URL);
            String coordinatorScheme = System.getProperty(com.arjuna.mw.wst.common.Environment.COORDINATOR_SCHEME);
            String coordinatorHost = System.getProperty(com.arjuna.mw.wst.common.Environment.COORDINATOR_HOST);
            String coordinatorPort = System.getProperty(com.arjuna.mw.wst.common.Environment.COORDINATOR_PORT);
            String coordinatorPath = System.getProperty(com.arjuna.mw.wst.common.Environment.COORDINATOR_PATH);
            if (coordinatorURL != null) {
                System.setProperty(com.arjuna.wsc.common.Environment.XTS_COMMAND_LINE_COORDINATOR_URL, coordinatorURL);
            } else if (coordinatorScheme != null || coordinatorHost != null || coordinatorPort != null || coordinatorPath != null) {
                if (coordinatorScheme == null) {
                    coordinatorScheme = "http";
                }
                if (coordinatorHost == null) {
                    coordinatorHost = (bindAddress != null ? bindAddress : "127.0.0.1");
                }
                if (coordinatorPort == null) {
                    if ("https".equals(coordinatorScheme)) {
                        coordinatorPort = (httpsPort != 0 ? ""+httpsPort : "8443");
                    } else {
                        coordinatorPort = (httpPort != 0 ? ""+httpPort : "8080");
                    }
                }
                if (coordinatorPath == null) {
                    coordinatorPath = "ws-c10/soap/ActivationCoordinator";
                }
                coordinatorURL = coordinatorScheme + "://" + coordinatorHost + ":" + coordinatorPort + "/" + coordinatorPath;
                System.setProperty(com.arjuna.wsc.common.Environment.XTS_COMMAND_LINE_COORDINATOR_URL, coordinatorURL);
                System.setProperty(com.arjuna.mw.wst.common.Environment.COORDINATOR_URL, coordinatorURL);
            }
        }

        // ok now do the same for the 1.1 env settings

        if (System.getProperty(com.arjuna.wsc11.common.Environment.XTS_COMMAND_LINE_COORDINATOR_URL) == null) {
            String coordinatorURL = System.getProperty(com.arjuna.mw.wst11.common.Environment.COORDINATOR_URL);
            String coordinatorScheme = System.getProperty(com.arjuna.mw.wst11.common.Environment.COORDINATOR_SCHEME);
            String coordinatorHost = System.getProperty(com.arjuna.mw.wst11.common.Environment.COORDINATOR_HOST);
            String coordinatorPort = System.getProperty(com.arjuna.mw.wst11.common.Environment.COORDINATOR_PORT);
            String coordinatorPath = System.getProperty(com.arjuna.mw.wst11.common.Environment.COORDINATOR_PATH);
            if (coordinatorURL != null) {
                System.setProperty(com.arjuna.wsc11.common.Environment.XTS_COMMAND_LINE_COORDINATOR_URL, coordinatorURL);
            } else if (coordinatorScheme != null || coordinatorHost != null || coordinatorPort != null || coordinatorPath != null) {
                if (coordinatorScheme == null) {
                    coordinatorScheme = "http";
                }
                if (coordinatorHost == null) {
                    coordinatorHost = (bindAddress != null ? bindAddress : "127.0.0.1");
                }
                if (coordinatorPort == null) {
                    if ("https".equals(coordinatorScheme)) {
                        coordinatorPort = (httpsPort != 0 ? ""+httpsPort : "8443");
                    } else {
                        coordinatorPort = (httpPort != 0 ? ""+httpPort : "8080");
                    }
                }
                if (coordinatorPath == null) {
                    coordinatorPath = "ws-c11/ActivationService";
                }
                coordinatorURL = coordinatorScheme + "://" + coordinatorHost + ":" + coordinatorPort + "/" + coordinatorPath;
                System.setProperty(com.arjuna.wsc11.common.Environment.XTS_COMMAND_LINE_COORDINATOR_URL, coordinatorURL);
                System.setProperty(com.arjuna.mw.wst11.common.Environment.COORDINATOR_URL, coordinatorURL);
            }
        }
        // now it is safe to let the Sequencer class run any intiialisation routines it needs

        Sequencer.unlatch();

        TaskManagerInitialisation(); // com.arjuna.services.framework.admin.TaskManagerInitialisation : initialise the Task Manager


        // install the AT recovery modules
        
        acCoordinatorRecoveryModule = new ACCoordinatorRecoveryModule();

        // ensure Implementations are installed into the inventory before we register the module

        acCoordinatorRecoveryModule.install();

        // we don't need to install anything in the Inventory for this recovery module as it
        // uses the same records as those employed by ACCoordinatorRecoveryModule

        subordinateCoordinatorRecoveryModule = new SubordinateCoordinatorRecoveryModule();

        subordinateCoordinatorRecoveryModule.install();

        // we don't need to install anything in the Inventory for this recovery module as it
        // manages its own ObjectStore records but we do need it to create the recovery manager
        // singleton.

        atParticipantRecoveryModule = new ATParticipantRecoveryModule();

        atParticipantRecoveryModule.install();

        // ok, now the recovery mocules for the BA coordinator and participant

        baCoordinatorRecoveryModule = new BACoordinatorRecoveryModule();

        // ensure Implementations are installed into the inventory before we register the module

        baCoordinatorRecoveryModule.install();

        // we don't need to install anything in the Inventory for this recovery module as it
        // manages its own ObjectStore records but we do need it to create the recovery manager
        // singleton.

        baParticipantRecoveryModule = new BAParticipantRecoveryModule();

        baParticipantRecoveryModule.install();

        // we assume the tx manager has started, hence initializing the recovery manager.
        // to guarantee this our mbean should depend on the tx mgr mbean. (but does that g/tee start or just load?)

        //  recovery should perform better if we register partiicpants first since this allows the XTS client
        // recovery module to have a try at recreating the participant before its coordinator attempts to
        // talk to it when they are both in the same VM. it also means the participant nay attempt bottom-up
        // recovery before the coordinator is ready but coordinator recoveyr is probably going to happen quicker.

        RecoveryManager.manager().addModule(atParticipantRecoveryModule);
        RecoveryManager.manager().addModule(baParticipantRecoveryModule);

        RecoveryManager.manager().addModule(acCoordinatorRecoveryModule);
        RecoveryManager.manager().addModule(subordinateCoordinatorRecoveryModule);
        RecoveryManager.manager().addModule(baCoordinatorRecoveryModule);
    }

    public void stop() throws Exception
    {
        log.info("JBossTS XTS Transaction Service - stopping");

        if (baCoordinatorRecoveryModule != null) {
            // remove the module, making sure any scan which might be using it has completed
            RecoveryManager.manager().removeModule(baCoordinatorRecoveryModule, true);
            // ok, now it is safe to get the recovery manager to uninstall its Implementations from the inventory
            baCoordinatorRecoveryModule.uninstall();
        }
        if (subordinateCoordinatorRecoveryModule != null) {
            // remove the module, making sure any scan which might be using it has completed
            RecoveryManager.manager().removeModule(subordinateCoordinatorRecoveryModule, true);
            // ok, now it is safe to get the recovery manager to uninstall its Implementations from the inventory
            subordinateCoordinatorRecoveryModule.uninstall();
        }
        if (acCoordinatorRecoveryModule != null) {
            // remove the module, making sure any scan which might be using it has completed
            RecoveryManager.manager().removeModule(acCoordinatorRecoveryModule, true);
            // ok, now it is safe to get the recovery manager to uninstall its Implementations from the inventory
            acCoordinatorRecoveryModule.uninstall();
        }
        if (baParticipantRecoveryModule != null) {
            // remove the module, making sure any scan which might be using it has completed
            RecoveryManager.manager().removeModule(baParticipantRecoveryModule, true);
            // call uninstall even though it is currently a null op for this module
            baParticipantRecoveryModule.uninstall();
        }
        if (atParticipantRecoveryModule != null) {
            // remove the module, making sure any scan which might be using it has completed
            RecoveryManager.manager().removeModule(atParticipantRecoveryModule, true);
            // call uninstall even though it is currently a null op for this module
            atParticipantRecoveryModule.uninstall();
        }

        TaskManager.getManager().shutdown() ; // com.arjuna.services.framework.admin.TaskManagerInitialisation

        /*
         * this will be done by the servlet shutdown code
        // HttpClientInitialisation
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        soapRegistry.removeSoapClient("http") ;
        soapRegistry.removeSoapClient("https") ;
        */
    }

    private void TaskManagerInitialisation()
    {
        final TaskManager taskManager = TaskManager.getManager() ;
        taskManager.setMinimumWorkerCount(taskManagerMinWorkerCount) ;
        taskManager.setMaximumWorkerCount(taskManagerMaxWorkerCount) ;
    }

    public MBeanServer getMbeanServer()
    {
       return mbeanServer;
    }

    public void setMbeanServer(MBeanServer mbeanServer)
    {
       this.mbeanServer = mbeanServer;
    }

    private MBeanServer mbeanServer = null;

    ///////////////

    // These setters are used to allow MC/ServiceBindingManger to relay information from the Web server
    // seee bindings.xml and jboss-beans.xml

    public InetAddress getHttpBindInetAddress() {
        return httpBindInetAddress;
    }

    public void setHttpBindInetAddress(InetAddress httpBindInetAddress) {
        this.httpBindInetAddress = httpBindInetAddress;
    }

    private InetAddress httpBindInetAddress = null;

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    int httpPort = 0;

    public int getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsPort(int httpsPort) {
        this.httpsPort = httpsPort;
    }

    int httpsPort = 0;
}