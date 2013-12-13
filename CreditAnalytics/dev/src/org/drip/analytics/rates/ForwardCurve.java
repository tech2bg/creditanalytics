
package org.drip.analytics.rates;

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
 * ForwardCurve is the stub for the forward curve functionality. It extends the Curve object by exposing the
 * 	following functions:
 * 	- The tenor associated with the forward rate instance.
 *  - Forward Rate to a specific date/tenor.
 *  - Generate scenario curves from the base forward curve (flat/parallel/custom)
 *  - Value Jacobian.
 *  - Cross Jacobian.
 *  - Quote Jacobian.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class ForwardCurve extends org.drip.service.stream.Serializer implements
	org.drip.analytics.rates.ForwardRateEstimator, org.drip.analytics.definition.Curve {
	private double _dblEpochDate = java.lang.Double.NaN;
	private org.drip.product.params.FloatingRateIndex _fri = null;

	protected ForwardCurve (
		final double dblEpochDate,
		final org.drip.product.params.FloatingRateIndex fri)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblEpochDate = dblEpochDate) || null == (_fri = fri))
			throw new java.lang.Exception ("ForwardCurve ctr: Invalid Inputs");
	}

	@Override public java.lang.String name()
	{
		return _fri.fullyQualifiedName();
	}

	@Override public java.lang.String currency()
	{
		return _fri.currency();
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

	@Override public java.lang.String tenor()
	{
		return _fri.tenor();
	}

	/**
	 * Retrieve the Forward Rate Index
	 * 
	 * @return The Forward Rate Index
	 */

	@Override public org.drip.product.params.FloatingRateIndex index()
	{
		return _fri;
	}

	@Override public double forward (
		final org.drip.analytics.date.JulianDate dt)
		throws java.lang.Exception
	{
		if (null == dt) throw new java.lang.Exception ("ForwardRate::forward got null for date");

		return forward (dt.getJulian());
	}

	/**
	 * Calculate the Forward Rate to the tenor implied by the given date
	 * 
	 * @param strTenor The Tenor
	 * 
	 * @return The Forward Rate
	 * 
	 * @throws java.lang.Exception Thrown if the Forward Rate cannot be calculated
	 */

	@Override public double forward (
		final java.lang.String strTenor)
		throws java.lang.Exception
	{
		if (null == strTenor || strTenor.isEmpty())
			throw new java.lang.Exception ("ForwardRate::forward got bad tenor");

		return forward (epoch().addTenor (strTenor));
	}

	@Override public boolean setCCIS (
		final org.drip.analytics.definition.CurveConstructionInputSet ccis)
	{
		return true;
	}

	@Override public org.drip.product.definition.CalibratableComponent[] calibComp()
	{
		return null;
	}

	@Override public double manifestMeasure (
		final java.lang.String strInstrumentCode)
		throws java.lang.Exception
	{
		return java.lang.Double.NaN;
	}

	@Override public org.drip.state.representation.LatentStateMetricMeasure[] lsmm()
	{
		return null;
	}

	@Override public org.drip.state.representation.LatentState parallelShiftManifestMeasure (
		final double dblShift)
	{
		return null;
	}

	@Override public org.drip.state.representation.LatentState shiftManifestMeasure (
		final int iSpanIndex,
		final double dblShift)
	{
		return null;
	}

	@Override public org.drip.state.representation.LatentState customTweakManifestMeasure (
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
}
