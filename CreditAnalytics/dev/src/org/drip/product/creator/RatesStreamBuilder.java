
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
	 * @param dtMaturity JulianDate maturity
	 * @param dblCoupon Double coupon
	 * @param strIR IR curve name
	 * @param strCalendar Optional Holiday Calendar for coupon accrual
	 * 
	 * @return The Fixed Stream Instance
	 */

	public static final org.drip.product.rates.FixedStream CreateFixedStream (
		final org.drip.analytics.date.JulianDate dtEffective,
		final org.drip.analytics.date.JulianDate dtMaturity,
		final double dblCoupon,
		final java.lang.String strIR,
		final java.lang.String strCalendar)
	{
		try {
			org.drip.analytics.daycount.DateAdjustParams dap = new
				org.drip.analytics.daycount.DateAdjustParams (org.drip.analytics.daycount.Convention.DR_FOLL,
					strCalendar);

			return new org.drip.product.rates.FixedStream (dtEffective.getJulian(), dtMaturity.getJulian(),
				dblCoupon, 2, "30/360", "30/360", false, null, dap, dap, dap, dap, dap, null, null, 1.,
					strIR, strCalendar);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create a Floating Stream instance from effective/maturity dates, coupon, IR curve name, and floater
	 *  index
	 * 
	 * @param dtEffective JulianDate effective
	 * @param dtMaturity JulianDate maturity
	 * @param dblCoupon Double coupon
	 * @param strIR IR curve name
	 * @param strFloatingRateIndex Floater Index
	 * @param strCalendar Optional Holiday Calendar for coupon accrual
	 * @param bIsReference Flag indicating whether the Stream corresponds to a Reference Leg
	 * 
	 * @return The Floating Stream Instance
	 */

	public static final org.drip.product.rates.FloatingStream CreateFloatingStream (
		final org.drip.analytics.date.JulianDate dtEffective,
		final org.drip.analytics.date.JulianDate dtMaturity,
		final double dblCoupon,
		final java.lang.String strIR,
		final java.lang.String strFloatingRateIndex,
		final java.lang.String strCalendar,
		final boolean bIsReference)
	{
		try {
			org.drip.analytics.daycount.DateAdjustParams dap = new
				org.drip.analytics.daycount.DateAdjustParams (org.drip.analytics.daycount.Convention.DR_FOLL,
					strCalendar);

			return new org.drip.product.rates.FloatingStream (dtEffective.getJulian(),
				dtMaturity.getJulian(), 0., bIsReference, org.drip.product.params.FloatingRateIndex.Create
					(strFloatingRateIndex), 4, "Act/360", "Act/360", false, null, dap, dap, dap, dap, dap,
						dap, null, null, -1., strIR, strCalendar);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create an IRS product from effective/maturity dates, coupon, and IR curve name/rate index
	 * 
	 * @param dtEffective JulianDate effective
	 * @param dtMaturity JulianDate maturity
	 * @param dblCoupon Double coupon
	 * @param strIR IR curve name
	 * @param strFloatingRateIndex Floater Index
	 * @param strCalendar Optional Holiday Calendar for coupon accrual
	 * 
	 * @return IRS product
	 */

	public static final org.drip.product.definition.RatesComponent CreateIRS (
		final org.drip.analytics.date.JulianDate dtEffective,
		final org.drip.analytics.date.JulianDate dtMaturity,
		final double dblCoupon,
		final java.lang.String strIR,
		final java.lang.String strFloatingRateIndex,
		final java.lang.String strCalendar)
	{
		if (null == dtEffective || null == dtMaturity || null == strIR || strIR.isEmpty() ||
			!org.drip.quant.common.NumberUtil.IsValid (dblCoupon)) return null;

		try {
			org.drip.product.rates.FixedStream fixStream = CreateFixedStream (dtEffective, dtMaturity,
				dblCoupon, strIR, strCalendar);

			org.drip.product.rates.FloatingStream floatStream = CreateFloatingStream (dtEffective,
				dtMaturity, dblCoupon, strIR, strFloatingRateIndex, strCalendar, true);

			org.drip.product.rates.IRSComponent irs = new org.drip.product.rates.IRSComponent (fixStream,
				floatStream);

			irs.setPrimaryCode ("IRS." + dtMaturity.toString() + "." + strIR);

			return irs;
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create an IRS product from effective date, tenor, coupon, and IR curve name/rate index
	 * 
	 * @param dtEffective JulianDate effective
	 * @param strTenor String tenor
	 * @param dblCoupon Double coupon
	 * @param strIR IR curve name
	 * @param strFloatingRateIndex Floater Index
	 * @param strCalendar Optional Holiday Calendar for coupon accrual
	 * 
	 * @return IRS product
	 */

	public static final org.drip.product.definition.RatesComponent CreateIRS (
		final org.drip.analytics.date.JulianDate dtEffective,
		final java.lang.String strTenor,
		final double dblCoupon,
		final java.lang.String strIR,
		final java.lang.String strFloatingRateIndex,
		final java.lang.String strCalendar)
	{
		if (null == dtEffective || null == strTenor || strTenor.isEmpty() || null == strIR || strIR.isEmpty()
			|| !org.drip.quant.common.NumberUtil.IsValid (dblCoupon)) return null;

		try {
			org.drip.product.rates.FixedStream fixStream = CreateFixedStream (dtEffective,
				dtEffective.addTenor (strTenor), dblCoupon, strIR, strCalendar);

			org.drip.product.rates.FloatingStream floatStream = CreateFloatingStream (dtEffective,
				dtEffective.addTenor (strTenor), dblCoupon, strIR, strFloatingRateIndex, strCalendar, true);

			org.drip.product.rates.IRSComponent irs = new org.drip.product.rates.IRSComponent (fixStream,
				floatStream);

			irs.setPrimaryCode ("IRS." + strTenor + "." + strIR);

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
			return new org.drip.product.rates.FixedStream (ab);
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
			return new org.drip.product.rates.FloatingStream (ab);
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
