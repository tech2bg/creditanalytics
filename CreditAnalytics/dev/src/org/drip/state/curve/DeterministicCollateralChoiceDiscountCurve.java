
package org.drip.state.curve;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
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
 * DeterministicCollateralChoiceDiscountCurve implements the Dynamically Switchable Collateral Choice
 * 	Discount Curve among the choice of provided "deterministic" collateral curves.
 *
 * @author Lakshmi Krishnamurthy
 */

public class DeterministicCollateralChoiceDiscountCurve extends org.drip.analytics.rates.DiscountCurve {
	private int _iDiscreteCollateralizationIncrement = -1;
	private org.drip.analytics.rates.DiscountCurve _dcDomesticCollateralized = null;
	private org.drip.state.curve.ForeignCollateralizedDiscountCurve[] _aFCDC = null;

	/**
	 * DeterministicCollateralChoiceDiscountCurve constructor
	 * 
	 * @param dcDomesticCollateralized The Domestic Collateralized Curve
	 * @param aFCDC Array of The Foreign Collateralized Curves
	 * @param iDiscreteCollateralizationIncrement The Discrete Collateralization Increment
	 * 
	 * @throws java.lang.Exception
	 */

	public DeterministicCollateralChoiceDiscountCurve (
		final org.drip.analytics.rates.DiscountCurve dcDomesticCollateralized,
		final org.drip.state.curve.ForeignCollateralizedDiscountCurve[] aFCDC,
		final int iDiscreteCollateralizationIncrement)
		throws java.lang.Exception
	{
		super (dcDomesticCollateralized.epoch().julian(), dcDomesticCollateralized.currency(), null,
			null);

		if (0 >= (_iDiscreteCollateralizationIncrement = iDiscreteCollateralizationIncrement))
			throw new java.lang.Exception
				("DeterministicCollateralChoiceDiscountCurve ctr: Invalid Collateralization Increment!");

		_aFCDC = aFCDC;
		_dcDomesticCollateralized = dcDomesticCollateralized;
	}

	@Override public double df (
		final double dblDate)
		throws java.lang.Exception
	{
		if (null == _aFCDC) return _dcDomesticCollateralized.df (dblDate);

		int iNumCollateralizer = _aFCDC.length;

		if (0 == iNumCollateralizer) return _dcDomesticCollateralized.df (dblDate);

		double dblStartDate = _dcDomesticCollateralized.epoch().julian();

		if (dblDate <= dblStartDate) return 1.;

		double dblDF = 1.;
		double dblWorkoutDate = dblStartDate;

		while (java.lang.Math.abs (dblDate - dblWorkoutDate) > _iDiscreteCollateralizationIncrement) {
			double dblWorkoutEndDate = dblWorkoutDate + _iDiscreteCollateralizationIncrement;

			double dblDFIncrement = _dcDomesticCollateralized.df (dblWorkoutEndDate) /
				_dcDomesticCollateralized.df (dblWorkoutDate);

			for (int i = 0; i < iNumCollateralizer; ++i) {
				double dblCollateralizerDFIncrement = _aFCDC[i].df (dblWorkoutEndDate) / _aFCDC[i].df
					(dblWorkoutDate);

				if (dblCollateralizerDFIncrement < dblDFIncrement)
					dblDFIncrement = dblCollateralizerDFIncrement;
			}

			dblDF *= dblDFIncrement;
			dblWorkoutDate = dblWorkoutEndDate;
		}

		if (dblDate > dblWorkoutDate) {
			double dblDFIncrement = _dcDomesticCollateralized.df (dblDate) / _dcDomesticCollateralized.df
				(dblWorkoutDate);

			for (int i = 0; i < iNumCollateralizer; ++i) {
				double dblCollateralizerDFIncrement = _aFCDC[i].df (dblDate) / _aFCDC[i].df (dblWorkoutDate);

				if (dblCollateralizerDFIncrement < dblDFIncrement)
					dblDFIncrement = dblCollateralizerDFIncrement;
			}

			dblDF *= dblDFIncrement;
		}

		return dblDF;
	}

	@Override public double forward (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate1) || !org.drip.quant.common.NumberUtil.IsValid
			(dblDate2))
			throw new java.lang.Exception
				("DeterministicCollateralChoiceDiscountCurve::forward => Invalid input");

		double dblStartDate = epoch().julian();

		if (dblDate1 < dblStartDate || dblDate2 < dblStartDate) return 0.;

		return 365.25 / (dblDate2 - dblDate1) * java.lang.Math.log (df (dblDate1) / df (dblDate2));
	}

	@Override public double zero (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception
				("DeterministicCollateralChoiceDiscountCurve::zero => Invalid Date");

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

	@Override public java.lang.String latentStateQuantificationMetric()
	{
		return null;
	}

	@Override public DiscountFactorDiscountCurve parallelShiftManifestMeasure (
		final java.lang.String strManifestMeasure,
		final double dblShift)
	{
		return null;
	}

	@Override public DiscountFactorDiscountCurve shiftManifestMeasure (
		final int iSpanIndex,
		final java.lang.String strManifestMeasure,
		final double dblShift)
	{
		return null;
	}

	@Override public org.drip.analytics.rates.DiscountCurve customTweakManifestMeasure (
		final java.lang.String strManifestMeasure,
		final org.drip.param.definition.ResponseValueTweakParams rvtp)
	{
		return null;
	}

	@Override public DiscountFactorDiscountCurve parallelShiftQuantificationMetric (
		final double dblShift)
	{
		return null;
	}

	@Override public org.drip.analytics.definition.Curve customTweakQuantificationMetric (
		final org.drip.param.definition.ResponseValueTweakParams rvtp)
	{
		return null;
	}

	@Override public org.drip.quant.calculus.WengertJacobian jackDDFDManifestMeasure (
		final double dblDate,
		final java.lang.String strManifestMeasure)
	{
		return null;
	}

	@Override public boolean setCCIS (
		final org.drip.analytics.input.CurveConstructionInputSet ccis)
	{
		return false;
	}

	@Override public org.drip.product.definition.CalibratableFixedIncomeComponent[] calibComp()
	{
		return null;
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> manifestMeasure (
		final java.lang.String strInstr)
	{
		return null;
	}
}
