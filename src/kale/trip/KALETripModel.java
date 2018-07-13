package kale.trip;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

import kale.struct.Matrix;
import kale.struct.Triple;
import kale.struct.TripleSet;
import kale.util.MetricMonitor;
import kale.util.NegativeTripleGeneration;
import test.Eval_LinkPrediction;

public class KALETripModel {
	public TripleSet m_TrainingTriples;
	public TripleSet m_ValidateTriples;
	public TripleSet m_TestingTriples;
	public TripleSet m_Triples;
	public Matrix m_Entity_Factor_MatrixE;
	public Matrix m_Relation_Factor_MatrixR;
	public Matrix m_MatrixEGradient;
	public Matrix m_MatrixRGradient;
	
	public int m_NumRelation;
	public int m_NumEntity;
	public String m_MatrixE_prefix = "";
	public String m_MatrixR_prefix = "";
	
	public int m_NumFactor = 20;
	public int m_NumMiniBatch = 100;
	public double m_Delta = 0.1;
	public double m_GammaE = 0.01;
	public double m_GammaR = 0.01;
	public int m_NumIteration = 1000;
	public int m_OutputIterSkip = 50;
	
	
	
	java.text.DecimalFormat decimalFormat = new java.text.DecimalFormat("#.####");
	
	public KALETripModel() {
	}
	
	public void Initialization(String strNumRelation, String strNumEntity,
			String fnTrainingTriples, String fnValidateTriples, String fnTestingTriples) throws Exception {
		m_NumRelation = Integer.parseInt(strNumRelation);
		m_NumEntity = Integer.parseInt(strNumEntity);
		m_MatrixE_prefix = "MatrixE-k" + m_NumFactor 
				+ "-d" + decimalFormat.format(m_Delta) 
				+ "-ge" + decimalFormat.format(m_GammaE) 
				+ "-gr" + decimalFormat.format(m_GammaR);
		m_MatrixR_prefix = "MatrixR-k" + m_NumFactor 
				+ "-d" + decimalFormat.format(m_Delta) 
				+ "-ge" + decimalFormat.format(m_GammaE) + "-gr" + decimalFormat.format(m_GammaR);
		
		System.out.println("\nLoading training and validate triples");
		m_TrainingTriples = new TripleSet(m_NumEntity, m_NumRelation);
		m_ValidateTriples = new TripleSet(m_NumEntity, m_NumRelation); 
		m_Triples = new TripleSet();
		m_TrainingTriples.load(fnTrainingTriples);
		m_ValidateTriples.subload(fnValidateTriples);
		m_Triples.loadStr(fnTrainingTriples);
		m_Triples.loadStr(fnValidateTriples);
		m_Triples.loadStr(fnTestingTriples);
		System.out.println("Success.");
		
		System.out.println("\nRandomizing initial matrix E and matrix R");
		m_Entity_Factor_MatrixE = new Matrix(m_NumEntity, m_NumFactor);
		m_Entity_Factor_MatrixE.setToRandom();
		m_Entity_Factor_MatrixE.normalizeByRow();
		m_Relation_Factor_MatrixR = new Matrix(m_NumRelation, m_NumFactor);
		m_Relation_Factor_MatrixR.setToRandom();
		m_Relation_Factor_MatrixR.normalizeByRow();
		System.out.println("Success.");
		
		System.out.println("\nInitializing gradients of matrix E and matrix R");
		m_MatrixEGradient = new Matrix(m_NumEntity, m_NumFactor);
		m_MatrixRGradient = new Matrix(m_NumRelation, m_NumFactor);
		System.out.println("Success.");
	}
	
	public void TransE_Learn() throws Exception {
		HashMap<Integer, ArrayList<Triple>> lstPosTriples = new HashMap<Integer, ArrayList<Triple>>();
		HashMap<Integer, ArrayList<Triple>> lstHeadNegTriples = new HashMap<Integer, ArrayList<Triple>>();
		HashMap<Integer, ArrayList<Triple>> lstTailNegTriples = new HashMap<Integer, ArrayList<Triple>>();
		
		String PATHLOG = "log-k" + m_NumFactor 
				+ "-d" + decimalFormat.format(m_Delta) 
				+ "-ge" + decimalFormat.format(m_GammaE) 
				+ "-gr" + decimalFormat.format(m_GammaR);
		
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(PATHLOG), "UTF-8"));
		
		
		int iIter = 0;
		writer.write("Complete iteration #" + iIter + ":\n");
		System.out.println("Complete iteration #" + iIter + ":");
		MetricMonitor first_metrics = new MetricMonitor(
				m_ValidateTriples,
				m_Triples.tripleSet(),
				m_Entity_Factor_MatrixE,
				m_Relation_Factor_MatrixR);
		first_metrics.calculateMetrics();
		double dCurrentHits = first_metrics.dHits;
		double dCurrentMRR = first_metrics.dMRR;
		writer.write("------Current MRR:"+ dCurrentMRR + "\tCurrent Hits@10:" + dCurrentHits + "\n");
		System.out.print("\n");
		double dBestHits = first_metrics.dHits;
		double dBestMRR = first_metrics.dMRR;
		int iBestIter = 0;
		
		long startTime = System.currentTimeMillis();
		while (iIter < m_NumIteration) {
			m_TrainingTriples.randomShuffle();
			for (int iIndex = 0; iIndex < m_TrainingTriples.triples(); iIndex++) {
				Triple PosTriple = m_TrainingTriples.get(iIndex);
				NegativeTripleGeneration negTripGen = new NegativeTripleGeneration(
						PosTriple, m_NumEntity, m_NumRelation);
				Triple headNegTriple = negTripGen.generateHeadNegTriple();
				Triple tailNegTriple = negTripGen.generateTailNegTriple();
				
				int iID = iIndex % m_NumMiniBatch;
				if (!lstPosTriples.containsKey(iID)) {
					ArrayList<Triple> tmpPosLst = new ArrayList<Triple>();
					ArrayList<Triple> tmpHeadNegLst = new ArrayList<Triple>();
					ArrayList<Triple> tmpTailNegLst = new ArrayList<Triple>();
					tmpPosLst.add(PosTriple);
					tmpHeadNegLst.add(headNegTriple);
					tmpTailNegLst.add(tailNegTriple);
					lstPosTriples.put(iID, tmpPosLst);
					lstHeadNegTriples.put(iID, tmpHeadNegLst);
					lstTailNegTriples.put(iID, tmpTailNegLst);
				} else {
					lstPosTriples.get(iID).add(PosTriple);
					lstHeadNegTriples.get(iID).add(headNegTriple);
					lstTailNegTriples.get(iID).add(tailNegTriple);
				}
			}
			
			double m_BatchSize= m_TrainingTriples.triples()/(double)m_NumMiniBatch;

			for (int iID = 0; iID < m_NumMiniBatch; iID++) {
				StochasticUpdate stochasticUpdate = new StochasticUpdate(
						lstPosTriples.get(iID),
						lstHeadNegTriples.get(iID),
						lstTailNegTriples.get(iID),
						m_Entity_Factor_MatrixE,
						m_Relation_Factor_MatrixR,
						m_MatrixEGradient,
						m_MatrixRGradient,
						m_GammaE ,
						m_GammaR/m_BatchSize,						
						m_Delta
						);
				stochasticUpdate.stochasticIteration();
			}
			lstPosTriples = new HashMap<Integer, ArrayList<Triple>>();
			lstHeadNegTriples = new HashMap<Integer, ArrayList<Triple>>();
			lstTailNegTriples = new HashMap<Integer, ArrayList<Triple>>();
			
			iIter++;
		
			if (iIter % m_OutputIterSkip == 0) {
				System.out.println("Complete iteration #" + iIter + ":");
				writer.write("Complete iteration #" + iIter + ":\n");
				MetricMonitor metric = new MetricMonitor(
						m_ValidateTriples,
						m_Triples.tripleSet(),
						m_Entity_Factor_MatrixE,
						m_Relation_Factor_MatrixR);
				metric.calculateMetrics();
				dCurrentHits = metric.dHits;
				dCurrentMRR = metric.dMRR;
				writer.write("------Current MRR:"+ dCurrentMRR + "\tCurrent Hits@10:" + dCurrentHits + "\n");
				if (dCurrentMRR > dBestMRR) {
					m_Relation_Factor_MatrixR.output(m_MatrixR_prefix + ".best");
					m_Entity_Factor_MatrixE.output(m_MatrixE_prefix + ".best");
					dBestHits = dCurrentHits;
					dBestMRR = dCurrentMRR;
					iBestIter = iIter;
				}
				writer.write("------Best iteration #" + iBestIter + "\t" + dBestMRR + "\t" + dBestHits+"\n");
				writer.flush();
				System.out.println("------\tBest iteration #" + iBestIter + "\tBest MRR:" + dBestMRR + "Best \tHits@10:" + dBestHits);
				writer.flush();
			}
		}
		long endTime = System.currentTimeMillis();
		System.out.println("All running time:" + (endTime-startTime)+"ms");
		writer.close();
	}
}
