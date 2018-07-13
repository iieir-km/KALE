package kale.joint;
import java.util.ArrayList;

import kale.struct.Matrix;
import kale.struct.Rule;
import kale.struct.Triple;

public class StochasticUpdate {
	public ArrayList<Triple> lstPosTriples;
	public ArrayList<Triple> lstHeadNegTriples;
	public ArrayList<Triple> lstTailNegTriples;
	public ArrayList<Rule> lstRules;
//	public ArrayList<Rule> lstFstRelNegRules;
	public ArrayList<Rule> lstSndRelNegRules;

	public Matrix MatrixE;
	public Matrix MatrixR;
	public Matrix MatrixEGradient;
	public Matrix MatrixRGradient;
	public double dGammaE;
	public double dGammaR;
	public double dDelta;
	public double m_Weight;
	
	public StochasticUpdate(
			ArrayList<Triple> inLstPosTriples,
			ArrayList<Triple> inLstHeadNegTriples,
			ArrayList<Triple> inLstTailNegTriples,
			ArrayList<Rule> inlstRules,
			ArrayList<Rule> inlstSndRelNegRules,
			Matrix inMatrixE, 
			Matrix inMatrixR,
			Matrix inMatrixEGradient, 
			Matrix inMatrixRGradient,
			double inGammaE,
			double inGammaR,
			double inDelta,
			double in_m_Weight) {
		lstPosTriples = inLstPosTriples;
		lstHeadNegTriples = inLstHeadNegTriples;
		lstTailNegTriples = inLstTailNegTriples;
		lstRules = inlstRules;
		lstSndRelNegRules = inlstSndRelNegRules;
		MatrixE = inMatrixE;
		MatrixR = inMatrixR;
		MatrixEGradient = inMatrixEGradient;
		MatrixRGradient = inMatrixRGradient;
		m_Weight = in_m_Weight;
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
			
			TripleGradient headGradient = new TripleGradient(
					PosTriple,
					HeadNegTriple,
					MatrixE,
					MatrixR,
					MatrixEGradient,
					MatrixRGradient,
					dDelta);
			headGradient.calculateGradient();

			TripleGradient tailGradient = new TripleGradient(
					PosTriple,
					TailNegTriple,
					MatrixE,
					MatrixR,
					MatrixEGradient,
					MatrixRGradient,
					dDelta);
			tailGradient.calculateGradient();
		}

		for (int iID = 0; iID < lstRules.size(); iID++) {
			Rule rule = lstRules.get(iID);
			Rule sndRelNegrule = lstSndRelNegRules.get(iID);
			
			RuleGradient tailruleGradient = new RuleGradient(
					rule,
					sndRelNegrule,
					MatrixE,
					MatrixR,
					MatrixEGradient,
					MatrixRGradient,
					dDelta);
			tailruleGradient.calculateGradient(m_Weight);	
		}
		
		MatrixEGradient.rescaleByRow();
		MatrixRGradient.rescaleByRow();
		
		for (int i = 0; i < MatrixE.rows(); i++) {
			for (int j = 0; j < MatrixE.columns(); j++) {
				double dValue = MatrixEGradient.get(i, j);
				MatrixEGradient.accumulatedByGrad(i, j);
				double dLrate = Math.sqrt(MatrixEGradient.getSum(i, j)) + 1e-8;
				MatrixE.add(i, j, -1.0 * dGammaE * dValue / dLrate);
			}
		}
		for (int i = 0; i < MatrixR.rows(); i++) {
			for (int j = 0; j < MatrixR.columns(); j++) {
				double dValue = MatrixRGradient.get(i, j);
				MatrixRGradient.accumulatedByGrad(i, j);
				double dLrate = Math.sqrt(MatrixRGradient.getSum(i, j)) + 1e-8;
				MatrixR.add(i, j, -1.0 * dGammaR * dValue / dLrate);
			}
		}
		MatrixE.normalizeByRow();
		MatrixR.normalizeByRow();
	}
}
