
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
 * ComponentMeasures is the place holder for analytical single component output measures, optionally across
 * 	scenarios. It contains measure maps for the following scenarios:
 * 	- Unadjusted Base IR/credit curves
 *	- Flat delta/gamma bump measure maps for IR/credit bump curves
 *	- Tenor bump double maps for IR/credit curves
 *	- Flat/recovery bumped measure maps for recovery bumped credit curves
 *	- Measure Maps generated for Custom Scenarios
 *	- Accessor Functions for the above fields
 *	- Serialize into and de-serialize out of byte arrays
 *
 * @author Lakshmi Krishnamurthy
 */

public class ComponentMeasures {

	/**
	 * Calculation Time
	 */

	public double _dblCalcTime = java.lang.Double.NaN;

	/**
	 * Map of the base measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mBase = null;

	/**
	 * Map of the parallel RR delta measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mRRDelta = null;

	/**
	 * Map of the parallel RR gamma measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mRRGamma = null;

	/**
	 * Map of the parallel IR delta measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mFlatIRDelta = null;

	/**
	 * Map of the parallel IR gamma measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mFlatIRGamma = null;

	/**
	 * Map of the parallel credit delta measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mFlatCreditDelta = null;

	/**
	 * Map of the parallel credit gamma measures
	 */

	public org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double> _mFlatCreditGamma = null;

	/**
	 * Map of the tenor IR delta measure map
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			_mmTenorIRDelta = null;

	/**
	 * Map of the tenor IR gamma measure map
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			_mmTenorIRGamma = null;

	/**
	 * Map of the tenor credit delta measure map
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			_mmTenorCreditDelta = null;

	/**
	 * Map of the tenor credit gamma measure map
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			_mmTenorCreditGamma = null;

	/**
	 * Map of the custom scenario measure map
	 */

	public
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>>
			_mmCustom = null;

	/**
	 * Empty constructor - all members initialized to NaN or null
	 */

	public ComponentMeasures()
	{
	}
}
