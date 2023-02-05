package org.injecto;

import auction.Bidder;

public class Auction {
    public static final int TURN_QUANTITY = 2;
    private static final int TIE_QUANTITY = 1;
    private final Bidder bidder1;
    private final Bidder bidder2;
    private final BidderState bidder1State;
    private final BidderState bidder2State;
    private int leftQuantity;


    public Auction(int quantity, int initialCash, Bidder bidder1, Bidder bidder2) {
        if (quantity < 0) throw new IllegalArgumentException("quantity should be non-negative");
        if (initialCash < 0) throw new IllegalArgumentException("cash should be non-negative");
        if (quantity % TURN_QUANTITY != 0)
            throw new IllegalArgumentException("quantity should be multiple of " + TURN_QUANTITY);

        this.bidder1 = bidder1;
        this.bidder2 = bidder2;
        this.leftQuantity = quantity;
        this.bidder1State = new BidderState(quantity, initialCash);
        this.bidder2State = new BidderState(quantity, initialCash);

        bidder1.init(quantity, initialCash);
        bidder2.init(quantity, initialCash);
    }

    /**
     * @return the winner or null in case of a tie
     */
    public Bidder hold() {
        while (turn()) {
        }
        var cmp = bidder1State.compareTo(bidder2State);
        if (cmp == 0) {
            return null;
        } else {
            return cmp > 0 ? bidder1 : bidder2;
        }
    }

    private boolean turn() {
        if (leftQuantity <= 0) return false;

        var bid1 = this.bidder1.placeBid();
        if (bid1 > this.bidder1State.cash)
            throw new IllegalStateException("bidder 1 made bid " + bid1 + " but it has just " + this.bidder1State.cash);
        var bid2 = this.bidder2.placeBid();
        if (bid2 > this.bidder2State.cash)
            throw new IllegalStateException("bidder 2 made bid " + bid2 + " but it has just " + this.bidder2State.cash);

        this.bidder1State.cash -= bid1;
        this.bidder2State.cash -= bid2;
        if (bid1 == bid2) {
            this.bidder1State.quantity += TIE_QUANTITY;
            this.bidder2State.quantity += TIE_QUANTITY;
            this.leftQuantity -= 2 * TIE_QUANTITY;
        } else if (bid1 > bid2) {
            this.bidder1State.quantity += TURN_QUANTITY;
            this.leftQuantity -= TURN_QUANTITY;
        } else {
            this.bidder2State.quantity += TURN_QUANTITY;
            this.leftQuantity -= TURN_QUANTITY;
        }

        this.bidder1.bids(bid1, bid2);
        this.bidder2.bids(bid2, bid1);

        return true;
    }

    private static class BidderState implements Comparable<BidderState> {
        int quantity;
        int cash;

        public BidderState(int quantity, int cash) {
            this.quantity = quantity;
            this.cash = cash;
        }


        @Override
        public int compareTo(BidderState o) {
            var cmp = quantity - o.quantity;
            if (cmp != 0) return cmp;
            return cash - o.cash;
        }
    }
}
