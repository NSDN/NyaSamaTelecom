package org.thewdj.telecom;

/**
 * Created by drzzm32 on 2016.10.8.
 */
public interface IRegister extends ISideable {
    Object[] getData();
    Object getData(int index);
    void setData(Object[] data);
    void setData(Object data, int index);
}
