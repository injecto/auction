package org.injecto.bidder;

import auction.Bidder;

import java.util.Arrays;

import static java.lang.Math.ceil;
import static java.lang.Math.min;

/**
 * Follows the opponent's bids and tries to bid a bit more than it's median bid among the last N bids.
 * <p>
 * Assumption: try to be not worse than the opponent despite its strategy.
 */
public class ReactiveBidder implements Bidder {
    private int ownCash;
    private Window opponentBids;


    @Override
    public void init(int quantity, int cash) {
        ownCash = cash;
        opponentBids = new Window(5); // TODO better heuristic, according to the provided quantity maybe
    }

    @Override
    public int placeBid() {
        var median = opponentBids.median();
        var ceil = ceil(median);
        var bid = 0;
        if (median < ceil) {
            bid = (int) ceil;
        } else {
            bid = (int) (ceil + 1);
        }
        bid = correctBid(bid);
        ownCash -= bid;
        return bid;
    }

    private int correctBid(int bid) {
        return min(bid, ownCash);
    }

    @Override
    public void bids(int own, int other) {
        opponentBids.add(other);
    }

    @Override
    public String toString() {
        return "Reactive";
    }

    /**
     * Sliding window of values.
     *
     * TODO unit tests
     */
    private static class Window {
        int seqNo;
        int lowestSeqNo;
        int[] values;
        int[] seqNos;
        int capacity;

        public Window(int size) {
            values = new int[size];
            seqNos = new int[size];
        }

        public void add(int val) {
            if (capacity >= values.length) {
                // remove the oldest
                var removedIdx = removeVal(lowestSeqNo, seqNos);
                lowestSeqNo++;
                remove(removedIdx, values);
                capacity--;
            }
            var seqNo = this.seqNo++;
            var insertionIdx = Arrays.binarySearch(values, 0, capacity, val);
            if (insertionIdx < 0) {
                insertionIdx = -(insertionIdx + 1);
            }
            insert(insertionIdx, val, values);
            insert(insertionIdx, seqNo, seqNos);
            capacity++;
        }

        public double median() {
            if (capacity == 0) return 0;

            if (capacity % 2 == 1) {
                return values[capacity / 2];
            } else {
                var i = capacity / 2;
                return (values[i - 1] + values[i]) * 0.5;
            }
        }

        static int removeVal(int val, int[] arr) {
            int idx = -1;
            for (int i = 0; i < arr.length; i++) {
                if (idx >= 0) {
                    arr[i - 1] = arr[i];
                } else if (arr[i] == val) {
                    idx = i;
                }
            }
            arr[arr.length - 1] = 0;
            return idx;
        }

        static void remove(int idx, int[] arr) {
            for (int i = idx + 1; i < arr.length; i++) {
                arr[i - 1] = arr[i];
            }
            arr[arr.length - 1] = 0;
        }

        static void insert(int idx, int val, int[] arr) {
            for (int i = (arr.length - 1); i > idx; i--) {
                arr[i] = arr[i - 1];
            }
            arr[idx] = val;
        }
    }
}
