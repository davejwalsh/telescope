package com.ska.telescopeSimulator.device.reciever;

import com.ska.telescopeSimulator.states.ReceiverState;

public interface ReceiverStateListener {
    void onReceiverStateChanged(ReceiverState newState);
}
