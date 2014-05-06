
package org.drip.analytics.definition;

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
 * TermStructure exposes the stub that implements the latent state's Deterministic Term Structure - by
 * 	construction, this is expected to be non-local.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class TermStructure extends org.drip.service.stream.Serializer implements
	org.drip.analytics.definition.Curve {
	protected java.lang.String _strName = "";
	protected java.lang.String _strCurrency = "";
	protected double _dblEpochDate = java.lang.Double.NaN;

	protected TermStructure (
		final double dblEpochDate,
		final java.lang.String strName,
		final java.lang.String strCurrency)
		throws java.lang.Exception
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (_dblEpochDate = dblEpochDate) || null == (_strName =
			strName) || _strName.isEmpty() || null == (_strCurrency = strCurrency) || _strCurrency.isEmpty())
			throw new java.lang.Exception ("TermStructure ctr: Invalid Inputs");
	}

	@Override public java.lang.String name()
	{
		return _strName;
	}

	@Override public java.lang.String currency()
	{
		return _strCurrency;
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

	@Override public boolean setCCIS (
		final org.drip.analytics.definition.CurveConstructionInputSet ccis)
	{
		return false;
	}

	@Override public org.drip.product.definition.CalibratableFixedIncomeComponent[] calibComp()
	{
		return null;
	}

	@Override public org.drip.state.representation.LatentStateMetricMeasure[] lsmm()
	{
		return null;
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> manifestMeasure (
		final java.lang.String strInstr)
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
	 * Get the Market Node at the given Predictor Ordinate
	 * 
	 * @param dblPredictorOrdinate The Predictor Ordinate
	 * 
	 * @return The Node evaluated from the Term Structure
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public abstract double node (
		final double dblPredictorOrdinate)
		throws java.lang.Exception;

	/**
	 * Get the Market Node Derivative at the given Predictor Ordinate
	 * 
	 * @param dblPredictorOrdinate The Predictor Ordinate
	 * @param iOrder Order of the Derivative
	 * 
	 * @return The Node Derivative evaluated from the Term Structure
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public abstract double nodeDerivative (
		final double dblPredictorOrdinate,
		final int iOrder)
		throws java.lang.Exception;

	/**
	 * Get the Market Node at the given Maturity
	 * 
	 * @param dt The Julian Maturity Date
	 * 
	 * @return The Node evaluated from the Term Structure
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public double node (
		final org.drip.analytics.date.JulianDate dt)
		throws java.lang.Exception
	{
		if (null == dt) throw new java.lang.Exception ("TermStructure::node => Invalid Inputs");

		return node (dt.getJulian());
	}

	/**
	 * Get the Market Node at the given Maturity
	 * 
	 * @param strTenor The Maturity Tenor
	 * 
	 * @return The Node evaluated from the Term Structure
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public double node (
		final java.lang.String strTenor)
		throws java.lang.Exception
	{
		if (null == strTenor || strTenor.isEmpty())
			throw new java.lang.Exception ("TermStructure::node => Invalid Inputs");

		return node (epoch().addTenor (strTenor).getJulian());
	}

	/**
	 * Get the Market Node Derivative at the given Maturity
	 * 
	 * @param dt The Julian Maturity Date
	 * @param iOrder Order of the Derivative
	 * 
	 * @return The Node Derivative evaluated from the Term Structure
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public double nodeDerivative (
		final org.drip.analytics.date.JulianDate dt,
		final int iOrder)
		throws java.lang.Exception
	{
		if (null == dt) throw new java.lang.Exception ("TermStructure::nodeDerivative => Invalid Inputs");

		return nodeDerivative (dt.getJulian(), iOrder);
	}

	/**
	 * Get the Market Node Derivative at the given Maturity
	 * 
	 * @param strTenor The Maturity Tenor
	 * @param iOrder Order of the Derivative
	 * 
	 * @return The Node Derivative evaluated from the Term Structure
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are Invalid
	 */

	public double nodeDerivative (
		final java.lang.String strTenor,
		final int iOrder)
		throws java.lang.Exception
	{
		if (null == strTenor || strTenor.isEmpty())
			throw new java.lang.Exception ("TermStructure::nodeDerivative => Invalid Inputs");

		return nodeDerivative (epoch().addTenor (strTenor).getJulian(), iOrder);
	}
}
