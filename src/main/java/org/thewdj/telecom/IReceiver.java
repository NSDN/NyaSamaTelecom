package org.thewdj.telecom;

/**
 * Created by drzzm32 on 2016.10.8.
 */
public interface IReceiver extends ISideable {
    void distributeData(Object[] data);
}
