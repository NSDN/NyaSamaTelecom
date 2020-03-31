package club.nsdn.nyasamatelecom.api.tool.util;

import club.nsdn.nyasamatelecom.api.tool.NGTablet;
import club.nsdn.nyasamatelecom.api.util.NSASM;
import club.nsdn.nyasamatelecom.api.util.Util;
import club.nsdn.nyasamatelecom.network.NetworkWrapper;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NGTCommand implements ICommand {

    @Override
    @Nonnull
    public String getName() {
        return "ngt";
    }

    @Override
    @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "just type /ngt";
    }

    @Override
    @Nonnull
    public List<String> getAliases() {
        return Arrays.asList("ngt", "NGT");
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        if (sender instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) sender;
            ItemStack stack = player.getHeldItemMainhand();
            if (player.getHeldItemOffhand().getItem() instanceof NGTablet)
                stack = player.getHeldItemOffhand();
            if (!(stack.getItem() instanceof NGTablet)) {
                player.sendMessage(new TextComponentString(TextFormatting.DARK_RED + "You should held NGTablet!"));
                return;
            }
            NBTTagList list = Util.getTagListFromNGT(stack);
            String code = "";
            if (list != null)
                code = NSASM.getCodeString(list);

            String finalCode = code;
            new Thread(() -> new NGTEditor(NetworkWrapper.instance).setCode(finalCode)).start();
            player.sendMessage(new TextComponentString(TextFormatting.GRAY + "NGTEditor started"));
        }
    }

    @Override
    public boolean checkPermission(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender) {
        return true;
    }

    @Override
    @Nonnull
    public List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args, @Nullable BlockPos pos) {
        return Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(@Nonnull String[] args, int i) {
        return false;
    }

    @Override
    public int compareTo(@Nonnull ICommand o) {
        return 0;
    }

}
