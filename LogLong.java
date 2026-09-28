package org.ChiefGuy;
//represents the log #, with a special case for if its not known by allowing String values.
public class LogLong {
    private Long value;
    private String alternative;
    // standard constructor
    public LogLong(long value) {
        this.value = value;
    }
    // int -> long
    public LogLong(int value) {
        if (value >= -1)
            this.value = Long.valueOf(value);
        else
            Main.log("There was an error converting an int to a long in LogLong.", new NumberFormatException("The value should be >= 0 because of how the log works"));
    }
    // if should be something like -1 or "ERROR"
    public LogLong(String noValue) {
        this.alternative = noValue;
    }
    public Object returnMe() {
        if (value == null)
            return alternative;
        else
            return value;
    }
}
