package org.ChiefGuy;

public record Log(LLong threadNum, LLong iterationNum, String time, String message, boolean notError, boolean poisonPill, Exception e){
    @Override
    public String message() {
        return message + "\"";
    }
};
