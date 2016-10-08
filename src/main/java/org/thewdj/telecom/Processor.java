package org.thewdj.telecom;

import java.util.LinkedList;
import java.util.LinkedHashMap;

/**
 * Created by drzzm32 on 2016.10.8.
 */

public class Processor<KeyType> {
    protected LinkedList<KeyType> workQueue;    //Manual transmit
    protected LinkedList<KeyType> workList;     //Auto transmit

    protected LinkedHashMap<KeyType, ITransmitter> transmitters;
    protected LinkedHashMap<KeyType, LinkedList<IReceiver>> receivers;

    public Processor() {
        this.workQueue = new LinkedList<>();
        this.workList = new LinkedList<>();

        this.transmitters = new LinkedHashMap<>();
        this.receivers = new LinkedHashMap<>();
    }

    public KeyType[] toArray(int index) {
        if (index == 0) {
            return (KeyType[]) workQueue.toArray();
        } else if (index == 1) {
            return (KeyType[]) workList.toArray();
        }
        return null;
    }

    /*By default the transmitter is working in auto transmit mode*/
    public boolean registerTransmitter(ITransmitter transmitter, KeyType key) {
        if (!workList.contains(key)) {
            workList.add(key);
            transmitters.put(key, transmitter);
            return true;
        }
        return false;
    }

    public void disposeTransmitter(KeyType key) {
        if (workList.contains(key)) {
            if (transmitters.containsKey(key))
                transmitters.remove(key);
            workList.remove(key);
        }
    }

    public boolean loginReceiver(IReceiver receiver, KeyType key) {
        if (!receivers.get(key).contains(receiver)) {
            receivers.get(key).add(receiver);
            return true;
        }
        return false;
    }

    public void logoutReceiver(IReceiver receiver, KeyType key) {
        if (receivers.get(key).contains(receiver)) {
            receivers.get(key).remove(receiver);
        }
    }

    public boolean requestForTransmit(KeyType key) {
        if(!workList.contains(key)){
            workQueue.offer(key);
            return true;
        }
        return false;
    }

    public void markForManualTransmit(KeyType key) {
        workList.remove(key);
        workQueue.offer(key);
    }

    /*runs every tick*/
    public void tick() {
        KeyType key;
        ITransmitter transmitter;
        LinkedList<IReceiver> receiver;
        while (!workQueue.isEmpty()) {
            key = workQueue.poll();
            transmitter = transmitters.get(key);
            receiver = receivers.get(key);
            for (IReceiver r : receiver) {
                r.distributeData(transmitter.collectData());
            }
        }

        for (KeyType k : workList) {
            transmitter = transmitters.get(k);
            receiver = receivers.get(k);
            for (IReceiver r : receiver) {
                r.distributeData(transmitter.collectData());
            }
        }
    }
}
