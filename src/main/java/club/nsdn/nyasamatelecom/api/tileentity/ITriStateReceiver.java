package club.nsdn.nyasamatelecom.api.tileentity;

/**
 * Ported unchanged from the 1.12.2 {@code ITriStateReceiver}: a device that can be pushed to the
 * positive or negative state. A positive command while already negative (or vice versa) first returns
 * the device to zero -- see {@link TileEntityTriStateReceiver}.
 */
public interface ITriStateReceiver {

    void setStatePos();

    void setStateNeg();
}
