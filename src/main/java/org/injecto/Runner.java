package org.injecto;

import auction.Bidder;
import org.injecto.bidder.EvenlyBidder;
import org.injecto.bidder.EvenlyHalfBidder;
import org.injecto.bidder.RandomBidder;
import org.injecto.bidder.ReactiveBidder;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static java.lang.Math.max;

public class Runner {
    public double[] run(int runsNumber, Supplier<Bidder> bidder1Factory, Supplier<Bidder> bidder2Factory) {
        return Arrays.stream(IntStream.range(0, runsNumber).parallel().map(i -> {
                    var turnsNumber = ThreadLocalRandom.current().nextInt(1, 100);
                    var cash = ThreadLocalRandom.current().nextInt(turnsNumber, turnsNumber * 10);
                    Bidder bidder1 = bidder1Factory.get();
                    var auction = new Auction(turnsNumber * Auction.TURN_QUANTITY, cash, bidder1, bidder2Factory.get());
                    var winner = auction.hold();
                    if (winner == null) {
                        return 0;
                    } else {
                        return winner.equals(bidder1) ? 1 : 2;
                    }
                }).collect(() -> new int[3], (counters, winner) -> counters[winner]++, (accumulator, counters) -> {
                    for (int i = 0; i < counters.length; i++) {
                        accumulator[i] += counters[i];
                    }
                })).mapToDouble(cnt -> cnt * 100. / runsNumber)
                .toArray();
    }

    public static void main(String[] args) {
        var runner = new Runner();
        var runsNumber = 1_000_000;
        Supplier<Bidder> testBidder = ReactiveBidder::new;
        var opponents = Arrays.asList(
                RandomBidder::new,
                EvenlyBidder::new,
                EvenlyHalfBidder::new,
                testBidder
        );
        var data = opponents.stream().parallel().map(opponent -> {
            var percents = runner.run(runsNumber, testBidder, opponent);
            return new String[]{opponent.get().toString(), String.format("%2.1f", percents[1]), String.format("%2.1f", percents[2]), String.format("%2.1f", percents[0])};
        }).toArray(String[][]::new);
        var header = new String[]{testBidder.get().toString() + " vs...", "win%", "loss%", "tie%"};
        renderTable(header, data);
    }

    private static void renderTable(String[] header, String[][] data) {
        var columnWidths = Arrays.stream(header).mapToInt(String::length).toArray();
        for (String[] row : data) {
            for (int c = 0; c < row.length; c++) {
                columnWidths[c] = max(columnWidths[c], row[c].length());
            }
        }
        var sb = new StringBuilder();
        for (var w : columnWidths) {
            sb.append(String.format("%%%ds", w + 1));
        }
        sb.append('\n');
        var format = sb.toString();
        System.err.format(format, (Object[]) header);
        for (String[] row : data) {
            System.err.format(format, (Object[]) row);
        }
    }
}
