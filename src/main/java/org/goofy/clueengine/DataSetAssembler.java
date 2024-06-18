package org.goofy.clueengine;

import org.neuroph.core.data.DataSet;
import org.neuroph.core.data.DataSetRow;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.IntStream;

public class DataSetAssembler {
    public static DataSet clueDataSet = new DataSet(5, 1);
    public static final HashMap<NNCalculations.Guess, Double[]> guessChances = new HashMap<>();
    private static final String clueDataSetFilePath = "C:\\Users\\roddy\\IdeaProjects\\clueengine\\src\\main\\datasets\\clueDataSet.csv";

    private DataSetAssembler() {
        Arrays.fill(NNCalculations.getInstance().cardChances, 18.0/21.0);
    }

    public static DataSet updateClueDataSet(int[] finalCards) {
        int i;
        for (NNCalculations.Guess guess : guessChances.keySet()) {
            i = 0;
            for (int card : guess.cards()) {
                DataSetRow newRow = new DataSetRow();
                int[] relevants = guess.getRelevantTo(i);
                newRow.setInput(new double[] {guessChances.get(guess)[i], relevants[0],relevants[1],relevants[2],relevants[3]});
                newRow.setDesiredOutput(
                        new double[] {(IntStream.of(finalCards).anyMatch(x -> x==card)) ? 1 : 0}
                );
                clueDataSet.add(newRow);
                }
        }
        saveClueDataSet();
        return clueDataSet;
    }

    public static void saveClueDataSet() {
        clueDataSet.save(clueDataSetFilePath);
    }

    public static void loadClueDataSet() {
        clueDataSet = DataSet.load(clueDataSetFilePath);
    }
}
