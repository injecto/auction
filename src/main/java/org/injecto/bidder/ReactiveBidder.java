package org.injecto.bidder;

import auction.Bidder;

import static java.lang.Math.min;

public class ReactiveBidder implements Bidder {
    private int ownQuantity;
    private int ownCash;
    private int opponentCash;
    private int opponentQuantity;
    private int leftQuantity;


    @Override
    public void init(int quantity, int cash) {
        opponentCash = cash;
        ownCash = cash;
        leftQuantity = quantity;
    }

    @Override
    public int placeBid() {
        var bid = 0;
        bid = correctBid(min(ownCash, opponentCash) / turnsLeft() + 1);

//        if (ownQuantity > opponentQuantity) {
//            bid = correctBid(1);
//        } else {
//            bid = correctBid(ownCash / (leftQuantity / 2) + 1);
//            bid = min(bid, opponentCash + 1);
//        }

        ownCash -= bid;
        return bid;
    }

    private int correctBid(int bid) {
        return min(bid, ownCash);
    }

    private int turnsLeft() {
        return leftQuantity / 2;
    }

    @Override
    public void bids(int own, int other) {
        opponentCash -= other;
        if (own == other) {
            ownQuantity++;
            opponentQuantity++;
        } else if (own > other) {
            ownQuantity += 2;
        } else {
            opponentQuantity += 2;
        }
        leftQuantity -= 2;
    }

    @Override
    public String toString() {
        return "ReactiveBidder";
    }
}
