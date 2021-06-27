package com.mojagap.backenstarter.infrastructure.utility;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    public static final String DD_MMM_YYY = "dd/MMM/yyyy";

    public static SimpleDateFormat DefaultDateFormat() {
        return new SimpleDateFormat(DD_MMM_YYY);
    }

    public static Date now() {
        return Calendar.getInstance().getTime();
    }
}
