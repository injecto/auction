package org.injecto.bidder;

import auction.Bidder;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.min;

/**
 * Evenly distributes the cash to handle half of the quantity.
 * <p>
 * Assumption: should win in 50% + 1 of cases providing maximum available cash for the bid.
 */
public class EvenlyHalfBidder implements Bidder {
    private int[] bids;
    private int turn;

    @Override
    public void init(int quantity, int cash) {
        initBids(quantity, cash);
        shuffle(bids);
    }

    private void initBids(int quantity, int cash) {
        var turnsNum = quantity / 2;
        bids = new int[turnsNum];

        var cashLeft = cash;
        var turnsHalf = turnsNum / 2;
        for (int i = 0; i < turnsHalf; i++) {
            if (cashLeft <= 0) return;
            cashLeft--;
            bids[i]++;
        }
        var turnsLeft = turnsNum - turnsHalf;
        var cashPerTurn = cashLeft / turnsLeft;
        var stash = cashLeft % turnsLeft;
        for (int i = turnsHalf; i < bids.length; i++) {
            var bid = cashPerTurn;
            if (stash > 0) {
                bid++;
                stash--;
            }
            bid = min(cashLeft, bid);
            cashLeft -= bid;
            bids[i] = bid;
        }
    }

    @Override
    public int placeBid() {
        return bids[turn++];
    }

    @Override
    public void bids(int own, int other) {

    }

    @Override
    public String toString() {
        return "EvenlyHalf";
    }

    private static void shuffle(int[] arr) {
        Random rnd = ThreadLocalRandom.current();
        for (int i = arr.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            int tmp = arr[index];
            arr[index] = arr[i];
            arr[i] = tmp;
        }
    }
}
