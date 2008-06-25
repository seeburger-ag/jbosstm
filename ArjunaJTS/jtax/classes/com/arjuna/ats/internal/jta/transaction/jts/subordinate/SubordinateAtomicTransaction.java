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
 * Copyright (C) 2003,
 * 
 * Hewlett-Packard Arjuna Labs, Newcastle upon Tyne, Tyne and Wear, UK.
 * 
 * $Id: SubordinateAtomicTransaction.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.jta.transaction.jts.subordinate;

import org.omg.CORBA.SystemException;
import org.omg.CosTransactions.HeuristicHazard;
import org.omg.CosTransactions.HeuristicMixed;
import org.omg.CosTransactions.NoTransaction;
import org.omg.CosTransactions.WrongTransaction;

import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.coordinator.TwoPhaseOutcome;
import com.arjuna.ats.internal.jts.ControlWrapper;
import com.arjuna.ats.internal.jts.interposition.ServerControlWrapper;
import com.arjuna.ats.internal.jts.orbspecific.interposition.ServerControl;
import com.arjuna.ats.internal.jts.orbspecific.interposition.coordinator.ServerTransaction;

/**
 * A subordinate JTA transaction; used when importing another
 * transaction context.
 * 
 * @author mcl
 */

public class SubordinateAtomicTransaction extends com.arjuna.ats.internal.jta.transaction.jts.AtomicTransaction
{

	public SubordinateAtomicTransaction (ControlWrapper tx)
	{
		super(tx);
	}

	/**
	 * @message com.arjuna.ats.internal.jta.transaction.jts.atomictxnobegin
	 *          [com.arjuna.ats.internal.jta.transaction.jts.atomictxnobegin]
	 *          Cannot call begin on imported AtomicTransaction instance.
	 */

	/**
	 * Does not change thread-to-tx association as base class commit does.
	 */

	public synchronized void end (boolean report_heuristics)
			throws NoTransaction, HeuristicMixed, HeuristicHazard,
			WrongTransaction, SystemException
	{
		throw new WrongTransaction();
	}
	
	/**
	 * Does not change thread-to-tx association as base class rollback does.
	 */

	public synchronized void abort () throws NoTransaction, WrongTransaction,
			SystemException
	{
		throw new WrongTransaction();
	}
	
	/**
	 * 
	 * @return TwoPhaseOutcome
	 * @throws SystemException
	 */
	
	public int doPrepare () throws SystemException
	{
		ServerTransaction stx = getTransaction();
		
		// TODO make sure synchronizations go off
		// require registration of synchronization interposed resource as well!
		
		if (stx != null)
			return stx.doPrepare();
		else
			return TwoPhaseOutcome.INVALID_TRANSACTION;
	}
	
	/**
	 * 
	 * @return ActionStatus
	 * @throws SystemException
	 */
	
	public int doCommit () throws SystemException
	{	
		ServerTransaction stx = getTransaction();
		
		try
		{
			if (stx != null)
				return stx.doPhase2Commit(false);
		}
		catch (Exception ex)
		{
		}
		
		// TODO error
		
		return TwoPhaseOutcome.ONE_PHASE_ERROR;
	}
	
	/**
	 * 
	 * @return ActionStatus
	 * @throws SystemException
	 */
	
	public int doRollback () throws SystemException
	{
		ServerTransaction stx = getTransaction();
		
		try
		{	
			if (stx != null)
				return stx.doPhase2Abort();
		}
		catch (Exception ex)
		{
		}
			
		// TODO error
		
		return TwoPhaseOutcome.FINISH_ERROR;
	}
	
	public int doOnePhaseCommit () throws SystemException
	{
		try
		{
			int res = doPrepare();
			
			switch (res)
			{
				case TwoPhaseOutcome.PREPARE_OK:
					doCommit();
					break;
				case TwoPhaseOutcome.PREPARE_NOTOK:
					doRollback();
				
					return TwoPhaseOutcome.HEURISTIC_ROLLBACK;
				case TwoPhaseOutcome.PREPARE_READONLY:
					return TwoPhaseOutcome.FINISH_OK;
				default:
					doRollback();
				
					return res;
			}
		}
		catch (Exception ex)
		{
			return TwoPhaseOutcome.HEURISTIC_HAZARD;
		}
		
		return TwoPhaseOutcome.FINISH_OK;
		
		// TODO error
	}
	
	public void doForget () throws SystemException
	{
		ServerTransaction stx = getTransaction();
		
		try
		{	
			if (stx != null)
				stx.doForget();
		}
		catch (Exception ex)
		{
		}
	}
	
	/**
	 * By default the BasicAction class only allows the termination of a
	 * transaction if it's the one currently associated with the thread. We
	 * override this here.
	 * 
	 * @return <code>false</code> to indicate that this transaction can only
	 *         be terminated by the right thread.
	 */

	protected boolean checkForCurrent ()
	{
		return false;
	}

	private ServerTransaction getTransaction ()
	{
		ServerControlWrapper scw = (ServerControlWrapper) super._theAction;
		ServerControl sc = (ServerControl) scw.getImple();
		
		return (ServerTransaction) sc.getImplHandle();
	}
	
}
