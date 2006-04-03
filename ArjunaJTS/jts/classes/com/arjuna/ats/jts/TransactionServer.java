/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
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
 * $Id: TransactionServer.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.jts;

import com.arjuna.ats.jts.common.jtsPropertyManager;

import com.arjuna.ats.internal.jts.ORBManager;

import com.arjuna.ats.arjuna.common.*;

import com.arjuna.orbportability.*;

import com.arjuna.common.util.propertyservice.PropertyManager;

import org.omg.CosTransactions.*;

public class TransactionServer
{
    
    public static void main (String[] args)
    {
	String refFile = com.arjuna.orbportability.Services.transactionService;
	String objectName = null;
	boolean printReady = false;
	ORB myORB = null;
	RootOA myOA = null;
	
	for (int i = 0; i < args.length; i++)
	{
	    if (args[i].compareTo("-otsname") == 0)
		objectName = args[i+1];
	    if (args[i].compareTo("-test") == 0)
		printReady = true;
	    if (args[i].compareTo("-help") == 0)
	    {
		System.out.println("foo Usage: [-otsname <name>] [-help] [-version]");
		System.exit(0);
	    }
	    if (args[i].compareTo("-version") == 0)
	    {
		System.out.println("TransactionServer version "+com.arjuna.ats.jts.common.Configuration.version());
		System.exit(0);
	    }
	}

	com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple theOTS = null;
	int resolver = com.arjuna.orbportability.common.Configuration.bindDefault();
	String resolveService = jtsPropertyManager.propertyManager.getProperty(com.arjuna.orbportability.common.Environment.RESOLVE_SERVICE);

	if (resolveService != null)
	{
	    if (resolveService.compareTo("NAME_SERVICE") == 0)
		resolver = com.arjuna.orbportability.Services.NAME_SERVICE;
	    else
	    {
		if (resolveService.compareTo("BIND_CONNECT") == 0)
		    resolver = com.arjuna.orbportability.Services.BIND_CONNECT;
		else
		{
		    if (resolveService.compareTo("FILE") == 0)
			resolver = com.arjuna.orbportability.Services.FILE;
		    else
		    {
			if (resolveService.compareTo("RESOLVE_INITIAL_REFERENCES") == 0)
			    resolver = com.arjuna.orbportability.Services.RESOLVE_INITIAL_REFERENCES;
		    }
		}
	    }
	}
	
	try
	{
	    try
	    {
		myORB = ORB.getInstance("TransactionServer");
		myOA = OA.getRootOA(myORB);
	    
		myORB.initORB(args, null);
		myOA.initOA();

		ORBManager.setORB(myORB);
		ORBManager.setPOA(myOA);
	    }
	    catch (Exception e)
	    {
		System.err.println("Initialisation of TransactionServer failed: "+e);

		throw e;
	    }

	    theOTS = new com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple(objectName);

	    try
	    {
		Services myServ = new Services(myORB);
		
		System.err.println("**checking "+resolver);
		
		if (resolver != com.arjuna.orbportability.Services.BIND_CONNECT)
		{
		    String[] params = new String[1];

		    params[0] = com.arjuna.orbportability.Services.otsKind;

		    /*
		     * Register using the default mechanism.
		     */

		    System.err.println("**registering**");
		    
		    myServ.registerService(theOTS.getReference(), com.arjuna.orbportability.Services.transactionService, params, resolver);

		    params = null;
		}

		if (!printReady)
		    System.out.println("Transaction manager registered.");
	    }
	    catch (Exception e1)
	    {
		System.err.println("Failed to bind transaction manager: "+e1);
		System.exit(0);
	    }

	    if (printReady)
		System.out.println("Ready");
	    else
		System.out.println("ArjunaTS OTS Server startup.");

	    if (resolver == com.arjuna.orbportability.Services.BIND_CONNECT)
		myOA.run(com.arjuna.orbportability.Services.transactionService);
	    else
		myOA.run();
	}
	catch (Exception e2)
	{
	    System.err.println("TransactionServer caught exception "+e2);
	}
    
	System.out.println("ArjunaTS OTS Server shutdown");

	theOTS = null;

	if (myOA != null)
	    myOA.destroy();

	if (myORB != null)
	    myORB.shutdown();
    }

}
