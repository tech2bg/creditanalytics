
package org.drip.analytics.support;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * 
 *  This file is part of DRIP, a free-software/open-source library for fixed income analysts and developers -
 * 		http://www.credit-trader.org/Begin.html
 * 
 *  DRIP is a free, full featured, fixed income rates, credit, and FX analytics library with a focus towards
 *  	pricing/valuation, risk, and market making.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *   	you may not use this file except in compliance with the License.
 *   
 *  You may obtain a copy of the License at
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  	distributed under the License is distributed on an "AS IS" BASIS,
 *  	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  
 *  See the License for the specific language governing permissions and
 *  	limitations under the License.
 */

/**
 * ResetUtil contains the Reset Period Manipulation Functionality
 * 
 * @author Lakshmi Krishnamurthy
 */

public class ResetUtil {

	/**
	 * Accrual Compounding Rule - Arithmetic
	 */

	public static final int ACCRUAL_COMPOUNDING_RULE_ARITHMETIC = 1;

	/**
	 * Accrual Compounding Rule - Geometric
	 */

	public static final int ACCRUAL_COMPOUNDING_RULE_GEOMETRIC = 2;

	/**
	 * Verify if the Specified Accrual Compounding Rule is a Valid One
	 * 
	 * @param iAccrualCompoundingRule The Accrual Compounding Rule
	 * 
	 * @return TRUE => The Accrual Compounding Rule is valid
	 */

	public static final boolean ValidateCompoundingRule (
		final int iAccrualCompoundingRule)
	{
		return ACCRUAL_COMPOUNDING_RULE_ARITHMETIC == iAccrualCompoundingRule ||
			ACCRUAL_COMPOUNDING_RULE_GEOMETRIC == iAccrualCompoundingRule;
	}

	/**
	 * Create a Reset Period Container that from the List of Daily Reset Periods between the specified Dates
	 * 
	 * @param dblLeft The Left Date
	 * @param dblRight The Right Date
	 * @param iAccrualCompoundingRule The Accrual Compounding Rule
	 * @param strCalendar The Calendar
	 * 
	 * @return The Reset Period Container that uses the List of Daily Reset Periods between the specified
	 * 	Dates
	 */

	public static final org.drip.analytics.period.ResetPeriodContainer DailyResetPeriod (
		final double dblLeft,
		final double dblRight,
		final int iAccrualCompoundingRule,
		final java.lang.String strCalendar)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblLeft) || !org.drip.quant.common.NumberUtil.IsValid
			(dblRight) || dblLeft >= dblRight)
			return null;

		double dblStart = dblLeft;
		double dblEnd = java.lang.Double.NaN;
		org.drip.analytics.period.ResetPeriodContainer rpc = null;

		try {
			dblEnd = new org.drip.analytics.date.JulianDate (dblStart).addBusDays (1, strCalendar).julian();

			rpc = new org.drip.analytics.period.ResetPeriodContainer (iAccrualCompoundingRule);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		while (dblEnd <= dblRight) {
			try {
				if (!rpc.appendResetPeriod (new org.drip.analytics.period.ResetPeriod (dblStart, dblEnd,
					dblStart)))
					return null;

				dblStart = dblEnd;

				dblEnd = new org.drip.analytics.date.JulianDate (dblStart).addBusDays (1,
					strCalendar).julian();
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return rpc;
	}

	/**
	 * Merge the specified Reset Period Lists onto a single Composite Reset Period List
	 * 
	 * @param rpcLeft The Left Reset Period Container
	 * @param rpcRight The Right Reset Period Container
	 * 
	 * @return The Composite Reset Period Container
	 */

	public static final org.drip.analytics.period.ResetPeriodContainer MergeResetPeriods (
		final org.drip.analytics.period.ResetPeriodContainer rpcLeft,
		final org.drip.analytics.period.ResetPeriodContainer rpcRight)
	{
		if (null == rpcLeft || null == rpcRight) return null;

		int iAccrualCompoundingRule = rpcLeft.accrualCompoundingRule();

		if (iAccrualCompoundingRule != rpcRight.accrualCompoundingRule()) return null;

		java.util.List<org.drip.analytics.period.ResetPeriod> lsResetPeriodsLeft = rpcLeft.resetPeriods();

		java.util.List<org.drip.analytics.period.ResetPeriod> lsResetPeriodsRight = rpcRight.resetPeriods();

		if (null == lsResetPeriodsLeft || null == lsResetPeriodsRight) return null;

		int iNumPeriodsLeft = lsResetPeriodsLeft.size();

		int iNumPeriodsRight = lsResetPeriodsRight.size();

		if (0 == iNumPeriodsLeft || 0 == iNumPeriodsRight ||
			lsResetPeriodsLeft.get (iNumPeriodsLeft - 1).end() != lsResetPeriodsRight.get (0).start())
			return null;

		org.drip.analytics.period.ResetPeriodContainer rpc = null;

		try {
			rpc = new org.drip.analytics.period.ResetPeriodContainer (iAccrualCompoundingRule);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		for (org.drip.analytics.period.ResetPeriod rp : lsResetPeriodsLeft) {
			if (!rpc.appendResetPeriod (rp)) return null;
		}

		for (org.drip.analytics.period.ResetPeriod rp : lsResetPeriodsRight) {
			if (!rpc.appendResetPeriod (rp)) return null;
		}

		return rpc;
	}
}
