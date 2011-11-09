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
 * Copyright (C) 2002,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: PrintTransaction.java,v 1.1.24.1 2005/11/22 10:36:12 kconner Exp $
 */
package com.arjuna.wst11.tests.junit.basic;

import javax.inject.Inject;

import org.junit.Test;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.runner.RunWith;

/**
 * @author Mark Little (mark.little@arjuna.com)
 * @version $Id: PrintTransaction.java,v 1.1.24.1 2005/11/22 10:36:12 kconner Exp $
 * @since 1.0.
 */

@RunWith(Arquillian.class)
public class PrintTransactionTest
{
	@Inject
	PrintTransaction test;

	@Deployment
	public static WebArchive createDeployment() {
		WebArchive archive = ShrinkWrap
		.create(WebArchive.class, "test.war")
		.addClass(PrintTransaction.class)
		.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");

		archive.delete(ArchivePaths.create("META-INF/MANIFEST.MF"));

		// Need to make sure we add the arquillian-service and msc as a
		// dependency
		final String ManifestMF = "Manifest-Version: 1.0\n"
			+ "Dependencies: org.jboss.modules,deployment.arquillian-service,org.jboss.msc,org.jboss.jts,org.jboss.xts\n";
		archive.setManifest(new StringAsset(ManifestMF));

		return archive;
	}

	@Test
	public void test() throws Exception {
		test.testPrintTransaction();
	}
}
