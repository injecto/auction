package org.injecto.bidder;

import auction.Bidder;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Makes random bids. There is no idea behind, used as a baseline mostly.
 */
public class RandomBidder implements Bidder {
    private int cash;

    @Override
    public void init(int quantity, int cash) {
        this.cash = cash;
    }

    @Override
    public int placeBid() {
        var bid = ThreadLocalRandom.current().nextInt(this.cash + 1);
        this.cash -= bid;
        return bid;
    }

    @Override
    public void bids(int own, int other) {
        
    }

    @Override
    public String toString() {
        return "Random";
    }
}
