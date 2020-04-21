package com.kenbie.listeners;

/**
 * Created by rajaw on 5/24/2017.
 */

public interface APIResponseHandler {
    public void getError(String error, int APICode);

    public void getResponse(String response, int APICode);

    public void networkError(String error, int APICode);
}
