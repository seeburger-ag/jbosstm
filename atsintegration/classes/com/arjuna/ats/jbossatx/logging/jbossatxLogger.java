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
/*
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: jbossatxLogger.java $
 */

package com.arjuna.ats.jbossatx.logging;

import com.arjuna.common.util.logging.*;
import com.arjuna.common.internal.util.logging.commonPropertyManager;
import org.jboss.logging.Logger;

import java.util.Locale;
import java.util.ResourceBundle;

public class jbossatxLogger
{
    public static LogNoi18n      logger;
    //public static Logi18n        loggerI18N;

    public static jbossatxI18NLogger i18NLogger;

    public static void initialize(LogNoi18n noi18n, Logi18n i18n)
    {
        logger = noi18n;
        //loggerI18N = i18n;
        i18NLogger = new jbossatxI18NLoggerImpl(Logger.getLogger("com.arjuna.ats.jbossatx"));
    }

    static
    {
        LogFactory.initializeModuleLogger(jbossatxLogger.class, "jbossatx_msg", "com.arjuna.ats.jbossatx");
    }
}
