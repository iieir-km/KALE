package kale.trip;

import basic.util.Arguments;

public class Program {
	public static void main(String[] args) throws Exception {
		Arguments cmmdArg = new Arguments(args);
		KALETripModel transe = new KALETripModel();
		String fnTrainingTriples = "";
		String fnValidateTriples = "";
		String fnTestingTriples = "";
		String strNumRelation = "";
		String strNumEntity = "";
		
		try {
			fnTrainingTriples = cmmdArg.getValue("train");
			if (fnTrainingTriples == null || fnTrainingTriples.equals("")) {
				Usage();
				return;
			}
			fnValidateTriples = cmmdArg.getValue("valid");
			if (fnValidateTriples == null || fnValidateTriples.equals("")) {
				Usage();
				return;
			}
			fnTestingTriples = cmmdArg.getValue("test");
			if (fnTestingTriples == null || fnTestingTriples.equals("")) {
				Usage();
				return;
			}
			strNumRelation = cmmdArg.getValue("m");
			if (strNumRelation == null || strNumRelation.equals("")) {
				Usage();
				return;
			}
			strNumEntity = cmmdArg.getValue("n");
			if (strNumEntity == null || strNumEntity.equals("")) {
				Usage();
				return;
			}
			
			if (cmmdArg.getValue("k") != null && !cmmdArg.getValue("k").equals("")) {
				transe.m_NumFactor = Integer.parseInt(cmmdArg.getValue("k"));
			}
			if (cmmdArg.getValue("d") != null && !cmmdArg.getValue("d").equals("")) {
				transe.m_Delta = Double.parseDouble(cmmdArg.getValue("d"));
			}
			if (cmmdArg.getValue("ge") != null && !cmmdArg.getValue("ge").equals("")) {
				transe.m_GammaE = Double.parseDouble(cmmdArg.getValue("ge"));
			}
			if (cmmdArg.getValue("gr") != null && !cmmdArg.getValue("gr").equals("")) {
				transe.m_GammaR = Double.parseDouble(cmmdArg.getValue("gr"));
			}
			if (cmmdArg.getValue("#") != null && !cmmdArg.getValue("#").equals("")) {
				transe.m_NumIteration = Integer.parseInt(cmmdArg.getValue("#"));
			}
			if (cmmdArg.getValue("skip") != null && !cmmdArg.getValue("skip").equals("")) {
				transe.m_OutputIterSkip = Integer.parseInt(cmmdArg.getValue("skip"));
			}

			long startTime = System.currentTimeMillis();
			transe.Initialization(strNumRelation, strNumEntity, fnTrainingTriples, fnValidateTriples, fnTestingTriples);
			
			System.out.println("\nStart learning TransE-linear model (triples only)");
			transe.TransE_Learn();
			System.out.println("Success.");
			long endTime = System.currentTimeMillis();
			System.out.println("run time:" + (endTime-startTime)+"ms");
		} catch (Exception e) {
			e.printStackTrace();
			Usage();
			return;
		}
	}
	
	static void Usage() {
		System.out.println(
				"Usagelala: java TransE -t training_triples -v validate_triples -m number_of_relations -n number_of_entities [options]\n\n"
				+

				"Options: \n"
				+ "   -k        -> number of latent factors (default 20)\n"
				+ "   -d        -> value of the margin (default 0.1)\n"
				+ "   -ge       -> learning rate of matrix E (default 0.01)\n"
				+ "   -gr       -> learning rate of tensor R (default 0.01)\n"
				+ "   -#        -> number of iterations (default 1000)\n"
				+ "   -skip     -> number of skipped iterations (default 50)\n"
				);
	}
}
