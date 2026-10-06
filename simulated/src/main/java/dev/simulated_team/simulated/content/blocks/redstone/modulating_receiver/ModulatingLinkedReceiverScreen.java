package dev.simulated_team.simulated.content.blocks.redstone.modulating_receiver;

import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.gui.widget.ScrollInput;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import dev.simulated_team.simulated.data.SimLang;
import dev.simulated_team.simulated.index.SimGUITextures;
import dev.simulated_team.simulated.index.SimPartialModels;
import dev.simulated_team.simulated.network.packets.ConfigureModulatingLinkedRecieverPacket;
import dev.simulated_team.simulated.util.SimColors;
import foundry.veil.api.network.VeilPacketManager;
import com.zurrtum.create.client.catnip.gui.AbstractSimiScreen;
import com.zurrtum.create.client.catnip.gui.ScreenOpener;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

public class ModulatingLinkedReceiverScreen extends AbstractSimiScreen {
    private final ModulatingLinkedReceiverBlockEntity be;
    private final SimGUITextures background;
    private IconButton confirmButton;
    private ScrollInput minScroll;
    private ScrollInput maxScroll;
    private int lastModification;
    private final GuiGameElement.GuiPartialRenderBuilder lowerPlate;
    private final GuiGameElement.GuiPartialRenderBuilder upperPlate;
    private final GuiGameElement.GuiBlockStateRenderBuilder blockPreview;
    private int previewMinRange = -1;
    private int previewMaxRange = -1;

    public ModulatingLinkedReceiverScreen(final ModulatingLinkedReceiverBlockEntity be) {
        super(SimLang.translate("gui.modulating_linked_receiver.title").component());
        this.be = be;
        this.background = SimGUITextures.MODULATINGLINK;
        this.lastModification = -1;
        // Create Fly caches partial preview textures by render-state identity.
        // Keep these builders for the screen's lifetime, then release them.
        this.lowerPlate = createPlatePreview(true);
        this.upperPlate = createPlatePreview(false);
        this.blockPreview = GuiGameElement.of(be.getBlockState()
                        .setValue(ModulatingLinkedReceiverBlock.FACING, Direction.UP))
                .scale(2.5f).padding(20).rotate(-22, 63, 0);
    }

    private GuiGameElement.GuiPartialRenderBuilder createPlatePreview(final boolean bottom) {
        return GuiGameElement.of(SimPartialModels.MODULATING_RECEIVER_PLATE)
                .scale(2.5f).padding(20)
                .transform((pose, pt) -> {
                    final float distance = (bottom ? this.be.minRange : this.be.maxRange);
                    final float offset = 5.5f * (distance - 1) * (20 + 256 - 1)
                            / ((256 - 1) * (20 + distance - 1));
                    pose.translate(0.75, 0.75, 0);
                    TransformStack.of(pose).rotateXDegrees(-22).rotateYDegrees(63);
                    pose.scale(1, -1, 1);
                    pose.translate(-0.5, -0.5, -0.5);
                    pose.translate(0, (offset + (bottom ? 0 : 0.5)) / 16.0, 0);
                });
    }

    public static void open(final ModulatingLinkedReceiverBlockEntity be) {
        ScreenOpener.open(new ModulatingLinkedReceiverScreen(be));
    }

    public boolean isThisBlock(final BlockPos pos) {
        return this.be.getBlockPos().equals(pos);
    }

    @Override
    protected void init() {
        this.setWindowSize(this.background.width, this.background.height);
        this.setWindowOffset(-20, 0);
        super.init();

        final int x = this.guiLeft;
        final int y = this.guiTop;

        this.confirmButton =
                new IconButton(x + this.background.width - 33, y + this.background.height - 24, AllIcons.I_CONFIRM);
        this.confirmButton.withCallback(() -> {
            this.onClose();
        });
        this.addRenderableWidget(this.confirmButton);

        this.minScroll = new ScrollInput(x + 55, y + 47, 26, 16);
        this.maxScroll = new ScrollInput(x + 132, y + 47, 26, 16);

        this.minScroll.calling(value -> {
            this.be.minRange = value;
            this.be.maxRange = Math.max(this.be.maxRange, value);
            this.maxScroll.setState(this.be.maxRange);
            this.lastModification = 0;
        });
        this.maxScroll.calling(value -> {
            this.be.maxRange = value;
            this.be.minRange = Math.min(this.be.minRange, value);
            this.minScroll.setState(this.be.minRange);
            this.lastModification = 0;
        });


        this.minScroll.withRange(1, 257)
                .titled(SimLang.translate("gui.modulating_linked_receiver.minimum_range").component())
                .withShiftStep(10)
                .setState(this.be.minRange)
                .onChanged();
        this.maxScroll.withRange(1, 257)
                .titled(SimLang.translate("gui.modulating_linked_receiver.maximum_range").component())
                .withShiftStep(10)
                .setState(this.be.maxRange)
                .onChanged();
        this.addRenderableWidgets(this.minScroll);
        this.addRenderableWidgets(this.maxScroll);

    }

    public static int distanceGuiOffset(final float value, final float maxValue, final float width, final float smoothing) {
        return Math.round(
            (width * (value - 1) * (smoothing + maxValue - 1))
            / ((maxValue - 1) * (smoothing + value - 1))
        );
    }

    @Override
    protected void renderWindow(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final float partialTicks) {
        final int x = this.guiLeft;
        final int y = this.guiTop;

        this.background.render(graphics, x, y);

        graphics.text(this.font, this.title, x + (this.background.width - 8) / 2 - this.font.width(this.title) / 2, y + 4, SimColors.TITLE_DARK_RED, false);

        int currentX = 22;

        this.label(graphics, currentX, 26 - 1, SimLang.translate("gui.modulating_linked_receiver.min").component());
        String text = Integer.toString(this.be.minRange);
        int stringWidth = this.font.width(text);
        this.label(graphics, currentX + 34 + (12 - stringWidth / 2), 26 - 1, Component.literal(text));

        currentX += 77;

        this.label(graphics, currentX, 26 - 1, SimLang.translate("gui.modulating_linked_receiver.max").component());
        text = Integer.toString(this.be.maxRange);
        stringWidth = this.font.width(text);
        this.label(graphics, currentX + 34 + (12 - stringWidth / 2), 26 - 1, Component.literal(text));

        final int bandStart = 37;
        final int bandEnd = 156;
        final int bandWidth = bandEnd - bandStart;
        final float smoothing = 20f;
        final float maxDistance = 256;

        final int minPos = bandStart + distanceGuiOffset(this.be.minRange, maxDistance, bandWidth, smoothing);
        final int maxPos = bandStart + distanceGuiOffset(this.be.maxRange, maxDistance, bandWidth, smoothing);

        final SimGUITextures sprite = SimGUITextures.MODULATINGLINK_POWERED_LANE;

        graphics.blit(RenderPipelines.GUI_TEXTURED, sprite.location, x + bandStart + 1, y + 25,
                sprite.startX, sprite.startY, minPos - bandStart, sprite.height, 256, 256);
        graphics.blit(RenderPipelines.GUI_TEXTURED, sprite.location, x + minPos, y + 25,
                sprite.startX + minPos - bandStart, sprite.startY, maxPos - minPos, sprite.height, 256, 256);

        SimGUITextures.MODULATINGLINK_MARKER.render(graphics, x + minPos, y + 23);
        SimGUITextures.MODULATINGLINK_MARKER.render(graphics, x + maxPos, y + 23);

        if (this.be.getClientDistance(partialTicks) < ModulatingLinkedReceiverBlockEntity.RANGE_LIMIT) {
            final int sourcePos = bandStart + distanceGuiOffset((float) this.be.getClientDistance(partialTicks), maxDistance, bandWidth, smoothing);
            SimGUITextures.MODULATINGLINK_TARGET.render(graphics, x + sourcePos, y + 16);
        }

        if (this.previewMinRange != this.be.minRange || this.previewMaxRange != this.be.maxRange) {
            this.lowerPlate.markDirty();
            this.upperPlate.markDirty();
            this.previewMinRange = this.be.minRange;
            this.previewMaxRange = this.be.maxRange;
        }
        final int previewX = x + this.background.width - 8;
        final int previewY = y + this.background.height - 52;
        this.lowerPlate.at(previewX, previewY).render(graphics);
        this.upperPlate.at(previewX, previewY).render(graphics);
        this.blockPreview.at(previewX, previewY).render(graphics);
    }

    private void label(final GuiGraphicsExtractor graphics, final int x, final int y, final Component text) {
        graphics.text(this.font, text, this.guiLeft + x, this.guiTop + 26 + y, 0xFFFFFFEE);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.lastModification >= 0)
            this.lastModification++;

        if (this.lastModification >= 20) {
            this.lastModification = -1;
            this.send();
        }
    }

    @Override
    public void removed() {
        this.lowerPlate.clear();
        this.upperPlate.clear();
        this.blockPreview.clear();
        this.send();
        super.removed();
    }

    protected void send() {
        VeilPacketManager.server().sendPacket(new ConfigureModulatingLinkedRecieverPacket(this.be.getBlockPos(), this.minScroll.getState(), this.maxScroll.getState()));
    }

}
