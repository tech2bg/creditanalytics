
package org.drip.math.grid;

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
 * Span is the interface that exposes the functionality behind the collection of Regimes that may be
 *  overlapping or non-overlapping.
 *
 * @author Lakshmi Krishnamurthy
 */

public interface Span {

	/**
	 * Add a Regime to the Span
	 * 
	 * @param regime Regime to be added
	 * 
	 * @return TRUE => Regime added successfully
	 */

	public abstract boolean addRegime (
		final org.drip.math.regime.MultiSegmentRegime regime);

	/**
	 * Retrieve the first Regime that contains the Predictor Ordinate
	 * 
	 * @param dblPredictorOrdinate The Predictor Ordinate
	 * 
	 * @return The containing Regime
	 */

	public abstract org.drip.math.regime.MultiSegmentRegime getContainingRegime (
		final double dblPredictorOrdinate);

	/**
	 * Retrieve the Regime by Name
	 * 
	 * @param strName The Regime Name
	 * 
	 * @return The Regime
	 */

	public abstract org.drip.math.regime.MultiSegmentRegime getRegime (
		final java.lang.String strName);

	/**
	 * Compute the Response from the containing Regimes
	 * 
	 * @param dblPredictorOrdinate The Predictor Ordinate
	 * 
	 * @return The Response
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public abstract double calcResponseValue (
		final double dblPredictorOrdinate)
		throws java.lang.Exception;

	/**
	 * Retrieve the Left Span Edge
	 * 
	 * @return The Left Span Edge
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public abstract double left() throws java.lang.Exception;

	/**
	 * Retrieve the Right Span Edge
	 * 
	 * @return The Left Span Edge
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public abstract double right() throws java.lang.Exception;
}
