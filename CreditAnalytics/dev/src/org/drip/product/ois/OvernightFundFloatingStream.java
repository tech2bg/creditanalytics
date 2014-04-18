
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
 * OvernightFundFloatingStream contains an implementation of the OIS Floating leg daily compounded cash flow
 *  stream.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class OvernightFundFloatingStream extends org.drip.product.rates.FloatingStream {

	/**
	 * Create an Instance of OvernightFundFloatingStream
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
	 * @return Instance of OvernightFundFloatingStream
	 */

	public static final OvernightFundFloatingStream Create (
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
			return new OvernightFundFloatingStream (dblEffective, dblMaturity, dblSpread, bIsReference, fri,
				notlSchedule, dblNotional, strIR, lsCouponPeriod);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * OvernightFundFloatingStream constructor
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

	public OvernightFundFloatingStream (
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
	 * OvernightFundFloatingStream de-serialization from input byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @throws java.lang.Exception Thrown if OISFloatingStream cannot be properly de-serialized
	 */

	public OvernightFundFloatingStream (
		final byte[] ab)
		throws java.lang.Exception
	{
		super (ab);
	}

	@Override public java.lang.String componentName()
	{
		return "OvernightFundFloatingStream=" + org.drip.analytics.date.JulianDate.fromJulian (_dblMaturity);
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		try {
			return new OvernightFundFloatingStream (ab);
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

		OvernightFundFloatingStream fs = org.drip.product.ois.OvernightFundFloatingStream.Create
			(org.drip.analytics.date.JulianDate.Today().getJulian(),
				org.drip.analytics.date.JulianDate.Today().addTenor ("1Y").getJulian(), 0.03,
					org.drip.product.params.FloatingRateIndex.Create ("JPY-OIS-ON"), "30/360", null, null,
						null, 1., "JPY", "JPY", false);

		System.out.println ("\tEffective: " + new org.drip.analytics.date.JulianDate (fs._dblEffective) +
			"=>" + new org.drip.analytics.date.JulianDate (fs._dblMaturity));

		byte[] abFS = fs.serialize();

		System.out.println (new java.lang.String (abFS));

		OvernightFundFloatingStream fsDeser = new OvernightFundFloatingStream (abFS);

		System.out.println (new java.lang.String (fsDeser.serialize()));

		for (org.drip.analytics.period.CashflowPeriod cfPeriod : fsDeser.getCashFlowPeriod())
			System.out.println ("\t" + new org.drip.analytics.date.JulianDate (cfPeriod.getStartDate()) +
				"=>" + new org.drip.analytics.date.JulianDate (cfPeriod.getEndDate()));
	}
}
