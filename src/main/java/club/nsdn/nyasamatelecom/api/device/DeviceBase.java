package club.nsdn.nyasamatelecom.api.device;

import club.nsdn.nyasamatelecom.api.tileentity.TileEntityBase;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

/**
 * Base class for every NyaSama device block, ported from the 1.12.2 {@code DeviceBase}.
 *
 * <p>This class is the template for all device ports in telecom / optics / electricity / railway, so
 * the structural mapping is spelled out here:</p>
 * <ul>
 *   <li>{@code extends BlockContainer} becomes {@code extends Block implements EntityBlock}. There is
 *       no {@code BlockContainer} any more; the block entity is created by
 *       {@link #newBlockEntity(BlockPos, BlockState)} and ticked through
 *       {@link #getTicker(Level, BlockState, BlockEntityType)} -- the two halves of the old
 *       {@code createNewTileEntity} plus {@code ITickable}.</li>
 *   <li>Construction data ({@code setHardness}, {@code setResistance}, {@code setSoundType},
 *       {@code setLightLevel}, {@code setLightOpacity}) now lives in
 *       {@link BlockBehaviour.Properties}, which callers pass in. Note {@code setLightOpacity(0)} and
 *       the old {@code isOpaqueCube()}/{@code isFullCube()} overrides are expressed as
 *       {@code noOcclusion()} -- devices are see-through by design.</li>
 *   <li>The block's name comes from its registry entry, so the {@code blockName} constructor argument
 *       disappears.</li>
 *   <li>{@code onBlockPlacedBy} becomes {@link #setPlacedBy}; {@code MathHelper.floor} is now
 *       {@link Mth#floor}.</li>
 * </ul>
 *
 * <p>Because the codec of an abstract block cannot be generated automatically, {@code DeviceBase}
 * stays abstract and leaves {@code codec()} to its concrete subclasses; the same goes for
 * {@link #createBlockEntity(BlockEntityType, BlockPos, BlockState)}, which replaces the old
 * {@code createNewTileEntity()} hook.</p>
 */
public abstract class DeviceBase extends Block implements EntityBlock {

    private final Supplier<BlockEntityType<?>> blockEntityType;

    /**
     * @param properties      the usual block properties; callers should include {@code noOcclusion()}
     * @param blockEntityType the registered type, supplied lazily because the block and its block
     *                        entity type are registered through separate deferred registers
     */
    protected DeviceBase(BlockBehaviour.Properties properties,
                         Supplier<BlockEntityType<?>> blockEntityType) {
        super(properties);
        this.blockEntityType = blockEntityType;
    }

    /** Modern replacement for the 1.12.2 {@code createNewTileEntity()} hook. */
    protected abstract TileEntityBase createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state);

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return createBlockEntity(blockEntityType.get(), pos, state);
    }

    /** Drives {@link TileEntityBase#tick()} -- the 1.12.2 {@code ITickable.update()} equivalent. */
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != blockEntityType.get()) {
            return null;
        }
        return (tickLevel, pos, tickState, blockEntity) -> {
            if (blockEntity instanceof TileEntityBase base) {
                base.tick();
            }
        };
    }

    protected void setDeviceMeta(net.minecraft.world.level.BlockGetter level, BlockPos pos, int meta) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntityBase base) {
            base.META = meta;
        }
    }

    /**
     * Device shape follows the block entity's own {@code AABB} (the framework computes it from
     * {@code META} orientation and {@code SIZE}); 1.12.2 did this through {@code getBoundingBox}.
     */
    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TileEntityBase base) {
            return Shapes.create(base.AABB());
        }
        return Shapes.block();
    }

    /**
     * Ported from {@code onBlockPlacedBy}: the placement pitch/yaw picks one of the twelve
     * orientations used by {@code TileEntityBase.setBoundsByXYZ} -- yaw gives 0..3, pitch selects the
     * group (0, +4 for level placement, +8 for ceiling placement).
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        int val = Mth.floor((double) (placer.getYRot() * 4.0F / 360.0F) + 0.5D) & 3;

        if (placer.getXRot() > 22.5F) {
            setDeviceMeta(level, pos, val);
        } else if (placer.getXRot() > -22.5F) {
            setDeviceMeta(level, pos, val + 4);
        } else {
            setDeviceMeta(level, pos, val + 8);
        }
    }
}
