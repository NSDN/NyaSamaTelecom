package org.thewdj.telecom;

/**
 * Created by drzzm32 on 2016.10.8.
 */
public interface IAnalogIO extends ISideable {
    int analogRead(int index);
    void analogWrite(int index, int value);
}
