
package org.drip.quant.linearalgebra;

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
 * Matrix implements Matrix manipulation routines. It exports the following functionality:
 * 	- Matrix Inversion using Closed form solutions (for low-dimension matrices), or using Gaussian
 * 		elimination
 * 	- Matrix Product
 * 	- Matrix Diagonalization and Diagonal Pivoting
 * 	- Matrix Regularization through Row Addition/Row Swap
 *
 * @author Lakshmi Krishnamurthy
 */

public class Matrix {

	/**
	 * Diagonalize the specified row in the source matrix, and apply comparable operations to the target
	 * 
	 * @param iQ Row in the Source Matrix
	 * @param aadblZ2XJack Source Matrix
	 * @param aadblZ2YJack Target Matrix
	 * 
	 * @return TRUE => Diagonalization was successful
	 */

	public static final boolean DiagonalizeRow (
		final int iQ,
		final double[][] aadblZ2XJack,
		final double[][] aadblZ2YJack)
	{
		if (0. != aadblZ2XJack[iQ][iQ]) return true;

		int iSize = aadblZ2XJack.length;
		int iP = iSize - 1;

		while (0. == aadblZ2XJack[iP][iQ] && iP >= 0) --iP;

		if (0 > iP) return false;

		for (int j = 0; j < iSize; ++j)
			aadblZ2XJack[iQ][j] += aadblZ2XJack[iP][j];

		aadblZ2YJack[iQ][iP] += 1.;
		return true;
	}

	/**
	 * Compute the Product of an input matrix and a column
	 * 
	 * @param aadblA Matrix A
	 * @param adblB Array B
	 * 
	 * @return The Product
	 */

	public static final double[][] Product (
		final double[][] aadblA,
		final double[] adblB)
	{
		if (null == aadblA || null == adblB) return null;

		int iNumACol = aadblA[0].length;
		int iNumProductCol = adblB.length;
		int iNumProductRow = aadblA.length;
		double[][] aadblProduct = new double[iNumProductRow][iNumProductCol];

		if (0 == iNumACol || iNumACol != adblB.length || 0 == iNumProductRow || 0 == iNumProductCol)
			return null;

		for (int iRow = 0; iRow < iNumProductRow; ++iRow) {
			for (int iCol = 0; iCol < iNumProductCol; ++iCol) {
				aadblProduct[iRow][iCol] = 0.;

				for (int i = 0; i < iNumACol; ++i) {
					if (!org.drip.quant.common.NumberUtil.IsValid (aadblA[iRow][i]) ||
						!org.drip.quant.common.NumberUtil.IsValid (adblB[i]))
						return null;

					aadblProduct[iRow][iCol] += aadblA[iRow][i] * adblB[i];
				}
			}
		}

		return aadblProduct;
	}

	/**
	 * Compute the Product of an input column and a matrix
	 * 
	 * @param adblA Column A
	 * @param aadblB Matrix B
	 * 
	 * @return The Product
	 */

	public static final double[][] Product (
		final double[] adblA,
		final double[][] aadblB)
	{
		if (null == adblA || null == aadblB) return null;

		int iNumACol = adblA.length;
		int iNumProductCol = aadblB.length;
		double[][] aadblProduct = new double[iNumACol][iNumProductCol];

		if (0 == iNumACol || iNumACol != aadblB.length || 0 == iNumProductCol) return null;

		for (int iRow = 0; iRow < iNumACol; ++iRow) {
			for (int iCol = 0; iCol < iNumProductCol; ++iCol) {
				aadblProduct[iRow][iCol] = 0.;

				for (int i = 0; i < iNumACol; ++i) {
					if (!org.drip.quant.common.NumberUtil.IsValid (adblA[iRow]) ||
						!org.drip.quant.common.NumberUtil.IsValid (aadblB[i][iCol]))
						return null;

					aadblProduct[iRow][iCol] += adblA[iRow] * aadblB[i][iCol];
				}
			}
		}

		return aadblProduct;
	}

	/**
	 * Compute the Product of the input matrices
	 * 
	 * @param aadblA Matrix A
	 * @param aadblB Matrix B
	 * 
	 * @return The Product
	 */

	public static final double[][] Product (
		final double[][] aadblA,
		final double[][] aadblB)
	{
		if (null == aadblA || null == aadblB) return null;

		int iNumACol = aadblA[0].length;
		int iNumProductRow = aadblA.length;
		int iNumProductCol = aadblB.length;
		double[][] aadblProduct = new double[iNumProductRow][iNumProductCol];

		if (0 == iNumACol || iNumACol != aadblB.length || 0 == iNumProductRow || 0 == iNumProductCol)
			return null;

		for (int iRow = 0; iRow < iNumProductRow; ++iRow) {
			for (int iCol = 0; iCol < iNumProductCol; ++iCol) {
				aadblProduct[iRow][iCol] = 0.;

				for (int i = 0; i < iNumACol; ++i) {
					if (!org.drip.quant.common.NumberUtil.IsValid (aadblA[iRow][i]) ||
						!org.drip.quant.common.NumberUtil.IsValid (aadblB[i][iCol]))
						return null;

					aadblProduct[iRow][iCol] += aadblA[iRow][i] * aadblB[i][iCol];
				}
			}
		}

		return aadblProduct;
	}

	/**
	 * Invert a 2D Matrix using Cramer's Rule
	 * 
	 * @param aadblA Input 2D Matrix
	 * 
	 * @return The Inverted Matrix
	 */

	public static final double[][] Invert2DMatrixUsingCramerRule (
		final double[][] aadblA)
	{
		if (null == aadblA || 2 != aadblA.length || 2 != aadblA[0].length) return null;

		for (int i = 0; i < 2; ++i) {
			for (int j = 0; j < 2; ++j) {
				if (!org.drip.quant.common.NumberUtil.IsValid (aadblA[i][j])) return null;
			}
		}

		double dblScale = aadblA[0][0] * aadblA[1][1] - aadblA[0][1] * aadblA[1][0];

		if (0. == dblScale) return null;

		return new double[][] {{aadblA[1][1] / dblScale, -1. * aadblA[0][1] / dblScale}, {-1. * aadblA[1][0]
			/ dblScale, aadblA[0][0] / dblScale}};
	}

	/**
	 * Regularize the specified diagonal entry of the input matrix using Row Swapping
	 * 
	 * @param mct The Input Matrix Complement Transform
	 * 
	 * @return The Regularization was successful
	 */

	public static final boolean RegularizeUsingRowSwap (
		final org.drip.quant.linearalgebra.MatrixComplementTransform mct)
	{
		if (null == mct) return false;

		int iSize = mct.size();

		double[][] aadblSource = mct.getSource();

		double[][] aadblComplement = mct.getComplement();

		for (int iDiagonal = 0; iDiagonal < iSize; ++iDiagonal) {
			if (0. == aadblSource[iDiagonal][iDiagonal]) {
				int iSwapRow = iSize - 1;

				while (iSwapRow >= 0 && (0. == aadblSource[iSwapRow][iDiagonal] || 0. ==
					aadblSource[iDiagonal][iSwapRow]))
					--iSwapRow;

				if (0 > iSwapRow) {
					iSwapRow = 0;

					while (iSwapRow < iSize && 0. == aadblSource[iSwapRow][iDiagonal])
						++iSwapRow;

					if (iSwapRow >= iSize) return false;
				}

				for (int iCol = 0; iCol < iSize; ++iCol) {
					double dblComplementDiagonalEntry = aadblComplement[iDiagonal][iCol];
					aadblComplement[iDiagonal][iCol] = aadblComplement[iSwapRow][iCol];
					aadblComplement[iSwapRow][iCol] = dblComplementDiagonalEntry;
					double dblSourceDiagonalEntry = aadblSource[iDiagonal][iCol];
					aadblSource[iDiagonal][iCol] = aadblSource[iSwapRow][iCol];
					aadblSource[iSwapRow][iCol] = dblSourceDiagonalEntry;
				}
			}
		}

		for (int iDiagonal = 0; iDiagonal < iSize; ++iDiagonal) {
			if (0. == aadblSource[iDiagonal][iDiagonal]) {
				org.drip.quant.common.NumberUtil.Print2DArray ("ZERO DIAG!", aadblSource, false);

				return false;
			}
		}

		return true;
	}

	/**
	 * Regularize the specified diagonal entry of the input matrix using Row Addition
	 * 
	 * @param mct The Input Matrix Complement Transform
	 * 
	 * @return The Regularization was successful
	 */

	public static final boolean RegularizeUsingRowAddition (
		final org.drip.quant.linearalgebra.MatrixComplementTransform mct)
	{
		if (null == mct) return false;

		int iSize = mct.size();

		double[][] aadblSource = mct.getSource();

		double[][] aadblComplement = mct.getComplement();

		for (int iDiagonal = 0; iDiagonal < iSize; ++iDiagonal) {
			if (0. == aadblSource[iDiagonal][iDiagonal]) {
				int iPivotRow = iSize - 1;

				while (0. == aadblSource[iPivotRow][iDiagonal] && iPivotRow >= 0) --iPivotRow;

				if (0 > iPivotRow) return false;

				for (int iCol = 0; iCol < iSize; ++iCol) {
					aadblSource[iDiagonal][iCol] += aadblSource[iPivotRow][iCol];
					aadblComplement[iDiagonal][iCol] += aadblComplement[iPivotRow][iCol];
				}
			}
		}

		return true;
	}

	/**
	 * Pivot the Diagonal of the Input Matrix
	 * 
	 * @param aadblA The Input Matrix
	 * 
	 * @return The Matrix Complement Transform Instance
	 */

	public static final org.drip.quant.linearalgebra.MatrixComplementTransform PivotDiagonal (
		final double[][] aadblA)
	{
		if (null == aadblA) return null;

		int iSize = aadblA.length;
		double[][] aadblSource = new double[iSize][iSize];
		double[][] aadblComplement = new double[iSize][iSize];
		org.drip.quant.linearalgebra.MatrixComplementTransform mctOut = null;

		if (0 == iSize || null == aadblA[0] || iSize != aadblA[0].length) return null;

		for (int i = 0; i < iSize; ++i) {
			for (int j = 0; j < iSize; ++j) {
				aadblSource[i][j] = aadblA[i][j];
				aadblComplement[i][j] = i == j ? 1. : 0.;
			}
		}

		try {
			mctOut = new org.drip.quant.linearalgebra.MatrixComplementTransform (aadblSource,
				aadblComplement);
		} catch (java.lang.Exception e) {
			e.printStackTrace();

			return null;
		}

		return RegularizeUsingRowSwap (mctOut) ? mctOut : null;
	}

	/**
	 * Invert the Source Matrix using Gaussian Elimination
	 * 
	 * @param aadblSource Source Matrix
	 * 
	 * @return The Inverted Matrix
	 */

	public static final double[][] InvertUsingGaussianElimination (
		final double[][] aadblSource)
	{
		org.drip.quant.linearalgebra.MatrixComplementTransform mctRegularized =
			org.drip.quant.linearalgebra.Matrix.PivotDiagonal (aadblSource);

		if (null == mctRegularized) return null;

		double[][] aadblRegularizedSource = mctRegularized.getSource();

		double[][] aadblRegularizedInverse = mctRegularized.getComplement();

		int iSize = aadblRegularizedSource.length;

		for (int iDiagonal = 0; iDiagonal < iSize; ++iDiagonal) {
			if (0. == aadblRegularizedSource[iDiagonal][iDiagonal]) return null;

			for (int iRow = 0; iRow < iSize; ++iRow) {
				if (iRow == iDiagonal || 0. == aadblRegularizedSource[iRow][iDiagonal]) continue;

				double dblColEntryEliminatorRatio = aadblRegularizedSource[iDiagonal][iDiagonal] /
					aadblRegularizedSource[iRow][iDiagonal];

				for (int iCol = 0; iCol < iSize; ++iCol) {
					aadblRegularizedSource[iRow][iCol] = aadblRegularizedSource[iRow][iCol] *
						dblColEntryEliminatorRatio - aadblRegularizedSource[iDiagonal][iCol];
					aadblRegularizedInverse[iRow][iCol] = aadblRegularizedInverse[iRow][iCol] *
						dblColEntryEliminatorRatio - aadblRegularizedInverse[iDiagonal][iCol];
				}
			}
		}

		for (int iDiagonal = 0; iDiagonal < iSize; ++iDiagonal) {
			double dblDiagScaleDown = aadblRegularizedSource[iDiagonal][iDiagonal];

			if (0. == dblDiagScaleDown) return null;

			for (int iCol = 0; iCol < iSize; ++iCol) {
				aadblRegularizedSource[iDiagonal][iCol] /= dblDiagScaleDown;
				aadblRegularizedInverse[iDiagonal][iCol] /= dblDiagScaleDown;
			}
		}

		return aadblRegularizedInverse;
	}

	/**
	 * Invert the input matrix using the specified Method
	 * 
	 * @param aadblA Input Matrix
	 * @param strMethod The Inversion Method
	 * 
	 * @return The Inverted Matrix
	 */

	public static final double[][] Invert (
		final double[][] aadblA,
		final java.lang.String strMethod)
	{
		if (null == aadblA) return null;

		int iSize = aadblA.length;
		double[][] aadblAInv = null;
		double[][] aadblASource = new double[iSize][iSize];
		double[][] aadblZ2YJack = new double[iSize][iSize];

		if (0 == iSize || iSize != aadblA[0].length) return null;

		for (int i = 0; i < iSize; ++i) {
			for (int j = 0; j < iSize; ++j) {
				if (!org.drip.quant.common.NumberUtil.IsValid (aadblASource[i][j] = aadblA[i][j]))
					return null;

				aadblZ2YJack[i][j] = i == j ? 1. : 0.;
			}
		}

		for (int i = 0; i < iSize; ++i) {
			if (0. == aadblASource[i][i] && !DiagonalizeRow (i, aadblASource, aadblZ2YJack)) return null;
		}

		if (null == strMethod || strMethod.isEmpty() || strMethod.equalsIgnoreCase ("GaussianElimination"))
			aadblAInv = InvertUsingGaussianElimination (aadblASource);

		if (null == aadblAInv || iSize != aadblAInv.length || iSize != aadblAInv[0].length) return null;

		return Product (aadblAInv, aadblZ2YJack);
	}

	/**
	 * Transpose the specified Square Matrix
	 * 
	 * @param aadblA The Input Square Matrix
	 * 
	 * @return The Transpose of the Square Matrix
	 */

	public static final double[][] Tranpose (
		final double[][] aadblA)
	{
		if (null == aadblA) return null;

		int iSize = aadblA.length;
		double[][] aadblATranspose = new double[iSize][iSize];

		if (0 == iSize || null == aadblA[0] || iSize != aadblA[0].length) return null;

		for (int i = 0; i < iSize; ++i) {
			for (int j = 0; j < iSize; ++j)
				aadblATranspose[i][j] = aadblA[j][i];
		}

		return aadblATranspose;
	}

	/**
	 * Compute the Cholesky-Banachiewicz Factorization of the specified Matrix.
	 * 
	 * @param aadblA The Input Matrix
	 * 
	 * @return The Factorized Matrix
	 */

	public static final double[][] CholeskyBanachiewiczFactorization (
		final double[][] aadblA)
	{
		if (null == aadblA) return null;

		int iSize = aadblA.length;
		double[][] aadblL = new double[iSize][iSize];

		if (0 == iSize || null == aadblA[0] || iSize != aadblA[0].length) return null;

		for (int i = 0; i < iSize; ++i) {
			for (int j = 0; j < iSize; ++j) {
				aadblL[i][j] = 0.;

				if (i == j) {
					for (int k = 0; k < j; ++k)
						aadblL[j][j] -= aadblL[j][k] * aadblL[j][k];

					aadblL[j][j] = java.lang.Math.sqrt (aadblL[j][j] + aadblA[j][j]);
				} else if (i > j) {
					for (int k = 0; k < j; ++k)
						aadblL[i][j] -= aadblL[i][k] * aadblL[j][k];

					aadblL[i][j] = (aadblA[i][j] + aadblL[i][j]) / aadblL[j][j];
				}
			}
		}

		return aadblL;
	}

	public static final void main (
		final java.lang.String[] astrArg)
	{
		// double[][] aadblA = new double[][] {{1, 2, 3}, {4, 5, 6}, {7, 8, 9.01}};
		// double[][] aadblA = new double[][] {{1, 2, 3}, {4, 5, 5}, {9, 7, 2}};
		// double[][] aadblA = new double[][] {{1, 1, 1}, {3, 0, 7}, {1, -1, 1}};
		/* double[][] aadblA = new double[][] {
			{1. / 6., 0., 0., 0.},
			{0., 0., 0., 1. / 6.},
			{-0.5, 0.5, 0., 0.},
			{1., -2., 1., 0.}
		}; */

		double[][] aadblA = new double[][] {
			{1.0000, 0.5000, 0.3333,  0.0000,  0.0000, 0.0000},
			{0.0000, 0.0000, 0.0000,  1.0000,  0.5000, 0.3333},
			{1.0000, 1.0000, 1.0000, -1.0000,  0.0000, 0.0000},
			{0.0000, 0.5000, 2.0000,  0.0000, -0.5000, 0.0000},
			{0.0000, 1.0000, 0.0000,  0.0000,  0.0000, 0.0000},
			{0.0000, 0.0000, 0.0000,  0.0000,  0.0000, 1.0000},
		};

		double[][] aadblAInv = Invert (aadblA, "");

		org.drip.quant.common.NumberUtil.Print2DArray ("AINV", aadblAInv, false);

		System.out.println ("\n");

		double[][] aadblProduct = Product (aadblA, aadblAInv);

		org.drip.quant.common.NumberUtil.Print2DArray ("PROD", aadblProduct, false);
	}
}
