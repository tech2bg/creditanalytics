
package org.drip.state.estimator;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
 * Copyright (C) 2014 Lakshmi Krishnamurthy
 * Copyright (C) 2013 Lakshmi Krishnamurthy
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
 * PredictorResponseRelationSetup holds the Linearized Constraints (and, optionally, their quote
 *  sensitivities) necessary needed for the Linear Calibration. Linearized Constraints are expressed as
 * 
 * 			Sum_i[Predictor Weight_i * Function (Response_i)] = Constraint Value
 * 
 * 	where Function can either be univariate function, or weighted spline basis set.
 * 
 * To this end, it implements the following functionality:
 * 	- Update/Retrieve Predictor/Response Weights and their Quote Sensitivities
 * 	- Update/Retrieve Predictor/Response Constraint Values and their Quote Sensitivities
 * 	- Display the contents of PredictorResponseRelationSetup
 * 
 * @author Lakshmi Krishnamurthy
 */

public class PredictorResponseRelationSetup {
	private double _dblValue = 0.;

	private java.util.TreeMap<java.lang.Double, java.lang.Double> _mapPredictorResponseWeight = new
		java.util.TreeMap<java.lang.Double, java.lang.Double>();

	/**
	 * Empty PredictorResponseRelationSetup constructor
	 */

	public PredictorResponseRelationSetup()
	{
	}

	/**
	 * Update the Constraint Value
	 * 
	 * @param dblValue The Constraint Value Update Increment
	 * 
	 * @return TRUE => This Update Succeeded
	 */

	public boolean updateValue (
		final double dblValue)
	{
		if (!org.drip.quant.common.NumberUtil.IsValid (dblValue)) return false;

		_dblValue += dblValue;
		return true;
	}

	/**
	 * Add a Predictor/Response Weight entry to the Linearized Constraint
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

		double dblResponseWeightPrior = _mapPredictorResponseWeight.containsKey (dblPredictor) ?
			_mapPredictorResponseWeight.get (dblPredictor) : 0.;

		_mapPredictorResponseWeight.put (dblPredictor, dblResponseWeight + dblResponseWeightPrior);

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
	 * Retrieve the Predictor <-> Response Weight Map
	 * 
	 * @return The Predictor <-> Response Weight Map
	 */

	public java.util.TreeMap<java.lang.Double, java.lang.Double> getPredictorResponseWeight()
	{
		return _mapPredictorResponseWeight;
	}

	/**
	 * Absorb the "Other" PRRS onto the current one
	 * 
	 * @param prrsOther The "Other" PRRS
	 * 
	 * @return TRUE => At least one Entry was absorbed
	 */

	public boolean absorb (
		final PredictorResponseRelationSetup prrsOther)
	{
		if (null == prrsOther || !updateValue (prrsOther.getValue())) return false;

		java.util.TreeMap<java.lang.Double, java.lang.Double> mapPRWOther =
			prrsOther.getPredictorResponseWeight();

		if (null == mapPRWOther || 0 == mapPRWOther.size()) return true;

		for (java.util.Map.Entry<java.lang.Double, java.lang.Double> me : mapPRWOther.entrySet()) {
			if (null != me && !addPredictorResponseWeight (me.getKey(), me.getValue())) return false;
		}

		return true;
	}
}
