package kale.util;

import java.util.HashMap;

import kale.struct.Matrix;
import kale.struct.TripleSet;

public class MetricMonitor {
	public TripleSet lstValidateTriples;
	public HashMap<String, Boolean> lstTriples;
	public Matrix MatrixE;
	public Matrix MatrixR;
	public double dMeanRank;
	public double dMRR;
	public double dHits;
	
	public MetricMonitor(TripleSet inLstValidateTriples,
			HashMap<String, Boolean> inlstTriples,
			Matrix inMatrixE,
			Matrix inMatrixR) {
		lstValidateTriples = inLstValidateTriples;
		lstTriples = inlstTriples;
		MatrixE = inMatrixE;
		MatrixR = inMatrixR;
	}
	
	public void calculateMetrics() throws Exception {
		int iNumberOfEntities = MatrixE.rows();
		int iNumberOfFactors = MatrixE.columns();
		
		int iCnt = 0;
		double avgMeanRank = 0.0;
		double avgMRR = 0.0;
		int avgHits = 0;
		for (int iID = 0; iID < lstValidateTriples.triples(); iID++) {
			int iRelationID = lstValidateTriples.get(iID).relation();
			int iSubjectID = lstValidateTriples.get(iID).head();
			int iObjectID = lstValidateTriples.get(iID).tail();
			double dTargetValue = 0.0;
			for (int p = 0; p < iNumberOfFactors; p++) {
				dTargetValue -= Math.abs(MatrixE.get(iSubjectID, p) + MatrixR.get(iRelationID, p) - MatrixE.get(iObjectID, p));
			}
			
			int iLeftRank = 1;
			int iLeftIdentical = 0;
			for (int iLeftID = 0; iLeftID < iNumberOfEntities; iLeftID++) {
				double dValue = 0.0;
				String negTiple = iLeftID + "\t" + iRelationID + "\t" +iObjectID;
				if(!lstTriples.containsKey(negTiple)){
					for (int p = 0; p < iNumberOfFactors; p++) {
						dValue -= Math.abs(MatrixE.get(iLeftID, p) + MatrixR.get(iRelationID, p) - MatrixE.get(iObjectID, p));
					}
					if (dValue > dTargetValue) {
						iLeftRank++;
					}
					if (dValue == dTargetValue) {
						iLeftIdentical++;
					}
				}

			}
			
			double dLeftRank = iLeftRank;
			int iLeftHitsAt10 = 0;
			if (dLeftRank <= 10.0) {
				iLeftHitsAt10 = 1;
			}
			avgMeanRank += dLeftRank;
			avgMRR += 1.0/(double)dLeftRank;
			avgHits += iLeftHitsAt10;
			iCnt++;
			
			int iRightRank = 1;
			int iRightIdentical = 0;
			for (int iRightID = 0; iRightID < iNumberOfEntities; iRightID++) {
				double dValue = 0.0;
				String negTiple = iSubjectID + "\t" + iRelationID + "\t" +iRightID;
				if(!lstTriples.containsKey(negTiple)){
					for (int p = 0; p < iNumberOfFactors; p++) {
						dValue -= Math.abs(MatrixE.get(iSubjectID, p) + MatrixR.get(iRelationID, p) - MatrixE.get(iRightID, p));
					}
					if (dValue > dTargetValue) {
						iRightRank++;
					}
					if (dValue == dTargetValue) {
						iRightIdentical++;
					}
				}
			}

			double dRightRank = iRightRank;
			int iRightHitsAt10 = 0;
			if (dRightRank <= 10.0) {
				iRightHitsAt10 = 1;
			}
			avgMeanRank += dRightRank;
			avgMRR += 1.0/(double)dRightRank;
			avgHits += iRightHitsAt10;
			iCnt++;	
		}
		
		dMRR = avgMRR / (double)(iCnt);
		dHits = (double)avgHits / (double)(iCnt);
		System.out.println("avgMRR:" + avgMRR + "\t" + "avgHits:" + avgHits);
		System.out.println("MRR:" + dMRR + "\t" + "Hits:" + dHits);
	}
}
