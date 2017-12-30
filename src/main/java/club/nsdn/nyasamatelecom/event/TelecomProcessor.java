package club.nsdn.nyasamatelecom.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.tileentity.TileEntity;
import org.thewdj.telecom.IWireless;
import net.minecraft.world.World;

import java.util.LinkedHashMap;
import java.util.LinkedList;

/**
 * Created by drzzm32 on 2017.12.29.
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
        public int dimension;

        public TileEntity dev() {
            World world = DimensionManager.getWorld(dimension);
            if (world == null) return null;
            return world.getTileEntity(x, y, z);
        }

    }

    private LinkedHashMap<String, DeviceInfo> devices;
    private LinkedHashMap<String, DeviceInfo> cache;
    private LinkedList<String> locks;

    private int gcCounter;

    public TelecomProcessor() {
        devices = new LinkedHashMap<>();
        cache = new LinkedHashMap<>();

        locks = new LinkedList<>();
        gcCounter = 0;
    }

    @SubscribeEvent
    public void tick(TickEvent.ServerTickEvent event) {
        if (gcCounter < 20) gcCounter += 1;
        else {
            gcCounter = 0;

            if (devices.isEmpty()) return;

            cache.clear(); cache.putAll(devices);
            cache.forEach((id, info) -> {
                if (!checkDevice(info)) devices.remove(id);
            });
        }
    }

    private boolean checkDevice(DeviceInfo info) {
        return info.dev() != null;
    }

    private String getDomain(String id) {
        if (id.split("\\.").length < 3) return id;
        return id.split("\\.")[0] + "." + id.split("\\.")[1];
    }

    public boolean lock(String id) {
        if (locks.contains(getDomain(id))) return false;
        locks.add(getDomain(id));
        return true;
    }

    public void unlock(String id) {
        if (locks.contains(getDomain(id)))
            locks.remove(getDomain(id));
    }

    public DeviceInfo device(String id) {
        if (!devices.containsKey(id)) return null;
        return devices.get(id);
    }

    private void register(String id, DeviceInfo info) {
        if (devices.containsKey(id)) devices.remove(id);
        devices.put(id, info);
    }

    public void register(IWireless<? extends TileEntity> device) {
        if (device.me() == null) return;
        if (device.id().equals("null") || device.key().equals("null")) return;

        DeviceInfo info = new DeviceInfo();
        info.key = device.key();
        info.dimension = device.me().getWorldObj().getWorldInfo().getVanillaDimension();
        info.x = device.me().xCoord; info.y = device.me().yCoord; info.z = device.me().zCoord;

        register(device.id(), info);
    }

}
