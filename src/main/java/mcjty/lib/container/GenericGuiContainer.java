package mcjty.lib.container;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import mcjty.lib.base.ModBase;
import mcjty.lib.entity.GenericTileEntity;
import mcjty.lib.gui.GuiSideWindow;
import mcjty.lib.gui.Window;
import mcjty.lib.network.Argument;
import mcjty.lib.network.PacketServerCommand;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Container;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class GenericGuiContainer<T extends GenericTileEntity> extends GuiContainer {

    protected ModBase modBase;
    protected SimpleNetworkWrapper network;

    protected Window window;
    protected final T tileEntity;

    private GuiSideWindow sideWindow;

    public GenericGuiContainer(ModBase mod, SimpleNetworkWrapper network, T tileEntity, Container container, int manual, String manualNode) {
        super(container);
        this.modBase = mod;
        this.network = network;
        this.tileEntity = tileEntity;
        sideWindow = new GuiSideWindow(manual, manualNode);
    }

    @Override
    public void initGui() {
        super.initGui();
        sideWindow.initGui(modBase, network, mc, this, guiLeft, guiTop, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int i, int i2) {
        int x = Mouse.getEventX() * width / mc.displayWidth;
        int y = height - Mouse.getEventY() * height / mc.displayHeight - 1;

        List<String> tooltips = window.getTooltips();
        if (tooltips != null) {
            drawHoveringText(tooltips, window.getTooltipItems(), x - guiLeft, y - guiTop, mc.fontRendererObj);
        }

        tooltips = sideWindow.getWindow().getTooltips();
        if (tooltips != null) {
            drawHoveringText(tooltips, window.getTooltipItems(), x - guiLeft, y - guiTop, mc.fontRendererObj);
        }
        RenderHelper.enableGUIStandardItemLighting();
    }

    private List parseString(String s, List<ItemStack> items) {
        List l = new ArrayList<>();
        String current = "";
        int i = 0;
        while (i < s.length()) {
            String c = s.substring(i, i + 1);
            if ("@".equals(c)) {
                if (!current.isEmpty()) {
                    l.add(current);
                    current = "";
                }
                i++;
                int itemIdx = Integer.parseInt(s.substring(i, i + 1));
                l.add(items.get(itemIdx));
            }
            i++;
        }
        if (!current.isEmpty()) {
            l.add(current);
        }
        return l;
    }

    private void drawHoveringText(List<String> textLines, List<ItemStack> items, int x, int y, FontRenderer font) {
        if (!textLines.isEmpty()) {
            GlStateManager.disableRescaleNormal();
            net.minecraft.client.renderer.RenderHelper.disableStandardItemLighting();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            int i = 0;

            int linesWithItemStacks = 0;
            for (String s : textLines) {
                int j;
                if (s.contains("@") && !items.isEmpty()) {
                    linesWithItemStacks++;
                    List list = parseString(s, items);
                    j = 0;
                    for (Object o : list) {
                        if (o instanceof String) {
                            j += font.getStringWidth((String) o);
                        } else {
                            j += 20;    // ItemStack
                        }
                    }

                } else {
                    j = font.getStringWidth(s);
                }

                if (j > i) {
                    i = j;
                }
            }

            int xx = x + 12;
            int yy = y - 12;
            int k = 8;

            if (textLines.size() > 1) {
                k += 2 + (textLines.size() - 1) * 10 + linesWithItemStacks * 8;
            }

            if (xx + i > this.width) {
                xx -= 28 + i;
            }

            if (yy + k + 6 > this.height) {
                yy = this.height - k - 6;
            }

            this.zLevel = 300.0F;
            this.itemRender.zLevel = 300.0F;
            int l = -267386864;
            this.drawGradientRect(xx - 3, yy - 4, xx + i + 3, yy - 3, l, l);
            this.drawGradientRect(xx - 3, yy + k + 3, xx + i + 3, yy + k + 4, l, l);
            this.drawGradientRect(xx - 3, yy - 3, xx + i + 3, yy + k + 3, l, l);
            this.drawGradientRect(xx - 4, yy - 3, xx - 3, yy + k + 3, l, l);
            this.drawGradientRect(xx + i + 3, yy - 3, xx + i + 4, yy + k + 3, l, l);
            int i1 = 1347420415;
            int j1 = (i1 & 16711422) >> 1 | i1 & -16777216;
            this.drawGradientRect(xx - 3, yy - 3 + 1, xx - 3 + 1, yy + k + 3 - 1, i1, j1);
            this.drawGradientRect(xx + i + 2, yy - 3 + 1, xx + i + 3, yy + k + 3 - 1, i1, j1);
            this.drawGradientRect(xx - 3, yy - 3, xx + i + 3, yy - 3 + 1, i1, i1);
            this.drawGradientRect(xx - 3, yy + k + 2, xx + i + 3, yy + k + 3, j1, j1);

            for (int k1 = 0; k1 < textLines.size(); ++k1) {
                String s1 = textLines.get(k1);
                if (s1.contains("@") && !items.isEmpty()) {
                    List list = parseString(s1, items);
                    int curx = xx;
                    for (Object o : list) {
                        if (o instanceof String) {
                            font.drawStringWithShadow(s1, (float) curx, (float) yy, -1);
                            curx += font.getStringWidth((String) o);
                        } else {
                            mcjty.lib.gui.RenderHelper.renderObject(mc, curx+1, yy, (ItemStack) o, false);
                            curx += 20;
                        }
                    }
                    y += 8;
                } else {
                    font.drawStringWithShadow(s1, (float) xx, (float) yy, -1);
                }

                if (k1 == 0) {
                    yy += 2;
                }

                yy += 10;
            }

            this.zLevel = 0.0F;
            this.itemRender.zLevel = 0.0F;
            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
            net.minecraft.client.renderer.RenderHelper.enableStandardItemLighting();
            GlStateManager.enableRescaleNormal();
        }
    }


    protected void drawWindow() {
        window.draw();
        sideWindow.getWindow().draw();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);
        window.mouseClicked(x, y, button);
        sideWindow.getWindow().mouseClicked(x, y, button);
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        window.handleMouseInput();
        sideWindow.getWindow().handleMouseInput();
    }

    /*
     * 99% sure this is the correct one
     */
    @Override
    protected void mouseReleased(int x, int y, int state) {
        super.mouseReleased(x, y, state);
        window.mouseMovedOrUp(x, y, state);
        sideWindow.getWindow().mouseMovedOrUp(x, y, state);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (!window.keyTyped(typedChar, keyCode)) {
            super.keyTyped(typedChar, keyCode);
        }
    }

    protected void sendServerCommand(SimpleNetworkWrapper network, String command, Argument... arguments) {
        network.sendToServer(new PacketServerCommand(tileEntity.getPos(), command, arguments));
    }
}
