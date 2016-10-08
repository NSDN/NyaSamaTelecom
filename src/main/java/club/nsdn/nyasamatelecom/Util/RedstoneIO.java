package club.nsdn.nyasamatelecom.Util;

import net.minecraft.world.World;
import org.thewdj.telecom.IDigitalIO;

/**
 * Created by drzzm32 on 2016.10.8.
 */
public class RedstoneIO implements IDigitalIO {

    protected World theWorld;

    public RedstoneIO(World world) {
        this.theWorld = world;
    }

    @Override
    public boolean digitalRead(int index) {
        return false;
    }

    @Override
    public void digitalWrite(int index, boolean value) {

    }
}
