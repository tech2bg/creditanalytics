
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
 * ForwardDecompositionUtil contains the utility functions needed to carry out periodic decomposition at MTM
 *  sync points for the given stream.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class ForwardDecompositionUtil {

	/**
	 * Decompose the Fixed Stream into an Array of Single Forward Period Fixed Streams
	 * 
	 * @param fs The Fixed Stream
	 * @param iNumPeriodsToAccumulate Number of Forward Periods to roll into one
	 * 
	 * @return The Array of Single Forward Period Fixed Streams
	 */
	
	public static final org.drip.product.cashflow.FixedStream[] SinglePeriodFixedStreamDecompose (
		final org.drip.product.cashflow.FixedStream fs,
		final int iNumPeriodsToAccumulate)
	{
		if (null == fs) return null;

		java.util.List<org.drip.analytics.period.CashflowPeriod> lsCouponFlow = fs.cashFlowPeriod();

		java.lang.String strCurrency = fs.couponCurrency()[0];

		double dblInitialNotional = fs.initialNotional();

		int iNumPeriods = lsCouponFlow.size();

		int iCFPIndex = 0;
		int iNumPeriodsAccumulated = 0;
		double dblCoupon = java.lang.Double.NaN;
		int iNumForward = iNumPeriods / iNumPeriodsToAccumulate;
		org.drip.product.cashflow.FixedStream[] aFS = new org.drip.product.cashflow.FixedStream[iNumForward];

		try {
			dblCoupon = fs.coupon (fs.effective().julian(), null, null).nominal();
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		java.util.List<java.util.List<org.drip.analytics.period.CashflowPeriod>> lslsCouponPeriod = new
			java.util.ArrayList<java.util.List<org.drip.analytics.period.CashflowPeriod>>();

		for (int i = 0; i < iNumForward; ++i)
			lslsCouponPeriod.add (new java.util.ArrayList<org.drip.analytics.period.CashflowPeriod>());

		for (org.drip.analytics.period.CashflowPeriod cfp : lsCouponFlow) {
			java.util.List<org.drip.analytics.period.CashflowPeriod> lsCouponPeriod = lslsCouponPeriod.get
				(iCFPIndex);

			lsCouponPeriod.add (cfp);

			if (++iNumPeriodsAccumulated != iNumPeriodsToAccumulate) continue;

			iNumPeriodsAccumulated = 0;

			try {
				aFS[iCFPIndex++] = new org.drip.product.cashflow.FixedStream (strCurrency, null, dblCoupon,
					dblInitialNotional, null, lsCouponPeriod);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return aFS;
	}

	/**
	 * Decompose the Floating Stream into an Array of Single Forward Period Floating Streams
	 * 
	 * @param fs The Floating Stream
	 * @param iNumPeriodsToAccumulate Number of Forward Periods to roll into one
	 * 
	 * @return The Array of Single Forward Period Floating Streams
	 */

	public static final org.drip.product.cashflow.FloatingStream[] SinglePeriodFloatingStreamDecompose (
		final org.drip.product.cashflow.FloatingStream fs,
		final int iNumPeriodsToAccumulate)
	{
		if (null == fs) return null;

		java.util.List<org.drip.analytics.period.CashflowPeriod> lsCouponFlow = fs.cashFlowPeriod();

		org.drip.state.identifier.ForwardLabel fri = fs.fri();

		java.lang.String strCurrency = fs.couponCurrency()[0];

		double dblNotional = fs.initialNotional();

		int iNumPeriods = lsCouponFlow.size();

		boolean bIsReference = fs.reference();

		double dblSpread = fs.spread();

		int iCFPIndex = 0;
		int iNumPeriodsAccumulated = 0;
		int iNumForward = iNumPeriods / iNumPeriodsToAccumulate;
		org.drip.product.cashflow.FloatingStream[] aFS = new org.drip.product.cashflow.FloatingStream[iNumForward];

		java.util.List<java.util.List<org.drip.analytics.period.CashflowPeriod>> lslsCouponPeriod = new
			java.util.ArrayList<java.util.List<org.drip.analytics.period.CashflowPeriod>>();

		for (int i = 0; i < iNumForward; ++i)
			lslsCouponPeriod.add (new java.util.ArrayList<org.drip.analytics.period.CashflowPeriod>());

		for (org.drip.analytics.period.CashflowPeriod cfp : lsCouponFlow) {
			java.util.List<org.drip.analytics.period.CashflowPeriod> lsCouponPeriod = lslsCouponPeriod.get
				(iCFPIndex);

			lsCouponPeriod.add (cfp);

			if (++iNumPeriodsAccumulated != iNumPeriodsToAccumulate) continue;

			iNumPeriodsAccumulated = 0;

			try {
				aFS[iCFPIndex++] = new org.drip.product.cashflow.FloatingStream (strCurrency, null, dblSpread,
					dblNotional, null, lsCouponPeriod, fri, bIsReference);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return aFS;
	}

	/**
	 * Decompose the Rates Stream into an Array of Single Forward Period Rates Streams
	 * 
	 * @param rc The Rates Stream Component
	 * @param iNumPeriodsToAccumulate Number of Forward Periods to roll into one
	 * 
	 * @return The Array of Single Forward Period Rates Streams
	 */

	private static final org.drip.product.definition.RatesComponent[] SinglePeriodStreamDecompose (
		final org.drip.product.definition.RatesComponent rc,
		final int iNumPeriodsToAccumulate)
	{
		if (rc instanceof org.drip.product.cashflow.FloatingStream)
			return SinglePeriodFloatingStreamDecompose ((org.drip.product.cashflow.FloatingStream) rc,
				iNumPeriodsToAccumulate);

		if (rc instanceof org.drip.product.cashflow.FixedStream)
			return SinglePeriodFixedStreamDecompose ((org.drip.product.cashflow.FixedStream) rc,
				iNumPeriodsToAccumulate);

		return null;
	}

	/**
	 * Decompose the Dual Stream Component into an Array of Single Forward Period Dual Streams
	 * 
	 * @param dsc The Dual Stream
	 * 
	 * @return The Array of Single Forward Period Dual Streams
	 */

	public static final org.drip.product.definition.RatesComponent[] DualStreamForwardArray (
		final org.drip.product.cashflow.DualStreamComponent dsc)
	{
		if (null == dsc) return null;

		org.drip.product.definition.RatesComponent rcDerived = dsc.derivedStream();

		org.drip.product.definition.RatesComponent rcReference = dsc.referenceStream();

		int iNumForward = 0;
		org.drip.product.definition.RatesComponent[] aRCDerivedForward = null;
		org.drip.product.definition.RatesComponent[] aRCReferenceForward = null;

		int iDerivedStreamTenorMonths = 12 / rcDerived.freq();

		int iReferenceStreamTenorMonths = 12 / rcReference.freq();

		if (iReferenceStreamTenorMonths > iDerivedStreamTenorMonths) {
			if (null == (aRCReferenceForward = SinglePeriodStreamDecompose (rcReference, 1)) || 0 ==
				(iNumForward = aRCReferenceForward.length))
				return null;

			if (null == (aRCDerivedForward = SinglePeriodStreamDecompose (rcDerived,
				iReferenceStreamTenorMonths / iDerivedStreamTenorMonths)) || iNumForward !=
					aRCDerivedForward.length)
				return null;
		} else {
			if (null == (aRCDerivedForward = SinglePeriodStreamDecompose (rcDerived, 1)) || 0 == (iNumForward
				= aRCDerivedForward.length))
				return null;

			if (null == (aRCReferenceForward = SinglePeriodStreamDecompose (rcReference,
				iDerivedStreamTenorMonths / iReferenceStreamTenorMonths)) || iNumForward !=
					aRCReferenceForward.length)
				return null;
		}

		org.drip.product.definition.RatesComponent[] aRC = new
			org.drip.product.definition.RatesComponent[iNumForward];

		for (int i = 0; i < iNumForward; ++i) {
			try {
				if (null == (aRC[i] = org.drip.product.creator.DualStreamComponentBuilder.MakeDualStream
					(aRCReferenceForward[i], aRCDerivedForward[i])))
					return null;

				aRC[i].setPrimaryCode (rcReference.name() + "::" + rcDerived.name() + "_" + i);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		return aRC;
	}

	/**
	 * Decompose the Rates Component into an Array of Single Forward Rates Components
	 * 
	 * @param rc The Rates Component
	 * 
	 * @return The Array of Single Forward Period Rates Components
	 */

	public static final org.drip.product.definition.RatesComponent[] RatesComponentForwardArray (
		final org.drip.product.definition.RatesComponent rc)
	{
		if (null == rc) return null;

		if (rc instanceof org.drip.product.cashflow.DualStreamComponent)
			return DualStreamForwardArray ((org.drip.product.cashflow.DualStreamComponent) rc);

		org.drip.product.definition.RatesComponent[] aRCForward = SinglePeriodStreamDecompose (rc, 1);

		if (null == aRCForward) return null;

		int iNumForward = aRCForward.length;

		if (0 == iNumForward) return null;

		for (int i = 0; i < iNumForward; ++i)
			aRCForward[i].setPrimaryCode (rc.name() + "_" + i);

		return aRCForward;
	}
}
