package com.kenbie.listeners;

import com.kenbie.model.MsgUserItem;

/**
 * Created by rajaw on 9/13/2017.
 */

public interface MsgUserActionListeners {
    public void updateFavStatus(int type, int pos);
    public void userConStart(MsgUserItem msgUserItem);
}
