package club.nsdn.nyasamatelecom.api.tool;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Tool base class, ported from the 1.12.2 {@code ToolBase}.
 *
 * <p>Almost every method here was renamed or reshaped in 1.21.1, so the mapping is listed rather than
 * left to the diff:</p>
 * <table>
 *   <tr><td>{@code ToolMaterial}</td><td>{@link Tier} ({@code getMaxUses} -&gt; {@code getUses},
 *       {@code getEnchantability} -&gt; {@code getEnchantmentValue}, repair item -&gt;
 *       {@code getRepairIngredient()})</td></tr>
 *   <tr><td>{@code setMaxStackSize/setMaxDamage} in the constructor</td><td>{@link Item.Properties}
 *       ({@code stacksTo(1).durability(...)}), passed in by the caller</td></tr>
 *   <tr><td>{@code hitEntity} / {@code onBlockDestroyed}</td><td>{@code hurtEnemy} /
 *       {@code mineBlock}; {@code damageItem} -&gt; {@code hurtAndBreak(amount, entity, slot)}</td></tr>
 *   <tr><td>{@code onItemRightClick} / {@code onItemUse} / {@code onItemUseFirst}</td>
 *       <td>{@code use} / {@code useOn(UseOnContext)} / {@code onItemUseFirst(ItemStack, UseOnContext)}</td></tr>
 *   <tr><td>{@code itemInteractionForEntity} / {@code onUpdate}</td><td>{@code interactLivingEntity} /
 *       {@code inventoryTick}</td></tr>
 *   <tr><td>{@code getIsRepairable} + OreDictionary matching</td><td>{@code isValidRepairItem} with the
 *       tier's {@code Ingredient} -- the ore dictionary is gone</td></tr>
 *   <tr><td>{@code isFull3D()}</td><td>removed: the held-item transform is data driven now</td></tr>
 * </table>
 */
public class ToolBase extends Item {

    public Tier tier;

    public ToolBase(Item.Properties properties, Tier tier) {
        super(properties.stacksTo(1).durability(tier.getUses()));
        this.tier = tier;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(2, target, EquipmentSlot.MAINHAND);
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        if (!level.isClientSide && state.getDestroySpeed(level, pos) != 0.0F) {
            stack.hurtAndBreak(1, miningEntity, EquipmentSlot.MAINHAND);
        }
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return this.tier.getEnchantmentValue();
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return this.tier.getRepairIngredient().test(repairCandidate)
                || super.isValidRepairItem(stack, repairCandidate);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        return false;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return InteractionResult.PASS;
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return InteractionResult.PASS;
    }

    /** 1.12.2 shape kept: force a block update so clients re-read the block entity. */
    public void updateTileEntity(BlockEntity blockEntity) {
        if (blockEntity == null || blockEntity.getLevel() == null) {
            return;
        }
        BlockState state = blockEntity.getBlockState();
        blockEntity.getLevel().sendBlockUpdated(blockEntity.getBlockPos(), state, state, 2);
    }
}
