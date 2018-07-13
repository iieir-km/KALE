package basic.dataProcess;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import basic.util.StringSplitter;

public class GroundAllRules {
	public HashMap<String, Integer> MapRelationID = null;
	public HashMap<Integer, ArrayList<Integer>> LstRuleTypeI = null;
	public HashMap<Integer, ArrayList<Integer>> LstRuleTypeII = null;
	public HashMap<String, ArrayList<Integer>> LstRuleTypeIII = null;
	public HashMap<Integer, ArrayList<String>> LstTrainingTriples = null;
	public HashMap<Integer, HashMap<Integer,HashMap<Integer,Boolean>>> LstInferredTriples = null;
	public HashMap<String, Boolean> TrainingTriples_list= null;
	
	public void GroundRuleGeneration(
			int iEntities,
			String fnRelationIDMap,
			String fnRules,
			String fnTrainingTriples,
			String fnOutput) throws Exception {
		readData(fnRelationIDMap, fnRules, fnTrainingTriples);
		groundRule(iEntities,fnOutput);
	}
	
	public void readData(String fnRelationIDMap,
			String fnRules,
			String fnTrainingTriples) throws Exception {
		MapRelationID = new HashMap<String, Integer>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(fnRelationIDMap), "UTF-8"));
		String line = "";
		while ((line = reader.readLine()) != null) {
//			System.out.println(line);
			String[] tokens = line.split("\t");
			int iRelationID = Integer.parseInt(tokens[0]);
			String strRelation = tokens[1];
//			System.out.println(iRelationID+strRelation);
			MapRelationID.put(strRelation, iRelationID);
		}
		reader.close();
		
		System.out.println("Start to load rules......");
		
		LstRuleTypeI = new HashMap<Integer, ArrayList<Integer>>();
		LstRuleTypeII = new HashMap<Integer, ArrayList<Integer>>();
		LstRuleTypeIII = new HashMap<String, ArrayList<Integer>>();
		if (!fnRules.equals("")) {
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(fnRules), "UTF-8"));
		     int count =0;
			while ((line = reader.readLine()) != null) {
				if (line.split("&&").length == 1 && line.endsWith("(x,y)")){
					String[] tokens = StringSplitter.RemoveEmptyEntries(StringSplitter
						.split("=> ", line));
					int iFstRelation = MapRelationID.get(tokens[0]);
					int iSndRelation = MapRelationID.get(tokens[1]);
					if(!LstRuleTypeI.containsKey(iFstRelation)){
						ArrayList<Integer> lstSnd=new ArrayList<Integer>();
						lstSnd.add(iSndRelation);
						LstRuleTypeI.put(iFstRelation, lstSnd);
					}
					else{
						LstRuleTypeI.get(iFstRelation).add(iSndRelation);
					}
				}
				else if(line.split("&&").length == 1 && line.endsWith("(y,x)")){
					String[] tokens = StringSplitter.RemoveEmptyEntries(StringSplitter
							.split("=> ", line));
					int iFstRelation = MapRelationID.get(tokens[0]);
					int iSndRelation = MapRelationID.get(tokens[1]);
					if(!LstRuleTypeII.containsKey(iFstRelation)){
						ArrayList<Integer> lstSnd=new ArrayList<Integer>();
						lstSnd.add(iSndRelation);
						LstRuleTypeII.put(iFstRelation, lstSnd);
					}
					else{
						LstRuleTypeII.get(iFstRelation).add(iSndRelation);
					}
				}
				else{
				    String[] tokens = StringSplitter.RemoveEmptyEntries(StringSplitter
						.split("=>& ", line));
				    int iFstRelation = MapRelationID.get(tokens[0]);
				    int iSndRelation = MapRelationID.get(tokens[1]);
				    int iTrdRelation = MapRelationID.get(tokens[2]);
					String  sFstCompon = iFstRelation+"&&"+iSndRelation;
					if(!LstRuleTypeIII.containsKey(sFstCompon)){
						ArrayList<Integer> lstSnd=new ArrayList<Integer>();
						lstSnd.add(iTrdRelation);
						LstRuleTypeIII.put(sFstCompon, lstSnd);
					}
					else{
						LstRuleTypeIII.get(sFstCompon).add(iTrdRelation);
					}
				}
				
		
			}
			reader.close();
		}

//		System.out.println(LstRuleTypeIII.size());		
		LstTrainingTriples = new HashMap<Integer, ArrayList<String>>();
		TrainingTriples_list = new HashMap<String,Boolean>();
		reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(fnTrainingTriples), "UTF-8"));
		while((line = reader.readLine()) != null) {
			String[] tokens = line.split("\t");
			int iRelationID = Integer.parseInt(tokens[1]);
			String strValue = tokens[0] + "_" + tokens[2];
			TrainingTriples_list.put(line,true);
			
			if (!LstTrainingTriples.containsKey(iRelationID)) {
				ArrayList<String> tmpLst = new ArrayList<String>();
				tmpLst.add(strValue);
				LstTrainingTriples.put(iRelationID, tmpLst);
			} else {
				LstTrainingTriples.get(iRelationID).add(strValue);
			}
		}
		reader.close();
		
		LstInferredTriples = new HashMap<Integer, HashMap<Integer,HashMap<Integer,Boolean>>>();
		reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(fnTrainingTriples), "UTF-8"));
		while((line = reader.readLine()) != null) {
			String[] tokens = line.split("\t");
			int iRelationID = Integer.parseInt(tokens[1]);
			int iSubjectID = Integer.parseInt(tokens[0]);
			int iObjectID = Integer.parseInt(tokens[2]);
			if (!LstInferredTriples.containsKey(iRelationID)) {
				HashMap<Integer,HashMap<Integer,Boolean>> tmpMap = new HashMap<Integer,HashMap<Integer,Boolean>>();
				if(!tmpMap.containsKey(iSubjectID)){
					HashMap<Integer,Boolean> tmpMap_in = new HashMap<Integer,Boolean>();
					tmpMap_in.put(iObjectID,true);
					tmpMap.put(iSubjectID, tmpMap_in);
				}
				else{
					tmpMap.get(iSubjectID).put(iObjectID,true);
					}
				LstInferredTriples.put(iRelationID, tmpMap);
			} else {
				HashMap<Integer,HashMap<Integer,Boolean>> tmpMap = LstInferredTriples.get(iRelationID);
				if(!tmpMap.containsKey(iSubjectID)){
					HashMap<Integer,Boolean> tmpMap_in = new HashMap<Integer,Boolean>();
					tmpMap_in.put(iObjectID,true);
					tmpMap.put(iSubjectID, tmpMap_in);
				}
				else{
					tmpMap.get(iSubjectID).put(iObjectID,true);
				}
			}
		}
		reader.close();
		System.out.println("Success!");
	}
	
	public void groundRule(int iEntities,String fnOutput) throws Exception {
		System.out.println("Start to propositionalize rules......");
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(fnOutput), "UTF-8"));
		
		HashMap<String, Boolean> tmpLst = new HashMap<String, Boolean>();
		
		int count=0;
		
		Iterator<Integer> lstRelations = LstRuleTypeI.keySet().iterator();
		while (lstRelations.hasNext()) {
			int iFstRelation = lstRelations.next();
			ArrayList<Integer> lstSndRelation = LstRuleTypeI.get(iFstRelation);
			
			int iSize = LstTrainingTriples.get(iFstRelation).size();
			for (int iIndex = 0; iIndex < iSize; iIndex++) {
				String strValue = LstTrainingTriples.get(iFstRelation).get(iIndex);
//				System.out.println(strValue);
				int iSubjectID = Integer.parseInt(strValue.split("_")[0]);
				int iObjectID = Integer.parseInt(strValue.split("_")[1]);
				for(int iSndRelation: lstSndRelation){
					String strKey = "(" + iSubjectID + "\t" + iFstRelation + "\t" + iObjectID + ")\t"
							+ "(" + iSubjectID + "\t" + iSndRelation + "\t" + iObjectID + ")";
					String strCons = iSubjectID + "\t" + iSndRelation + "\t" + iObjectID;
					if (!tmpLst.containsKey(strKey)) {
						writer.write(strKey + "\n");
						tmpLst.put(strKey, true);
					}
				}
			}
			for(int iSndRelation: lstSndRelation){
				iSize = LstTrainingTriples.get(iSndRelation).size();
				for (int iIndex = 0; iIndex < iSize; iIndex++) {
					String strValue = LstTrainingTriples.get(iSndRelation).get(iIndex);
					int iSubjectID = Integer.parseInt(strValue.split("_")[0]);
					int iObjectID = Integer.parseInt(strValue.split("_")[1]);
					String strKey = "(" + iSubjectID + "\t" + iFstRelation + "\t" + iObjectID + ")\t"
							+ "(" + iSubjectID + "\t" + iSndRelation + "\t" + iObjectID + ")";
					String strAnte = iSubjectID + "\t" + iFstRelation + "\t" + iObjectID;
					if (!tmpLst.containsKey(strKey)&&!TrainingTriples_list.containsKey(strAnte)) {
						writer.write(strKey + "\n");
						tmpLst.put(strKey, true);
					}
				}
			}
			writer.flush();
		}
		
		lstRelations = LstRuleTypeII.keySet().iterator();
		while (lstRelations.hasNext()) {
			int iFstRelation = lstRelations.next();
			ArrayList<Integer> lstSndRelation = LstRuleTypeII.get(iFstRelation);
//			Integer iSndRelation = LstRuleTypeII.get(iFstRelation);		
			int iSize = LstTrainingTriples.get(iFstRelation).size();
			for (int iIndex = 0; iIndex < iSize; iIndex++) {
				String strValue = LstTrainingTriples.get(iFstRelation).get(iIndex);
				int iSubjectID = Integer.parseInt(strValue.split("_")[0]);
				int iObjectID = Integer.parseInt(strValue.split("_")[1]);
				for(int iSndRelation: lstSndRelation){
					String strKey = "(" + iSubjectID + "\t" + iFstRelation + "\t" + iObjectID + ")\t"
						+ "(" + iObjectID + "\t" + iSndRelation + "\t" + iSubjectID + ")";
					String strCons = iObjectID + "\t" + iSndRelation + "\t" + iSubjectID;
					if (!tmpLst.containsKey(strKey)) {
						writer.write(strKey + "\n");
						tmpLst.put(strKey, true);
					}

				}
			}
			for(int iSndRelation: lstSndRelation){
				iSize = LstTrainingTriples.get(iSndRelation).size();
				for (int iIndex = 0; iIndex < iSize; iIndex++) {
					String strValue = LstTrainingTriples.get(iSndRelation).get(iIndex);
					int iSubjectID = Integer.parseInt(strValue.split("_")[1]);
					int iObjectID = Integer.parseInt(strValue.split("_")[0]);
					String strKey = "(" + iSubjectID + "\t" + iFstRelation + "\t" + iObjectID + ")\t"
							+ "(" + iObjectID + "\t" + iSndRelation + "\t" + iSubjectID + ")";
					String strAnte = iSubjectID + "\t" + iFstRelation + "\t" + iObjectID;
					if (!tmpLst.containsKey(strKey)&&!TrainingTriples_list.containsKey(strAnte)) {
						writer.write( strKey + "\n");
						tmpLst.put(strKey, true);
					}
				}
			}
		writer.flush();
		}
		tmpLst.clear();
		
		Iterator<String> lstFstCompon = LstRuleTypeIII.keySet().iterator();
		while (lstFstCompon.hasNext()) {
			String sFstCompon = lstFstCompon.next();
			ArrayList<Integer> lstTrdRelation = LstRuleTypeIII.get(sFstCompon);
			int iFstRelation = Integer.parseInt(sFstCompon.split("&&")[0]);
			int iSndRelation = Integer.parseInt(sFstCompon.split("&&")[1]);
			HashMap<Integer,HashMap<Integer,Boolean>> mapFstRel = LstInferredTriples.get(iFstRelation);
			HashMap<Integer,HashMap<Integer,Boolean>> mapSndRel = LstInferredTriples.get(iSndRelation);
			Iterator<Integer> lstSubjectID = mapFstRel.keySet().iterator();
			while (lstSubjectID.hasNext()) {
				    int iSubjectID = lstSubjectID.next();
					ArrayList<Integer> lstMedianID = new ArrayList<Integer>(mapFstRel.get(iSubjectID).keySet());
					int iFstSize = lstMedianID.size();
					for (int iFstIndex = 0; iFstIndex < iFstSize; iFstIndex++) {
						int iMedianID = lstMedianID.get(iFstIndex);
						if(mapSndRel.containsKey(iMedianID)){
							ArrayList<Integer> lstObjectID = new ArrayList<Integer>(mapSndRel.get(iMedianID).keySet());
							int iSize2 = lstObjectID.size();
							for (int iSndIndex = 0; iSndIndex < iSize2; iSndIndex++){
								int iObjectID = lstObjectID.get(iSndIndex);
								for(int iTrdRelation: lstTrdRelation){
									String infer=iSubjectID + "\t" + iTrdRelation + "\t" + iObjectID;
									String strKey = "(" + iSubjectID + "\t" + iFstRelation + "\t" + iMedianID + ")\t"
											+"(" + iMedianID + "\t" + iSndRelation + "\t" + iObjectID + ")\t"
											+ "(" + iSubjectID + "\t" + iTrdRelation + "\t" + iObjectID + ")";
									if (!tmpLst.containsKey(strKey)) {
										writer.write(strKey + "\n");
										tmpLst.put(strKey, true);
									}
								}
							}
						}
					}
			}

			for(int iTrdRelation: lstTrdRelation){
				HashMap<Integer,HashMap<Integer,Boolean>> mapTrdRel = LstInferredTriples.get(iTrdRelation);
				lstSubjectID = mapTrdRel.keySet().iterator();
				while (lstSubjectID.hasNext()) {
					int iSubjectID = lstSubjectID.next();
					ArrayList<Integer> lstObjectID = new ArrayList<Integer>(mapTrdRel.get(iSubjectID).keySet());
					int iTrdSize = lstObjectID.size();
					for (int iTrdIndex = 0; iTrdIndex < iTrdSize; iTrdIndex++) {
						int iObjectID = lstObjectID.get(iTrdIndex);					
						if(mapFstRel.containsKey(iSubjectID)){
							ArrayList<Integer> lstMedianID = new ArrayList<Integer>(mapFstRel.get(iSubjectID).keySet());
							int iFstSize = lstMedianID.size();
							for (int iFstIndex = 0; iFstIndex < iFstSize; iFstIndex++) {
								int iMedianID = lstMedianID.get(iFstIndex);
								String infer=iMedianID + "\t" + iSndRelation + "\t" + iObjectID;
								String strKey = "(" + iSubjectID + "\t" + iFstRelation + "\t" + iMedianID + ")\t"
										+"(" + iMedianID + "\t" + iSndRelation + "\t" + iObjectID + ")\t"
										+ "(" + iSubjectID + "\t" + iTrdRelation + "\t" + iObjectID + ")";
								if (!tmpLst.containsKey(strKey)&&!TrainingTriples_list.containsKey(infer)) {
									writer.write(strKey + "\n");
									tmpLst.put(strKey, true);
								}
							}
						}

						
						Iterator<Integer> iterMedianID = mapSndRel.keySet().iterator();
						while (iterMedianID.hasNext()) {
							int iMedianID = iterMedianID.next();
							if(mapSndRel.get(iMedianID).containsKey(iObjectID)){
								String infer=iSubjectID + "\t" + iFstRelation + "\t" + iMedianID;
								String strKey = "(" + iSubjectID + "\t" + iFstRelation + "\t" + iMedianID + ")\t"
										+"(" + iMedianID + "\t" + iSndRelation + "\t" + iObjectID + ")\t"
										+ "(" + iSubjectID + "\t" + iTrdRelation + "\t" + iObjectID + ")";
								if (!tmpLst.containsKey(strKey)&&!TrainingTriples_list.containsKey(infer)) {
									writer.write( strKey + "\n");
									tmpLst.put(strKey, true);
								}
							}
						}
					}
				}
			}
			writer.flush();


		}
		writer.close();
		System.out.println("Success!");

	}
	
	public static void main(String[] args) throws Exception {
		int iEntities = 14951;
		// Input file:
		String fnRelationIDMap = "datasets\\wn18\\relationid.txt";
		String fnRules = "datasets\\wn18\\wn18_rule";
		String fnTrainingTriples = "datasets\\wn18\\train.txt";
		//Output file:
        String fnOutput = "datasets\\wn18\\groundings.txt";
        long startTime = System.currentTimeMillis();
        GroundAllRules generator = new GroundAllRules();
        generator.GroundRuleGeneration(iEntities,fnRelationIDMap, 
        		fnRules, fnTrainingTriples, fnOutput);
        long endTime = System.currentTimeMillis();
		System.out.println("All running time:" + (endTime-startTime)+"ms");
	}
}
