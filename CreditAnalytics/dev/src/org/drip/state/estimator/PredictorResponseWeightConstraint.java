
package org.drip.state.estimator;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
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
 * PredictorResponseWeightConstraint holds the Linearized Constraints (and, optionally, their quote
 *  sensitivities) necessary needed for the Linear Calibration. Linearized Constraints are expressed as
 * 
 * 			Sum_i[Predictor Weight_i * Function (Response_i)] = Constraint Value
 * 
 * 	where Function can either be univariate function, or weighted spline basis set.
 * 
 * To this end, it implements the following functionality:
 * 	- Update/Retrieve Predictor/Response Weights and their Quote Sensitivities
 * 	- Update/Retrieve Predictor/Response Constraint Values and their Quote Sensitivities
 * 	- Display the contents of PredictorResponseWeightConstraint
 * 
 * @author Lakshmi Krishnamurthy
 */

public class PredictorResponseWeightConstraint {
	private java.util.Set<org.drip.state.identifier.LatentStateLabel> _setLSL = null;

	private org.drip.state.estimator.PredictorResponseRelationSetup _prrsCalib = new
		org.drip.state.estimator.PredictorResponseRelationSetup();

	private org.drip.analytics.support.CaseInsensitiveHashMap<org.drip.state.estimator.PredictorResponseRelationSetup>
		_mapPRRSSens = new
			org.drip.analytics.support.CaseInsensitiveHashMap<org.drip.state.estimator.PredictorResponseRelationSetup>();

	private org.drip.state.estimator.PredictorResponseRelationSetup getPRRS (
		final java.lang.String strManifestMeasure)
	{
		if (null == strManifestMeasure || strManifestMeasure.isEmpty()) return null;

		if (!_mapPRRSSens.containsKey (strManifestMeasure))
			_mapPRRSSens.put(strManifestMeasure, new
				org.drip.state.estimator.PredictorResponseRelationSetup());

		return _mapPRRSSens.get (strManifestMeasure);
	}

	/**
	 * Empty PredictorResponseWeightConstraint constructor
	 */

	public PredictorResponseWeightConstraint()
	{
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
		return _prrsCalib.addPredictorResponseWeight (dblPredictor, dblResponseWeight);
	}

	/**
	 * Add a Predictor/Response Weight entry to the Linearized Constraint
	 * 
	 * @param strManifestMeasure The Manifest Measure
	 * @param dblPredictor The Predictor Node
	 * @param dblDResponseWeightDManifestMeasure The Response Weight-to-Manifest Measure Sensitivity at the
	 * 	Node
	 * 
	 * @return TRUE => Successfully added
	 */

	public boolean addDResponseWeightDManifestMeasure (
		final java.lang.String strManifestMeasure,
		final double dblPredictor,
		final double dblDResponseWeightDManifestMeasure)
	{
		return getPRRS (strManifestMeasure).addPredictorResponseWeight (dblPredictor,
			dblDResponseWeightDManifestMeasure);
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
		return _prrsCalib.updateValue (dblValue);
	}

	/**
	 * Update the Constraint Value Sensitivity
	 * 
	 * @param strManifestMeasure The Manifest Measure
	 * @param dblDValueDManifestMeasure The Constraint Value Sensitivity Update Increment
	 * 
	 * @return TRUE => This Sensitivity Update Succeeded
	 */

	public boolean updateDValueDManifestMeasure (
		final java.lang.String strManifestMeasure,
		final double dblDValueDManifestMeasure)
	{
		return getPRRS (strManifestMeasure).updateValue (dblDValueDManifestMeasure);
	}

	/**
	 * Retrieve the Constraint Value
	 * 
	 * @return The Constraint Value
	 */

	public double getValue()
	{
		return _prrsCalib.getValue();
	}

	/**
	 * Retrieve the Constraint Value Sensitivity
	 * 
	 * @param strManifestMeasure The Manifest Measure
	 * 
	 * @return The Constraint Value Sensitivity
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public double getDValueDManifestMeasure (
		final java.lang.String strManifestMeasure)
		throws java.lang.Exception
	{
		if (!_mapPRRSSens.containsKey (strManifestMeasure))
			throw new java.lang.Exception
				("PredictorResponseWeightConstraint::getDValueDManifestMeasure => Cannot locate manifest measure "
					+ strManifestMeasure);

		return _mapPRRSSens.get (strManifestMeasure).getValue();
	}

	/**
	 * Add a Merging Latent State Label
	 * 
	 * @param lslMerge The Merging Latent State Label
	 * 
	 * @return TRUE => The Latent State Label Successfully Added
	 */

	public boolean addMergeLabel (
		final org.drip.state.identifier.LatentStateLabel lslMerge)
	{
		if (null == lslMerge) return false;

		if (null == _setLSL) _setLSL = new java.util.HashSet<org.drip.state.identifier.LatentStateLabel>();

		_setLSL.add (lslMerge);

		return true;
	}

	/**
	 * Return the Set of Merged Latent State Labels
	 * 
	 * @return The Set of Merged Latent State Labels
	 */

	public java.util.Set<org.drip.state.identifier.LatentStateLabel> mergeLabelSet()
	{
		return _setLSL;
	}

	/**
	 * Retrieve the Predictor <-> Response Weight Map
	 * 
	 * @return The Predictor <-> Response Weight Map
	 */

	public java.util.TreeMap<java.lang.Double, java.lang.Double> getPredictorResponseWeight()
	{
		return _prrsCalib.getPredictorResponseWeight();
	}

	/**
	 * Retrieve the Predictor <-> Response Weight Sensitivity Map
	 * 
	 * @param strManifestMeasure The Manifest Measure
	 * 
	 * @return The Predictor <-> Response Weight Sensitivity Map
	 */

	public java.util.TreeMap<java.lang.Double, java.lang.Double> getDResponseWeightDManifestMeasure (
		final java.lang.String strManifestMeasure)
	{
		return !_mapPRRSSens.containsKey (strManifestMeasure) ? null : _mapPRRSSens.get
			(strManifestMeasure).getPredictorResponseWeight();
	}

	/**
	 * "Absorb" the other PRWC Instance into the Current One
	 * 
	 * @param prwcOther The "Other" PRWC Instance
	 * 
	 * @return TRUE => At least one entry of the "Other" was absorbed
	 */

	public boolean absorb (
		final PredictorResponseWeightConstraint prwcOther)
	{
		if (null == prwcOther || !_prrsCalib.absorb (prwcOther._prrsCalib)) return false;

		if (0 == _mapPRRSSens.size() || 0 == prwcOther._mapPRRSSens.size()) return true;

		for (java.util.Map.Entry<java.lang.String, org.drip.state.estimator.PredictorResponseRelationSetup>
			me : _mapPRRSSens.entrySet()) {
			java.lang.String strKey = me.getKey();

			if (prwcOther._mapPRRSSens.containsKey (strKey))
				me.getValue().absorb (prwcOther._mapPRRSSens.get (strKey));
		}

		for (java.util.Map.Entry<java.lang.String, org.drip.state.estimator.PredictorResponseRelationSetup>
			me : prwcOther._mapPRRSSens.entrySet()) {
			java.lang.String strKey = me.getKey();

			if (!_mapPRRSSens.containsKey (strKey)) _mapPRRSSens.put (strKey, me.getValue());
		}

		return true;
	}

	/**
	 * Display the Constraints and the corresponding Weights
	 */

	public void displayString()
	{
		for (java.util.Map.Entry<java.lang.Double, java.lang.Double> me :
			_prrsCalib.getPredictorResponseWeight().entrySet()) {
			try {
				System.out.println ("\t\t" + new org.drip.analytics.date.JulianDate (me.getKey()) + " => " +
					me.getValue());
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
		}

		System.out.println ("\tConstraint: " + _prrsCalib.getValue());
	}
}
