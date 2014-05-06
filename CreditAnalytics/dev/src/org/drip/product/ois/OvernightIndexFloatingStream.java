
package org.drip.product.ois;

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
 * OvernightIndexFloatingStream contains an implementation of the Floating leg cash flow stream backed by an
 * 	Overnight Stream.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class OvernightIndexFloatingStream extends org.drip.product.rates.FloatingStream {

	/**
	 * Create an Instance of OvernightIndexFloatingStream
	 * 
	 * @param dblEffective Effective Date
	 * @param dblMaturity Maturity Date
	 * @param dblSpread Spread
	 * @param bIsReference Is this the Reference Leg in a Float-Float Swap?
	 * @param fri Floating Rate Index
	 * @param iFreq Frequency
	 * @param strCouponDC Coupon Day Count
	 * @param bApplyCpnEOMAdj TRUE => Apply the Coupon EOM Adjustment
	 * @param strAccrualDC Accrual Day Count
	 * @param bApplyAccEOMAdj TRUE => Apply the Accrual EOM Adjustment
	 * @param bFullFirstPeriod TRUE => Generate full first-stub
	 * @param dapEffective Effective DAP
	 * @param dapMaturity Maturity DAP
	 * @param dapPeriodStart Period Start DAP
	 * @param dapPeriodEnd Period End DAP
	 * @param dapAccrualStart Accrual Start DAP
	 * @param dapAccrualEnd Accrual End DAP
	 * @param dapPay Pay DAP
	 * @param dapReset Reset DAP
	 * @param notlSchedule Notional Schedule
	 * @param dblNotional Initial Notional Amount
	 * @param strIR IR Curve
	 * @param strCalendar Calendar
	 * 
	 * return Instance of OvernightIndexFloatingStream
	 */

	public static OvernightIndexFloatingStream Create (
		final double dblEffective,
		final double dblMaturity,
		final double dblSpread,
		final boolean bIsReference,
		final org.drip.product.params.FloatingRateIndex fri,
		final int iFreq,
		final java.lang.String strCouponDC,
		final boolean bApplyCpnEOMAdj,
		final java.lang.String strAccrualDC,
		final boolean bApplyAccEOMAdj,
		final boolean bFullFirstPeriod,
		final org.drip.analytics.daycount.DateAdjustParams dapEffective,
		final org.drip.analytics.daycount.DateAdjustParams dapMaturity,
		final org.drip.analytics.daycount.DateAdjustParams dapPeriodStart,
		final org.drip.analytics.daycount.DateAdjustParams dapPeriodEnd,
		final org.drip.analytics.daycount.DateAdjustParams dapAccrualStart,
		final org.drip.analytics.daycount.DateAdjustParams dapAccrualEnd,
		final org.drip.analytics.daycount.DateAdjustParams dapPay,
		final org.drip.analytics.daycount.DateAdjustParams dapReset,
		final org.drip.product.params.FactorSchedule notlSchedule,
		final double dblNotional,
		final java.lang.String strIR,
		final java.lang.String strCalendar)
	{
		java.util.List<org.drip.analytics.period.CashflowPeriod> lsCouponPeriod =
			org.drip.analytics.period.CashflowPeriod.GeneratePeriodsBackward (
				dblEffective, 		// Effective
				dblMaturity, 		// Maturity
				dapEffective, 		// Effective DAP
				dapMaturity, 		// Maturity DAP
				dapPeriodStart, 	// Period Start DAP
				dapPeriodEnd, 		// Period End DAP
				dapAccrualStart, 	// Accrual Start DAP
				dapAccrualEnd, 		// Accrual End DAP
				dapPay, 			// Pay DAP
				dapReset, 			// Reset DAP
				iFreq, 				// Coupon Freq
				strCouponDC, 		// Coupon Day Count
				bApplyCpnEOMAdj,
				strAccrualDC, 		// Accrual Day Count
				bApplyAccEOMAdj,
				bFullFirstPeriod,	// Full First Coupon Period?
				false, 				// Merge the first 2 Periods - create a long stub?
				false,
				strCalendar);

		try {
			return new OvernightIndexFloatingStream (dblEffective, dblMaturity, dblSpread, bIsReference, fri,
				notlSchedule, dblNotional, strIR, lsCouponPeriod);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override protected double getFixing (
		final double dblValueDate,
		final org.drip.product.params.FloatingRateIndex fri,
		final org.drip.analytics.period.CashflowPeriod currentPeriod,
		final org.drip.param.definition.ComponentMarketParams mktParams)
		throws java.lang.Exception
	{
		try {
			return super.getFixing (dblValueDate, org.drip.product.params.FloatingRateIndex.Create
				(fri.currency(), fri.index(), "1D"), currentPeriod, mktParams);
		} catch (java.lang.Exception e) {
		}

		java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mapFixings =
				mktParams.fixings();

		if (null == mapFixings || 0 == mapFixings.size())
			throw new java.lang.Exception
				("OvernightIndexFloatingStream::getCompoundedOvernightFixing => Cannot get Fixing");

		double dblPrevDate = currentPeriod.getStartDate();

		java.lang.String strCalendar = currentPeriod.calendar();

		java.lang.String strAccrualDC = currentPeriod.accrualDC();

		java.lang.String strFRIFullName = fri.fullyQualifiedName();

		double dblAccruedAmount = 0.;
		double dblDate = dblPrevDate + 1;
		double dblPeriodEndDate = dblValueDate;
		double dblLastCoupon = java.lang.Double.NaN;

		while (dblDate <= dblPeriodEndDate) {
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapFRIFixing = mapFixings.get
				(new org.drip.analytics.date.JulianDate (dblDate));

			if (null != mapFRIFixing && mapFRIFixing.containsKey (strFRIFullName)) {
				java.lang.Double dblFixing = mapFRIFixing.get (strFRIFullName);

				if (null != dblFixing && org.drip.quant.common.NumberUtil.IsValid (dblFixing)) {
					dblAccruedAmount += org.drip.analytics.daycount.Convention.YearFraction (dblPrevDate,
						dblDate, strAccrualDC, false, java.lang.Double.NaN, null, strCalendar) *
							(dblLastCoupon = dblFixing);

					dblPrevDate = dblDate;
				}
			}

			++dblDate;
		}

		if (!org.drip.quant.common.NumberUtil.IsValid (dblLastCoupon))
			throw new java.lang.Exception
				("OvernightIndexFloatingStream::getCompoundedOvernightFixing => Cannot get Fixing");

		return (dblAccruedAmount + org.drip.analytics.daycount.Convention.YearFraction (dblPrevDate,
			dblPeriodEndDate, currentPeriod.accrualDC(), false, java.lang.Double.NaN, null, strCalendar) *
				dblLastCoupon) / org.drip.analytics.daycount.Convention.YearFraction
					(currentPeriod.getStartDate(), dblValueDate, strAccrualDC, false, java.lang.Double.NaN,
						null, strCalendar);
	}

	/**
	 * OvernightIndexFloatingStream constructor
	 * 
	 * @param dblEffective Effective Date
	 * @param dblMaturity Maturity Date
	 * @param dblSpread Spread
	 * @param bIsReference Is this the Reference Leg in a Float-Float Swap?
	 * @param fri Floating Rate Index
	 * @param notlSchedule Notional Schedule
	 * @param dblNotional Initial Notional Amount
	 * @param strIR IR Curve
	 * @param lsCouponPeriod List of the Coupon Periods
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public OvernightIndexFloatingStream (
		final double dblEffective,
		final double dblMaturity,
		final double dblSpread,
		final boolean bIsReference,
		final org.drip.product.params.FloatingRateIndex fri,
		final org.drip.product.params.FactorSchedule notlSchedule,
		final double dblNotional,
		final java.lang.String strIR,
		final java.util.List<org.drip.analytics.period.CashflowPeriod> lsCouponPeriod)
		throws java.lang.Exception
	{
		super (dblEffective, dblMaturity, dblSpread, bIsReference, fri, notlSchedule, dblNotional, strIR,
			lsCouponPeriod);
	}
}
