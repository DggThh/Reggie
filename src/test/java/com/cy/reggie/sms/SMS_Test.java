package com.cy.reggie.sms;

import com.cy.reggie.utils.sms.SMS;
import org.junit.jupiter.api.Test;

public class SMS_Test {
    @Test
    public void send() {
        String p = "13859594663";
        String code = "123456";
        String time = "60";
        SMS.SM_1(p, code);
        //SMS.SM_2(p, code, time);
    }
}
