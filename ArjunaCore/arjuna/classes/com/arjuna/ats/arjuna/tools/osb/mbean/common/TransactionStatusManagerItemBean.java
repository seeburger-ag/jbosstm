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
 * (C) 2009
 * @author Red Hat Middleware LLC.
 */
package com.arjuna.ats.arjuna.tools.osb.mbean.common;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.tools.osb.mbean.ObjStoreTypeBean;

import java.util.Date;

/**
 * @see com.arjuna.ats.arjuna.tools.osb.mbean.common.TransactionStatusManagerItemBeanMBean
 */
public class TransactionStatusManagerItemBean extends UidBean implements TransactionStatusManagerItemBeanMBean
{
    private com.arjuna.ats.internal.arjuna.recovery.TransactionStatusManagerItem txStatusMgrItem;

    public TransactionStatusManagerItemBean(ObjStoreTypeBean parent, Uid uid)
    {
        super(parent, parent.getType(), uid);

        txStatusMgrItem = com.arjuna.ats.internal.arjuna.recovery.TransactionStatusManagerItem.recreate(uid);
    }

    public String getHost()
    {
        return txStatusMgrItem.host();
    }

    public int getPort()
    {
        return txStatusMgrItem.port();
    }

    public String getDeadTime()
    {
        Date d = txStatusMgrItem.getDeadTime();

        return (d == null ? "Still live" : StateBean.formatter.format(d));
    }
}