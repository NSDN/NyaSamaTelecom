package org.thewdj.telecom;

/**
 * Created by drzzm32 on 2016.10.8.
 */
public interface IDigitalIO extends ISideable {
    boolean digitalRead(int index);
    void digitalWrite(int index, boolean value);
}
