
package org.drip.param.definition;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
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
 * ScenarioForwardCurve abstract class exposes the interface the constructs scenario Forward curves. The
 *  following curve construction scenarios are supported:
 *  - Base, flat/tenor up/down by arbitrary bumps
 *  - Tenor bumped forward curve set - keyed using the tenor.
 *	- NTP-based custom scenario curves.
 *
 * @author Lakshmi Krishnamurthy
 */

public abstract class ScenarioForwardCurve {

	/**
	 * Forward Curve - Base
	 */

	public static final int FC_BASE = 0;

	/**
	 * Forward Curve - Parallel Bump Up
	 */

	public static final int FC_FLAT_UP = 1;

	/**
	 * Forward Curve - Parallel Bump Down
	 */

	public static final int FC_FLAT_DN = 2;

	/**
	 * Forward Curve - Tenor Bump Up
	 */

	public static final int FC_TENOR_UP = 4;

	/**
	 * Forward Curve Tenor Bump Down
	 */

	public static final int FC_TENOR_DN = 8;

	/**
	 * Generate the set of Forward curves from the scenario specified, and the instrument quotes.
	 * 
	 * @param valParams Valuation Parameters
	 * @param dc Discount Curve
	 * @param adblQuotes Matched array of the calibration instrument quotes
	 * @param dblBump Amount of bump to be applied
	 * @param astrCalibMeasure Matched array of the calibration instrument measures
	 * @param mmFixings Double map of date/rate index and fixings
	 * @param quotingParams Quoting Parameters
	 * @param iFCMode One of the values in the FC_ enum listed above.
	 * 
	 * @return Success (true), failure (false)
	 */

	public abstract boolean cookScenarioDC (
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.analytics.rates.DiscountCurve dc,
		final double[] adblQuotes,
		final double dblBump,
		final java.lang.String[] astrCalibMeasure,
		final java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final int iFCMode);

	/**
	 * Cook a custom Forward curve according to the desired tweak parameters
	 * 
	 * @param strCurveName Scenario Forward Curve Name
	 * @param strCustomName Custom Scenario Name
	 * @param valParams Valuation Parameters
	 * @param dc Discount Curve
	 * @param adblQuotes Double array of input quotes
	 * @param astrCalibMeasure Array of calibration measures
	 * @param mmFixings Date/Index fixings
	 * @param quotingParams Calibration quoting parameters
	 * @param ntpDC Node Tweak Parameters for the Base Discount Curve
	 * @param ntpFC Node Tweak Parameters for the Base Forward Curve
	 * 
	 * @return Creates a custom discount curve
	 */

	public abstract boolean cookCustomDC (
		final java.lang.String strCurveName,
		final java.lang.String strCustomName,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.analytics.rates.DiscountCurve dc,
		final double[] adblQuotes,
		final java.lang.String[] astrCalibMeasure,
		final java.util.Map<org.drip.analytics.date.JulianDate,
			org.drip.analytics.support.CaseInsensitiveTreeMap<java.lang.Double>> mmFixings,
		final org.drip.param.valuation.ValuationCustomizationParams quotingParams,
		final org.drip.param.definition.ResponseValueTweakParams ntpDC,
		final org.drip.param.definition.ResponseValueTweakParams ntpFC);

	/**
	 * Return the Base Forward Curve
	 * 
	 * @return The Base Forward Curve
	 */

	public abstract org.drip.analytics.rates.ForwardCurve getFCBase();

	/**
	 * Return the Bump Up Forward Curve
	 * 
	 * @return The Bump Up Forward Curve
	 */

	public abstract org.drip.analytics.rates.ForwardCurve getFCBumpUp();

	/**
	 * Return the Bump Down Forward Curve
	 * 
	 * @return The Bump Down Forward Curve
	 */

	public abstract org.drip.analytics.rates.ForwardCurve getFCBumpDn();

	/**
	 * Return the map of the tenor Bump Up Forward Curve
	 * 
	 * @return The map of the tenor Bump Up Forward Curve
	 */

	public abstract
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.ForwardCurve>
			getTenorFCBumpUp();

	/**
	 * Return the map of the tenor Bump Down Forward Curve
	 * 
	 * @return The map of the tenor Bump Down Forward Curve
	 */

	public abstract
		org.drip.analytics.support.CaseInsensitiveTreeMap<org.drip.analytics.rates.ForwardCurve>
			getTenorFCBumpDn();
}
