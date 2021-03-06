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
package com.arjuna.wsc11.tests;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.arjuna.wsc11.ContextFactoryMapper;
import com.arjuna.wsc11.RegistrarMapper;
import com.arjuna.wsc.tests.TestUtil;

/**
 * Initialise the test.
 * @author kevin
 */
public class TestInitialisation implements ServletContextListener
{
    /**
     * The context has been initialized.
     * @param servletContextEvent The servlet context event.
     */
    public void contextInitialized(final ServletContextEvent servletContextEvent)
    {
        final ContextFactoryMapper contextFactoryMapper = ContextFactoryMapper.getMapper() ;
        final TestContextFactory testContextFactory = new TestContextFactory(TestUtil.COORDINATION_TYPE) ;

        contextFactoryMapper.addContextFactory(TestUtil.COORDINATION_TYPE, testContextFactory) ;
        contextFactoryMapper.addContextFactory(TestUtil.INVALID_CREATE_PARAMETERS_COORDINATION_TYPE, testContextFactory) ;

        final RegistrarMapper registrarMapper = RegistrarMapper.getFactory() ;
        final TestRegistrar testRegistrar = new TestRegistrar() ;

        registrarMapper.addRegistrar(TestUtil.PROTOCOL_IDENTIFIER, testRegistrar) ;
        registrarMapper.addRegistrar(TestUtil.ALREADY_REGISTERED_PROTOCOL_IDENTIFIER, testRegistrar) ;
        registrarMapper.addRegistrar(TestUtil.INVALID_PROTOCOL_PROTOCOL_IDENTIFIER, testRegistrar) ;
        registrarMapper.addRegistrar(TestUtil.INVALID_STATE_PROTOCOL_IDENTIFIER, testRegistrar) ;
        registrarMapper.addRegistrar(TestUtil.NO_ACTIVITY_PROTOCOL_IDENTIFIER, testRegistrar) ;
    }

    /**
     * The context is about to be destroyed.
     * @param servletContextEvent The servlet context event.
     */
    public void contextDestroyed(final ServletContextEvent servletContextEvent)
    {
        final ContextFactoryMapper contextFactoryMapper = ContextFactoryMapper.getMapper() ;
        contextFactoryMapper.removeContextFactory(TestUtil.COORDINATION_TYPE);
        contextFactoryMapper.removeContextFactory(TestUtil.INVALID_CREATE_PARAMETERS_COORDINATION_TYPE);

        final RegistrarMapper registrarMapper = RegistrarMapper.getFactory() ;
        registrarMapper.removeRegistrar(TestUtil.PROTOCOL_IDENTIFIER);
        registrarMapper.removeRegistrar(TestUtil.ALREADY_REGISTERED_PROTOCOL_IDENTIFIER);
        registrarMapper.removeRegistrar(TestUtil.INVALID_PROTOCOL_PROTOCOL_IDENTIFIER);
        registrarMapper.removeRegistrar(TestUtil.INVALID_STATE_PROTOCOL_IDENTIFIER);
        registrarMapper.removeRegistrar(TestUtil.NO_ACTIVITY_PROTOCOL_IDENTIFIER);
    }
}