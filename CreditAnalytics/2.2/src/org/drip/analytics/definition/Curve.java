
package org.drip.analytics.definition;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
 * Copyright (C) 2012 Lakshmi Krishnamurthy
 * Copyright (C) 2011 Lakshmi Krishnamurthy
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
 * Curve extends the Latent State to abstract the functionality required among all financial curve. It
 *  exposes the following functionality:
 *  - Set the Epoch and the Identifiers
 *  - Set up/retrieve the Calibration Inputs
 *  - Retrieve the Latent State Metric Measures
 *
 * @author Lakshmi Krishnamurthy
 */

public interface Curve extends org.drip.state.representation.LatentState {

	/**
	 * Get the Curve Name
	 * 
	 * @return The Curve Name
	 */

	public abstract java.lang.String name();

	/**
	 * Get the Epoch Date
	 * 
	 * @return The Epoch Date
	 */

	public abstract org.drip.analytics.date.JulianDate epoch();

	/**
	 * Get the Currency
	 * 
	 * @return Currency
	 */

	public abstract java.lang.String currency();

	/**
	 * Set the Curve Construction Input Set Parameters
	 * 
	 * @param ccis The Curve Construction Input Set Parameters
	 * 
	 * @return TRUE => Inputs successfully Set
	 */

	public boolean setCCIS (
		final org.drip.analytics.definition.CurveConstructionInputSet ccis);

	/**
	 * Retrieve the Calibration Components
	 * 
	 * @return Array of Calibration Components
	 */

	public abstract org.drip.product.definition.CalibratableComponent[] calibComp();

	/**
	 * Retrieve the Manifest Measure of the given Instrument used to construct the Curve
	 * 
	 * @param strInstrumentCode The Calibration Instrument's Code whose Manifest Measure is sought
	 * 
	 * @return The Manifest Measure of the given Instrument used to construct the Curve
	 */

	public abstract double manifestMeasure (
		final java.lang.String strInstrumentCode)
		throws java.lang.Exception;
}
