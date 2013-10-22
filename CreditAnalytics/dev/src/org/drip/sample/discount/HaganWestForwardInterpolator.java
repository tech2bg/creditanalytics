
package org.drip.sample.discount;

import org.drip.math.common.FormatUtil;
import org.drip.math.pchip.MonotoneConvexHaganWest;

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
 * This sample illustrates using the Hagan and West (2006) Estimator. It provides the following
 *  functionality:
 * 	- Ensure that the estimated regime is monotone an convex.
 * 	- If need be, enforce positivity.
 * 	- Apply segment-by-segment range bounds as needed.
 *
 * @author Lakshmi Krishnamurthy
 */

public class HaganWestForwardInterpolator {
	private static void DisplayOP (
		final MonotoneConvexHaganWest mchw,
		final double[] adblTime)
		throws Exception
	{
		double dblTimeBegin = 0.;
		double dblTimeFinish = 3.;
		double dblTimeDelta = 0.25;
		double dblTime = dblTimeBegin;

		while (dblTime <= dblTimeFinish) {
			System.out.println ("\t\tResponse[" +
				FormatUtil.FormatDouble (dblTime, 1, 2, 1.) + "]: " +
				FormatUtil.FormatDouble (mchw.responseValue (dblTime), 1, 6, 1.));

			dblTime += dblTimeDelta;
		}

		System.out.println ("\t----------------------------------------------------------------");

		System.out.println ("\t\tPositivity enforced? " + mchw.enforcePositivity());

		System.out.println ("\t----------------------------------------------------------------");

		dblTime = dblTimeBegin;

		while (dblTime <= dblTimeFinish) {
			System.out.println ("\t\tPositivity Enforced Response[" +
				FormatUtil.FormatDouble (dblTime, 1, 2, 1.) + "]: " +
				FormatUtil.FormatDouble (mchw.responseValue (dblTime), 1, 6, 1.));

			dblTime += dblTimeDelta;
		}
	}

	public static final void main (
		final String[] astrArgs)
		throws Exception
	{
		double[] adblTime = new double[] {0., 0.5, 1.0, 1.5, 2.0, 2.5, 3.0};
		double[] adblForwardRate = new double[] {0.02, 0.027, 0.034, 0.009, 0.0002, 0.044};

		System.out.println ("\n\t----------------------------------------------------------------");

		System.out.println ("\t     MONOTONE CONVEX HAGAN WEST WITH LINEAR FORWARD STATE");

		System.out.println ("\t----------------------------------------------------------------");

		DisplayOP (MonotoneConvexHaganWest.Create (adblTime, adblForwardRate, false), adblTime);

		System.out.println ("\n\n\t----------------------------------------------------------------");

		System.out.println ("\t     MONOTONE CONVEX HAGAN WEST WITH HARMONIC FORWARD STATE");

		System.out.println ("\t----------------------------------------------------------------");

		DisplayOP (MonotoneConvexHaganWest.Create (adblTime, adblForwardRate, true), adblTime);
	}
}
