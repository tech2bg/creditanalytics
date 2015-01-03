
package org.drip.analytics.input;

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
 * LatentStateShapePreservingCCIS contains the Parameters needed for the Curve Calibration/Estimation. It
 * 	contains the following:
 *  - Calibration Valuation Parameters
 *  - Calibration Quoting Parameters
 *  - Calibration Market Parameters
 *  - Calibration Pricing Parameters
 *  - Array of Calibration Span Representation
 *  - Map of Calibration Quotes
 *  - Map of Calibration Measures
 *  - Double Map of the Date/Index Fixings
 *  - The Shape Preserving Linear Latent State Calibrator
 *
 * Additional functions provide for retrieval of the above and specific instrument quotes.
 *
 * @author Lakshmi Krishnamurthy
 */

public class LatentStateShapePreservingCCIS implements
	org.drip.analytics.input.CurveConstructionInputSet {
	private org.drip.param.pricer.PricerParams _pricerParam = null;
	private org.drip.param.market.CurveSurfaceQuoteSet _csqs = null;
	private org.drip.param.valuation.ValuationParams _valParam = null;
	private org.drip.param.market.LatentStateFixingsContainer _lsfc = null;
	private org.drip.param.valuation.ValuationCustomizationParams _vcp = null;
	private org.drip.state.inference.LatentStateStretchSpec[] _aStretchSpec = null;
	private org.drip.state.inference.LinearLatentStateCalibrator _llscShapePreserving = null;

	/**
	 * LatentStateShapePreservingCCIS constructor
	 * 
	 * @param llscShapePreserving Shape Preserving LinearLatentStateCalibrator instance
	 * @param aStretchSpec Array of the Latent State Stretch Representation Specifications
	 * @param valParam Valuation Parameters
	 * @param pricerParam Pricer Parameters
	 * @param vcp Valuation Customization Parameters
	 * @param csqs Market Parameters
	 * 
	 * @throws java.lang.Exception Thrown if Inputs are invalid
	 */

	public LatentStateShapePreservingCCIS (
		final org.drip.state.inference.LinearLatentStateCalibrator llscShapePreserving,
		final org.drip.state.inference.LatentStateStretchSpec[] aStretchSpec,
		final org.drip.param.valuation.ValuationParams valParam,
		final org.drip.param.pricer.PricerParams pricerParam,
		final org.drip.param.valuation.ValuationCustomizationParams vcp,
		final org.drip.param.market.CurveSurfaceQuoteSet csqs)
		throws java.lang.Exception
	{
		if (null == (_llscShapePreserving = llscShapePreserving) || null == (_aStretchSpec = aStretchSpec) ||
			0 == _aStretchSpec.length || null == (_valParam = valParam))
			throw new java.lang.Exception ("LatentStateShapePreservingCCIS ctr: Invalid Inputs");

		_vcp = vcp;
		_csqs = csqs;
		_pricerParam = pricerParam;
	}

	@Override public org.drip.param.valuation.ValuationParams valuationParameter()
	{
		return _valParam;
	}

	@Override public org.drip.param.valuation.ValuationCustomizationParams quotingParameter()
	{
		return _vcp;
	}

	@Override public org.drip.product.definition.CalibratableFixedIncomeComponent[] components()
	{
		java.util.List<org.drip.product.definition.CalibratableFixedIncomeComponent> lsComp = new
			java.util.ArrayList<org.drip.product.definition.CalibratableFixedIncomeComponent>();

		for (org.drip.state.inference.LatentStateStretchSpec stretchSpec : _aStretchSpec) {
			if (null == stretchSpec) continue;

			org.drip.state.inference.LatentStateSegmentSpec[] aSegmentSpec = stretchSpec.segmentSpec();

			if (null == aSegmentSpec) continue;

			int iNumComp = aSegmentSpec.length;

			if (0 == iNumComp) continue;

			for (int i = 0; i < iNumComp; ++i) {
				if (null == aSegmentSpec[i]) continue;

				org.drip.product.definition.CalibratableFixedIncomeComponent comp =
					aSegmentSpec[i].component();

				if (null != comp) lsComp.add (comp);
			}
		}

		int iNumComp = lsComp.size();

		if (0 == iNumComp) return null;

		org.drip.product.definition.CalibratableFixedIncomeComponent[] aComp = new
			org.drip.product.definition.CalibratableFixedIncomeComponent[iNumComp];

		for (int i = 0; i < iNumComp; ++i)
			aComp[i] = lsComp.get (i);

		return aComp;
	}

	@Override public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			quoteMap()
	{
		return null;
	}

	@Override public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.String[]> measures()
	{
		return null;
	}

	@Override public org.drip.param.market.LatentStateFixingsContainer fixing()
	{
		return _lsfc;
	}

	/**
	 * Retrieve the Pricer Parameters
	 * 
	 * @return The Pricer Parameters
	 */

	public org.drip.param.pricer.PricerParams pricerParameter()
	{
		return _pricerParam;
	}

	/**
	 * Retrieve the Market Parameters
	 * 
	 * @return The Market Parameters
	 */

	public org.drip.param.market.CurveSurfaceQuoteSet marketParameters()
	{
		return _csqs;
	}

	/**
	 * Retrieve the Array of Latent State Stretch Representation Specifications
	 * 
	 * @return The Array of Latent State Stretch Representation Specifications
	 */

	public org.drip.state.inference.LatentStateStretchSpec[] stretchSpec()
	{
		return _aStretchSpec;
	}

	/**
	 * Retrieve the Shape Preserving Linear Latent State Calibrator
	 * 
	 * @return The Shape Preserving Linear Latent State Calibrator
	 */

	public org.drip.state.estimator.GlobalControlCurveParams shapePreservingLLSC()
	{
		return _llscShapePreserving;
	}
}
