package org.injecto.bidder;

import auction.Bidder;

import static java.lang.Math.min;

/**
 * Evenly distributes the cash to handle the quantity.
 */
public class EvenlyBidder implements Bidder {
    private int leftCash;
    private int cashPerTurn;
    private int stash;

    @Override
    public void init(int quantity, int cash) {
        var turnsNumber = quantity / 2;
        this.cashPerTurn = cash / turnsNumber;
        this.stash = cash % turnsNumber;
        this.leftCash = cash;
    }

    @Override
    public int placeBid() {
        var bid = cashPerTurn;
        if (stash > 0) {
            bid++;
            stash--;
        }
        bid = min(bid, leftCash);
        leftCash -= bid;
        return bid;
    }

    @Override
    public void bids(int own, int other) {
    }

    @Override
    public String toString() {
        return "Evenly";
    }
}
