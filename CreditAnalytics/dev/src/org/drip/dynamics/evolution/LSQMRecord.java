
package org.drip.dynamics.evolution;

/*
 * -*- mode: java; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 */

/*!
 * Copyright (C) 2015 Lakshmi Krishnamurthy
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
 * LSQMRecord contains the Record of the Evolving Latent State Quantification Metrics.
 *
 * @author Lakshmi Krishnamurthy
 */

public class LSQMRecord {
	private java.util.Map<java.lang.String, java.util.Map<java.lang.String, java.lang.Double>> _mmLSQMValue =
		new java.util.HashMap<java.lang.String, java.util.Map<java.lang.String, java.lang.Double>>();

	/**
	 * Empty LSQMRecord Constructor
	 */

	public LSQMRecord()
	{
	}

	/**
	 * Retrieve the Latent State Labels
	 * 
	 * @return The Latent State Labels
	 */

	public java.util.Set<java.lang.String> latentStateLabel()
	{
		return _mmLSQMValue.keySet();
	}

	/**
	 * Indicate if Quantification Metrics are available for the specified Latent State
	 * 
	 * @param lsl The Latent State Label
	 * 
	 * @return TRUE => Quantification Metrics are available for the specified Latent State
	 */

	public boolean containsLatentState (
		final org.drip.state.identifier.LatentStateLabel lsl)
	{
		return null == lsl ? false : _mmLSQMValue.containsKey (lsl.fullyQualifiedName());
	}

	/**
	 * Set the LSQM Value
	 * 
	 * @param lsl The Latent State Label
	 * @param strLatentState The Quantification Metric
	 * @param dblValue The QM's Value
	 * 
	 * @return TRUE => The QM successfully set
	 */

	public boolean setQM (
		final org.drip.state.identifier.LatentStateLabel lsl,
		final java.lang.String strQM,
		final double dblValue)
	{
		if (null == lsl || null == strQM || strQM.isEmpty() || !org.drip.quant.common.NumberUtil.IsValid
			(dblValue))
			return false;

		java.util.Map<java.lang.String, java.lang.Double> mapLSQM = _mmLSQMValue.containsKey
			(lsl.fullyQualifiedName()) ? _mmLSQMValue.get (lsl.fullyQualifiedName()) : new
				java.util.HashMap<java.lang.String, java.lang.Double>();

		mapLSQM.put (strQM, dblValue);

		_mmLSQMValue.put (lsl.fullyQualifiedName(), mapLSQM);

		return true;
	}

	/**
	 * Indicate if the Value for the specified Quantification Metric is available
	 * 
	 * @param lsl The Latent State Label
	 * @param strQM The Quantification Metric
	 * 
	 * @return TRUE => The Requested Value is available
	 */

	public boolean containsQM (
		final org.drip.state.identifier.LatentStateLabel lsl,
		final java.lang.String strQM)
	{
		return null == lsl || null == strQM || strQM.isEmpty() ? false : _mmLSQMValue.containsKey
			(lsl.fullyQualifiedName()) && _mmLSQMValue.get (lsl.fullyQualifiedName()).containsKey (strQM);
	}

	/**
	 * Retrieve the specified Quantification Metric Value
	 * 
	 * @param lsl The Latent State Label
	 * @param strQM The Quantification Metric
	 * 
	 * @return The Quantification Metric Value
	 * 
	 * @throws java.lang.Exception Thrown if the Quantification Metric is not available
	 */

	public double qm (
		final org.drip.state.identifier.LatentStateLabel lsl,
		final java.lang.String strQM)
		throws java.lang.Exception
	{
		if (null == lsl || null == strQM || strQM.isEmpty() || !_mmLSQMValue.containsKey
			(lsl.fullyQualifiedName()))
			throw new java.lang.Exception ("LSQMRecord::qm => Invalid Inputs");

		java.util.Map<java.lang.String, java.lang.Double> mapLSQM = _mmLSQMValue.get
			(lsl.fullyQualifiedName());

		if (!mapLSQM.containsKey (strQM))
			throw new java.lang.Exception ("LSQMRecord::qm => No LSQM Entry");

		return mapLSQM.get (strQM);
	}
}
