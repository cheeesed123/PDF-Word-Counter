package org.ChiefGuy;
//these objects represent logs to be written in logs.yaml. They're just records.
public record Log(LogLong threadNum, LogLong iterationNum, String time, String message, boolean notError, boolean poisonPill, Exception e){
    @Override
    public String message() {
        return message + "\"";
    }
};
