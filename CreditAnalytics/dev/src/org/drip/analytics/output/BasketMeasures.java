
package org.drip.analytics.output;

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
 * BasketMeasures is the place holder for the analytical basket measures, optionally across scenarios. It
 * 	contains the following scenario measure maps:
 * 	- Unadjusted Base Measures
 *	- Flat delta/gamma bump measure maps for IR/credit/RR bump curves
 *	- Component/tenor bump double maps for IR/credit/RR curves
 *	- Flat/component recovery bumped measure maps for recovery bumped credit curves
 *	- Custom scenario measure map
 * 
 * @author Lakshmi Krishnamurthy
 */

public class BasketMeasures {

	/**
	 * Basket output calculation time
	 */

	public double _dblCalcTime = java.lang.Double.NaN;

	/**
	 * Map of the base measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mBase = null;

	/**
	 * Map of the parallel IR delta measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mFlatIRDelta = null;

	/**
	 * Map of the parallel IR gamma measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mFlatIRGamma = null;

	/**
	 * Map of the parallel RR delta measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mFlatRRDelta = null;

	/**
	 * Map of the parallel RR gamma measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mFlatRRGamma = null;

	/**
	 * Map of the parallel credit delta measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mFlatCreditDelta = null;

	/**
	 * Map of the parallel credit gamma measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mFlatCreditGamma = null;

	/**
	 * Map of the component IR delta measure map
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			_mmIRDelta = null;

	/**
	 * Map of the component IR gamma measure map
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			_mmIRGamma = null;

	/**
	 * Map of the component credit delta measure map
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			_mmCreditDelta = null;

	/**
	 * Map of the component credit gamma measure map
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			_mmCreditGamma = null;

	/**
	 * Map of the component RR delta measure map
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			_mmRRDelta = null;

	/**
	 * Map of the component RR gamma measure map
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			_mmRRGamma = null;

	/**
	 * Triple Map of the component, IR tenor, measure, and delta value
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>>
			_mmmIRTenorDelta = null;

	/**
	 * Triple Map of the component, IR tenor, measure, and gamma value
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>>
			_mmmIRTenorGamma = null;

	/**
	 * Triple Map of the component, credit tenor, measure, and delta value
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>>
			_mmmCreditTenorDelta = null;

	/**
	 * Triple Map of the component, credit tenor, measure, and gamma value
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>>
			_mmmCreditTenorGamma = null;

	/**
	 * Map of the custom scenario measure map
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			_mmCustom = null;

	/**
	 * Empty constructor - all members initialized to NaN or null
	 */

	public BasketMeasures()
	{
	}
}
