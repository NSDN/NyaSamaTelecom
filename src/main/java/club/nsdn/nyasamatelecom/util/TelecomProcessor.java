package club.nsdn.nyasamatelecom.util;

import cn.ac.nya.telecom.IWireless;
import cn.ac.nya.telecom.IWirelessRx;
import cn.ac.nya.telecom.IWirelessTx;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.LinkedHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The wireless device bookkeeping of the telecom module, ported from the 1.12.2 {@code TelecomProcessor}.
 *
 * <p>This is the server-side registry that makes the "{@code dev}/{@code key}" script functions work: every
 * wireless box registers itself under its own id, and the processor turns the states the boxes write into
 * the states the boxes read, once per server tick. The whole algorithm -- the two {@code LinkedHashMap}s
 * of inputs/outputs, the {@code State} tri-state SUP/ZERO/ONE protocol, the {@code gcCounter} sweep every
 * 20 ticks and the {@link ReentrantLock} guarding every access -- is carried over unchanged.</p>
 *
 * <h2>The two 1.12.2 APIs that no longer exist</h2>
 * <ul>
 *   <li>{@code DimensionManager.getWorld(int)} is gone. A {@code DeviceInfo} now stores the level's
 *       {@link ResourceKey} and resolves it through {@code MinecraftServer#getLevel(ResourceKey)}. The
 *       server itself comes from NeoForge's {@code ServerLifecycleHooks.getCurrentServer()}, which is the
 *       modern replacement for the old global dimension manager; when it is null (client side, or before
 *       the server exists) {@link DeviceInfo#dev()} returns null, which is exactly what the old
 *       {@code getWorld(dimension) == null} guard did.</li>
 *   <li>{@code TileEntity} is {@code BlockEntity} and {@code getPos()} is {@code getBlockPos()};
 *       {@code world.provider.getDimension()} is {@code level.dimension()}. The {@code dimension} field is
 *       therefore a {@code ResourceKey<Level>} rather than an {@code int} -- it is still only used to look
 *       the level back up, so no behaviour depends on the numeric form.</li>
 * </ul>
 *
 * <p>{@code DeviceInfo} still has no {@code equals}/{@code hashCode} override, so the input/output maps key
 * on identity, exactly as before; {@link #register(String, DeviceInfo)} puts the very instance the maps
 * were populated with into {@link #devices}, which is what makes that work.</p>
 */
public class TelecomProcessor {

    private static TelecomProcessor instance;

    public static TelecomProcessor instance() {
        if (instance == null) instance = new TelecomProcessor();
        return instance;
    }

    public static class DeviceInfo {

        public String key;
        public int x, y, z;
        public ResourceKey<Level> dimension;

        /** Resolves the device lazily, exactly as the 1.12.2 {@code DimensionManager} lookup did. */
        @PackagePrivate
        BlockEntity dev() {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server == null) return null;
            ServerLevel level = server.getLevel(dimension);
            if (level == null) return null;
            return level.getBlockEntity(new BlockPos(x, y, z));
        }
    }

    private LinkedHashMap<String, DeviceInfo> devices;
    private LinkedHashMap<String, DeviceInfo> cache;

    public enum State { SUP, ZERO, ONE }
    private LinkedHashMap<DeviceInfo, State> inputs;
    private LinkedHashMap<DeviceInfo, State> outputs;

    private LinkedHashMap<String, Integer> innerInputs;
    private LinkedHashMap<String, Boolean> innerOutputs;

    private final ReentrantLock lock = new ReentrantLock();

    private int gcCounter;

    public TelecomProcessor() {
        devices = new LinkedHashMap<>();
        cache = new LinkedHashMap<>();

        inputs = new LinkedHashMap<>();
        outputs = new LinkedHashMap<>();

        innerInputs = new LinkedHashMap<>();
        innerOutputs = new LinkedHashMap<>();

        gcCounter = 0;
    }

    /** Driven once per server tick by {@code event.TelecomHandler}. */
    public void update() {
        if (!lock.tryLock()) return;

        devices.forEach((id, info) -> {
            if (checkDevice(info)) {
                if (info.dev() instanceof IWirelessRx) {
                    if (inputs.containsKey(info)) {
                        if (inputs.get(info) != State.SUP) {
                            State state = inputs.get(info);
                            boolean result = false;
                            if (state == State.ONE) result = true;
                            if (state == State.ZERO) result = false;
                            ((IWirelessRx) info.dev()).setState(result);

                            inputs.replace(info, State.SUP);
                        }
                    }
                } else if (info.dev() instanceof IWirelessTx) {
                    if (outputs.containsKey(info)) {
                        boolean state = ((IWirelessTx) info.dev()).getState();
                        outputs.replace(info, state ? State.ONE : State.ZERO);
                    }
                }
            }
        });

        innerInputs.forEach((k, v) -> innerOutputs.put(k, v > 0));
        innerInputs.clear();

        if (gcCounter < 20) gcCounter += 1;
        else {
            gcCounter = 0;

            gc();
        }

        lock.unlock();
    }

    private void gc() {
        if (devices.isEmpty()) return;

        cache.clear(); cache.putAll(devices);
        cache.forEach((id, info) -> {
            if (!checkDevice(info)) {
                devices.remove(id);
                if (inputs.containsKey(info))
                    inputs.remove(info);
                if (outputs.containsKey(info))
                    outputs.remove(info);
            }
        });
    }

    private boolean checkDevice(DeviceInfo info) {
        return info.dev() != null;
    }

    public DeviceInfo device(String id) {
        try {
            lock.lock();
            if (!devices.containsKey(id)) return null;
            return devices.get(id);
        } finally {
            lock.unlock();
        }
    }

    public boolean isRx(DeviceInfo info) {
        try {
            lock.lock();
            return inputs.containsKey(info);
        } finally {
            lock.unlock();
        }
    }

    public boolean isTx(DeviceInfo info) {
        try {
            lock.lock();
            return outputs.containsKey(info);
        } finally {
            lock.unlock();
        }
    }

    public void set(DeviceInfo info, boolean state) {
        lock.lock();
        if (inputs.containsKey(info)) {
            inputs.replace(info, state ? State.ONE : State.ZERO);
        }
        lock.unlock();
    }

    public boolean get(DeviceInfo info) {
        try {
            lock.lock();
            if (outputs.containsKey(info)) {
                State state = outputs.get(info);
                if (state == State.ONE) return true;
                if (state == State.ZERO) return false;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    public void set(String id, boolean state) {
        lock.lock();
        if (!innerInputs.containsKey(id))
            innerInputs.put(id, 0);
        int val = innerInputs.get(id);
        val += (state ? 1 : -1);
        innerInputs.put(id, val);
        lock.unlock();
    }

    public boolean get(String id) {
        try {
            lock.lock();
            if (innerOutputs.containsKey(id))
                return innerOutputs.get(id);
            return false;
        } finally {
            lock.unlock();
        }
    }

    private void register(String id, DeviceInfo info) {
        try {
            lock.lock();
            if (devices.containsKey(id)) devices.remove(id);
            devices.put(id, info);
            if (info.dev() instanceof IWirelessRx) {
                if (inputs.containsKey(info)) inputs.remove(info);
                inputs.put(info, State.SUP);
            } else if (info.dev() instanceof IWirelessTx) {
                if (outputs.containsKey(info)) outputs.remove(info);
                outputs.put(info, State.ZERO);
            }
        } finally {
            lock.unlock();
        }
    }

    public void register(IWireless<? extends BlockEntity> device) {
        if (device.me() == null) return;
        if (device.id().equals("null") || device.key().equals("null")) return;
        Level level = device.me().getLevel();
        if (level == null) return;

        DeviceInfo info = new DeviceInfo();
        info.key = device.key();
        info.dimension = level.dimension();
        info.x = device.me().getBlockPos().getX();
        info.y = device.me().getBlockPos().getY();
        info.z = device.me().getBlockPos().getZ();

        register(device.id(), info);
    }
}
