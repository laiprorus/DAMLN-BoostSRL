/**
 *
 */
package edu.wisc.cs.will.Boosting.MLN;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import edu.wisc.cs.will.Boosting.Common.RunBoostedModels;
import edu.wisc.cs.will.Boosting.EM.HiddenLiteralSamples;
import edu.wisc.cs.will.Boosting.RDN.ConditionalModelPerPredicate;
import edu.wisc.cs.will.Boosting.RDN.InferBoostedRDN;
import edu.wisc.cs.will.Boosting.RDN.JointModelSampler;
import edu.wisc.cs.will.Boosting.RDN.JointRDNModel;
import edu.wisc.cs.will.Boosting.RDN.LearnBoostedRDN;
import edu.wisc.cs.will.Boosting.RDN.WILLSetup;
import edu.wisc.cs.will.Boosting.Scaling.Config;
import edu.wisc.cs.will.Boosting.Trees.ClauseBasedTree;
import edu.wisc.cs.will.Boosting.Utils.BoostingUtils;
import edu.wisc.cs.will.Boosting.Utils.CommandLineArguments;
import edu.wisc.cs.will.DataSetUtils.ComputeDatasetSize;
import edu.wisc.cs.will.FOPC.*;
import edu.wisc.cs.will.Utils.Utils;
import edu.wisc.cs.will.Utils.check_disc;
import edu.wisc.cs.will.Utils.disc;
import edu.wisc.cs.will.Utils.condor.CondorFileWriter;
import edu.wisc.cs.will.Utils.condor.CondorFileReader;
import graphdbInt.GenerateSchema;
//import edu.wisc.cs.will.test.ILP.AdviceTest;
import graphdbInt.GraphDB;

/**
 * MLN-Boost specific code for learning and inference
 * For e.g. RDN-Boost learns all the trees for one predicate at a time whereas MLN-Boost learns
 * one tree at a time for every predicate
 * Also sets the required flags for MLN-Boost.
 * @author tkhot
 *
 */
public class RunBoostedMLN extends RunBoostedModels {

    JointRDNModel fullModel = null;
    public static GraphDB gdb; //change by MD & DD
    //public static CommandLineArguments cmdGlob;//change by MD & DD

    @Override
    public void beforeLearn() {
        super.beforeLearn();
        if (cmdArgs.isLearningMLNClauses()) {
            saveTrainDomainSizes();
        }
    }

    public void learn() {
        fullModel = new JointRDNModel();
        Map<String, LearnBoostedRDN> learners = new HashMap<String, LearnBoostedRDN>();
        int minTreesInModel = Integer.MAX_VALUE;


        for (String pred : cmdArgs.getTargetPredVal()) {
            fullModel.put(pred, new ConditionalModelPerPredicate(setup));
            fullModel.get(pred).setTargetPredicate(pred);

            LearnBoostedRDN learner = new LearnBoostedRDN(cmdArgs, setup);
            learner.setTargetPredicate(pred);
            learners.put(pred, learner);
            if (cmdArgs.useCheckPointing()) {
                learner.loadCheckPointModel(fullModel.get(pred));
            }
            minTreesInModel = Math.min(fullModel.get(pred).getNumTrees(), minTreesInModel);
        }
        if (!cmdArgs.isDisableAdvice()) {
            String adviceFile = setup.getOuterLooper().getWorkingDirectory() + "/" + cmdArgs.getPriorAdvice();
            BoostingUtils.loadAdvice(setup, fullModel, adviceFile, true);
        }
        MLNInference sampler = new MLNInference(setup, fullModel);

        int iterStepSize = 1;
        if (cmdArgs.getTargetPredVal().size() == 1) {
            iterStepSize = cmdArgs.getMaxTreesVal();
        }

        if ((cmdArgs.getHiddenStrategy().equals("EM") || cmdArgs.getHiddenStrategy().equals("MAP"))
                && setup.getHiddenExamples() != null) {
            iterStepSize = 2;
        }
        if (cmdArgs.getRdnIterationStep() != -1) {
            iterStepSize = cmdArgs.getRdnIterationStep();
        }
        boolean newModel = true;
        for (int i = 0; i < cmdArgs.getMaxTreesVal(); i += iterStepSize) {
            if ((cmdArgs.getHiddenStrategy().equals("EM") || cmdArgs.getHiddenStrategy().equals("MAP"))
                    && setup.getHiddenExamples() != null && newModel) {
                long sampleStart = System.currentTimeMillis();
                JointModelSampler jtSampler = new JointModelSampler(fullModel, setup, cmdArgs, false);
                HiddenLiteralSamples sampledStates = new HiddenLiteralSamples();
                setup.addAllExamplesToFacts();
                if (i > minTreesInModel) {
                    minTreesInModel = i;
                }


                int maxSamples = 30 * ((minTreesInModel / iterStepSize) + 1);
                maxSamples = 500;
                // TODO (tvk) Get more samples but pick the 200 most likely states.
                //if (cmdArgs.getNumberOfHiddenStates() > 0 ) {
                //	maxSamples = cmdArgs.getNumberOfHiddenStates();
                //}
                if (cmdArgs.getHiddenStrategy().equals("MAP")) {
                    maxSamples = -1;
                }
                boolean returnMap = false;
                if (cmdArgs.getHiddenStrategy().equals("MAP")) {
                    returnMap = true;
                }
                jtSampler.sampleWorldStates(setup.getHiddenExamples(), sampledStates, false, maxSamples, returnMap);
//					if (cmdArgs.getHiddenStrategy().equals("MAP")) {
//						sampledStates = sampledStates.getMostLikelyState();
//						Utils.println("% Percent of true states:" + sampledStates.getWorldStates().get(0).percentOfTrues());
//					}
                if (sampledStates.getWorldStates().size() == 0) {
                    Utils.waitHere("No sampled states");
                }
                // This state won't change anymore so cache probs;
                Utils.println("Building assignment map");
                sampledStates.buildExampleToAssignMap();

                if (cmdArgs.getHiddenStrategy().equals("EM")) {
                    // Build the probabilities for each example conditioned on the assignment to all other examples
                    Utils.println("Building probability map");
                    sampledStates.buildExampleToCondProbMap(setup, fullModel);
                    if (cmdArgs.getNumberOfHiddenStates() > 0) {
                        Utils.println("Picking top K");
                        sampledStates.pickMostLikelyStates(cmdArgs.getNumberOfHiddenStates());
                    }
                }
                //double cll = BoostingUtils.computeHiddenStateCLL(sampledStates, setup.getHiddenExamples());
                //Utils.println("CLL of hidden states:" + cll);
                //Utils.println("Prob of states: " + sampledStates.toString());
                setup.setLastSampledWorlds(sampledStates);
                newModel = false;
                long sampleEnd = System.currentTimeMillis();
                Utils.println("Time to sample world state: " + Utils.convertMillisecondsToTimeSpan(sampleEnd - sampleStart));
            }
            for (String pred : cmdArgs.getTargetPredVal()) {

                if (fullModel.get(pred).getNumTrees() >= (i + iterStepSize)) {
                    continue;
                }
                int currIterStep = (i + iterStepSize) - fullModel.get(pred).getNumTrees();
                Utils.println("% Learning " + currIterStep + " trees in this iteration for " + pred);
                newModel = true;
                learners.get(pred).learnNextModel(this, sampler, fullModel.get(pred), currIterStep);
            }
        }

        for (String pred : cmdArgs.getTargetPredVal()) {
            String saveModelName = BoostingUtils.getModelFile(cmdArgs, pred, true);
            fullModel.get(pred).saveModel(saveModelName); // Do a final save since learnModel doesn't save every time (though we should make it do so at the end).
            // No need for checkpoint file anymore
            clearCheckPointFiles(saveModelName);
        }

    }

    @Override
    protected void afterLearn() {
        super.afterLearn();
        if (cmdArgs.isLearningMLNClauses()) {
            saveModelAsMLN();
        }
    }

    public void beforeInfer() {
        super.beforeInfer();
        if (cmdArgs.isLearningMLNClauses() && cmdArgs.isDomainSizeScaling()) {
            applyDomainSizeScaling();
        }
    }

    public void infer() {
        InferBoostedRDN infer = new InferBoostedRDN(cmdArgs, setup);
        //infer.runInference(fullModel, 0.5);
        infer.runInference(fullModel, cmdArgs.getThreshold()); // change by MD for threshold
    }

    private void saveModelAsMLN() {

        String mlnFile = setup.getOuterLooper().getWorkingDirectory() + "/" +
                (cmdArgs.getModelFileVal() == null ? "" : cmdArgs.getModelFileVal()) + ".mln";
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new CondorFileWriter(mlnFile));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        AllOfFOPC.printUsingAlchemyNotation = true;
        boolean oldSTD = setup.getHandler().usingStdLogicNotation();
        boolean oldAnon = setup.getHandler().underscoredAnonymousVariables;
        setup.getHandler().underscoredAnonymousVariables = false;
        setup.getHandler().prettyPrintClauses = false;
        setup.getHandler().useStdLogicNotation();

//		Set<String> modes = setup.getInnerLooper().getAlchemyModeStrings(setup.getInnerLooper().getBodyModes());
//		modes.addAll(setup.getInnerLooper().getAlchemyModeStrings(setup.getInnerLooper().getTargetModes()));
//		for (String str : modes) {
//			try {
//				writer.write(str);
//				writer.newLine();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}

        for (ConditionalModelPerPredicate model : fullModel.values()) {
            for (Entry<Clause, Double> entry : model.convertToSingleMLN().entrySet()) {
                try {
                    entry.getKey().setWeightOnSentence(entry.getValue());
                    writer.write(entry.getKey().toString());
                    writer.newLine();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        if (!oldSTD) {
            setup.getHandler().usePrologNotation();
        }
        setup.getHandler().underscoredAnonymousVariables = oldAnon;
        AllOfFOPC.printUsingAlchemyNotation = false;
        try {
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void loadModel() {
        if (fullModel == null) {
            fullModel = new JointRDNModel();
        }
        Set<String> modelPreds = cmdArgs.getLoadPredModelVal();
        if (modelPreds == null) {
            modelPreds = cmdArgs.getTargetPredVal();
        }
        for (String pred : modelPreds) {
            ConditionalModelPerPredicate rdn = null;
            if (fullModel.containsKey(pred)) {
                rdn = fullModel.get(pred);
                rdn.reparseModel(setup);
            } else {
                Utils.println("% Did not learn a model for '" + pred + "' this run.");
                // YapFile doesn't matter here.
                rdn = new ConditionalModelPerPredicate(setup);

                if (useSingleTheory(setup)) {
                    rdn.setHasSingleTheory(true);
                    rdn.setTargetPredicate(pred);
                    rdn.loadModel(LearnBoostedRDN.getWILLFile(cmdArgs.getModelDirVal(), cmdArgs.getModelFileVal(), pred), setup, cmdArgs.getMaxTreesVal());
                } else {
                    rdn.loadModel(BoostingUtils.getModelFile(cmdArgs, pred, true), setup, cmdArgs.getMaxTreesVal());
                }
                rdn.setNumTrees(cmdArgs.getMaxTreesVal());
                fullModel.put(pred, rdn);
            }
        }
        if (!cmdArgs.isDisableAdvice()) {
            String adviceFile = setup.getOuterLooper().getWorkingDirectory() + "/" + cmdArgs.getPriorAdvice();
            BoostingUtils.loadAdvice(setup, fullModel, adviceFile, true);
        }
    }

    private boolean useSingleTheory(WILLSetup setup2) {
        String lookup;
        if ((lookup = setup2.getHandler().getParameterSetting("singleTheory")) != null) {
            return Boolean.parseBoolean(lookup);
        }
        return false;
    }

    private void logScaling(String line, BufferedWriter logWriter) {
        Utils.print(line);
        try {
            logWriter.write(line);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void logScalingLine(String line, BufferedWriter logWriter) {
        Utils.println(line);
        try {
            logWriter.write(line);
            logWriter.newLine();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void saveTrainDomainSizes() {
        // get train domain sizes
        ComputeDatasetSize dataSize = new ComputeDatasetSize();
        dataSize.loadDirectory(cmdArgs.getTrainDirVal());
        Map<String, Integer> domainSizes = dataSize.getDomainSizes();

        // open file stream
        String dsFile = cmdArgs.getModelDirVal() + Config.domainSizesFile;
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new CondorFileWriter(dsFile));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // write domain sizes
        for (Map.Entry<String, Integer> entry : domainSizes.entrySet()) {
            String domainName = entry.getKey();
            int domainSize = entry.getValue();

            try {
                writer.write(domainName + ": " + domainSize);
                writer.newLine();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        // close file stream
        try {
            writer.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Utils.println("");
        Utils.println("% 'models/" + Config.domainSizesFile + "' saved.");
    }

    private void applyDomainSizeScaling() {
        // open log file stream
        String dsFile = cmdArgs.getTestDirVal() + "scaling_log.txt";
        BufferedWriter logWriter = null;
        try {
            logWriter = new BufferedWriter(new CondorFileWriter(dsFile));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Map<String, Double> domainSizeScales = getDomainScales(logWriter);

        applyScalingFactors(domainSizeScales, logWriter);

        // close log file stream
        try {
            logWriter.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    ;

    private Map<String, Double> getDomainScales(BufferedWriter logWriter) {
        logScalingLine("", logWriter);
        logScalingLine("Loading domain sizes... ", logWriter);

        // get test domain sizes
        ComputeDatasetSize dataSize = new ComputeDatasetSize();
        dataSize.loadDirectory(this.cmdArgs.getTestDirVal());
        Map<String, Integer> testDomainSizes = dataSize.getDomainSizes();

        // get train domain sizes
        Map<String, Integer> trainDomainSizes = new HashMap();

        // open file stream
        String dsFile = cmdArgs.getModelDirVal() + Config.domainSizesFile;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new CondorFileReader(dsFile));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // read train domain sizes
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(": ");
                String domainName = tokens[0];
                int domainSize = Integer.parseInt(tokens[1]);
                trainDomainSizes.put(domainName, domainSize);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // close file stream
        try {
            reader.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // calculate domain size scales (test/train)
        Map<String, Double> domainSizeScales = new HashMap();
        for (Entry<String, Integer> entry : testDomainSizes.entrySet()) {
            String domainName = entry.getKey();
            int testDomainSize = 1;
            if (testDomainSizes.containsKey(domainName)) testDomainSize = testDomainSizes.get(domainName);
            int trainDomainSize = 1;
            if (trainDomainSizes.containsKey(domainName)) trainDomainSize = trainDomainSizes.get(domainName);
            domainSizeScales.put(domainName, ((double) testDomainSize) / trainDomainSize);
        }

        logScalingLine("Domain sizes loaded!", logWriter);

        logScalingLine("", logWriter);
        logScalingLine("Train domain sizes: ", logWriter);
        for (Entry<String, Integer> entry : trainDomainSizes.entrySet()) {
            logScalingLine(entry.getKey() + ": " + entry.getValue(), logWriter);
        }

        logScalingLine("", logWriter);
        logScalingLine("Test domain sizes: ", logWriter);
        for (Entry<String, Integer> entry : testDomainSizes.entrySet()) {
            logScalingLine(entry.getKey() + ": " + entry.getValue(), logWriter);
        }

        logScalingLine("", logWriter);
        logScalingLine("Domain size scales: ", logWriter);
        for (Entry<String, Double> entry : domainSizeScales.entrySet()) {
            logScalingLine(entry.getKey() + ": " + entry.getValue(), logWriter);
        }

        return domainSizeScales;
    }

    private void applyScalingFactors(Map<String, Double> domainSizeScales, BufferedWriter logWriter) {

        logScalingLine("", logWriter);
        logScalingLine("Scaling weights...", logWriter);

        for (ConditionalModelPerPredicate model : fullModel.values()) {
            // model (per target predicate)

            logScalingLine("", logWriter);
            logScalingLine("Target: " + model.getTargetPredicate(), logWriter);

            for (int treeIndex = 0; treeIndex < model.getNumTrees(); treeIndex++) {
                // tree
                logScalingLine("", logWriter);
                logScalingLine("Tree #" + treeIndex, logWriter);

                ClauseBasedTree tree = model.getTree(0, treeIndex);
                for (Clause clause : tree.getRegressionClauses()) {
                    // clause

                    // get Head typed variables
                    Map<String, String> headVariables = new HashMap<String, String>();

                    Literal headLiteral = clause.getPosLiteral(0);
                    PredicateSpec headPredicateSpec = headLiteral.getPredicateName().getTypeOnlyList().get(0);

                    List<Term> headArguments = headLiteral.getArguments();
                    List<Term> headSignatures = headPredicateSpec.getSignature();
                    List<TypeSpec> headTypes = headPredicateSpec.getTypeSpecList();

                    for (int signatureIndex = 0; signatureIndex < headSignatures.size(); signatureIndex++) {
                        Term signature = headSignatures.get(signatureIndex);
                        if (signature instanceof StringConstant) {
                            String signatureName = ((StringConstant) signature).getName();
                            if (signatureName.toLowerCase().equals("const")) {
                                Term variableTerm = headArguments.get(signatureIndex);
                                String type = headTypes.get(signatureIndex).getType().typeName;
                                if (variableTerm instanceof Variable) {
                                    String variableName = ((Variable) variableTerm).getName();
                                    // add typed variable and numerate anonymous variables
                                    headVariables.put(variableName.equals("_") ? "_" + signatureIndex : variableName, type);
                                }
                            }
                        }
                    }

                    // get Body typed variables
                    Map<String, String> bodyVariables = new HashMap<String, String>();

                    List<Literal> bodyLiterals = clause.getNegativeLiterals();
                    for (Literal bodyLiteral : bodyLiterals) {
                        PredicateSpec bodyPredicateSpec = bodyLiteral.getPredicateName().getTypeOnlyList().get(0);

                        List<Term> bodyArguments = bodyLiteral.getArguments();
                        List<Term> bodySignatures = bodyPredicateSpec.getSignature();
                        List<TypeSpec> bodyTypes = bodyPredicateSpec.getTypeSpecList();

                        for (int signatureIndex = 0; signatureIndex < bodySignatures.size(); signatureIndex++) {
                            Term signature = bodySignatures.get(signatureIndex);
                            if (signature instanceof StringConstant) {
                                String signatureName = ((StringConstant) signature).getName();
                                if (signatureName.toLowerCase().equals("const")) {
                                    Term variableTerm = bodyArguments.get(signatureIndex);
                                    String type = bodyTypes.get(signatureIndex).getType().typeName;
                                    if (variableTerm instanceof Variable) {
                                        String variableName = ((Variable) variableTerm).getName();
                                        // add typed variable and numerate anonymous variables
                                        bodyVariables.put(variableName.equals("_") ? "_" + signatureIndex : variableName, type);
                                    }
                                }
                            }
                        }
                    }

                    // calculate scaling factor
                    Double scalingFactorProduct = (double) 1;

                    logScalingLine("", logWriter);
                    logScalingLine("Clause: " + clause.toString(), logWriter);
                    logScaling("Connection variables: ", logWriter);
                    for (Entry bodyVariable : bodyVariables.entrySet()) {
                        String name = (String) bodyVariable.getKey();
                        String type = (String) bodyVariable.getValue();
                        double scalingFactor = domainSizeScales.get(type);
                        // apply scaling if variable is ...
                        // anonymous variable
                        if (name.startsWith("_")) {
                            logScaling(name + " (" + type + "), ", logWriter);
                            scalingFactorProduct *= scalingFactor;
                            continue;
                        }
                        // not in Head predicate
                        if (!headVariables.containsKey(name)) {
                            logScaling(name + " (" + type + "), ", logWriter);
                            scalingFactorProduct *= scalingFactor;
                            continue;
                        }
                    }
                    logScalingLine("", logWriter);

                    // scale weight
                    Term weight = headLiteral.getArgument(headLiteral.numberArgs() - 1);
                    logScaling("Scaling factor: " + scalingFactorProduct + ", Weight: " + weight.toString(), logWriter);
                    if (weight instanceof NumericConstant) {
                        ((NumericConstant) weight).value = ((NumericConstant) weight).value.doubleValue() / scalingFactorProduct;
                    }
                    logScalingLine(", Scaled weight: " + weight.toString(), logWriter);

                }
            }
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        args = Utils.chopCommentFromArgs(args);
        boolean disc_flag = false;

        CommandLineArguments cmd = RunBoostedModels.parseArgs(args);
        //cmdGlob = cmd;//change MD & DD
        if (cmd == null) {
            Utils.error(CommandLineArguments.getUsageString());
        }
        disc discObj = new disc();
        check_disc flagObj = new check_disc();

        if ((cmd.getTrainDirVal() != null)) {
            try {
                disc_flag = flagObj.checkflagvalues(cmd.getTrainDirVal());
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            /*Updates the names of the training and Test file based on discretization is needed or not*/
            cmd.update_file_name(disc_flag);
        } else if ((cmd.getTestDirVal() != null)) {
            try {
                System.out.println("cmd.getTestDirVal()" + cmd.getTestDirVal());
                disc_flag = flagObj.checkflagvalues(cmd.getTestDirVal());

                /*Updates the names of the training and Test file based on discretization is needed or not*/
                cmd.update_file_name(disc_flag);
//			System.out.println("Hellooooooooooooooooooooo"+cmd.get_filename());

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try {
            if (cmd.getTrainDirVal() != null) {
//				File f = new File(cmd.getTrainDirVal().replace("/","\\"+cmd.trainDir+"_facts_disc.txt"));
                File f = new File(cmd.getTrainDirVal() + "\\" + cmd.trainDir + "_facts_disc.txt");
                if (f.exists()) {
                    f.delete();
                }

                if (disc_flag == true) {
                    discObj.Discretization(cmd.getTrainDirVal());
                }
            }
            if (cmd.getTestDirVal() != null) {
                File f = new File(cmd.getTestDirVal() + "\\" + cmd.testDir + "_facts_disc.txt");
                if (f.exists()) {
                    f.delete();
                }
                if (disc_flag == true) {
                    discObj.Discretization(cmd.getTestDirVal());
                }

            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try //change MD & DD
        {
            if (cmd.isLearnVal()) {
                GenerateSchema.generateSchema(cmd.getTrainDirVal(), "/train_bk.txt");
                if (disc_flag == true) {
                    gdb = new GraphDB(cmd.getTrainDirVal() + "/train_facts_disc.txt", cmd.getTrainDirVal() + "/schema.db", "train", true);
                } else {
                    gdb = new GraphDB(cmd.getTrainDirVal() + "/train_facts.txt", cmd.getTrainDirVal() + "/schema.db", "train", true);
                }
            } else if (cmd.isInferVal()) {
                GenerateSchema.generateSchema(cmd.getTestDirVal(), "/test_bk.txt");
                if (disc_flag == true) {
                    gdb = new GraphDB(cmd.getTestDirVal() + "/test_facts_disc.txt", cmd.getTestDirVal() + "/schema.db", "test", true);
                } else {
                    gdb = new GraphDB(cmd.getTestDirVal() + "/test_facts.txt", cmd.getTestDirVal() + "/schema.db", "test", true);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        RunBoostedModels runClass = null;
        runClass = new RunBoostedMLN();
        if (!cmd.isLearnMLN()) {
            Utils.waitHere("Set \"-mln\"  in cmdline arguments to ensure that we intend to learn MLNs. Will now learn an MLN.");
        }
        cmd.setLearnMLN(true);
        runClass.setCmdArgs(cmd);
        runClass.runJob();
    }
}

