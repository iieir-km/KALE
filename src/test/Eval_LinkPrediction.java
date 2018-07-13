package test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import kale.struct.Matrix;

public class Eval_LinkPrediction {
	public int iNumberOfEntities;
	public int iNumberOfRelations;
	public int iNumberOfFactors;
	
	public Matrix MatrixE = null;
	public Matrix MatrixR = null;
	List<Double> iFiltList = new ArrayList<Double>();
	List<Double> iRawList = new ArrayList<Double>();
	public HashMap<String, Boolean> lstTriples = null;
	
	
	public Eval_LinkPrediction(int iEntities, int iRelations, int iFactors) {
		iNumberOfEntities = iEntities;
		iNumberOfRelations = iRelations;
		iNumberOfFactors = iFactors;
	}
	
	public void LPEvaluation(
			String fnMatrixE, 
			String fnMatrixR, 
			String fnTrainTriples, 
			String fnValidTriples, 
			String fnTestTriples) throws Exception {
		preprocess(fnTrainTriples,fnValidTriples,fnTestTriples,fnMatrixE, fnMatrixR);
		evaluate(fnTestTriples);
	}
	
	public void preprocess(
			String fnTrainTriples, String fnValidTriples, String fnTestTriples, 
			String fnMatrixE, String fnMatrixR) throws Exception {
		MatrixE = new Matrix(iNumberOfEntities, iNumberOfFactors);
		MatrixE.load(fnMatrixE);
		
		MatrixR = new Matrix(iNumberOfRelations, iNumberOfFactors);
		MatrixR.load(fnMatrixR);
		
		BufferedReader train = new BufferedReader(new InputStreamReader(
				new FileInputStream(fnTrainTriples), "UTF-8"));	
		BufferedReader valid = new BufferedReader(new InputStreamReader(
				new FileInputStream(fnValidTriples), "UTF-8"));
		BufferedReader test = new BufferedReader(new InputStreamReader(
				new FileInputStream(fnTestTriples), "UTF-8"));
		lstTriples = new HashMap<String, Boolean> ();
		String line = "";
		while ((line = train.readLine()) != null) {
			if (!lstTriples.containsKey(line.trim())) {

					lstTriples.put(line.trim(), true);
				} 
		}	
		line = "";
		while ((line = valid.readLine()) != null) {
			if (!lstTriples.containsKey(line.trim())) {

				lstTriples.put(line.trim(), true);
			} 
		}
		line = "";
		while ((line = test.readLine()) != null) {
			if (!lstTriples.containsKey(line.trim())) {

				lstTriples.put(line.trim(), true);
			} 

		}
		System.out.println("triples:"+lstTriples.size());
		valid.close();
		test.close();
		train.close();
	}
	
	public void evaluate(String fnTestTriples) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(fnTestTriples), "UTF-8"));
		String line = "";
		int iCnt = 0;
		double dTotalMeanRank_filt = 0.0;
		double dTotalMRR_filt = 0.0;
		int iTotalHits1_filt = 0;
		int iTotalHits3_filt = 0;
		int iTotalHits5_filt = 0;
		int iTotalHits10_filt = 0;
		double dMedian_filt = 0.0;
		
		double dTotalMeanRank_raw = 0.0;
		double dTotalMRR_raw = 0.0;
		int iTotalHits1_raw = 0;
		int iTotalHits3_raw = 0;
		int iTotalHits5_raw = 0;
		int iTotalHits10_raw = 0;
		double dMedian_raw = 0.0;

		
		while ((line = reader.readLine()) != null) {
			System.out.println("triple:" + iCnt/2);
			String[] tokens = line.split("\t");
			int iRelationID = Integer.parseInt(tokens[1]);
			int iSubjectID = Integer.parseInt(tokens[0]);
			int iObjectID = Integer.parseInt(tokens[2]);
			double dTargetValue = 0.0;
			for (int p = 0; p < iNumberOfFactors; p++) {
				dTargetValue -= Math.abs(MatrixE.get(iSubjectID, p) + MatrixR.get(iRelationID, p) - MatrixE.get(iObjectID, p));
			}
			
			int iLeftRank_filt = 1;
			int iLeftIdentical_filt = 0;
			int iLeftRank_raw = 1;
			int iLeftIdentical_raw = 0;
			
			for (int iLeftID = 0; iLeftID < iNumberOfEntities; iLeftID++) {
				double dValue = 0.0;
				String negTiple = iLeftID + "\t" + iRelationID + "\t" +iObjectID;
				for (int p = 0; p < iNumberOfFactors; p++) {
					dValue -= Math.abs(MatrixE.get(iLeftID, p) + MatrixR.get(iRelationID, p) - MatrixE.get(iObjectID, p));
				}
				if(!lstTriples.containsKey(negTiple)){
					if (dValue > dTargetValue) {
						iLeftRank_filt++;
					}
					if (dValue == dTargetValue) {
						iLeftIdentical_filt++;
					}
				}	
				if (dValue > dTargetValue) {
					iLeftRank_raw++;
				}
				if (dValue == dTargetValue) {
					iLeftIdentical_raw++;
				}
			}

			double dLeftRank_filt = (double)iLeftRank_filt;
			double dLeftRank_raw = (double)(2.0 * iLeftRank_raw + iLeftIdentical_raw -1.0) / 2.0;
			int iLeftHitsAt1_filt = 0,iLeftHitsAt3_filt = 0,iLeftHitsAt5_filt = 0,iLeftHitsAt10_filt = 0;
			int iLeftHitsAt1_raw = 0,iLeftHitsAt3_raw = 0,iLeftHitsAt5_raw = 0,iLeftHitsAt10_raw = 0;
			if (dLeftRank_filt <= 1.0) {
				iLeftHitsAt1_filt = 1;
			}
			if (dLeftRank_filt <= 3.0) {
				iLeftHitsAt3_filt = 1;
			}
			if (dLeftRank_filt <= 5.0) {
				iLeftHitsAt5_filt = 1;
			}
			if (dLeftRank_filt <= 10.0) {
				iLeftHitsAt10_filt = 1;
			}
			
			if (dLeftRank_raw <= 1.0) {
				iLeftHitsAt1_raw = 1;
			}
			if (dLeftRank_raw <= 3.0) {
				iLeftHitsAt3_raw = 1;
			}
			if (dLeftRank_raw <= 5.0) {
				iLeftHitsAt5_raw = 1;
			}
			if (dLeftRank_raw <= 10.0) {
				iLeftHitsAt10_raw = 1;
			}
		
			dTotalMeanRank_filt += dLeftRank_filt;
			dTotalMRR_filt += 1.0/(double)dLeftRank_filt;
			iTotalHits1_filt += iLeftHitsAt1_filt;
			iTotalHits3_filt += iLeftHitsAt3_filt;
			iTotalHits5_filt += iLeftHitsAt5_filt;
			iTotalHits10_filt += iLeftHitsAt10_filt;
			iFiltList.add(dLeftRank_filt);
			
			dTotalMeanRank_raw += dLeftRank_raw;
			dTotalMRR_raw += 1.0/(double)dLeftRank_raw;
			iTotalHits1_raw += iLeftHitsAt1_raw;
			iTotalHits3_raw += iLeftHitsAt3_raw;
			iTotalHits5_raw += iLeftHitsAt5_raw;
			iTotalHits10_raw += iLeftHitsAt10_raw;
			iRawList.add(dLeftRank_raw);
			iCnt++;
			
			int iRightRank_filt = 1;
			int iRightIdentical_filt = 0;
			int iRightRank_raw = 1;
			int iRightIdentical_raw = 0;
			for (int iRightID = 0; iRightID < iNumberOfEntities; iRightID++) {
				double dValue = 0.0;
				String negTiple = iSubjectID + "\t" + iRelationID + "\t" +iRightID;
				for (int p = 0; p < iNumberOfFactors; p++) {
					dValue -= Math.abs(MatrixE.get(iSubjectID, p) + MatrixR.get(iRelationID, p) - MatrixE.get(iRightID, p));
				}
				if(!lstTriples.containsKey(negTiple)){
					if (dValue > dTargetValue) {
						iRightRank_filt++;						
					}
					if (dValue == dTargetValue) {
						iRightIdentical_filt++;
					}					
				}
				if (dValue > dTargetValue) {
					iRightRank_raw++;						
				}
				if (dValue == dTargetValue) {
					iRightIdentical_raw++;
				}	
			}
			
			double dRightRank_filt = (double)iRightRank_filt;
			double dRightRank_raw = (double)(2.0 * iRightRank_raw + iRightIdentical_raw -1.0) / 2.0;
			int iRightHitsAt1_filt = 0,iRightHitsAt3_filt = 0,iRightHitsAt5_filt = 0,iRightHitsAt10_filt = 0;
			int iRightHitsAt1_raw = 0,iRightHitsAt3_raw = 0,iRightHitsAt5_raw= 0,iRightHitsAt10_raw = 0;
			if (dRightRank_filt <= 1.0) {
				iRightHitsAt1_filt = 1;
			}
			if (dRightRank_filt <= 3.0) {
				iRightHitsAt3_filt = 1;
			}
			if (dRightRank_filt <= 5.0) {
				iRightHitsAt5_filt = 1;
			}
			if (dRightRank_filt <= 10.0) {
				iRightHitsAt10_filt = 1;
			}
			
			if (dRightRank_raw <= 1.0) {
				iRightHitsAt1_raw = 1;
			}
			if (dRightRank_raw <= 3.0) {
				iRightHitsAt3_raw = 1;
			}
			if (dRightRank_raw <= 5.0) {
				iRightHitsAt5_raw = 1;
			}
			if (dRightRank_raw <= 10.0) {
				iRightHitsAt10_raw = 1;
			}
			
			dTotalMeanRank_filt += dRightRank_filt;
			dTotalMRR_filt += 1.0/(double)dRightRank_filt;
			iTotalHits1_filt += iRightHitsAt1_filt;
			iTotalHits3_filt += iRightHitsAt3_filt;
			iTotalHits5_filt += iRightHitsAt5_filt;
			iTotalHits10_filt += iRightHitsAt10_filt;
			iFiltList.add(dRightRank_filt);
			
			dTotalMeanRank_raw += dRightRank_raw;
			dTotalMRR_raw += 1.0/(double)dRightRank_raw;
			iTotalHits1_raw += iRightHitsAt1_raw;
			iTotalHits3_raw += iRightHitsAt3_raw;
			iTotalHits5_raw += iRightHitsAt5_raw;
			iTotalHits10_raw += iRightHitsAt10_raw;
			iRawList.add(dRightRank_raw);
			iCnt++;	
			
		}
		Collections.sort(iFiltList);
		int indx=iFiltList.size()/2;
		if (iFiltList.size()%2==0) {
			dMedian_filt = (iFiltList.get(indx-1)+iFiltList.get(indx))/2.0;
		}
		else {
			dMedian_filt = iFiltList.get(indx);
		}
		
		Collections.sort(iRawList);
		indx=iRawList.size()/2;
		if (iRawList.size()%2==0) {
			dMedian_raw = (iRawList.get(indx-1)+iRawList.get(indx))/2.0;
		}
		else {
			dMedian_raw = iRawList.get(indx);
		}
		
		System.out.println("Filt setting:");
		System.out.println("MeanRank: "+(dTotalMeanRank_filt / (double)iCnt) + "\n"  
				+ "MRR: "+(dTotalMRR_filt / (double)iCnt) + "\n" 
				+ "Median: " + dMedian_filt + "\n" 
				+ "Hit@1: "+((double)iTotalHits1_filt / (double)iCnt) + "\n" 
				+ "Hit@3: " + ((double)iTotalHits3_filt / (double)iCnt) + "\n" 
				+ "Hit@5: " +((double)iTotalHits5_filt / (double)iCnt)+ "\n"
				+ "Hit@10: " +((double)iTotalHits10_filt / (double)iCnt)+ "\n");
		
		System.out.println("Raw setting:");
		System.out.println("MeanRank: "+(dTotalMeanRank_raw / (double)iCnt) + "\n"  
				+ "MRR: "+(dTotalMRR_raw / (double)iCnt) + "\n" 
				+ "Median: " + dMedian_raw + "\n" 
				+ "Hit@1: "+((double)iTotalHits1_raw / (double)iCnt) + "\n" 
				+ "Hit@3: " + ((double)iTotalHits3_raw / (double)iCnt) + "\n" 
				+ "Hit@5: " +((double)iTotalHits5_raw / (double)iCnt)+ "\n"
				+ "Hit@10: " +((double)iTotalHits10_raw / (double)iCnt)+ "\n");
		reader.close();
	}
	
	public static void main(String[] args) throws Exception {
		int iEntities = 40943;
		int iRelations = 18;
		int iFactors = 50;
		String fnMatrixE = "MatrixE.real.best";
		String fnMatrixR = "MatrixR.real.best";
		String fnTrainTriples = "datasets\\wn18\\train.txt";
		String fnValidTriples = "datasets\\wn18\\valid.txt";
		String fnTestTriples = "datasets\\wn18\\test.txt";

//		int iEntities = Integer.parseInt(args[0]);
//		int iRelations = Integer.parseInt(args[1]);
//		int iFactors = Integer.parseInt(args[2]);
//		String fnMatrixE = args[3];
//		String fnMatrixR = args[4];
//		String fnTrainTriples = args[5];
//		String fnValidTriples = args[6];
//		String fnTestTriples = args[7];

		
		Eval_LinkPrediction eval = new Eval_LinkPrediction(iEntities, iRelations, iFactors);
		eval.LPEvaluation(fnMatrixE, fnMatrixR, 
				fnTrainTriples, fnValidTriples, fnTestTriples);
	}
}
