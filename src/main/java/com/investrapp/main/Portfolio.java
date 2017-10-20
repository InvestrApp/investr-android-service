package com.investrapp.main;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Portfolio implements Comparable<Portfolio> {

    public int rank;
    public Double value;
    public Object competition;
    public Object player;

    private final static Logger LOGGER = Logger.getLogger(Portfolio.class.getName());

    public Portfolio(Object player, Object competition, Double value) {
        this.player = player;
        this.competition = competition;
        this.value = value;
        rank = Integer.MAX_VALUE;
        LOGGER.log(Level.INFO, "Player: " + player + ", competition: " + competition + ", value: " + value + ", rank: " + rank);
    }

    @Override
    public int compareTo(Portfolio o) {
        if (this.value < o.value) {
            return 1;
        } else if (this.value.equals(o.value)) {
            return 0;
        } else {
            return -1;
        }
    }

}
