package club.nsdn.nyasamatelecom.network.webservice;

import club.nsdn.nyasamatelecom.event.TelecomProcessor;
import org.thewdj.telecom.IWirelessRx;
import org.thewdj.telecom.IWirelessTx;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import java.security.KeyPair;
import java.util.Date;

/**
 * Created by drzzm32 on 2017.12.29.
 */
@WebService
public class TelecomImpl implements ITelecom {

    private static class Result {

        boolean finished;
        TelecomProcessor.DeviceInfo info;
        String message;

        public Result(boolean finished, TelecomProcessor.DeviceInfo info, String message) {
            this.finished = finished;
            this.info = info;
            this.message = message;
        }

        public String make() {
            return (
                String.format("finished:%s\n", finished) +
                (info == null ? "info:null" : String.format("info.dimension:%d\n", info.dimension)) +
                (info == null ? "" : String.format("info.pos:(%d,%d,%d)\n", info.x, info.y, info.z)) +
                String.format("message:%s\n", message)
            );
        }

        public String make(boolean state) {
            return (
                    String.format("finished:%s\n", finished) +
                    (info == null ? "info:null\n" : String.format("info.dimension:%d\n", info.dimension)) +
                    (info == null ? "" : String.format("info.pos:(%d,%d,%d)\n", info.x, info.y, info.z)) +
                    (info == null ? "" : String.format("info.state:%s\n", state)) +
                    String.format("message:%s\n", message)
            );
        }

    }

    private KeyPair keyPair;

    public TelecomImpl() {
        keyPair = CryptManager.createNewKeyPair();
    }

    private long getMinute() {
        return new Date().getTime() / 1000 / 60;
    }

    private boolean verifyToken(String token) {
        String[] tokens = token.split(" ");
        byte[] data = new byte[tokens.length];
        for (int i = 0; i < tokens.length; i++) data[i] = Integer.valueOf(tokens[i], 16).byteValue();
        long minute = Long.parseLong(new String(CryptManager.decryptData(keyPair.getPublic(), data)));
        return getMinute() - minute <= 1;
    }

    @Override
    @WebMethod
    @WebResult(name = "fromDevice")
    public String fromDevice(
            @WebParam(name = "token") String token,
            @WebParam(name = "id") String id,
            @WebParam(name = "key") String key
    ) {
        if (!verifyToken(token)) {
            return new Result(false, null, "Token is incorrect").make();
        }

        TelecomProcessor.DeviceInfo info = TelecomProcessor.instance().device(id);
        if (info == null) {
            return new Result(false, null, "No device found").make();
        }
        if (!info.key.equals(key)) {
            return new Result(false, info, "Key is incorrect").make();
        }
        if (info.dev() instanceof IWirelessTx) {
            boolean state = ((IWirelessTx) info.dev()).getState();
            return new Result(true, info, "Operation finished").make(state);
        }
        return new Result(false, info, "Device type error").make();
    }

    @Override
    @WebMethod
    @WebResult(name = "toDevice")
    public String toDevice(
            @WebParam(name = "token") String token,
            @WebParam(name = "id") String id,
            @WebParam(name = "key") String key,
            @WebParam(name = "state") boolean state
    ) {
        if (!verifyToken(token)) {
            return new Result(false, null, "Token is incorrect").make();
        }

        if (!TelecomProcessor.instance().lock(id)) {
            return new Result(false, null, "Device had been locked").make();
        }

        TelecomProcessor.DeviceInfo info = TelecomProcessor.instance().device(id);
        if (info == null) {
            TelecomProcessor.instance().unlock(id);
            return new Result(false, null, "No device found").make();
        }
        if (!info.key.equals(key)) {
            TelecomProcessor.instance().unlock(id);
            return new Result(false, info, "Key is incorrect").make();
        }
        if (info.dev() instanceof IWirelessRx) {
            /* TODO: may cause bug without lock */
            ((IWirelessRx) info.dev()).setState(state);
            TelecomProcessor.instance().unlock(id);
            return new Result(true, info, "Operation finished").make();
        }

        TelecomProcessor.instance().unlock(id);
        return new Result(false, info, "Device type error").make();
    }

    @Override
    @WebMethod
    @WebResult(name = "toDeviceExt")
    public String toDeviceExt(
            @WebParam(name = "token") String token,
            @WebParam(name = "id") String id,
            @WebParam(name = "key") String key,
            @WebParam(name = "data") Object data
    ) {
        return "NOP!";
    }

    @Override
    @WebMethod
    @WebResult(name = "requestToken")
    public String requestToken() {
        byte[] data = CryptManager.encryptData(keyPair.getPrivate(), Long.toString(getMinute()).getBytes());
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            builder.append(String.format("%02X", data[i]));
            if (i < data.length - 1) builder.append(" ");
        }
        return builder.toString();
    }

    @Override
    @WebMethod
    @WebResult(name = "ping")
    public String ping(@WebParam(name = "token") String token) {
        if (verifyToken(token)) return "PONG!";
        return "AH?";
    }

}
