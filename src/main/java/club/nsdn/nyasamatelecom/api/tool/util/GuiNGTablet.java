package club.nsdn.nyasamatelecom.api.tool.util;

import club.nsdn.nyasamatelecom.api.network.NGTPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/**
 * In-game NGT script editor, ported from the 1.12.2 {@code GuiNGTablet} ({@code GuiScreen}).
 *
 * <p>This is the first real {@code Screen} of the port, so the contract changes are listed:</p>
 * <ul>
 *   <li>{@code initGui} -&gt; {@link #init()}; {@code buttonList.add} -&gt;
 *       {@code addRenderableWidget}. The paging buttons keep their custom atlas drawing (regions
 *       0/23 horizontally and 192/205 vertically of the same texture), now via
 *       {@code GuiGraphics#blit} instead of {@code drawTexturedModalRect}.</li>
 *   <li>{@code drawScreen} -&gt; {@link #render}; every {@code GL11} call is gone -- the texture bind
 *       is implicit in {@code blit}'s {@link ResourceLocation} argument, and {@code glColor4f} is not
 *       needed because the blit is already un-tinted.</li>
 *   <li>Text goes through {@code GuiGraphics#drawString}/{@code drawWordWrap} with {@code font}
 *       (the old {@code fontRenderer} field), and {@code I18n.format} becomes
 *       {@link Component#translatable}.</li>
 *   <li>{@code keyTyped(char, int)} splits in 1.21.1: key <i>codes</i> (backspace, enter, paste) are
 *       handled in {@link #keyPressed}, while plain character input arrives in
 *       {@link #charTyped(char, int)}. {@code ChatAllowedCharacters} is now
 *       {@link SharedConstants#isAllowedChatCharacter(char)} and the clipboard is read through
 *       {@code Minecraft#keyboardHandler}.</li>
 *   <li>Item data moves to the {@code CUSTOM_DATA} component: {@code hasTagCompound/getTagCompound}
 *       become {@code get(DataComponents.CUSTOM_DATA)}, and the page list is read/written through
 *       {@code CustomData.update}.</li>
 *   <li>{@code wrapper.sendToServer} becomes {@link PacketDistributor#sendToServer}, so the screen no
 *       longer needs a network wrapper (the constructor argument is gone).</li>
 * </ul>
 *
 * <p>Dropped on purpose: {@code Keyboard.enableRepeatEvents(true/false)} has no equivalent in 1.21.1
 * (key repeat is handled by the client's keyboard handler / options). The 1.12.2 {@code try/catch}
 * around {@code super.keyTyped} that printed to {@code System.err} is not reproduced either -- modern
 * input dispatch does not throw there.</p>
 */
public class GuiNGTablet extends Screen {

    private final ResourceLocation ngtGuiTextures;
    private final ItemStack ngtObj;

    private boolean isNotEmpty;
    private int updateCount;
    private final int ngtImageWidth = 192;
    private final int ngtImageHeight = 192;
    private int ngtTotalPages = 1;
    private int currPage;
    private ListTag ngtPages;
    private NextPageButton buttonNextPage;
    private NextPageButton buttonPreviousPage;
    private Button buttonBack;

    public GuiNGTablet(String modid, String texture, ItemStack stack) {
        super(Component.translatable("item." + modid.toLowerCase() + ".ngt"));
        this.ngtGuiTextures = ResourceLocation.fromNamespaceAndPath(modid.toLowerCase(), texture);
        this.ngtObj = stack;

        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            this.ngtPages = data.copyTag().getList("pages", Tag.TAG_STRING);
            this.ngtPages = this.ngtPages.copy();
            this.ngtTotalPages = this.ngtPages.size();
            if (this.ngtTotalPages < 1) {
                this.ngtTotalPages = 1;
            }
        }

        if (this.ngtPages == null) {
            this.ngtPages = new ListTag();
            this.ngtPages.add(StringTag.valueOf(""));
            this.ngtTotalPages = 1;
        }
    }

    @Override
    public void tick() {
        super.tick();
        ++this.updateCount;
    }

    @Override
    protected void init() {
        int left = (this.width - this.ngtImageWidth) / 2;
        int offset = 2;

        // Button's (x, y, width, height, message, onPress, narration) constructor is protected, so
        // outside a Button subclass the builder is the public entry point.
        this.buttonBack = this.addRenderableWidget(Button.builder(
                        CommonComponents.GUI_BACK, button -> {
                            this.onClose();
                            this.sendNgtToServer();
                        })
                .bounds(this.width / 2 + 2, offset + this.ngtImageHeight, 98, 20)
                .build());

        this.buttonNextPage = this.addRenderableWidget(new NextPageButton(1, left + 120, offset + 160, true));
        this.buttonPreviousPage = this.addRenderableWidget(new NextPageButton(2, left + 38, offset + 160, false));
        this.updateButtons();
    }

    private void updateButtons() {
        this.buttonNextPage.visible = true;
        this.buttonPreviousPage.visible = this.currPage > 0;
        this.buttonBack.visible = true;
    }

    private void sendNgtToServer() {
        if (!this.isNotEmpty || this.ngtPages == null) {
            return;
        }

        while (this.ngtPages.size() > 1) {
            String endContent = this.ngtPages.getString(this.ngtPages.size() - 1);
            if (!endContent.isEmpty()) {
                break;
            }
            this.ngtPages.remove(this.ngtPages.size() - 1);
        }

        CustomData.update(DataComponents.CUSTOM_DATA, this.ngtObj, tag -> tag.put("pages", this.ngtPages));
        PacketDistributor.sendToServer(new NGTPacket(this.ngtObj));
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // 1.12.2 key code 14 = backspace, 28/156 = enter (main / numpad).
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            String current = this.pageGetCurrent();
            if (!current.isEmpty()) {
                this.pageSetCurrent(current.substring(0, current.length() - 1));
            }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            this.pageInsertIntoCurrent("\n");
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_V && hasControlDown()) {
            // 1.12.2 handled the paste control character (\u0016) here.
            this.pageInsertIntoCurrent(Minecraft.getInstance().keyboardHandler.getClipboard());
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (StringUtil.isAllowedChatCharacter(codePoint)) {
            this.pageInsertIntoCurrent(Character.toString(codePoint));
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    private String pageGetCurrent() {
        return this.ngtPages != null && this.currPage >= 0 && this.currPage < this.ngtPages.size()
                ? this.ngtPages.getString(this.currPage) : "";
    }

    private void pageSetCurrent(String str) {
        if (this.ngtPages != null && this.currPage >= 0 && this.currPage < this.ngtPages.size()) {
            this.ngtPages.set(this.currPage, StringTag.valueOf(str));
            this.isNotEmpty = true;
        }
    }

    private void pageInsertIntoCurrent(String str) {
        String current = this.pageGetCurrent();
        String next = current + str;
        int height = this.font.wordWrapHeight(next + ChatFormatting.BLACK + "_", 118);
        if (height <= 118 && next.length() < 256) {
            this.pageSetCurrent(next);
        }
    }

    private void addNewPage() {
        if (this.ngtPages != null && this.ngtPages.size() < 50) {
            this.ngtPages.add(StringTag.valueOf(""));
            ++this.ngtTotalPages;
            this.isNotEmpty = true;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int left = (this.width - this.ngtImageWidth) / 2;
        int offset = 2;

        guiGraphics.blit(this.ngtGuiTextures, left, offset, 0, 0, this.ngtImageWidth, this.ngtImageHeight);

        Component pageIndicator = Component.translatable("book.pageIndicator", this.currPage + 1, this.ngtTotalPages);
        String page = this.ngtPages != null && this.currPage >= 0 && this.currPage < this.ngtPages.size()
                ? this.ngtPages.getString(this.currPage) : "";

        if (this.font.isBidirectional()) {
            page = page + "_";
        } else if (this.updateCount / 6 % 2 == 0) {
            page = page + "" + ChatFormatting.WHITE + "_";
        } else {
            page = page + "" + ChatFormatting.GRAY + "_";
        }

        int indicatorWidth = this.font.width(pageIndicator);
        guiGraphics.drawString(this.font, pageIndicator,
                left - indicatorWidth + this.ngtImageWidth - 44, offset + 16, 0xFF6F00, false);
        guiGraphics.drawWordWrap(this.font, Component.literal(page),
                left + 36, offset + 16 + 16, 116, 0xFF6F00);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    /** Paging button drawn from the atlas, ported from the 1.12.2 inner {@code NextPageButton}. */
    class NextPageButton extends Button {

        /** Which page direction this button steps in. */
        private final boolean isForward;

        /** The 1.12.2 ids (1 = next, 2 = previous) kept for the action switch below. */
        private final int legacyId;

        NextPageButton(int legacyId, int x, int y, boolean isForward) {
            // 1.12.2 built these with an empty label and drew the atlas region itself.
            super(x, y, 23, 13, Component.empty(), button -> {
            }, Button.DEFAULT_NARRATION);
            this.legacyId = legacyId;
            this.isForward = isForward;
        }

        public int legacyId() {
            return legacyId;
        }

        @Override
        public void onPress() {
            GuiNGTablet.this.actionPerformed(this.legacyId());
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            if (!this.visible) {
                return;
            }
            boolean hovered = mouseX >= this.getX() && mouseY >= this.getY()
                    && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;

            int u = 0;
            int v = 192;
            if (hovered) {
                u += 23;
            }
            if (!this.isForward) {
                v += 13;
            }

            guiGraphics.blit(ngtGuiTextures, this.getX(), this.getY(), u, v, 23, 13);
        }
    }

    /** 1.12.2 {@code actionPerformed(GuiButton)}; ids are preserved so the switch reads the same. */
    private void actionPerformed(int id) {
        if (id == 0) {
            this.onClose();
            this.sendNgtToServer();
        } else if (id == 1) {
            if (this.currPage < this.ngtTotalPages - 1) {
                ++this.currPage;
            } else {
                this.addNewPage();
                if (this.currPage < this.ngtTotalPages - 1) {
                    ++this.currPage;
                }
            }
        } else if (id == 2) {
            if (this.currPage > 0) {
                --this.currPage;
            }
        }

        this.updateButtons();
    }

    @Override
    public boolean isPauseScreen() {
        // 1.12.2 had no pause flag; keeping the world running matches the old behaviour.
        return false;
    }
}
