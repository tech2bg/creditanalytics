
package org.drip.state.curve;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * ForeignCollateralizedDiscountCurve computes the discount factor corresponding to one unit of domestic
 * 	currency collateralized by a foreign collateral.
 *
 * @author Lakshmi Krishnamurthy
 */

public class ForeignCollateralizedDiscountCurve extends org.drip.analytics.rates.ExplicitBootDiscountCurve {
	private java.lang.String _strCurrency = null;
	private org.drip.function.deterministic.AbstractUnivariate _auFX = null;
	private org.drip.function.deterministic.AbstractUnivariate _auFXVolSurface = null;
	private org.drip.analytics.rates.DiscountCurve _dcForeignCollateralized = null;
	private org.drip.function.deterministic.AbstractUnivariate _auForeignRatesVolSurface = null;
	private org.drip.function.deterministic.AbstractUnivariate _auFXForeignRatesCorrSurface = null;

	/**
	 * ForeignCollateralizedDiscountCurve constructor
	 * 
	 * @param strCurrency The Currency
	 * @param dcForeignCollateralized The Collateralized Foreign Discount Curve
	 * @param auFX The FX Univariate Function
	 * @param auForeignRatesVolSurface The Foreign Rates Volatility Surface
	 * @param auFXVolSurface The FX Volatility Surface
	 * @param auFXForeignRatesCorrSurface The FX Foreign Rates Correlation Surface
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public ForeignCollateralizedDiscountCurve (
		final java.lang.String strCurrency,
		final org.drip.analytics.rates.DiscountCurve dcForeignCollateralized,
		final org.drip.function.deterministic.AbstractUnivariate auFX,
		final org.drip.function.deterministic.AbstractUnivariate auForeignRatesVolSurface,
		final org.drip.function.deterministic.AbstractUnivariate auFXVolSurface,
		final org.drip.function.deterministic.AbstractUnivariate auFXForeignRatesCorrSurface)
		throws java.lang.Exception
	{
		super (dcForeignCollateralized.epoch().julian(), strCurrency,
			dcForeignCollateralized.collateralParams());

		if (null == (_strCurrency = strCurrency) || _strCurrency.isEmpty() || null ==
			(_auForeignRatesVolSurface = auForeignRatesVolSurface) || null == (_auFXVolSurface =
				auFXVolSurface) || null == (_auFXForeignRatesCorrSurface = auFXForeignRatesCorrSurface) ||
					null == (_dcForeignCollateralized = dcForeignCollateralized) || null == (_auFX = auFX))
			throw new java.lang.Exception ("ForeignCollateralizedDiscountCurve ctr: Invalid Inputs");
	}

	@Override public org.drip.param.valuation.CollateralizationParams collateralParams()
	{
		return _dcForeignCollateralized.collateralParams();
	}

	@Override public double df (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("ForeignCollateralizedDiscountCurve::df => Got NaN for date");

		if (dblDate <= _dblEpochDate) return 1.;

		return _dcForeignCollateralized.df (dblDate) * _auFX.evaluate (dblDate) * java.lang.Math.exp (-1. *
			org.drip.analytics.support.OptionHelper.IntegratedCrossVolQuanto (_auFXVolSurface,
				_auForeignRatesVolSurface, _auFXForeignRatesCorrSurface, _dblEpochDate, dblDate));
	}

	@Override public double forward (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate1) || !org.drip.quant.common.NumberUtil.IsValid
			(dblDate2))
			throw new java.lang.Exception ("ForeignCollateralizedDiscountCurve::forward => Invalid input");

		double dblStartDate = epoch().julian();

		if (dblDate1 < dblStartDate || dblDate2 < dblStartDate) return 0.;

		return 365.25 / (dblDate2 - dblDate1) * java.lang.Math.log (df (dblDate1) / df (dblDate2));
	}

	@Override public double zero (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("ForeignCollateralizedDiscountCurve::zero => Invalid Date");

		double dblStartDate = epoch().julian();

		if (dblDate < dblStartDate) return 0.;

		return -365.25 / (dblDate - dblStartDate) * java.lang.Math.log (df (dblDate));
	}

	@Override public org.drip.analytics.rates.ForwardRateEstimator forwardRateEstimator (
		final double dblDate,
		final org.drip.state.identifier.ForwardLabel fri)
	{
		return null;
	}

	@Override public java.util.Map<java.lang.Double, java.lang.Double> canonicalTruthness (
		final java.lang.String strLatentQuantificationMetric)
	{
		return null;
	}

	@Override public FlatForwardDiscountCurve parallelShiftManifestMeasure (
		final java.lang.String strManifestMeasure,
		final double dblShift)
	{
		return null;
	}

	@Override public FlatForwardDiscountCurve shiftManifestMeasure (
		final int iSpanIndex,
		final java.lang.String strManifestMeasure,
		final double dblShift)
	{
		return null;
	}

	@Override public org.drip.analytics.rates.ExplicitBootDiscountCurve customTweakManifestMeasure (
		final java.lang.String strManifestMeasure,
		final org.drip.param.definition.ResponseValueTweakParams rvtp)
	{
		return null;
	}

	@Override public FlatForwardDiscountCurve parallelShiftQuantificationMetric (
		final double dblShift)
	{
		return null;
	}

	@Override public org.drip.analytics.definition.Curve customTweakQuantificationMetric (
		final org.drip.param.definition.ResponseValueTweakParams rvtp)
	{
		return null;
	}

	@Override public FlatForwardDiscountCurve createBasisRateShiftedCurve (
		final double[] adblDate,
		final double[] adblBasis)
	{
		return null;
	}

	@Override public java.lang.String latentStateQuantificationMetric()
	{
		return org.drip.analytics.definition.LatentStateStatic.DISCOUNT_QM_ZERO_RATE;
	}

	@Override public org.drip.quant.calculus.WengertJacobian jackDDFDManifestMeasure (
		final double dblDate,
		final java.lang.String strManifestMeasure)
	{
		return null;
	}

	@Override public boolean setNodeValue (
		final int iNodeIndex,
		final double dblValue)
	{
		return true;
	}

	@Override public boolean bumpNodeValue (
		final int iNodeIndex,
		final double dblValue)
	{
		return true;
	}

	@Override public boolean setFlatValue (
		final double dblValue)
	{
		return true;
	}
}
