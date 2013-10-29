
package org.drip.spline.params;

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
 * SegmentDesignInelasticControl implements basis per-segment inelastic parameter set. Currently it contains
 *  Ck and roughness penalty derivative order.
 *
 * @author Lakshmi Krishnamurthy
 */

public class SegmentDesignInelasticControl {
	private int _iCk = -1;
	private org.drip.spline.params.SegmentCurvaturePenaltyControl _scpp = null;

	/**
	 * Create the C2 Design Inelastic Params
	 * 
	 * @return SegmentDesignInelasticControl instance
	 */

	public static final SegmentDesignInelasticControl MakeC2DesignInelasticControl()
	{
		try {
			return new SegmentDesignInelasticControl (2, new
				org.drip.spline.params.SegmentCurvaturePenaltyControl (2, 1.));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Create the Design Inelastic Parameters for the desired Ck Criterion and the Roughness Penalty Order
	 * 
	 * @param iCk Continuity Order
	 * @param iRoughnessPenaltyDerivativeOrder Roughness Penalty Derivative Order
	 * 
	 * @return SegmentDesignInelasticControl instance
	 */

	public static final SegmentDesignInelasticControl Create (
		final int iCk,
		final int iRoughnessPenaltyDerivativeOrder)
	{
		try {
			return new SegmentDesignInelasticControl (iCk, new
				org.drip.spline.params.SegmentCurvaturePenaltyControl (iRoughnessPenaltyDerivativeOrder,
					1.));
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	private SegmentDesignInelasticControl (
		final int iCk,
		final org.drip.spline.params.SegmentCurvaturePenaltyControl scpp)
		throws java.lang.Exception
	{
		if (0 > (_iCk = iCk))
			throw new java.lang.Exception ("SegmentDesignInelasticControl ctr: Invalid Inputs");

		_scpp = scpp;
	}

	/**
	 * Retrieve the Continuity Order
	 * 
	 * @return The Continuity Order
	 */

	public int getCk()
	{
		return _iCk;
	}

	/**
	 * Retrieve the Curvature Penalty Parameters
	 * 
	 * @return The Curvature Penalty Parameters
	 */

	public org.drip.spline.params.SegmentCurvaturePenaltyControl getCPP()
	{
		return _scpp;
	}
}
