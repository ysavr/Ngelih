package com.mythcon.savr.ngelih.Common;

import com.mythcon.savr.ngelih.Model.User;

/**
 * Created by SAVR on 20/02/2018.
 * untuk membuat current user
 */

public class Common {
    public static User currentUser;

    public static String convertOrderStatus(String status) {
        if(status == null) {
            return "Can't proces";
        }
        if (status.equals("0"))
            return "Placed";
        else if (status.equals("1"))
            return "On my way";
        else
            return "Shipped";
    }
}
