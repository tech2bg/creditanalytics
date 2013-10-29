
package org.drip.state.estimator;

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
 * PredictorResponseRelationConstraint holds the Linearized Constraints necessary needed for the Linear
 * 	Calibration. Linearized Constraints are expressed as
 * 
 * 			Sum_i[Predictor Weight_i * Function (Response_i)] = Constraint Value
 * 
 * 	where Function can either be univariate function, or weighted spline basis set.
 * 
 * @author Lakshmi Krishnamurthy
 */

public class PredictorResponseLinearConstraint {
	private double _dblValue = 0.;
	private java.util.TreeMap<java.lang.Double, java.lang.Double> _mapResponsePredictorWeight = null;

	/**
	 * Empty PredictorResponseRelationConstraint constructor
	 */

	public PredictorResponseLinearConstraint()
	{
	}

	/**
	 * Adds a Predictor/Response Weight entry to the Linearized Constraint
	 * 
	 * @param dblPredictor The Predictor Node
	 * @param dblResponseWeight The Response Weight at the Node
	 * 
	 * @return TRUE => Successfully added
	 */

	public boolean addPredictorResponseWeight (
		final double dblPredictor,
		final double dblResponseWeight)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblPredictor) ||
			!org.drip.quant.common.NumberUtil.IsValid (dblResponseWeight))
			return false;

		if (null == _mapResponsePredictorWeight)
			_mapResponsePredictorWeight = new java.util.TreeMap<java.lang.Double, java.lang.Double>();

		if (!_mapResponsePredictorWeight.containsKey (dblPredictor))
			_mapResponsePredictorWeight.put (dblPredictor, dblResponseWeight);
		else
			_mapResponsePredictorWeight.put (dblPredictor, dblResponseWeight +
				_mapResponsePredictorWeight.get (dblPredictor));

		return true;
	}

	/**
	 * Set the Constraint Value
	 * 
	 * @param dblValue The Constraint Value
	 * 
	 * @return The Constraint Value
	 */

	public boolean setValue (
		final double dblValue)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblValue)) return false;

		_dblValue = dblValue;
		return true;
	}

	/**
	 * Retrieve the Constraint Value
	 * 
	 * @return The Constraint Value
	 */

	public double getValue()
	{
		return _dblValue;
	}

	/**
	 * Retrieve the Response <-> Predictor Weight Map
	 * 
	 * @return The Response <-> Predictor Weight Map
	 */

	public java.util.TreeMap<java.lang.Double, java.lang.Double> getResponsePredictorWeight()
	{
		return _mapResponsePredictorWeight;
	}

	/**
	 * Display to Constraints and the corresponding Weights
	 */

	public void displayString()
	{
		for (java.util.Map.Entry<java.lang.Double, java.lang.Double> me :
			_mapResponsePredictorWeight.entrySet()) {
			try {
				System.out.println ("\t\t" + new org.drip.analytics.date.JulianDate (me.getKey()) + " => " +
					me.getValue());
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
		}

		System.out.println ("\tConstraint: " + _dblValue);
	}
}
