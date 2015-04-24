
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
 * LSQMCurveUpdate contains the Snapshot and the Increment of the Evolving Curve Latent State Quantification
 *  Metrics.
 *
 * @author Lakshmi Krishnamurthy
 */

public class LSQMCurveUpdate {
	private double _dblFinalDate = java.lang.Double.NaN;
	private double _dblInitialDate = java.lang.Double.NaN;
	private org.drip.dynamics.evolution.LSQMCurveRecord _lcrSnapshot = null;
	private org.drip.dynamics.evolution.LSQMCurveRecord _lcrIncrement = null;

	/**
	 * LSQMCurveUpdate Constructor
	 * 
	 * @param dblInitialDate The Initial Date
	 * @param dblFinalDate The Final Date
	 * @param lcrSnapshot The LSQM Curve Record Snapshot
	 * @param lcrIncrement The LSQM Curve Record Update
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public LSQMCurveUpdate (
		final double dblInitialDate,
		final double dblFinalDate,
		final org.drip.dynamics.evolution.LSQMCurveRecord lcrSnapshot,
		final org.drip.dynamics.evolution.LSQMCurveRecord lcrIncrement)
		throws java.lang.Exception
	{
		if (null == (_lcrSnapshot = lcrSnapshot) || !org.drip.quant.common.NumberUtil.IsValid
			(_dblInitialDate = dblInitialDate) || !org.drip.quant.common.NumberUtil.IsValid (_dblFinalDate =
				dblFinalDate) || _dblFinalDate < _dblInitialDate)
			throw new java.lang.Exception ("LSQMCurveUpdate ctr: Invalid Inputs");

		_lcrIncrement = lcrIncrement;
	}

	/**
	 * Retrieve the Initial Date
	 * 
	 * @return The Initial Date
	 */

	public double initialDate()
	{
		return _dblInitialDate;
	}

	/**
	 * Retrieve the Final Date
	 * 
	 * @return The Final Date
	 */

	public double finalDate()
	{
		return _dblFinalDate;
	}

	/**
	 * Retrieve the LSQM Curve Snapshot
	 * 
	 * @return The LSQM Curve Snapshot
	 */

	public org.drip.dynamics.evolution.LSQMCurveRecord snapshot()
	{
		return _lcrSnapshot;
	}

	/**
	 * Retrieve the LSQM Curve Increment
	 * 
	 * @return The LSQM Curve Increment
	 */

	public org.drip.dynamics.evolution.LSQMCurveRecord increment()
	{
		return _lcrIncrement;
	}
}
