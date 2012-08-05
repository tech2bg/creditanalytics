
package org.drip.service.sample;

/*
 * Credit Product imports
 */

import org.drip.analytics.date.JulianDate;
import org.drip.analytics.daycount.Convention;
import org.drip.analytics.definition.*;
import org.drip.analytics.period.*;
import org.drip.analytics.support.*;
import org.drip.param.definition.*;
import org.drip.param.valuation.*;
import org.drip.product.definition.*;

/*
 * Credit Analytics API imports
 */

import org.drip.analytics.creator.*;
import org.drip.param.creator.*;
import org.drip.product.creator.*;
import org.drip.service.api.CreditAnalytics;

public class PriceBND {
	private static final String FIELD_SEPARATOR = "    ";

	public static final void CustomBondAPISample()
		throws Exception
	{
		boolean bPrintCF = false;

		Bond bond = BondBuilder.CreateSimpleFixed (	// Simple Fixed Rate Bond
				"TEST",		// Name
				"USD",			// Currency
				0.0425,			// Bond Coupon
				2,				// Frequency
				"30/360",		// Day Count
				JulianDate.CreateFromYMD (2009, 5, 15), // Effective
				JulianDate.CreateFromYMD (2039, 5, 15),	// Maturity
				null,		// Principal Schedule
				null);

		DiscountCurve dc = DiscountCurveBuilder.CreateFromFlatRate (JulianDate.Today(), "USD", 0.04);

		DiscountCurve dcTSY = DiscountCurveBuilder.CreateFromFlatRate (JulianDate.Today(), "USD", 0.03);

		CreditCurve cc = CreditCurveBuilder.FromFlatHazard (JulianDate.Today().getJulian(), "CC", 0.01, 0.4);

		if (bPrintCF) {
			System.out.println ("\nAcc Start     Acc End     Pay Date      Cpn DCF       Pay01       Surv01");

			System.out.println ("---------    ---------    ---------    ---------    ---------    --------");

			for (Period p : bond.getCouponPeriod())
				System.out.println (
					JulianDate.fromJulian (p.getAccrualStartDate()) + FIELD_SEPARATOR +
					JulianDate.fromJulian (p.getAccrualEndDate()) + FIELD_SEPARATOR +
					JulianDate.fromJulian (p.getPayDate()) + FIELD_SEPARATOR +
					GenericUtil.FormatSpreadSimple (p.getCouponDCF(), 1, 4, 1.) + FIELD_SEPARATOR +
					GenericUtil.FormatSpreadSimple (dc.getDF (p.getPayDate()), 1, 4, 1.) + FIELD_SEPARATOR +
					GenericUtil.FormatSpreadSimple (cc.getSurvival (p.getPayDate()), 1, 4, 1.)
				);
		}

		ComponentMarketParams cmp = ComponentMarketParamsBuilder.CreateComponentMarketParams (
			dc,		// Discount curve
			dcTSY,	// TSY Discount Curve
			dcTSY,	// EDSF Discount Curve (proxied to TSY Discount Curve
			cc,		// Credit Curve
			null,	// TSY quotes
			null,	// Bond market quote
			AnalyticsHelper.CreateFixingsObject (bond, JulianDate.Today(), 0.04)	// Fixings
		);

		double dblPrice = 1.3268;

		JulianDate dtValuation = JulianDate.CreateFromYMD (2012, 6, 25);

		ValuationParams valParams = ValuationParams.CreateValParams (dtValuation, 0, "", Convention.DR_ACTUAL);

		System.out.println ("Valuation Date: " + dtValuation);

		System.out.println ("Yield From Price: " + GenericUtil.FormatPrice (bond.calcYieldFromPrice (valParams, cmp, null, dblPrice), 2, 2, 100.));

		System.out.println ("Mod Dur From Price: " + GenericUtil.FormatPrice (bond.calcDurationFromPrice (valParams, cmp, null, dblPrice), 2, 2, 10000.));

		System.out.println ("Yield 01 From Price: " + GenericUtil.FormatPrice (dblPrice * 0.01 * bond.calcDurationFromPrice (valParams, cmp, null, dblPrice), 1, 3, 10000.));

		System.out.println ("Convexity From Price: " + GenericUtil.FormatPrice (bond.calcConvexityFromPrice (valParams, cmp, null, dblPrice), 2, 2, 100000000.));
	}

	public static final void main (
		final String astrArgs[])
		throws Exception
	{
		// String strConfig = "c:\\Lakshmi\\BondAnal\\Config.xml";

		String strConfig = "";

		CreditAnalytics.Init (strConfig);

		CustomBondAPISample();
	}
}
