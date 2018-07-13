-----------------------------------------------------------------------
-- CODE USED FOR JOINTLY EMBEDDING KNOWLEDGE GRAPHS AND LOGICAL RULES--
-----------------------------------------------------------------------

------------------
OUTLINE:
1. Introduction
2. Preprocessing
3. Training
4. Testing
5. How to cite
6. Contact
------------------


------------------
1. INTRODUCTION:
------------------

The codes in the folder KALE/ are used for jointly embedding knowledge graphs and logical rules. 
We peovide how to run the experiments of Link Prediction task in the following.


------------------
2. PREPROCESSING
------------------

To run the experiments, you need to preprocess datasets in the folder datasets/ following the steps below:
(1) Change data form: call the program ConvertDataForm.java in the folder src/basic/dataProcess to convert the string form of original data into digital form, and get resultant files “train/valid/test.txt” in the folder datasets/
(2) Propositionalize logic rules: call the program GroundAllRules.java in folder src\basic\dataProcess to propositionalize logic rules in the folder datasets/, and get a resultant file “groundings.txt”


------------------
3. TRAINING
------------------
To train a model, you need to follow the steps below:
(1) Export KALEProgram.java in the folder src/kale/joint as runnable JAR file, for example, termed as KALE.jar
(2) Call the program KALE.jar with parameters, for example, as follows:

java -jar KALE.jar -train datasets\\wn18\\train.txt -valid datasets\\wn18\\valid.txt -test datasets\\wn18\\test.txt -rule datasets\\wn18\\groundings.txt -m 18 -n 40943 -w 0.1 -k 50 -d 0.2 -ge 0.1 -gr 0.1 -# 1000 -skip 50

You can also change the parameters when running RUGE.jar:
  - train: the path of training triples 
  - valid: the path of validate triples 
  - test: the path of testing triples 
  - rule: the path of grounded rules
  - m: number of relations 
  - n: number of entities 
  - k: embedding dimensionality (default 20)
  - d: value of the margin (default 0.1)
  - ge: initial learning rate of matrix E for AdaGrad(default 0.01)
  - gr: initial learning rate of matrix R for AdaGrad(default 0.01)
  - #: number of iterations (default 1000)
  - skip: number of skipped iterations (default 50)

The program will train a model with the input parameters, and output 5 files:
  - result.log: the specification document
  - MatrixE.best: learned entity embeddings
  - MatrixR.best: learned realtion embeddings
  
***Please note that in this new implementation version, KALE is optimized by a new mode, using SGD with AdaGrad and gradient normalization.

------------------
4. TESTING
------------------
To evaluate on the test datasets, you need call the program Eval_LinkPrediction.java in the folder src/test

You can also change the input parameters when running:
  - iEntities: number of entities
  - iRelations: number of relations
  - iFactors: embedding dimensionality
  - fnMatrixE: the file path of entity embeddings
  - fnMatrixR: the file path of realtion embeddings
  - fnTrainTriples: the file path of training data (digital form)
  - fnValidTriples: the file path of validation data (digital form)
  - fnTestTriples: the file path of testing data (digital form)

The program will evaluate on testing data, and report the metrics of MRR, MED, and Hits@N (raw and filtered setting).


------------------
5. HOW TO CITE
------------------

When using this data, one should cite the original paper:  
  @inproceedings{guo2016:KALE,  
    title     = {Jointly Embedding Knowledge Graphs and Logical Rules},  
    author    = {Shu Guo and Quan Wang and Lihong Wang and Bin Wang and Li Guo},  
    booktitle = {Proceedings of the 2016 Conference on Empirical Methods in Natural Language Processing},  
    year      = {2016},  
    page      = {192-202}<br> 
  }


------------------  
6. CONTACT
------------------

For all remarks or questions please contact Quan Wang:
wangquan (at) iie (dot) ac (dot) cn .

