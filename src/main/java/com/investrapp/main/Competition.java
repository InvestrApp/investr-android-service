package com.investrapp.main;

import org.parse4j.ParseObject;

public class Competition {

    ParseObject competition;
    String objectId;

    public Competition(ParseObject competition) {
        this.competition = competition;
        this.objectId = competition.getObjectId();
    }

}
