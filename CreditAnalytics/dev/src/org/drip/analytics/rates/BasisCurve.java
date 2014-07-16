
package org.drip.analytics.rates;

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
 * BasisCurve is the stub for the forward curve functionality.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class BasisCurve extends org.drip.service.stream.Serializer implements
	org.drip.analytics.rates.BasisEstimator, org.drip.analytics.definition.Curve {

	/**
	 * Basis Latent State
	 */

	public static final java.lang.String LATENT_STATE_BASIS = "LATENT_STATE_BASIS";

	/**
	 * Basis Latent State Quantification Metric - Discount Factor
	 */

	public static final java.lang.String QUANTIFICATION_METRIC_FORWARD_RATE =
		"QUANTIFICATION_METRIC_FORWARD_RATE";

	private boolean _bBasisOnReference = false;
	private double _dblEpochDate = java.lang.Double.NaN;
	private org.drip.product.params.FloatingRateIndex _friDerived = null;
	private org.drip.product.params.FloatingRateIndex _friReference = null;

	protected BasisCurve (
		final double dblEpochDate,
		final org.drip.product.params.FloatingRateIndex friReference,
		final org.drip.product.params.FloatingRateIndex friDerived,
		final boolean bBasisOnReference)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblEpochDate = dblEpochDate) || null == (_friDerived
			= friDerived) || null == (_friReference = friReference))
			throw new java.lang.Exception ("BasisCurve ctr: Invalid Inputs");

		_bBasisOnReference = bBasisOnReference;
	}

	@Override public java.lang.String name()
	{
		return _bBasisOnReference ? _friReference.fullyQualifiedName() + "::" +
			_friDerived.fullyQualifiedName() : _friDerived.fullyQualifiedName() + "::" +
				_friReference.fullyQualifiedName();
	}

	@Override public java.lang.String currency()
	{
		return _bBasisOnReference ? _friReference.currency() : _friDerived.currency();
	}

	@Override public org.drip.analytics.date.JulianDate epoch()
	{
		try {
			return new org.drip.analytics.date.JulianDate (_dblEpochDate);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override public org.drip.product.params.FloatingRateIndex referenceIndex()
	{
		return _friReference;
	}

	@Override public org.drip.product.params.FloatingRateIndex derivedIndex()
	{
		return _friDerived;
	}

	@Override public double basis (
		final org.drip.analytics.date.JulianDate dt)
		throws java.lang.Exception
	{
		if (null == dt) throw new java.lang.Exception ("BasisCurve::basis got null for date");

		return basis (dt.julian());
	}

	@Override public double basis (
		final java.lang.String strTenor)
		throws java.lang.Exception
	{
		if (null == strTenor || strTenor.isEmpty())
			throw new java.lang.Exception ("BasisCurve::basis got bad tenor");

		return basis (epoch().addTenor (strTenor));
	}

	@Override public boolean setCCIS (
		final org.drip.analytics.definition.CurveConstructionInputSet ccis)
	{
		return true;
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

	@Override public org.drip.state.representation.LatentStateMetricMeasure[] lsmm()
	{
		return null;
	}

	@Override public org.drip.state.representation.LatentState parallelShiftManifestMeasure (
		final java.lang.String strManifestMeasure,
		final double dblShift)
	{
		return null;
	}

	@Override public org.drip.state.representation.LatentState shiftManifestMeasure (
		final int iSpanIndex,
		final java.lang.String strManifestMeasure,
		final double dblShift)
	{
		return null;
	}

	@Override public org.drip.state.representation.LatentState customTweakManifestMeasure (
		final java.lang.String strManifestMeasure,
		final org.drip.param.definition.ResponseValueTweakParams rvtp)
	{
		return null;
	}

	@Override public org.drip.state.representation.LatentState parallelShiftQuantificationMetric (
		final double dblShift)
	{
		return null;
	}

	@Override public org.drip.state.representation.LatentState customTweakQuantificationMetric (
		final org.drip.param.definition.ResponseValueTweakParams rvtp)
	{
		return null;
	}

	/**
	 * Retrieve the Manifest Measure Jacobian of the Forward Rate to the given date
	 * 
	 * @param strManifestMeasure Manifest Measure
	 * @param dblDate Date
	 * 
	 * @return The Manifest Measure Jacobian of the Forward Rate to the given date
	 */

	public abstract org.drip.quant.calculus.WengertJacobian jackDForwardDManifestMeasure (
		final java.lang.String strManifestMeasure,
		final double dblDate);

	/**
	 * Retrieve the Manifest Measure Jacobian of the Forward Rate to the given date
	 * 
	 * @param strManifestMeasure Manifest Measure
	 * @param dt Date
	 * 
	 * @return The Manifest Measure Jacobian of the Forward Rate to the given date
	 */

	public org.drip.quant.calculus.WengertJacobian jackDForwardDManifestMeasure (
		final java.lang.String strManifestMeasure,
		final org.drip.analytics.date.JulianDate dt)
	{
		if (null == dt) return null;

		return jackDForwardDManifestMeasure (strManifestMeasure, dt.julian());
	}

	/**
	 * Retrieve the Manifest Measure Jacobian of the Forward Rate to the date implied by the given Tenor
	 * 
	 * @param strManifestMeasure Manifest Measure
	 * @param strTenor Tenor
	 * 
	 * @return The Manifest Measure Jacobian of the Forward Rate to the date implied by the given Tenor
	 */

	public org.drip.quant.calculus.WengertJacobian jackDForwardDManifestMeasure (
		final java.lang.String strManifestMeasure,
		final java.lang.String strTenor)
	{
		if (null == strTenor || strTenor.isEmpty()) return null;

		try {
			return jackDForwardDManifestMeasure (strManifestMeasure, epoch().addTenor (strTenor));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
