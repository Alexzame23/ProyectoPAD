package es.fdi.ucm.pad.notnotion.data.model;

import java.io.Serializable;

public class Notification implements Serializable {

    private long millisBeforeEvent;
    private int quantity;
    private TimeUnit timeUnit;

    public enum TimeUnit {
        MINUTES("Minutos", 60 * 1000L),
        HOURS("Horas", 60 * 60 * 1000L),
        DAYS("Días", 24 * 60 * 60 * 1000L),
        WEEKS("Semanas", 7 * 24 * 60 * 60 * 1000L);

        private final String displayName;
        private final long millisPerUnit;

        TimeUnit(String displayName, long millisPerUnit) {
            this.displayName = displayName;
            this.millisPerUnit = millisPerUnit;
        }

        public String getDisplayName() { return displayName; }
        public long getMillisPerUnit() { return millisPerUnit; }
    }

    public Notification() {
        this.quantity = 5;
        this.timeUnit = TimeUnit.MINUTES;
        calculateMillis();
    }

    public Notification(int quantity, TimeUnit timeUnit) {
        this.quantity = quantity;
        this.timeUnit = timeUnit;
        calculateMillis();
    }

    public Notification(long millisBeforeEvent) {
        this.millisBeforeEvent = millisBeforeEvent;
        decomposeMillis();
    }

    private void calculateMillis() {
        this.millisBeforeEvent = quantity * timeUnit.getMillisPerUnit();
    }

    private void decomposeMillis() {
        for (TimeUnit unit : new TimeUnit[]{TimeUnit.WEEKS, TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES}) {
            long units = millisBeforeEvent / unit.getMillisPerUnit();
            if (units > 0 && units * unit.getMillisPerUnit() == millisBeforeEvent) {
                this.quantity = (int) units;
                this.timeUnit = unit;
                return;
            }
        }
        this.quantity = (int) (millisBeforeEvent / TimeUnit.MINUTES.getMillisPerUnit());
        this.timeUnit = TimeUnit.MINUTES;
    }

    public long getMillisBeforeEvent() { return millisBeforeEvent; }
    public void setMillisBeforeEvent(long millisBeforeEvent) {
        this.millisBeforeEvent = millisBeforeEvent;
        decomposeMillis();
    }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        calculateMillis();
    }

    public TimeUnit getTimeUnit() { return timeUnit; }
    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
        calculateMillis();
    }

    public String getDisplayText() {
        String unit = timeUnit.getDisplayName().toLowerCase();
        if (quantity == 1 && unit.endsWith("s")) {
            unit = unit.substring(0, unit.length() - 1);
        }
        return quantity + " " + unit + " antes";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Notification)) return false;
        Notification other = (Notification) obj;
        return millisBeforeEvent == other.millisBeforeEvent;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(millisBeforeEvent);
    }
}
