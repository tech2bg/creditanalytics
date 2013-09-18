
package org.drip.state.estimator;

public class LinearCurveCalibrator {
	private org.drip.math.regime.RegimeCalibrationSetting _rcs = null;
	private org.drip.math.segment.PredictorResponseBuilderParams _sbpRegular = null;

	private static final org.drip.math.segment.ResponseValueConstraint GenerateSegmentConstraint (
		final org.drip.state.estimator.PredictorResponseLinearConstraint prlc,
		final org.drip.math.regime.MultiSegmentRegime regimeCurrent,
		final org.drip.math.regime.MultiSegmentRegime regimePrev)
	{
		java.util.TreeMap<java.lang.Double, java.lang.Double> mapResponsePredictorWeight =
			prlc.getResponsePredictorWeight();

		if (null == mapResponsePredictorWeight || 0 == mapResponsePredictorWeight.size()) return null;

		java.util.Set<java.util.Map.Entry<java.lang.Double, java.lang.Double>> setRP =
			mapResponsePredictorWeight.entrySet();

		if (null == setRP) return null;

		double dblValue = 0.;

		java.util.List<java.lang.Double> lsPredictor = new java.util.ArrayList<java.lang.Double>();

		java.util.List<java.lang.Double> lsResponseWeight = new java.util.ArrayList<java.lang.Double>();

		for (java.util.Map.Entry<java.lang.Double, java.lang.Double> me : setRP) {
			if (null == me) return null;

			double dblDate = me.getKey();

			try {
				if (null != regimePrev && regimePrev.in (dblDate)) {
					try {
						dblValue -= regimePrev.response (dblDate) * me.getValue();
					} catch (java.lang.Exception e) {
						e.printStackTrace();

						return null;
					}
				} else if (null != regimeCurrent && regimeCurrent.in (dblDate)) {
					try {
						dblValue -= regimeCurrent.response (dblDate) * me.getValue();
					} catch (java.lang.Exception e) {
						e.printStackTrace();

						return null;
					}
				} else {
					lsPredictor.add (dblDate);

					lsResponseWeight.add (me.getValue());
				}
			} catch (java.lang.Exception e) {
				e.printStackTrace();

				return null;
			}
		}

		int iSize = lsPredictor.size();

		double[] adblPredictor = new double[iSize];
		double[] adblResponseWeight = new double[iSize];

		for (int i = 0; i < iSize; ++i) {
			adblPredictor[i] = lsPredictor.get (i);

			adblResponseWeight[i] = lsResponseWeight.get (i);
		}

		try {
			return new org.drip.math.segment.ResponseValueConstraint (adblPredictor, adblResponseWeight,
				prlc.getValue() + dblValue);
		} catch (java.lang.Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	public LinearCurveCalibrator (
		final double dblTensionRegular,
		final org.drip.math.regime.RegimeCalibrationSetting rcs)
		throws java.lang.Exception
	{
		if (null == (_rcs = rcs))
			throw new java.lang.Exception ("LinearCurveCalibrator ctr: Invalid Inputs");

		_sbpRegular = new org.drip.math.segment.PredictorResponseBuilderParams
			(org.drip.math.regime.RegimeBuilder.BASIS_SPLINE_POLYNOMIAL, new
				org.drip.math.spline.PolynomialBasisSetParams (4), new
					org.drip.math.segment.DesignInelasticParams (2, 2), new
						org.drip.math.segment.ResponseScalingShapeController (true, new
							org.drip.math.function.RationalShapeControl (0.)));
	}

	public org.drip.math.grid.OverlappingRegimeSpan calibrateSpan (
		final org.drip.state.estimator.RegimeBuilderSet[] aRBS,
		final org.drip.param.valuation.ValuationParams valParams,
		final org.drip.param.pricer.PricerParams pricerParams,
		final org.drip.param.definition.ComponentMarketParams cmp,
		final org.drip.param.valuation.QuotingParams quotingParams)
	{
		if (null == aRBS || null == valParams) return null;

		int iNumRegime = aRBS.length;
		org.drip.math.grid.OverlappingRegimeSpan span = null;
		org.drip.math.regime.MultiSegmentRegime regimePrev = null;

		if (0 == iNumRegime) return null;

		for (org.drip.state.estimator.RegimeBuilderSet rbs : aRBS) {
			if (null == rbs) return null;

			org.drip.state.estimator.LatentStateMetricMeasure[] aLSMM = rbs.getLSMM();

			org.drip.product.definition.CalibratableComponent[] aCalibComp = rbs.getCalibComp();

			int iNumCalibComp = aCalibComp.length;
			org.drip.math.regime.MultiSegmentRegime regime = null;

			for (int i = 0; i < iNumCalibComp; ++i) {
				if (null == aCalibComp[i]) return null;

				org.drip.state.estimator.PredictorResponseLinearConstraint prlc =
					aCalibComp[i].generateCalibPRLC (valParams, pricerParams, cmp, quotingParams, aLSMM[i]);

				if (null == prlc) return null;

				org.drip.math.segment.ResponseValueConstraint rvc = GenerateSegmentConstraint (prlc, regime,
					regimePrev);

				if (null == rvc) return null;

				double dblMaturity = aCalibComp[i].getMaturityDate().getJulian();

				try {
					if (null == regime) {
						if (null == (regime =
							org.drip.math.regime.RegimeBuilder.CreateUncalibratedRegimeEstimator
								(rbs.getName(), new double[] {valParams._dblValue, dblMaturity}, new
									org.drip.math.segment.PredictorResponseBuilderParams[] {_sbpRegular})) ||
										!regime.setup (1., new
											org.drip.math.segment.ResponseValueConstraint[] {rvc}, _rcs))
							return null;
					} else {
						if (null == (regime = org.drip.math.regime.RegimeModifier.AppendSegment (regime,
							dblMaturity, rvc, _sbpRegular, _rcs)))
							return null;
					}
				} catch (java.lang.Exception e) {
					e.printStackTrace();

					return null;
				}
			}

			if (null == span) {
				try {
					span = new org.drip.math.grid.OverlappingRegimeSpan (regime);
				} catch (java.lang.Exception e) {
					e.printStackTrace();

					return null;
				}
			} else {
				if (!span.addRegime (regime)) return null;
			}

			regimePrev = regime;
		}

		return span;
	}
}
