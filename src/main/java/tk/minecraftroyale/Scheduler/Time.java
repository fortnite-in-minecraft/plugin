package tk.minecraftroyale.Scheduler;

public class Time {
    public static long ticksFromSeconds(long seconds) { return seconds * 20; }
    public static long ticksFromMinutes(long minutes) { return ticksFromSeconds(minutes * 60); }
    public static long ticksFromHours(int hours) { return ticksFromMinutes(hours * 60); }
    public static long ticksFromDays(int days) { return ticksFromHours(days * 24); }

    private long ticks;

    public Time(int days, int hours, long minutes, long seconds, long ticks) {
        this.ticks = ticksFromDays(days) +
                     ticksFromHours(hours) +
                     ticksFromMinutes(minutes) +
                     ticksFromSeconds(seconds) +
                     ticks;
    }

    public long getTicks() { return ticks; }
}
