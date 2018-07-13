package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kale.struct.Matrix;

public class MeanRank_Hits1_3_5 {
	public int iNumberOfEntities;
	public int iNumberOfRelations;
	public int iNumberOfFactors;
	
	public Matrix MatrixE = null;
	public Matrix MatrixR = null;
	List<Double> iList = new ArrayList<Double>();
	
	
	public MeanRank_Hits1_3_5(int iEntities, int iRelations, int iFactors) {
		iNumberOfEntities = iEntities;
		iNumberOfRelations = iRelations;
		iNumberOfFactors = iFactors;
	}
	
	public void LPEvaluation(
			String fnMatrixE, 
			String fnMatrixR, 
			String fnTestTriples, 
			String fnResults) throws Exception {
		preprocess(fnMatrixE, fnMatrixR);
		evaluate(fnTestTriples, fnResults);
	}
	
	public void preprocess(String fnMatrixE, String fnMatrixR) throws Exception {
		MatrixE = new Matrix(iNumberOfEntities, iNumberOfFactors);
		MatrixE.load(fnMatrixE);
		
		MatrixR = new Matrix(iNumberOfRelations, iNumberOfFactors);
		MatrixR.load(fnMatrixR);
	}
	
	public void evaluate(String fnTestTriples, String fnResults) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(fnTestTriples), "UTF-8"));
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(fnResults), "UTF-8"));
		
		String line = "";
		int iCnt = 0;
		double dTotalMeanRank = 0.0;
		double dTotalMRR = 0.0;
		int iTotalHits1 = 0;
		int iTotalHits3 = 0;
		int iTotalHits5 = 0;
		int iTotalHits10 = 0;
		double dMedian = 0.0;
		while ((line = reader.readLine()) != null) {
			String[] tokens = line.split("\t");
			int iRelationID = Integer.parseInt(tokens[1]);
			int iSubjectID = Integer.parseInt(tokens[0]);
			int iObjectID = Integer.parseInt(tokens[2]);
			double dTargetValue = 0.0;
			for (int p = 0; p < iNumberOfFactors; p++) {
				dTargetValue -= Math.abs(MatrixE.get(iSubjectID, p) + MatrixR.get(iRelationID, p) - MatrixE.get(iObjectID, p));
			}
			
			int iLeftRank = 1;
			int iLeftIdentical = 0;
			for (int iLeftID = 0; iLeftID < iNumberOfEntities; iLeftID++) {
				double dValue = 0.0;
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
			double dLeftRank = (double)(2.0 * iLeftRank + iLeftIdentical - 1.0) / 2.0;
			int iLeftHitsAt1 = 0,iLeftHitsAt3 = 0,iLeftHitsAt5 = 0,iLeftHitsAt10 = 0;
			if (dLeftRank <= 1.0) {
				iLeftHitsAt1 = 1;
			}
			if (dLeftRank <= 3.0) {
				iLeftHitsAt3 = 1;
			}
			if (dLeftRank <= 5.0) {
				iLeftHitsAt5 = 1;
			}
			if (dLeftRank <= 10.0) {
				iLeftHitsAt10 = 1;
			}
			writer.write(iSubjectID + "\t" + iRelationID+ "\t" + iObjectID +  ":" + dLeftRank + "\t" + iLeftHitsAt10 + "\n");
			dTotalMeanRank += dLeftRank;
			dTotalMRR += 1.0/(double)dLeftRank;
			iTotalHits1 += iLeftHitsAt1;
			iTotalHits3 += iLeftHitsAt3;
			iTotalHits5 += iLeftHitsAt5;
			iTotalHits10 += iLeftHitsAt10;
			iList.add(dLeftRank);
			iCnt++;
			
			int iRightRank = 1;
			int iRightIdentical = 0;
			for (int iRightID = 0; iRightID < iNumberOfEntities; iRightID++) {
				double dValue = 0.0;
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
			double dRightRank = (double)(2.0 * iRightRank + iRightIdentical - 1.0) / 2.0;
			int iRightHitsAt1 = 0,iRightHitsAt3 = 0,iRightHitsAt5 = 0,iRightHitsAt10 = 0;
			if (dRightRank <= 1.0) {
				iRightHitsAt1 = 1;
			}
			if (dRightRank <= 3.0) {
				iRightHitsAt3 = 1;
			}
			if (dRightRank <= 5.0) {
				iRightHitsAt5 = 1;
			}
			if (dRightRank <= 10.0) {
				iRightHitsAt10 = 1;
			}
			writer.write(iSubjectID + "\t" + iRelationID+ "\t" + iObjectID +  ":" + dRightRank + "\t" + iRightHitsAt10 + "\n");
			dTotalMeanRank += dRightRank;
			dTotalMRR += 1.0/(double)dRightRank;
			iTotalHits1 += iRightHitsAt1;
			iTotalHits3 += iRightHitsAt3;
			iTotalHits5 += iRightHitsAt5;
			iTotalHits10 += iRightHitsAt10;
			iList.add(dRightRank);
			iCnt++;
		}
		Collections.sort(iList);
		int indx=iList.size()/2;
		if (iList.size()%2==0) {
			dMedian = (iList.get(indx-1)+iList.get(indx))/2.0;
		}
		else {
			dMedian = iList.get(indx);
		}
		System.out.println(iCnt);
		System.out.println(iTotalHits10);
		writer.write("MeanRank: "+(dTotalMeanRank / (double)iCnt) + "\t" 
				+ "MRR: "+(dTotalMRR / (double)iCnt) 
				+ "\t" +"Median: " + dMedian + "\t" + "Hit@10: " +((double)iTotalHits10 / (double)iCnt)+ "\n");
		writer.write("Hit@1: "+((double)iTotalHits1 / (double)iCnt) + "\t" + "Hit@3: " + ((double)iTotalHits3 / (double)iCnt) + "\t" + "Hit@5: " +((double)iTotalHits5 / (double)iCnt)+ "\n");
		System.out.println("MeanRank: "+(dTotalMeanRank / (double)iCnt) + "\t" 
				+ "MRR: "+(dTotalMRR / (double)iCnt) 
				+ "Median: " + dMedian + "\t" + "Hit@10: " +((double)iTotalHits10 / (double)iCnt)+ "\n");
		System.out.println("Hit@1: "+((double)iTotalHits1 / (double)iCnt) + "\t" + "Hit@3: " + ((double)iTotalHits3 / (double)iCnt) + "\t" + "Hit@5: " +((double)iTotalHits5 / (double)iCnt) + "\n");
		reader.close();
		writer.close();
	}
	
	public static void main(String[] args) throws Exception {
//		int iEntities = 40943;
//		int iRelations = 18;
//		int iFactors = 20;
//		String fnMatrixE = "Output\\MatrixE-20-0.01-0.01.best";
//		String strTensorRPrefix = "Output\\TensorR-20-0.01-0.01";
//		String fnTestTriples = "..\\WN18-test.txt";
//		String fnResults = "FB15K-Evaluation\\dir-1000\\Test-DIR-k50-d1-ge0.01-gr1-iter1000.eval";
		int iEntities = 9738;
		int iRelations = 122;
		int iFactors = 100;
		String fnMatrixE = "Output\\FB122-model\\pre-rdivb\\MatrixE-base-k100-d0.12-ge0.05-gr10.best";
		String fnMatrixR = "Output\\FB122-model\\pre-rdivb\\MatrixR-base-k100-d0.12-ge0.05-gr10.best";
		String fnTestTriples = "data\\FB122data-Filt2\\freebase-test-filt2.txt";
		String fnResults = "Evaluation\\FB122-model\\pre-rdivb\\Test-base-k100-d0.12-ge0.05-gr10-all.eval";
		
//		int iEntities = Integer.parseInt(args[0]);
//		int iRelations = Integer.parseInt(args[1]);
//		int iFactors = Integer.parseInt(args[2]);
//		String fnMatrixE = args[3];
//		String fnMatrixR = args[4];
//		String fnTestTriples = args[5];
//		String fnResults = args[6];
		
		MeanRank_Hits1_3_5 eval = new MeanRank_Hits1_3_5(iEntities, iRelations, iFactors);
		eval.LPEvaluation(fnMatrixE, fnMatrixR, fnTestTriples, fnResults);
	}
}
