package com.clasificahumor.android;

import java.io.Serializable;

/**
 * Created by Santiago on 25/08/2014.
**/
public class Tweet implements Serializable {

    private String id_tweet;
    private String text_tweet;

    public String getId_tweet() {
        return id_tweet;
    }

    public void setId_tweet(String id_tweet) {
        this.id_tweet = id_tweet;
    }

    public String getText_tweet() {
        return text_tweet;
    }

    public void setText_tweet(String text_tweet) {
        this.text_tweet = text_tweet;
    }
}
