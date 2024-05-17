package io.github.lumine1909.object;

import java.util.List;

public record Date(int month, int day) {

    private final static List<Integer> dayOfMonth = List.of(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31, 114514);

    public static Date of(int dayOfYear) {
        int index = 0;
        while (dayOfYear > dayOfMonth.get(index)) {
            dayOfYear -= dayOfMonth.get(index);
            index++;
        }
        return new Date(index + 1, dayOfYear + 1);
    }

    public static Date fromTickLong(long seed) {
        return of((int) (seed / 24000 % 365 + 1));
    }

    public Date {
        if (month > 12 || month < 0) throw new RuntimeException();
        if (day < 0 || day > dayOfMonth.get(month - 1)) throw new RuntimeException();
    }

    public int getDayOfYear() {
        int day = 0;
        for (int i = 0; i < month - 1; i++) {
            day += dayOfMonth.get(i);
        }
        return day + this.day;
    }

    public int toLongSeed(int tickOfDay) {
        tickOfDay %= 24000;
        return (getDayOfYear() - 1) * 24000 + tickOfDay;
    }

    public SeasonAccess.Type getSeason() {
        int calc = (month + 9) % 12;
        if (calc <= 2) {
            return SeasonAccess.Type.SPRING;
        } else if (calc <= 5) {
            return SeasonAccess.Type.SUMMER;
        } else if (calc <= 8) {
            return SeasonAccess.Type.AUTUMN;
        } else {
            return SeasonAccess.Type.WINTER;
        }
    }

    @Override
    public String toString() {
        return (month < 10 ? "0" : "") + month + "/" + (day < 10 ? "0" : "") + day;
    }
}