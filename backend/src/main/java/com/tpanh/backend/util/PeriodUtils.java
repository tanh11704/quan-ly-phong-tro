package com.tpanh.backend.util;

import java.time.YearMonth;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PeriodUtils {
    public static String getPreviousMonth(final String period) {
        try {
            return YearMonth.parse(period).minusMonths(1).toString();
        } catch (final Exception e) {
            return "";
        }
    }
}
