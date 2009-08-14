/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
 * (C) 2009,
 * @author Red Hat Middleware LLC.
 */
package com.arjuna.ats.jbossatx.jta;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.internal.jbossatx.jta.XAResourceRecoveryHelperWrapper;
import com.arjuna.ats.arjuna.common.Configuration;
import com.arjuna.common.util.logging.LogFactory;

import org.jboss.logging.Logger;
import org.jboss.tm.XAResourceRecovery;
import org.jboss.tm.XAResourceRecoveryRegistry;

import java.util.Vector;

/**
 * JBoss Transaction Recovery Service.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com)
 * @version $Id$
 */
public class RecoveryManagerService implements XAResourceRecoveryRegistry
{
    static {
		/*
		 * Override the default logging config, force use of the plugin that rewrites log levels to reflect app server level semantics.
		 * This must be done before the loading of anything that uses the logging, otherwise it's too late to take effect.
		 * Hence the static initializer block.
		 * see also http://jira.jboss.com/jira/browse/JBTM-20
		 */
		com.arjuna.ats.arjuna.common.arjPropertyManager.getPropertyManager().setProperty(LogFactory.LOGGER_PROPERTY, "log4j_releveler");
		//System.setProperty(LogFactory.LOGGER_PROPERTY, "log4j_releveler") ;
	}

    private final Logger log = org.jboss.logging.Logger.getLogger(RecoveryManagerService.class);

    private RecoveryManager _recoveryManager;

    public void create() throws Exception
    {
        String tag = Configuration.getBuildTimeProperty("SOURCEID");

        log.info("JBossTS Recovery Service (JTA version - tag:"+tag+") - JBoss Inc.");

        RecoveryManager.delayRecoveryManagerThread();
        // listener (if any) is created here:
        _recoveryManager = RecoveryManager.manager();
    }

    public void destroy()
    {
    }

    public void start()
    {
        log.info("Starting transaction recovery manager");

        _recoveryManager.initialize();
        _recoveryManager.startRecoveryManagerThread() ;
    }

    public void stop() throws Exception
    {
        log.info("Stopping transaction recovery manager");

        _recoveryManager.terminate();
    }

    //////////////////////////////

    public void addXAResourceRecovery(XAResourceRecovery xaResourceRecovery)
    {
        if(_recoveryManager == null) {
            log.error("No recovery system in which to register XAResourceRecovery instance");
            throw new IllegalStateException("No recovery system present in this server");
        }

        XARecoveryModule xaRecoveryModule = null;
        for(RecoveryModule recoveryModule : ((Vector<RecoveryModule>)_recoveryManager.getModules())) {
            if(recoveryModule instanceof XARecoveryModule) {
                xaRecoveryModule = (XARecoveryModule)recoveryModule;
                break;
            }
        }

        if(xaRecoveryModule == null) {
            log.error("No suitable recovery module in which to register XAResourceRecovery instance");
            throw new IllegalStateException("No xa recovery module present in this server");
        }

        xaRecoveryModule.addXAResourceRecoveryHelper(new XAResourceRecoveryHelperWrapper(xaResourceRecovery));
    }

    public void removeXAResourceRecovery(XAResourceRecovery xaResourceRecovery)
    {
        if(_recoveryManager == null) {
            log.warn("No recovery system from which to remove XAResourceRecovery instance");
            return;
        }

        XARecoveryModule xaRecoveryModule = null;
        for(RecoveryModule recoveryModule : ((Vector <RecoveryModule>)_recoveryManager.getModules())) {
            if(recoveryModule instanceof XARecoveryModule) {
                xaRecoveryModule = (XARecoveryModule)recoveryModule;
                break;
            }
        }

        if(xaRecoveryModule == null) {
            log.warn("No suitable recovery module in which to register XAResourceRecovery instance");
            return;
        }

        xaRecoveryModule.removeXAResourceRecoveryHelper(new XAResourceRecoveryHelperWrapper(xaResourceRecovery));
    }
}