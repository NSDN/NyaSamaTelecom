package club.nsdn.nyasamatelecom.util;

import club.nsdn.nyasamatelecom.NyaSamaTelecom;
import cn.ac.nya.nsasm.NSASM;
import cn.ac.nya.nsasm.Util;
import com.google.common.collect.Lists;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/**
 * Created by drzzm32 on 2019.2.23.
 */
public class NSASMCommand extends CommandBase {

        public NSASMCommand(){
            aliases = Lists.newArrayList(NyaSamaTelecom.MODID, "NSASM", "nsasm");
        }

        private final List<String> aliases;

        @Override
        @Nonnull
        public String getName() {
            return "nsasm";
        }

        @Override
        @Nonnull
        public String getUsage(@Nonnull ICommandSender sender) {
            return "nsasm <code...> (use \";;\" as \\n)";
        }

        @Override
        @Nonnull
        public List<String> getAliases() {
            return aliases;
        }

        @Override
        public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) {
            if (args.length < 1) {
                sender.sendMessage(new TextComponentString(TextFormatting.AQUA + getUsage(sender)));
                return;
            }

            String code = "";
            for (String str : args)
                code = code.concat(str + " ");
            code = code.replace(";;", "\n");

            Util.BUFFER = new StringBuilder();
            new NSASM(32, 32, 16, Util.getSegments(code)).run();
            String result = Util.BUFFER.toString();
            for (String line : result.split("\n"))
                sender.sendMessage(new TextComponentString(TextFormatting.GRAY + line));
        }

        @Override
        public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
            if (sender instanceof EntityPlayer)
                return ((EntityPlayer) sender).isCreative();
            return false;
        }

        @Override
        @Nonnull
        public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
            return Collections.emptyList();
        }

}
