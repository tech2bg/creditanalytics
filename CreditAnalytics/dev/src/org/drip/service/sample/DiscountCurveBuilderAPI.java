
package org.drip.service.sample;

public class DiscountCurveBuilderAPI {
	private static final org.drip.product.definition.CalibratableComponent[] CashInstrumentsFromMaturityDays (
		final org.drip.analytics.date.JulianDate dtEffective,
		final int[] aiDay,
		final int iNumFutures)
		throws java.lang.Exception
	{
		org.drip.product.definition.CalibratableComponent[] aCalibComp = new
			org.drip.product.definition.CalibratableComponent[aiDay.length + iNumFutures];

		for (int i = 0; i < aiDay.length; ++i) {
			aCalibComp[i] = org.drip.product.creator.CashBuilder.CreateCash (dtEffective,
				dtEffective.addBusDays (aiDay[i], "USD"), "USD");

			System.out.println ("Cash Mat: " + aCalibComp[i].getMaturityDate());
		}

		org.drip.product.definition.CalibratableComponent[] aEDF =
			org.drip.product.creator.EDFutureBuilder.GenerateEDPack (dtEffective, iNumFutures, "USD");

		for (int i = aiDay.length; i < aiDay.length + iNumFutures; ++i) {
			aCalibComp[i] = aEDF[i - aiDay.length];

			System.out.println ("EDF Mat: " + aCalibComp[i].getMaturityDate());
		}

		return aCalibComp;
	}

	private static final org.drip.product.definition.CalibratableComponent[] SwapInstrumentsFromMaturityTenor (
		final org.drip.analytics.date.JulianDate dtEffective,
		final java.lang.String[] astrTenor)
		throws java.lang.Exception
	{
		org.drip.product.definition.CalibratableComponent[] aCalibComp = new
			org.drip.product.definition.CalibratableComponent[astrTenor.length];

		for (int i = 0; i < astrTenor.length; ++i) {
			aCalibComp[i] = org.drip.product.creator.RatesStreamBuilder.CreateIRS (dtEffective,
				dtEffective.addTenor (astrTenor[i]), 0., "USD", "USD-LIBOR-6M", "USD");

			System.out.println ("Swap Mat: " + aCalibComp[i].getMaturityDate());
		}

		return aCalibComp;
	}

	private static final org.drip.analytics.calibration.LatentStateMetricMeasure[] LSMMFromQuotes (
		final double[] adblQuote)
		throws java.lang.Exception
	{
		org.drip.analytics.calibration.LatentStateMetricMeasure[] aLSMM = new
			org.drip.analytics.calibration.LatentStateMetricMeasure[adblQuote.length];

		for (int i = 0; i < adblQuote.length; ++i)
			aLSMM[i] = new org.drip.analytics.calibration.LatentStateMetricMeasure
				(org.drip.analytics.calibration.LatentStateMetricMeasure.LATENT_STATE_DISCOUNT,
					org.drip.analytics.calibration.LatentStateMetricMeasure.QUANTIFICATION_METRIC_DISCOUNT_FACTOR,
						"Rate", adblQuote[i]);

		return aLSMM;
	}

	public static final void main (
		final java.lang.String[] astrArgs)
		throws java.lang.Exception
	{
		org.drip.service.api.CreditAnalytics.Init ("");

		org.drip.analytics.calibration.LinearCurveCalibrator lcc = new
			org.drip.analytics.calibration.LinearCurveCalibrator (1., 100000000.);

		org.drip.analytics.date.JulianDate dtToday = org.drip.analytics.date.JulianDate.Today();

		org.drip.product.definition.CalibratableComponent[] aCashComp = CashInstrumentsFromMaturityDays
			(dtToday, new int[] {1, 2, 7, 14, 30, 60}, 8);

		org.drip.analytics.calibration.LatentStateMetricMeasure[] aCashLSMM = LSMMFromQuotes (new double[]
			{0.0013, 0.0017, 0.0017, 0.0018, 0.0020, 0.0023, 0.0027, 0.0032, 0.0041, 0.0054, 0.0077,
				0.0104, 0.0134, 0.0160});

		org.drip.math.grid.MultiSegmentSpan span = lcc.spanFromCashInstruments (aCashComp,
			org.drip.param.valuation.ValuationParams.CreateSpotValParams (dtToday.getJulian()), null, null,
				null, aCashLSMM);

		org.drip.product.definition.CalibratableComponent[] aSwapComp = SwapInstrumentsFromMaturityTenor
			(dtToday, new java.lang.String[] {"4Y", "5Y", "6Y", "7Y", "8Y", "9Y", "10Y", "11Y", "12Y", "15Y",
				"20Y", "25Y", "30Y", "40Y", "50Y"});

		org.drip.analytics.calibration.LatentStateMetricMeasure[] aSwapLSMM = LSMMFromQuotes (new double[]
			{0.0166, 0.0206, 0.0241, 0.0269, 0.0292, 0.0311, 0.0326, 0.0340, 0.0351, 0.0375, 0.0393, 0.0402,
				0.0407, 0.0409, 0.0409});

		span = lcc.spanFromSwapInstruments (span, aSwapComp,
			org.drip.param.valuation.ValuationParams.CreateSpotValParams (dtToday.getJulian()), null, null,
				null, aSwapLSMM);

		for (double dblX = span.getLeftEdge(); dblX <= span.getRightEdge(); dblX += 0.05 *
			(span.getRightEdge() - span.getLeftEdge())) {
			try {
				System.out.println ("Discount Factor[" + new org.drip.analytics.date.JulianDate (dblX) +
					"] = " + org.drip.math.common.FormatUtil.FormatDouble (span.calcValue (dblX), 1, 8, 1.) +
						" | " + span.monotoneType (dblX));
			} catch (java.lang.Exception e) {
				e.printStackTrace();
			}
		}
	}
}
