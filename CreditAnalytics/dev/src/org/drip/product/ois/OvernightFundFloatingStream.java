
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
	 * OvernightFundFloatingStream constructor
	 * 
	 * @param strCurrency Cash Flow Currency
	 * @param dblSpread Spread
	 * @param dblNotional Initial Notional Amount
	 * @param notlSchedule Notional Schedule
	 * @param lsCouponPeriod List of the Coupon Periods
	 * @param fri Floating Rate Index
	 * @param bIsReference Is this the Reference Leg in a Float-Float Swap?
	 * 
	 * @throws java.lang.Exception Thrown if inputs are invalid
	 */

	public OvernightFundFloatingStream (
		final java.lang.String strCurrency,
		final double dblSpread,
		final double dblNotional,
		final org.drip.product.params.FactorSchedule notlSchedule,
		final java.util.List<org.drip.analytics.period.CashflowPeriod> lsCouponPeriod,
		final org.drip.product.params.FloatingRateIndex fri,
		final boolean bIsReference)
		throws java.lang.Exception
	{
		super (strCurrency, dblSpread, dblNotional, notlSchedule, lsCouponPeriod, fri, bIsReference);
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

	@Override public java.lang.String name()
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

		java.util.List<org.drip.analytics.period.CashflowPeriod> lsOISFloatCouponPeriod =
			org.drip.analytics.period.CashflowPeriod.GenerateDailyPeriod
				(org.drip.analytics.date.JulianDate.Today().julian(),
					org.drip.analytics.date.JulianDate.Today().addTenor ("1Y").julian(), null, null,
						"30/360", "JPY", "JPY");

		OvernightFundFloatingStream fs = new OvernightFundFloatingStream ("JPY", 0.03, -1., null,
			lsOISFloatCouponPeriod, org.drip.product.params.FloatingRateIndex.Create ("JPY-OIS-ON"), false);

		System.out.println ("\tEffective: " + new org.drip.analytics.date.JulianDate (fs._dblEffective) +
			"=>" + new org.drip.analytics.date.JulianDate (fs._dblMaturity));

		byte[] abFS = fs.serialize();

		System.out.println (new java.lang.String (abFS));

		OvernightFundFloatingStream fsDeser = new OvernightFundFloatingStream (abFS);

		System.out.println (new java.lang.String (fsDeser.serialize()));

		for (org.drip.analytics.period.CashflowPeriod cfPeriod : fsDeser.cashFlowPeriod())
			System.out.println ("\t" + new org.drip.analytics.date.JulianDate (cfPeriod.getStartDate()) +
				"=>" + new org.drip.analytics.date.JulianDate (cfPeriod.getEndDate()));
	}
}
