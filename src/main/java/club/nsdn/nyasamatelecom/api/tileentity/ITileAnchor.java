package club.nsdn.nyasamatelecom.api.tileentity;

import net.minecraftforge.common.ForgeChunkManager;

import javax.annotation.Nullable;

/**
 * Created by drzzm32 on 2020.4.7.
 */
public interface ITileAnchor {
    void setChunkTicket(@Nullable ForgeChunkManager.Ticket tick);
    void forceChunkLoading();
}
