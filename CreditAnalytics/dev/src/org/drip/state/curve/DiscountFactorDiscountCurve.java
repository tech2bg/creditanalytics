
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
 * DiscountFactorDiscountCurve manages the Discounting Latent State, using the Discount Factor as the State
 *  Response Representation. It exports the following functionality:
 *  - Compute the discount factor, forward rate, or the zero rate from the Discount Factor Latent State
 *  - Create a ForwardRateEstimator instance for the given Index
 *  - Retrieve Array of the Calibration Components and their LatentStateMetricMeasure's
 *  - Retrieve the Curve Construction Input Set
 *  - Compute the Jacobian of the Discount Factor Latent State to the input Quote
 *  - Synthesize scenario Latent State by parallel shifting/custom tweaking the quantification metric
 *  - Synthesize scenario Latent State by parallel/custom shifting/custom tweaking the manifest measure
 *  - Serialize into and de-serialize out of byte array
 *
 * @author Lakshmi Krishnamurthy
 */

public class DiscountFactorDiscountCurve extends org.drip.analytics.rates.DiscountCurve {
	private org.drip.spline.grid.Span _span = null;
	private double _dblRightFlatForwardRate = java.lang.Double.NaN;
	private org.drip.analytics.definition.CurveSpanConstructionInput _rcci = null;

	private DiscountFactorDiscountCurve shiftManifestMeasure (
		final double[] adblShiftedManifestMeasure)
	{
		org.drip.state.estimator.StretchRepresentationSpec[] aRBS = _rcci.srs();

		int iRBSIndex = 0;
		int iCalibInstrIndex = 0;
		org.drip.state.estimator.StretchRepresentationSpec[] aRBSBumped = new
			org.drip.state.estimator.StretchRepresentationSpec[aRBS.length];

		for (org.drip.state.estimator.StretchRepresentationSpec rbs : aRBS) {
			org.drip.state.representation.LatentStateMetricMeasure[] aLSMM = rbs.getLSMM();

			int iNumLSMM = aLSMM.length;

			java.util.List<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
				lsMapManifestQuote = new
					java.util.ArrayList<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>();

			for (int i = 0; i < iNumLSMM; ++i) {
				org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapManifestQuote = new
					org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>();

				mapManifestQuote.put (aLSMM[i].manifestMeasures()[0],
					adblShiftedManifestMeasure[iCalibInstrIndex++]);

				lsMapManifestQuote.add (mapManifestQuote);
			}

			try {
				aRBSBumped[iRBSIndex++] = new org.drip.state.estimator.StretchRepresentationSpec
					(rbs.getName(), aLSMM[0].id(), aLSMM[0].quantificationMetric(), rbs.getCalibComp(),
						lsMapManifestQuote, null);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		try {
			return new org.drip.state.curve.DiscountFactorDiscountCurve (label().fullyQualifiedName(),
				collateralParams(), (_rcci.lcc().calibrateSpan (aRBSBumped, 1., _rcci.valuationParameter(),
					_rcci.pricerParameter(), _rcci.quotingParameter(), _rcci.marketParameters())));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * DiscountFactorDiscountCurve constructor
	 * 
	 * @param strCurrency Currency
	 * @param collatParams The Collateralization Parameters
	 * @param span The Span Instance
	 * 
	 * @throws java.lang.Exception
	 */

	public DiscountFactorDiscountCurve (
		final java.lang.String strCurrency,
		final org.drip.param.valuation.CollateralizationParams collatParams,
		final org.drip.spline.grid.Span span)
		throws java.lang.Exception
	{
		super (span.left(), strCurrency, collatParams, null);

		_dblRightFlatForwardRate = -365.25 * java.lang.Math.log ((_span = span).calcResponseValue
			(_span.right())) / (_span.right() - _span.left());
	}

	@Override public double df (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("DiscountFactorDiscountCurve::df => Invalid Inputs");

		double dblStartDate = _span.left();

		if (dblDate <= dblStartDate) return 1.;

		return (dblDate <= _span.right() ? _span.calcResponseValue (dblDate) : java.lang.Math.exp (-1. *
			_dblRightFlatForwardRate * (dblDate - dblStartDate) / 365.25)) * turnAdjust (epoch().julian(),
				dblDate);
	}

	@Override public double forward (
		final double dblDate1,
		final double dblDate2)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate1) || !org.drip.quant.common.NumberUtil.IsValid
			(dblDate2))
			throw new java.lang.Exception ("DiscountFactorDiscountCurve::forward => Invalid input");

		double dblStartDate = epoch().julian();

		if (dblDate1 < dblStartDate || dblDate2 < dblStartDate) return 0.;

		return 365.25 / (dblDate2 - dblDate1) * java.lang.Math.log (df (dblDate1) / df (dblDate2));
	}

	@Override public double zero (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("DiscountFactorDiscountCurve::zero => Invalid Date");

		double dblStartDate = epoch().julian();

		if (dblDate < dblStartDate) return 0.;

		return -365.25 / (dblDate - dblStartDate) * java.lang.Math.log (df (dblDate));
	}

	@Override public org.drip.analytics.rates.ForwardRateEstimator forwardRateEstimator (
		final double dblDate,
		final org.drip.state.identifier.ForwardLabel fri)
	{
		if (null == _span || !_span.isMergeState (dblDate, fri)) return null;

		try {
			return new org.drip.analytics.rates.DiscountForwardEstimator (this, fri);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public java.lang.String latentStateQuantificationMetric()
	{
		return org.drip.analytics.rates.DiscountCurve.QUANTIFICATION_METRIC_DISCOUNT_FACTOR;
	}

	@Override public DiscountFactorDiscountCurve parallelShiftManifestMeasure (
		final java.lang.String strManifestMeasure,
		final double dblShift)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblShift)) return null;

		org.drip.product.definition.CalibratableFixedIncomeComponent[] aCC = calibComp();

		if (null == aCC) return null;

		int iNumComp = aCC.length;
		double[] adblShiftedManifestMeasure = new double[iNumComp];

		for (int i = 0; i < iNumComp; ++i)
			adblShiftedManifestMeasure[i] += dblShift;

		return shiftManifestMeasure (adblShiftedManifestMeasure);
	}

	@Override public DiscountFactorDiscountCurve shiftManifestMeasure (
		final int iSpanIndex,
		final java.lang.String strManifestMeasure,
		final double dblShift)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblShift)) return null;

		org.drip.product.definition.CalibratableFixedIncomeComponent[] aCC = calibComp();

		if (null == aCC) return null;

		int iNumComp = aCC.length;
		double[] adblShiftedManifestMeasure = new double[iNumComp];

		if (iSpanIndex >= iNumComp) return null;

		for (int i = 0; i < iNumComp; ++i)
			adblShiftedManifestMeasure[i] += (i == iSpanIndex ? dblShift : 0.);

		return shiftManifestMeasure (adblShiftedManifestMeasure);
	}

	@Override public org.drip.analytics.rates.DiscountCurve customTweakManifestMeasure (
		final java.lang.String strManifestMeasure,
		final org.drip.param.definition.ResponseValueTweakParams rvtp)
	{
		if (null == rvtp) return null;

		org.drip.product.definition.CalibratableFixedIncomeComponent[] aCC = calibComp();

		if (null == aCC) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			mapQuote = _rcci.quoteMap();

		int iNumComp = aCC.length;
		double[] adblQuote = new double[iNumComp];

		for (int i = 0; i < iNumComp; ++i)
			adblQuote[i] = mapQuote.get (aCC[i].primaryCode()).get (strManifestMeasure);

		double[] adblShiftedManifestMeasure = org.drip.analytics.support.AnalyticsHelper.TweakManifestMeasure
			(adblQuote, rvtp);

		return shiftManifestMeasure (adblShiftedManifestMeasure);
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
		return null == _span ? null : _span.jackDResponseDManifestMeasure (strManifestMeasure, dblDate, 1);
	}

	@Override public boolean setCCIS (
		final org.drip.analytics.definition.CurveConstructionInputSet ccis)
	{
		if (null == ccis || !(ccis instanceof org.drip.analytics.definition.CurveSpanConstructionInput))
			return false;

		return super.setCCIS (_rcci = (org.drip.analytics.definition.CurveSpanConstructionInput) ccis);
	}

	@Override public org.drip.product.definition.CalibratableFixedIncomeComponent[] calibComp()
	{
		return null == _rcci ? null : _rcci.components();
	}

	@Override public org.drip.state.representation.LatentStateMetricMeasure[] lsmm()
	{
		if (null == _rcci) return null;

		java.util.List<org.drip.state.representation.LatentStateMetricMeasure> lsLSMM = new
			java.util.ArrayList<org.drip.state.representation.LatentStateMetricMeasure>();

		org.drip.state.estimator.StretchRepresentationSpec[] aRBS = _rcci.srs();

		for (org.drip.state.estimator.StretchRepresentationSpec rbs : aRBS) {
			org.drip.state.representation.LatentStateMetricMeasure[] aLSMM = rbs.getLSMM();

			int iNumLSMM = aLSMM.length;

			for (int i = 0; i < iNumLSMM; ++i)
				lsLSMM.add (aLSMM[i]);
		}

		int iNumLSMM = lsLSMM.size();

		org.drip.state.representation.LatentStateMetricMeasure[] aLSMM = new
			org.drip.state.representation.LatentStateMetricMeasure[iNumLSMM];

		for (int i = 0; i < iNumLSMM; ++i)
			aLSMM[i] = lsLSMM.get (i);

		return aLSMM;
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> manifestMeasure (
		final java.lang.String strInstrumentCode)
	{
		if (null == _rcci) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			mapQuote = _rcci.quoteMap();

		if (null == mapQuote || !mapQuote.containsKey (strInstrumentCode)) return null;

		return mapQuote.get (strInstrumentCode);
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
