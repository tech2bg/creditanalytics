
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
 * OISFloatingStream contains an implementation of the OIS Floating leg daily compounded cash flow stream.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class OISFloatingStream extends org.drip.product.rates.FloatingStream {
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
				mktParams.getFixings();

		if (null == mapFixings || 0 == mapFixings.size())
			throw new java.lang.Exception
				("OISFloatingStream::getCompoundedOvernightFixing => Cannot get Fixing");

		double dblAccruedAmount = 0.;
		double dblLastCoupon = java.lang.Double.NaN;

		double dblCouponEndDate = currentPeriod.getEndDate();

		java.lang.String strCalendar = currentPeriod.calendar();

		java.lang.String strAccrualDC = currentPeriod.accrualDC();

		double dblPrevDate = org.drip.analytics.daycount.Convention.RollDate (dblValueDate,
			org.drip.analytics.daycount.Convention.DR_FOLL, strCalendar);

		for (double dblDate = org.drip.analytics.daycount.Convention.RollDate (dblPrevDate + 1,
			org.drip.analytics.daycount.Convention.DR_FOLL, strCalendar); dblDate <= dblCouponEndDate;
				++dblDate) {
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapIndexFixing =
				mapFixings.get (new org.drip.analytics.date.JulianDate (dblDate));

			if (null == mapIndexFixing || 0 == mapIndexFixing.size()) {
				java.lang.Double dblFixing = mapIndexFixing.get (fri.fullyQualifiedName());

				if (null != dblFixing && org.drip.quant.common.NumberUtil.IsValid (dblFixing)) {
					dblAccruedAmount += org.drip.analytics.daycount.Convention.YearFraction (dblPrevDate,
						dblDate, strAccrualDC, false, java.lang.Double.NaN, null, strCalendar) *
							(dblLastCoupon = dblFixing);

					dblPrevDate = dblDate;
				}
			}
		}

		if (!org.drip.quant.common.NumberUtil.IsValid (dblLastCoupon))
			throw new java.lang.Exception
				("OISFloatingStream::getCompoundedOvernightFixing => Cannot get Fixing");

		return dblAccruedAmount += org.drip.analytics.daycount.Convention.YearFraction (dblPrevDate,
			dblCouponEndDate, currentPeriod.accrualDC(), false, java.lang.Double.NaN, null,
				currentPeriod.calendar()) * dblLastCoupon;
	}

	/**
	 * Create an Instance of OISFloatingStream
	 * 
	 * @param dblEffective Effective Date
	 * @param dblMaturity Maturity Date
	 * @param dblSpread Spread
	 * @param fri Floating Rate Index
	 * @param strDC Accrual/Coupon Day Count
	 * @param dapReset Reset DAP
	 * @param dapPay Pay DAP
	 * @param notlSchedule Notional Schedule
	 * @param dblNotional Initial Notional Amount
	 * @param strIR IR Curve
	 * @param strCalendar Calendar
	 * @param bIsReference Is this the Reference Leg in a Float-Float Swap?
	 * 
	 * @return Instance of OISFloatingStream
	 */

	public static final OISFloatingStream Create (
		final double dblEffective,
		final double dblMaturity,
		final double dblSpread,
		final org.drip.product.params.FloatingRateIndex fri,
		final java.lang.String strDC,
		final org.drip.analytics.daycount.DateAdjustParams dapReset,
		final org.drip.analytics.daycount.DateAdjustParams dapPay,
		final org.drip.product.params.FactorSchedule notlSchedule,
		final double dblNotional,
		final java.lang.String strIR,
		final java.lang.String strCalendar,
		final boolean bIsReference)
		throws java.lang.Exception
	{
		java.util.List<org.drip.analytics.period.CashflowPeriod> lsCouponPeriod =
			org.drip.analytics.period.CashflowPeriod.GenerateDailyPeriod (dblEffective, dblMaturity,
				dapReset, dapPay, strDC, strCalendar);

		try {
			return new OISFloatingStream (dblEffective, dblMaturity, dblSpread, bIsReference, fri,
				notlSchedule, dblNotional, strIR, lsCouponPeriod);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * OISFloatingStream constructor
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

	public OISFloatingStream (
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

	/**
	 * OISFloatingStream de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if OISFloatingStream cannot be properly de-serialized
	 */

	public OISFloatingStream (
		final byte[] ab)
		throws java.lang.Exception
	{
		super (ab);
	}

	@Override public java.lang.String getComponentName()
	{
		return "OISFloatingStream=" + org.drip.analytics.date.JulianDate.fromJulian (_dblMaturity);
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new OISFloatingStream (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public static final void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		org.drip.service.api.CreditAnalytics.Init ("");

		OISFloatingStream fs = org.drip.product.ois.OISFloatingStream.Create
			(org.drip.analytics.date.JulianDate.Today().getJulian(),
				org.drip.analytics.date.JulianDate.Today().addTenor ("1Y").getJulian(), 0.03,
					org.drip.product.params.FloatingRateIndex.Create ("JPY-OIS-ON"), "30/360", null, null,
						null, 1., "JPY", "JPY", false);

		System.out.println ("\tEffective: " + new org.drip.analytics.date.JulianDate (fs._dblEffective) +
			"=>" + new org.drip.analytics.date.JulianDate (fs._dblMaturity));

		byte[] abFS = fs.serialize();

		System.out.println (new java.lang.String (abFS));

		OISFloatingStream fsDeser = new OISFloatingStream (abFS);

		System.out.println (new java.lang.String (fsDeser.serialize()));

		for (org.drip.analytics.period.CashflowPeriod cfPeriod : fsDeser.getCashFlowPeriod())
			System.out.println ("\t" + new org.drip.analytics.date.JulianDate (cfPeriod.getStartDate()) +
				"=>" + new org.drip.analytics.date.JulianDate (cfPeriod.getEndDate()));
	}
}
