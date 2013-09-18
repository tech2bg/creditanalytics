
package org.drip.state.manager;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * 
 * This file is part of CreditAnalytics, a free-software/open-source library for fixed income analysts and
 * 		developers - http://www.credit-trader.org
 * 
 * CreditAnalytics is a free, full featured, fixed income credit analytics library, developed with a special
 * 		focus towards the needs of the bonds and credit products community.
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
 * DiscountFactorDiscountCurve manages the Discounting Latent State, using the Discount Factor as the State
 *  Response Representation. It exports the following functionality:
 *  - Calculate discount factor / discount factor Jacobian
 *  - Calculate implied forward rate / implied forward rate Jacobian
 *  - Construct tweaked curve instances (parallel/tenor/custom tweaks)
 *  - Optionally provide the calibration instruments and quotes used to build the curve.
 *
 * @author Lakshmi Krishnamurthy
 */

public class DiscountFactorDiscountCurve extends org.drip.analytics.definition.DiscountCurve {
	private static final int NUM_DF_QUADRATURES = 5;

	private java.lang.String _strCurrency = "";
	private org.drip.math.grid.Span _span = null;

	/**
	 * DiscountFactorDiscountCurve constructor
	 * 
	 * @param strCurrency Currency
	 * @param span The Span Instance
	 * 
	 * @throws java.lang.Exception
	 */

	public DiscountFactorDiscountCurve (
		final java.lang.String strCurrency,
		final org.drip.math.grid.Span span)
		throws java.lang.Exception
	{
		if (null == (_strCurrency = strCurrency) || _strCurrency.isEmpty() || null == (_span = span))
			throw new java.lang.Exception ("DiscountFactorDiscountCurve ctr: Invalid Inputs");
	}

	@Override public boolean initializeCalibrationRun (
		final double dblLeftSlope)
	{
		return false;
	}

	@Override public java.util.Map<org.drip.analytics.date.JulianDate,
		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			getCalibFixings()
	{
		return null;
	}

	@Override public void setInstrCalibInputs (
		final org.drip.param.valuation.ValuationParams valParam,
		final org.drip.product.definition.CalibratableComponent[] aCalibInst,
		final double[] adblCalibQuote,
		final java.lang.String[] astrCalibMeasure, final java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixing,
		final org.drip.param.valuation.QuotingParams quotingParams)
	{
	}

	@Override public org.drip.product.definition.CalibratableComponent[] getCalibComponents()
	{
		return null;
	}

	@Override public double[] getCompQuotes()
	{
		return null;
	}

	@Override public double getQuote (
		final java.lang.String strInstr)
		throws java.lang.Exception
	{
		throw new java.lang.Exception ("DiscountFactorDiscountCurve::getQuote => Cannot get quotes for " +
			strInstr);
	}

	@Override public DiscountFactorDiscountCurve createParallelShiftedCurve (
		final double dblShift)
	{
		return null;
	}

	@Override public DiscountFactorDiscountCurve createParallelRateShiftedCurve (
		final double dblShift)
	{
		return null;
	}

	@Override public DiscountFactorDiscountCurve createBasisRateShiftedCurve (
		final double[] adblDate,
		final double[] adblBasis)
	{
		return null;
	}

	@Override public org.drip.analytics.definition.DiscountCurve createTweakedCurve (
		final org.drip.param.definition.NodeTweakParams ntp)
	{
		return null;
	}

	@Override public java.lang.String getName()
	{
		return _strCurrency;
	}

	@Override public java.lang.String getCurrency()
	{
		return _strCurrency;
	}

	@Override public double getDF (
		final double dblDate)
		throws java.lang.Exception
	{
		return _span.calcResponseValue (dblDate);
	}

	@Override public org.drip.math.calculus.WengertJacobian getDFJacobian (
		final double dblDate)
	{
		return null;
	}

	@Override public double getEffectiveDF (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		if (dblDate1 == dblDate2) return getDF (dblDate1);

		int iNumQuadratures = 0;
		double dblEffectiveDF = 0.;
		double dblQuadratureWidth = (dblDate2 - dblDate1) / NUM_DF_QUADRATURES;

		for (double dblDate = dblDate1; dblDate <= dblDate2; dblDate += dblQuadratureWidth) {
			++iNumQuadratures;

			dblEffectiveDF += (getDF (dblDate) + getDF (dblDate + dblQuadratureWidth));
		}

		return dblEffectiveDF / (2. * iNumQuadratures);
	}

	@Override public double getEffectiveDF (
		final org.drip.analytics.date.JulianDate dt1,
		final org.drip.analytics.date.JulianDate dt2)
		throws java.lang.Exception
	{
		if (null == dt1 || null == dt2)
			throw new java.lang.Exception ("DiscountFactorDiscountCurve::getEffectiveDF => Got null for date");

		return getEffectiveDF (dt1.getJulian(), dt2.getJulian());
	}

	@Override public double getEffectiveDF (
		final java.lang.String strTenor1,
		final java.lang.String strTenor2)
		throws java.lang.Exception
	{
		if (null == strTenor1 || strTenor1.isEmpty() || null == strTenor2 || strTenor2.isEmpty())
			throw new java.lang.Exception ("DiscountFactorDiscountCurve::getEffectiveDF => Got bad tenor");

		return getEffectiveDF (new org.drip.analytics.date.JulianDate (_span.left()).addTenor (strTenor1),
			new org.drip.analytics.date.JulianDate (_span.left()).addTenor (strTenor2));
	}

	@Override public boolean setNodeValue (
		final int iNodeIndex,
		final double dblValue)
	{
		return false;
	}

	@Override public boolean bumpNodeValue (
		final int iNodeIndex,
		final double dblValue)
	{
		return false;
	}

	@Override public boolean setFlatValue (
		final double dblValue)
	{
		return false;
	}

	@Override public double calcImpliedRate (
		final double dblDt1,
		final double dblDt2)
		throws java.lang.Exception
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblDt1) || !org.drip.math.common.NumberUtil.IsValid
			(dblDt2))
			throw new java.lang.Exception ("DiscountFactorDiscountCurve::calcImpliedRate => Invalid input");

		if (dblDt1 < _span.left() || dblDt2 < _span.left()) return 0.;

		return 365.25 / (dblDt2 - dblDt1) * java.lang.Math.log (getDF (dblDt1) / getDF (dblDt2));
	}

	@Override public double calcImpliedRate (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.math.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("DiscountFactorDiscountCurve::calcImpliedRate => Invalid Date");

		return calcImpliedRate (_span.left(), dblDate);
	}

	@Override public double calcImpliedRate (
		final java.lang.String strTenor)
		throws java.lang.Exception
	{
		if (null == strTenor || strTenor.isEmpty())
			throw new java.lang.Exception ("DiscountFactorDiscountCurve::calcImpliedRate => Invalid date");

		return calcImpliedRate (_span.left(), new org.drip.analytics.date.JulianDate (_span.left()).addTenor
			(strTenor).getJulian());
	}

	@Override public double calcImpliedRate (
		final java.lang.String strTenor1,
		final java.lang.String strTenor2)
		throws java.lang.Exception
	{
		if (null == strTenor1 || strTenor1.isEmpty() || null == strTenor2 || strTenor2.isEmpty())
			throw new java.lang.Exception ("DiscountFactorDiscountCurve::calcImpliedRate => Invalid Date");

		org.drip.analytics.date.JulianDate dtStart = new org.drip.analytics.date.JulianDate (_span.left());

		return calcImpliedRate (dtStart.addTenor (strTenor1).getJulian(), dtStart.addTenor
			(strTenor2).getJulian());
	}

	@Override public org.drip.analytics.date.JulianDate getStartDate()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_span.left());
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public byte[] serialize()
	{
		return null;
	}

	@Override public org.drip.service.stream.Serializer deserialize (
		final byte[] ab)
	{
		return null;
	}
}
