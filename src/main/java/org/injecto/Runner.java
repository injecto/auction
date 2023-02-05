package org.injecto;

import auction.Bidder;
import org.injecto.bidder.EvenlyBidder;
import org.injecto.bidder.EvenlyHalfBidder;
import org.injecto.bidder.RandomBidder;
import org.injecto.bidder.ReactiveBidder;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class Runner {
    /**
     * @return [ties_number, bidder1_win_number, bidder2_win_number]
     */
    private static int[] run(int runsNumber, Supplier<Bidder> bidder1Factory, Supplier<Bidder> bidder2Factory) {
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
        })).toArray();
    }

    public static void main(String[] args) {
        var runsNumber = 1_000_000;
        List<Supplier<Bidder>> opponents = Arrays.asList(
                RandomBidder::new,
                EvenlyBidder::new,
                EvenlyHalfBidder::new,
                ReactiveBidder::new
        );
        var combinations = new ArrayList<int[]>();
        for (int i = 0; i < opponents.size(); i++) {
            for (int j = i; j < opponents.size(); j++) {
                combinations.add(new int[]{i, j});
            }
        }
        var wins = combinations.stream().parallel().map(opponentsIdx -> {
            var counters = run(runsNumber, opponents.get(opponentsIdx[0]), opponents.get(opponentsIdx[1]));
            var w = new int[opponents.size()];
            w[opponentsIdx[0]] = counters[1];
            w[opponentsIdx[1]] = counters[2];
            return w;
        }).reduce((wins1, wins2) -> {
            for (int i = 0; i < wins1.length; i++) {
                wins1[i] += wins2[i];
            }
            return wins1;
        }).orElse(new int[opponents.size()]);

        var rank = new TreeSet<RankRecord>(Collections.reverseOrder());
        for (int i = 0; i < wins.length; i++) {
            rank.add(new RankRecord(opponents.get(i).get().toString(), wins[i]));
        }
        for (var r : rank) {
            System.err.println(r);
        }
    }

    private static class RankRecord implements Comparable<RankRecord> {
        String name;
        int value;

        public RankRecord(String name, int value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RankRecord that = (RankRecord) o;
            return value == that.value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public int compareTo(RankRecord o) {
            return value - o.value;
        }

        @Override
        public String toString() {
            return "%d %s".formatted(value, name);
        }
    }
}
