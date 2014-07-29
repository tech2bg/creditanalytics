
package org.drip.product.creator;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * Copyright (C) 2011 Lakshmi Krishnamurthy
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
 * RatesStreamBuilder contains the suite of helper functions for creating the Stream-based Rates Products
 *  from different kinds of inputs. In particular, it demonstrates the following:
 *  - Construction of the custom/standard fixed/floating streams from parameters.
 *  - Construction of the custom/standard IRS from parameters.
 *  - Construction of the fixed/floating streams and IRS from byte arrays.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class RatesStreamBuilder {

	/**
	 * Create a Fixed Stream instance from effective/maturity dates, coupon, and IR curve name
	 * 
	 * @param dtEffective JulianDate effective
	 * @param strMaturityTenor Maturity Tenor
	 * @param dblCoupon coupon
	 * @param iFreq Coupon Frequency
	 * @param strDayCount Day Count Convention
	 * @param strCalendar Holiday Calendar for coupon accrual
	 * @param strCurrency Currency
	 * 
	 * @return The Fixed Stream Instance
	 */

	public static final org.drip.product.stream.FixedStream CreateFixedStream (
		final org.drip.analytics.date.JulianDate dtEffective,
		final java.lang.String strMaturityTenor,
		final double dblCoupon,
		final int iFreq,
		final java.lang.String strDayCount,
		final java.lang.String strCalendar,
		final java.lang.String strCurrency)
	{
		java.util.List<org.drip.analytics.period.CashflowPeriod> lsCouponPeriod =
			org.drip.analytics.period.CashflowPeriod.GeneratePeriodsRegular (dtEffective.julian(),
				strMaturityTenor, null, iFreq, strDayCount, false, true, strCalendar, strCurrency);

		try {
			return new org.drip.product.stream.FixedStream (strCurrency, null, dblCoupon, 1., null,
				lsCouponPeriod);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create a Fixed Stream instance from effective/maturity dates, coupon, and IR curve name
	 * 
	 * @param dtEffective Effective Date
	 * @param dtMaturity Maturity Date
	 * @param dblCoupon coupon
	 * @param iFreq Coupon Frequency
	 * @param strDayCount Day Count Convention
	 * @param strCalendar Holiday Calendar for coupon accrual
	 * @param strCurrency Currency
	 * 
	 * @return The Fixed Stream Instance
	 */

	public static final org.drip.product.stream.FixedStream CreateFixedStream (
		final org.drip.analytics.date.JulianDate dtEffective,
		final org.drip.analytics.date.JulianDate dtMaturity,
		final double dblCoupon,
		final int iFreq,
		final java.lang.String strDayCount,
		final java.lang.String strCalendar,
		final java.lang.String strCurrency)
	{
		java.util.List<org.drip.analytics.period.CashflowPeriod> lsCouponPeriod =
			org.drip.analytics.period.CashflowPeriod.GeneratePeriodsBackward (dtEffective.julian(),
				dtMaturity.julian(), null, iFreq, strDayCount, false,
					org.drip.analytics.period.CashflowPeriod.NO_ADJUSTMENT, true, strCalendar, strCurrency);

		try {
			return new org.drip.product.stream.FixedStream (strCurrency, null, dblCoupon, 1., null,
				lsCouponPeriod);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create a IBOR based Floating Stream instance from effective/maturity dates, coupon, and IR curve name
	 * 
	 * @param dtEffective JulianDate effective
	 * @param strMaturityTenor Maturity Tenor
	 * @param dblSpread Spread
	 * @param iFreq Coupon Frequency
	 * @param strDayCount Day Count Convention
	 * @param strCalendar Holiday Calendar for coupon accrual
	 * @param strCurrency Currency
	 * @param bIsReference TRUE => Corresponds to the Reference Leg
	 * 
	 * @return The Fixed Stream Instance
	 */

	public static final org.drip.product.stream.FloatingStream CreateIBORFloatingStream (
		final org.drip.analytics.date.JulianDate dtEffective,
		final java.lang.String strMaturityTenor,
		final double dblSpread,
		final int iFreq,
		final java.lang.String strDayCount,
		final java.lang.String strCalendar,
		final java.lang.String strCurrency,
		final boolean bIsReference)
	{
		java.util.List<org.drip.analytics.period.CashflowPeriod> lsCouponPeriod =
			org.drip.analytics.period.CashflowPeriod.GeneratePeriodsRegular (dtEffective.julian(),
				strMaturityTenor, null, iFreq, strDayCount, false, true, strCalendar, strCurrency);

		try {
			return new org.drip.product.stream.FloatingStream (strCurrency, null, dblSpread, -1., null,
				lsCouponPeriod, org.drip.state.identifier.ForwardLabel.Create (strCurrency, "LIBOR", (12 /
					iFreq) + "M"), bIsReference);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create a IBOR based Floating Stream instance from effective/maturity dates, coupon, and IR curve name
	 * 
	 * @param dtEffective JulianDate effective
	 * @param dtMaturity Maturity Date
	 * @param dblSpread Spread
	 * @param iFreq Coupon Frequency
	 * @param strDayCount Day Count Convention
	 * @param strCalendar Holiday Calendar for coupon accrual
	 * @param strCurrency Currency
	 * @param bIsReference TRUE => Corresponds to the Reference Leg
	 * 
	 * @return The Fixed Stream Instance
	 */

	public static final org.drip.product.stream.FloatingStream CreateIBORFloatingStream (
		final org.drip.analytics.date.JulianDate dtEffective,
		final org.drip.analytics.date.JulianDate dtMaturity,
		final double dblSpread,
		final int iFreq,
		final java.lang.String strDayCount,
		final java.lang.String strCalendar,
		final java.lang.String strCurrency,
		final boolean bIsReference)
	{
		java.util.List<org.drip.analytics.period.CashflowPeriod> lsCouponPeriod =
			org.drip.analytics.period.CashflowPeriod.GeneratePeriodsBackward (dtEffective.julian(),
				dtMaturity.julian(), null, iFreq, strDayCount, false,
					org.drip.analytics.period.CashflowPeriod.NO_ADJUSTMENT, true, strCalendar, strCurrency);

		try {
			return new org.drip.product.stream.FloatingStream (strCurrency, null, dblSpread, -1., null,
				lsCouponPeriod, org.drip.state.identifier.ForwardLabel.Create (strCurrency, "LIBOR", (12 /
					iFreq) + "M"), bIsReference);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create an IRS product from effective date, tenor, coupon, and IR curve name/rate index
	 * 
	 * @param dtEffective JulianDate effective
	 * @param strMaturityTenor Maturity Tenor
	 * @param dblFixedCoupon Fixed Leg Coupon
	 * @param iFixedFreq Fixed Leg Frequency
	 * @param strFixedDC Fixed Leg Day Count Convention
	 * @param dblFloatSpread Floating Leg Spread
	 * @param iFloatFreq Floating Leg Frequency
	 * @param strFloatDC Floating Leg Day Count Convention
	 * @param strCalendar Optional Holiday Calendar for coupon accrual
	 * @param strCurrency Currency
	 * 
	 * @return IRS product Instance
	 */

	public static final org.drip.product.rates.IRSComponent CreateIRS (
		final org.drip.analytics.date.JulianDate dtEffective,
		final java.lang.String strMaturityTenor,
		final double dblFixedCoupon,
		final int iFixedFreq,
		final java.lang.String strFixedDC,
		final double dblFloatSpread,
		final int iFloatFreq,
		final java.lang.String strFloatDC,
		final java.lang.String strCalendar,
		final java.lang.String strCurrency)
	{
		try {
			org.drip.product.stream.FixedStream fixStream = CreateFixedStream (dtEffective, strMaturityTenor,
				dblFixedCoupon, iFixedFreq, strFixedDC, strCalendar, strCurrency);

			org.drip.product.stream.FloatingStream floatStream = CreateIBORFloatingStream (dtEffective,
				strMaturityTenor, dblFloatSpread, iFloatFreq, strFloatDC, strCalendar, strCurrency, false);

			org.drip.product.rates.IRSComponent irs = new org.drip.product.rates.IRSComponent (fixStream,
				floatStream);

			irs.setPrimaryCode ("IRS." + strMaturityTenor + "." + strCurrency);

			return irs;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create an IRS product from effective date, maturity date, coupon, and IR curve name/rate index
	 * 
	 * @param dtEffective JulianDate effective
	 * @param dtMaturity Maturity Date
	 * @param dblFixedCoupon Fixed Leg Coupon
	 * @param iFixedFreq Fixed Leg Frequency
	 * @param strFixedDC Fixed Leg Day Count Convention
	 * @param dblFloatSpread Floating Leg Spread
	 * @param iFloatFreq Floating Leg Frequency
	 * @param strFloatDC Floating Leg Day Count Convention
	 * @param strCalendar Optional Holiday Calendar for coupon accrual
	 * @param strCurrency Currency
	 * 
	 * @return IRS product Instance
	 */

	public static final org.drip.product.rates.IRSComponent CreateIRS (
		final org.drip.analytics.date.JulianDate dtEffective,
		final org.drip.analytics.date.JulianDate dtMaturity,
		final double dblFixedCoupon,
		final int iFixedFreq,
		final java.lang.String strFixedDC,
		final double dblFloatSpread,
		final int iFloatFreq,
		final java.lang.String strFloatDC,
		final java.lang.String strCalendar,
		final java.lang.String strCurrency)
	{
		try {
			org.drip.product.stream.FixedStream fixStream = CreateFixedStream (dtEffective, dtMaturity,
				dblFixedCoupon, iFixedFreq, strFixedDC, strCalendar, strCurrency);

			org.drip.product.stream.FloatingStream floatStream = CreateIBORFloatingStream (dtEffective,
				dtMaturity, dblFloatSpread, iFloatFreq, strFloatDC, strCalendar, strCurrency, false);

			org.drip.product.rates.IRSComponent irs = new org.drip.product.rates.IRSComponent (fixStream,
				floatStream);

			irs.setPrimaryCode ("IRS." + dtMaturity + "." + strCurrency);

			return irs;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create a Fixed Stream Instance from the byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @return Fixed Stream Instance
	 */

	public static final org.drip.product.definition.RatesComponent FixedStreamFromByteArray (
		final byte[] ab)
	{
		if (null == ab || 0 == ab.length) return null;

		try {
			return new org.drip.product.stream.FixedStream (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create a Floating Stream Instance from the byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @return Floating Stream Instance
	 */

	public static final org.drip.product.definition.RatesComponent FloatingStreamFromByteArray (
		final byte[] ab)
	{
		if (null == ab || 0 == ab.length) return null;

		try {
			return new org.drip.product.stream.FloatingStream (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create a IRS Instance from the byte array
	 * 
	 * @param ab Byte Array
	 * 
	 * @return IRS Instance
	 */

	public static final org.drip.product.definition.RatesComponent IRSFromByteArray (
		final byte[] ab)
	{
		if (null == ab || 0 == ab.length) return null;

		try {
			return new org.drip.product.rates.IRSComponent (ab);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
