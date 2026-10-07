package club.nsdn.nyasamatelecom.api.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Root of the NyaSama device framework, ported from the 1.12.2 {@code TileEntityBase}.
 *
 * <p>The class name is kept deliberately (1.21.1 calls these "block entities"): every downstream mod
 * -- optics, electricity, railway -- inherits from it, so keeping the name keeps the diff against
 * 1.12.2 readable, which is the whole point of the one-to-one migration strategy.</p>
 *
 * <p>What changed structurally:</p>
 * <ul>
 *   <li>{@code extends TileEntity implements ITickable} becomes {@code extends BlockEntity}; ticking
 *       is no longer an interface but a {@link net.minecraft.world.level.block.entity.BlockEntityTicker}
 *       returned by the registered block entity type, and it calls {@link #tick()}.</li>
 *   <li>The no-arg constructor becomes the mandatory
 *       {@code (BlockEntityType, BlockPos, BlockState)} triple, because a 1.21.1 block entity must
 *       know its own type. Concrete devices pass their registered type through.</li>
 *   <li>{@code readFromNBT}/{@code writeToNBT} become {@code loadAdditional}/{@code saveAdditional}
 *       with a {@code HolderLookup.Provider}; the mod's own {@code fromNBT}/{@code toNBT} pair is
 *       preserved so subclasses keep their 1.12.2 shape.</li>
 *   <li>{@code getUpdatePacket}/{@code onDataPacket} collapse into
 *       {@link #getUpdatePacket()} returning a {@code ClientboundBlockEntityDataPacket} -- the
 *       client-side apply is handled by vanilla, so {@code onDataPacket} disappears.</li>
 *   <li>{@code shouldRefresh} disappears: vanilla recreates the block entity when the block changes,
 *       which is exactly what the old override implemented manually.</li>
 * </ul>
 *
 * <p>The geometry code ({@code setBlockBounds}, {@code setBoundsByXYZ}, {@code updateBounds}) is
 * carried over verbatim -- it is pure math and encodes the 12 orientation cases of {@code META}.</p>
 */
public class TileEntityBase extends BlockEntity {

    public int META = 0;
    protected int METAMAX = 12;
    /**
     * 1.12.2 initialised this from {@code Block.FULL_BLOCK_AABB}, which no longer exists in 1.21.1
     * (vanilla moved to {@code VoxelShape}). The equivalent plain box is used instead.
     */
    protected AABB AABB = new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
    protected Vec3 SIZE = new Vec3(1, 1, 1);

    public TileEntityBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected void setBlockBounds(double x1, double y1, double z1, double x2, double y2, double z2) {
        AABB = new AABB(x1, y1, z1, x2, y2, z2);
    }

    /** Orientation-aware bounds, ported verbatim: 12 cases driven by {@code META % METAMAX}. */
    protected void setBoundsByXYZ(double x1, double y1, double z1, double x2, double y2, double z2) {
        switch (META % METAMAX) {
            case 0:
                setBlockBounds(x1, y1, z1, x2, y2, z2);
                break;
            case 1:
                setBlockBounds(1.0 - z2, y1, x1, 1.0 - z1, y2, x2);
                break;
            case 2:
                setBlockBounds(1.0 - x2, y1, 1.0 - z2, 1.0 - x1, y2, 1.0 - z1);
                break;
            case 3:
                setBlockBounds(z1, y1, 1.0 - x2, z2, y2, 1.0 - x1);
                break;
            case 4:
                setBlockBounds(1.0 - x2, z1, 1.0 - y2, 1.0 - x1, z2, 1.0 - y1);
                break;
            case 5:
                setBlockBounds(y1, z1, 1.0 - x2, y2, z2, 1.0 - x1);
                break;
            case 6:
                setBlockBounds(x1, z1, y1, x2, z2, y2);
                break;
            case 7:
                setBlockBounds(1.0 - y2, z1, x1, 1.0 - y1, z2, x2);
                break;
            case 8:
                setBlockBounds(x1, 1.0 - y2, z1, x2, 1.0 - y1, z2);
                break;
            case 9:
                setBlockBounds(1.0 - z2, 1.0 - y2, x1, 1.0 - z1, 1.0 - y1, x2);
                break;
            case 10:
                setBlockBounds(1.0 - x2, 1.0 - y2, 1.0 - z2, 1.0 - x1, 1.0 - y1, 1.0 - z1);
                break;
            case 11:
                setBlockBounds(z1, 1.0 - y2, 1.0 - x2, z2, 1.0 - y1, 1.0 - x1);
                break;
            default:
                break;
        }
    }

    protected void updateBounds() {
        setBoundsByXYZ(
                0.5 - SIZE.x / 2, 0.0, 0.5 - SIZE.z / 2,
                0.5 + SIZE.x / 2, SIZE.y, 0.5 + SIZE.z / 2
        );
    }

    /** 1.12.2 {@code refresh()}: force a block update so clients re-read the block entity. */
    public void refresh() {
        if (level == null) {
            return;
        }
        BlockState state = getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
        setChanged();
    }

    protected void setInfo(int metaMax, double x, double y, double z) {
        METAMAX = metaMax;
        SIZE = new Vec3(x, y, z);
        updateBounds();
    }

    public AABB AABB() {
        updateBounds();
        return AABB;
    }

    /** Called by the registered ticker; the 1.12.2 server-side guard is preserved. */
    public void tick() {
        if (level != null && !level.isClientSide) {
            updateSignal(level, worldPosition);
        }
    }

    public void updateSignal(net.minecraft.world.level.Level level, BlockPos pos) {
    }

    public void fromNBT(CompoundTag tagCompound) {
        META = tagCompound.getInt("Meta");
        METAMAX = tagCompound.getInt("MetaMax");
        double x = tagCompound.getDouble("SizeX");
        double y = tagCompound.getDouble("SizeY");
        double z = tagCompound.getDouble("SizeZ");
        SIZE = new Vec3(x, y, z);
        updateBounds();
    }

    public CompoundTag toNBT(CompoundTag tagCompound) {
        tagCompound.putInt("Meta", META);
        tagCompound.putInt("MetaMax", METAMAX);
        tagCompound.putDouble("SizeX", SIZE.x);
        tagCompound.putDouble("SizeY", SIZE.y);
        tagCompound.putDouble("SizeZ", SIZE.z);
        return tagCompound;
    }

    @Override
    protected void loadAdditional(CompoundTag tagCompound, HolderLookup.Provider registries) {
        super.loadAdditional(tagCompound, registries);
        fromNBT(tagCompound);
    }

    @Override
    protected void saveAdditional(CompoundTag tagCompound, HolderLookup.Provider registries) {
        super.saveAdditional(tagCompound, registries);
        toNBT(tagCompound);
    }

    /*
     * NOT PORTED HERE: 1.12.2 overrode {@code getRenderBoundingBox()} (and
     * {@code getMaxRenderDistanceSquared()}) on the tile entity. Verified with javap against
     * neoforge-21.1.256-merged.jar: {@code BlockEntity} has neither method any more -- render bounds
     * and view distance moved to the renderer side ({@code BlockEntityRenderer#shouldRender} /
     * {@code getViewDistance}). They will be handled when the renderers themselves are ported.
     */

    /** 1.12.2 {@code getUpdateTag()}: full state, sent when the chunk is loaded. */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tagCompound = new CompoundTag();
        return toNBT(tagCompound);
    }

    /** 1.12.2 {@code getUpdatePacket()}: block update packet, built by vanilla in 1.21.1. */
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void onDestroy() {
    }
}
