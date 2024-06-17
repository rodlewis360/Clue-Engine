package org.goofy.clueengine;

import org.neuroph.core.Layer;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.Neuron;
import org.neuroph.nnet.comp.layer.CompetitiveLayer;
import org.neuroph.nnet.comp.layer.InputLayer;
import org.neuroph.nnet.comp.neuron.CompetitiveNeuron;
import org.neuroph.nnet.learning.BackPropagation;
import org.neuroph.util.ConnectionFactory;
import org.neuroph.util.NeuronProperties;
import org.neuroph.util.TransferFunctionType;

import java.util.Scanner;
import java.util.stream.IntStream;

public class NNCalculations {

    private NeuralNetwork<BackPropagation> cardGuesser = new NeuralNetwork<>();
    private final String NNFilePath = "C:\\Users\\roddy\\IdeaProjects\\clueengine\\src\\main\\networks\\clue.nn";
    // index = card #, value at index = percent chance of card
    public final double[] cardChances = new double[21];

    private static final NNCalculations INSTANCE = new NNCalculations();

    public static NNCalculations getInstance() {
        return INSTANCE;
    }

    public static void main(String[] args) {
        getInstance().load();
        getInstance().TUI();
    }

    public void learn() {
        cardGuesser = new NeuralNetwork<>();
        System.out.println("Initializing network...");

        // in 1: old percent chance of card existing
        // 2: times this card has been suggested,
        // 3: number of responses involving this card,
        // 4: number of suggestions involving the other cards in this guess,
        // 5: number of responses involving the other cards in this guess (in order)

        // out 1: new percent chance of card existing
        Layer inputLayer = new InputLayer(5);
        Layer calcLayer = new Layer(3, new NeuronProperties(Neuron.class, TransferFunctionType.SIGMOID)); // calculate probabilities
        Layer compLayer = new CompetitiveLayer(4, new NeuronProperties(CompetitiveNeuron.class, TransferFunctionType.GAUSSIAN)); // identify situations
        Layer consolidateLayer = new Layer(2, new NeuronProperties(Neuron.class, TransferFunctionType.SIGMOID)); // finish calculating probabilities
        Layer outputLayer = new Layer(1, new NeuronProperties(Neuron.class, TransferFunctionType.LINEAR)); // assemble probabilities

        cardGuesser.addLayer(0, inputLayer);
        cardGuesser.addLayer(1, calcLayer);
        cardGuesser.addLayer(2, compLayer);
        cardGuesser.addLayer(3, consolidateLayer);
        cardGuesser.addLayer(4, outputLayer);

        ConnectionFactory.fullConnect(inputLayer, calcLayer);
        ConnectionFactory.fullConnect(calcLayer, compLayer);
        ConnectionFactory.fullConnect(calcLayer, consolidateLayer);
        ConnectionFactory.fullConnect(compLayer, consolidateLayer);
        ConnectionFactory.fullConnect(consolidateLayer, outputLayer);

        cardGuesser.setInputNeurons(inputLayer.getNeurons());
        cardGuesser.setOutputNeurons(outputLayer.getNeurons());

        cardGuesser.setLearningRule(new BackPropagation());
        System.out.println("Network Initialized!");

        System.out.println("Learning...");
        getInstance().cardGuesser.learn(DataSetAssembler.clueDataSet);
        System.out.println("Done!");
        getInstance().cardGuesser.save(NNFilePath);
    }

    public void TUI() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Entered TUI Mode");
        int i = 0;
        while (true) {
            learn();
            Player.players[i].onTurn();
            i = (i + 1) % Player.players.length;
            System.out.flush();
            System.out.println("Press 'q' to quit, or enter to continue.");
            String input = scanner.nextLine();
            if (input.contains("q")) {
                break;
            }
        }
    }

    public void load() {
        cardGuesser = NeuralNetwork.load(NNFilePath);
    }

    public record Guess(int[] cards, Player guesser, Player responder) {
        public double getChanceOfCard(int index, double lastChanceOfCard) {
            int[] relevants = getRelevantTo(index);

            NeuralNetwork cardGuesser = NNCalculations.getInstance().cardGuesser;
            cardGuesser.setInput(lastChanceOfCard, relevants[0], relevants[1], relevants[2], relevants[3]);
            cardGuesser.calculate();
            return cardGuesser.getOutput()[0];
        }

        public int[] getRelevantTo(int index) {
            int thisCardGuesses = 0;
            int otherCardGuesses = 0;
            int thisCardResponses = 0;
            int otherCardResponses = 0;

            for (Player player : Player.players) {
                for (Guess guess : player.guesses()) {
                    for (int card : guess.cards) {
                        if (IntStream.of(cards).anyMatch(x -> x == card)) {
                            if (card == cards[index]) {
                                thisCardGuesses += 1;
                            } else {
                                otherCardGuesses += 1;
                            }
                        }
                    }
                }

                for (Guess response : player.responses()) {
                    for (int card : response.cards) {
                        if (IntStream.of(cards).anyMatch(x -> x == card)) {
                            if (card == cards[index]) {
                                thisCardResponses += 1;
                            } else {
                                otherCardResponses += 1;
                            }
                        }
                    }
                }
            }

            return new int[] {thisCardGuesses, thisCardResponses, otherCardGuesses, otherCardResponses};
        }

        public double[] getAllChancesOfCards(double[] cardChances) {
            return new double[] {
                    getChanceOfCard(0, cardChances[cards[0]]),
                    getChanceOfCard(1, cardChances[cards[1]]),
                    getChanceOfCard(2, cardChances[cards[2]]),
            };
        }
    }
}
