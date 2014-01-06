
package org.drip.state.curve;

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

public class DiscountFactorDiscountCurve extends org.drip.analytics.rates.DiscountCurve {
	private org.drip.spline.grid.Span _span = null;
	private double _dblRightFlatForwardRate = java.lang.Double.NaN;
	private org.drip.analytics.definition.CurveSpanConstructionInput _rcci = null;

	private DiscountFactorDiscountCurve shiftManifestMeasure (
		final double[] adblShiftedManifestMeasure)
	{
		org.drip.state.estimator.StretchRepresentationSpec[] aRBS = _rcci.getSRS();

		org.drip.state.estimator.StretchRepresentationSpec[] aRBSBumped = new
			org.drip.state.estimator.StretchRepresentationSpec[aRBS.length];

		int iRBSIndex = 0;
		int iCalibInstrIndex = 0;

		for (org.drip.state.estimator.StretchRepresentationSpec rbs : aRBS) {
			org.drip.state.representation.LatentStateMetricMeasure[] aLSMM = rbs.getLSMM();

			int iNumLSMM = aLSMM.length;
			double[] adblQuoteBumped = new double[iNumLSMM];
			java.lang.String[] astrManifestMeasure = new java.lang.String[iNumLSMM];

			for (int i = 0; i < iNumLSMM; ++i) {
				astrManifestMeasure[i] = aLSMM[i].getManifestMeasure();

				adblQuoteBumped[i] = adblShiftedManifestMeasure[iCalibInstrIndex++];
			}

			try {
				aRBSBumped[iRBSIndex++] = new org.drip.state.estimator.StretchRepresentationSpec
					(rbs.getName(), aLSMM[0].getID(), aLSMM[0].getQuantificationMetric(), rbs.getCalibComp(),
						astrManifestMeasure, adblQuoteBumped, null);
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		try {
			return new org.drip.state.curve.DiscountFactorDiscountCurve (name(), (_rcci.lcc().calibrateSpan
				(aRBSBumped, 1., _rcci.getValuationParameter(), _rcci.getPricerParameter(),
					_rcci.getQuotingParameter(), _rcci.getCMP())));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

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
		final org.drip.spline.grid.Span span)
		throws java.lang.Exception
	{
		super (span.left(), strCurrency, null);

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
			_dblRightFlatForwardRate * (dblDate - dblStartDate) / 365.25)) * turnAdjust (epoch().getJulian(),
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

		double dblStartDate = epoch().getJulian();

		if (dblDate1 < dblStartDate || dblDate2 < dblStartDate) return 0.;

		return 365.25 / (dblDate2 - dblDate1) * java.lang.Math.log (df (dblDate1) / df (dblDate2));
	}

	@Override public double zero (
		final double dblDate)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblDate))
			throw new java.lang.Exception ("DiscountFactorDiscountCurve::zero => Invalid Date");

		double dblStartDate = epoch().getJulian();

		if (dblDate < dblStartDate) return 0.;

		return -365.25 / (dblDate - dblStartDate) * java.lang.Math.log (df (dblDate));
	}

	@Override public org.drip.analytics.rates.ForwardRateEstimator forwardRateEstimator (
		final double dblDate,
		final org.drip.product.params.FloatingRateIndex fri)
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
		final double dblShift)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblShift)) return null;

		org.drip.product.definition.CalibratableComponent[] aCC = calibComp();

		if (null == aCC) return null;

		int iNumComp = aCC.length;
		double[] adblShiftedManifestMeasure = new double[iNumComp];

		for (int i = 0; i < iNumComp; ++i)
			adblShiftedManifestMeasure[i] += dblShift;

		return shiftManifestMeasure (adblShiftedManifestMeasure);
	}

	@Override public DiscountFactorDiscountCurve shiftManifestMeasure (
		final int iSpanIndex,
		final double dblShift)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblShift)) return null;

		org.drip.product.definition.CalibratableComponent[] aCC = calibComp();

		if (null == aCC) return null;

		int iNumComp = aCC.length;
		double[] adblShiftedManifestMeasure = new double[iNumComp];

		if (iSpanIndex >= iNumComp) return null;

		for (int i = 0; i < iNumComp; ++i)
			adblShiftedManifestMeasure[i] += (i == iSpanIndex ? dblShift : 0.);

		return shiftManifestMeasure (adblShiftedManifestMeasure);
	}

	@Override public org.drip.analytics.rates.DiscountCurve customTweakManifestMeasure (
		final org.drip.param.definition.ResponseValueTweakParams rvtp)
	{
		if (null == rvtp) return null;

		org.drip.product.definition.CalibratableComponent[] aCC = calibComp();

		if (null == aCC) return null;

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapQuote = _rcci.getQuote();

		int iNumComp = aCC.length;
		double[] adblQuote = new double[iNumComp];

		for (int i = 0; i < iNumComp; ++i)
			adblQuote[i] = mapQuote.get (aCC[i].getPrimaryCode());

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

	@Override public org.drip.quant.calculus.WengertJacobian jackDDFDQuote (
		final double dblDate)
	{
		return null == _span ? null : _span.jackDResponseDQuote (dblDate, 1);
	}

	@Override public boolean setCCIS (
		final org.drip.analytics.definition.CurveConstructionInputSet ccis)
	{
		if (null == ccis || !(ccis instanceof org.drip.analytics.definition.CurveSpanConstructionInput))
			return false;

		_rcci = (org.drip.analytics.definition.CurveSpanConstructionInput) ccis;
		return true;
	}

	@Override public org.drip.product.definition.CalibratableComponent[] calibComp()
	{
		return null == _rcci ? null : _rcci.getComponent();
	}

	@Override public org.drip.state.representation.LatentStateMetricMeasure[] lsmm()
	{
		if (null == _rcci) return null;

		java.util.List<org.drip.state.representation.LatentStateMetricMeasure> lsLSMM = new
			java.util.ArrayList<org.drip.state.representation.LatentStateMetricMeasure>();

		org.drip.state.estimator.StretchRepresentationSpec[] aRBS = _rcci.getSRS();

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

	@Override public double manifestMeasure (
		final java.lang.String strInstrumentCode)
		throws java.lang.Exception
	{
		if (null == _rcci)
			throw new java.lang.Exception ("DiscountFactorDiscountCurve::getManifestMeasure => Cannot get " +
				strInstrumentCode);

		org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> mapQuote = _rcci.getQuote();

		if (null == mapQuote || !mapQuote.containsKey (strInstrumentCode))
			throw new java.lang.Exception ("DiscountFactorDiscountCurve::getManifestMeasure => Cannot get " +
				strInstrumentCode);

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
