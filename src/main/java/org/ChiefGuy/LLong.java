package org.ChiefGuy;

public class LLong {
    private Long value;
    private String alternative;
    // standard constructor
    public LLong(long value) {
        this.value = value;
    }
    // int -> long
    public LLong(int value) {
        if (value >= -1)
            this.value = Long.valueOf(value);
        else
            Main.log("There was an error converting an int to a long in LLong.", new NumberFormatException("The value has to be >= 0 because of how longs work."));
    }
    // if should be something like -1 or "ERROR"
    public LLong(String noValue) {
        this.alternative = noValue;
    }
    public Object returnMe() {
        if (value == null)
            return alternative;
        else
            return value;
    }
}
