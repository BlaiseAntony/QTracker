package com.qburst.qtracker;

interface DataRecievedListener {
    void onResponseReceived(String name, String log, boolean conflict, int inOrOut, String firstIn,
                            String burned, String clocked, String breakDur, boolean isLeaveTime,
                            String outTim);
    void onFailure();
    
    
}
