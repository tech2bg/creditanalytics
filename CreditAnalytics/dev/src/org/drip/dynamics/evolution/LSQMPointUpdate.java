
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
 * LSQMPointUpdate contains the Snapshot and the Increment of the Evolving Point Latent State Quantification
 *  Metrics.
 *
 * @author Lakshmi Krishnamurthy
 */

public class LSQMPointUpdate {
	private double _dblFinalDate = java.lang.Double.NaN;
	private double _dblInitialDate = java.lang.Double.NaN;
	private org.drip.dynamics.evolution.LSQMPointRecord _lprSnapshot = null;
	private org.drip.dynamics.evolution.LSQMPointRecord _lprIncrement = null;

	/**
	 * LSQMPointUpdate Constructor
	 * 
	 * @param dblInitialDate The Initial Date
	 * @param dblFinalDate The Final Date
	 * @param lprSnapshot The LSQM Point Record Snapshot
	 * @param lprIncrement The LSQM Point Record Update
	 * 
	 * @throws java.lang.Exception Thrown if the Inputs are invalid
	 */

	public LSQMPointUpdate (
		final double dblInitialDate,
		final double dblFinalDate,
		final org.drip.dynamics.evolution.LSQMPointRecord lprSnapshot,
		final org.drip.dynamics.evolution.LSQMPointRecord lprIncrement)
		throws java.lang.Exception
	{
		if (null == (_lprSnapshot = lprSnapshot) || !org.drip.quant.common.NumberUtil.IsValid
			(_dblInitialDate = dblInitialDate) || !org.drip.quant.common.NumberUtil.IsValid (_dblFinalDate =
				dblFinalDate) || _dblFinalDate < _dblInitialDate)
			throw new java.lang.Exception ("LSQMPointUpdate ctr: Invalid Inputs");

		_lprIncrement = lprIncrement;
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
	 * Retrieve the LSQM Point Snapshot
	 * 
	 * @return The LSQM Point Snapshot
	 */

	public org.drip.dynamics.evolution.LSQMPointRecord snapshot()
	{
		return _lprSnapshot;
	}

	/**
	 * Retrieve the LSQM Point Increment
	 * 
	 * @return The LSQM Point Increment
	 */

	public org.drip.dynamics.evolution.LSQMPointRecord increment()
	{
		return _lprIncrement;
	}
}
