package kale.trip;

import java.util.ArrayList;

import kale.struct.Matrix;
import kale.struct.Triple;

public class StochasticUpdate {
	public ArrayList<Triple> lstPosTriples;
	public ArrayList<Triple> lstHeadNegTriples;
	public ArrayList<Triple> lstTailNegTriples;
	public Matrix MatrixE;
	public Matrix MatrixR;
	public Matrix MatrixEGradient;
	public Matrix MatrixRGradient;
	public double dGammaE;
	public double dGammaR;
	public double dDelta;
	
	public StochasticUpdate(
			ArrayList<Triple> inLstPosTriples,
			ArrayList<Triple> inLstHeadNegTriples,
			ArrayList<Triple> inLstTailNegTriples,
			Matrix inMatrixE,
			Matrix inMatrixR,
			Matrix inMatrixEGradient,
			Matrix inMatrixRGradient,
			double inGammaE,
			double inGammaR,
			double inDelta) {
		lstPosTriples = inLstPosTriples;
		lstHeadNegTriples = inLstHeadNegTriples;
		lstTailNegTriples = inLstTailNegTriples;
		MatrixE = inMatrixE;
		MatrixR = inMatrixR;
		MatrixEGradient = inMatrixEGradient;
		MatrixRGradient = inMatrixRGradient;
		dGammaE = inGammaE;
		dGammaR = inGammaR;
		dDelta = inDelta;
	}
	
	public void stochasticIteration() throws Exception {
		MatrixEGradient.setToValue(0.0);
		MatrixRGradient.setToValue(0.0);
		for (int iID = 0; iID < lstPosTriples.size(); iID++) {
			Triple PosTriple = lstPosTriples.get(iID);
			Triple HeadNegTriple = lstHeadNegTriples.get(iID);
			Triple TailNegTriple = lstTailNegTriples.get(iID);
			
			StochasticGradient headGradient = new StochasticGradient(
					PosTriple,
					HeadNegTriple,
					MatrixE,
					MatrixR,
					MatrixEGradient,
					MatrixRGradient,
					dDelta);
			headGradient.calculateGradient();
			
			StochasticGradient tailGradient = new StochasticGradient(
					PosTriple,
					TailNegTriple,
					MatrixE,
					MatrixR,
					MatrixEGradient,
					MatrixRGradient,
					dDelta);
			tailGradient.calculateGradient();
		}
		
		for (int i = 0; i < MatrixE.rows(); i++) {
			for (int j = 0; j < MatrixE.columns(); j++) {
				double dValue = MatrixEGradient.get(i, j);
				MatrixE.add(i, j, -1.0 * dGammaE * dValue);
			}
		}
		for (int i = 0; i < MatrixR.rows(); i++) {
			for (int j = 0; j < MatrixR.columns(); j++) {
				double dValue = MatrixRGradient.get(i, j);
				MatrixR.add(i, j, -1.0 * dGammaR * dValue);
			}
		}
		MatrixE.normalizeByRow();
		MatrixR.normalizeByRow();
	}
}
