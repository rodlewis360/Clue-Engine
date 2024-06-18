package org.goofy.clueengine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public record Player(ArrayList<NNCalculations.Guess> guesses, ArrayList<NNCalculations.Guess> responses) {
    public static final Player[] players = new Player[] {
            new Player(new ArrayList<>(), new ArrayList<>()),
            new Player(new ArrayList<>(), new ArrayList<>()),
            new Player(new ArrayList<>(), new ArrayList<>())
    };

    public void onTurn() {
        Scanner scanner = new Scanner(System.in);
        int[] cards;
        while (true) {
            try {
                System.out.println("Input guess for next player:");
                String input = scanner.nextLine();
                String[] cardstrings = input.split(",");
                cards = Arrays.stream(cardstrings).mapToInt(Integer::parseInt).toArray();
                break;
            } catch (NumberFormatException e) {
                System.out.println("Cards not valid, input as 'x,y,z'");
            }
        }

        Player responder;
        while (true) {
            try {
                System.out.println("Input player num of responder:");
                String input = scanner.nextLine();
                int playerNum = Integer.parseInt(input) - 1;
                responder = players[playerNum];
                break;
            } catch (Exception e) {
                System.out.println("Player number not valid");
            }
        }

        NNCalculations.Guess guess = new NNCalculations.Guess(cards);
        guesses.add(guess);
        responder.responses.add(guess);
        double[] cardChances = guess.getAllChancesOfCards(NNCalculations.getInstance().cardChances);
        DataSetAssembler.guessChances.putIfAbsent(guess, new Double[] {cardChances[0], cardChances[1], cardChances[2]});
    }

    public void onTurn(int[] cards, Player responder) {
        NNCalculations.Guess guess = new NNCalculations.Guess(cards);
        guesses.add(guess);
        responder.responses.add(guess);
        double[] cardChances = guess.getAllChancesOfCards(NNCalculations.getInstance().cardChances);
        DataSetAssembler.guessChances.putIfAbsent(guess, new Double[] {cardChances[0], cardChances[1], cardChances[2]});
    }
}
