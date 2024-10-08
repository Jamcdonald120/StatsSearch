package de.guntram.mcmod.statssearch.mixins;

import de.guntram.mcmod.statssearch.StatsSearch;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.StatsScreen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.stat.StatHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(StatsScreen.class)

public class StatsScreenMixin extends Screen {
    
    private TextFieldWidget searchField;
    @Shadow private AlwaysSelectedEntryListWidget<?> selectedList;
    @Shadow private StatsScreen.GeneralStatsListWidget generalStats;
    @Shadow private StatsScreen.ItemStatsListWidget itemStats;
    @Shadow private StatsScreen.EntityStatsListWidget mobStats;
    @Shadow public void createLists() {};
    @Shadow public void selectStatList(AlwaysSelectedEntryListWidget<?> list) {}
    @Shadow private @Final StatHandler statHandler;
    
    public StatsScreenMixin(Text text) {
        super(text);
    }

    @Inject(method="init", at=@At("RETURN"))
    private void resetSearch(CallbackInfo ci) {
        StatsSearch.setSearchString("");
    }
    
    @Inject(method="createButtons", at=@At("RETURN"))
    public void createSearchField(CallbackInfo ci) {
        this.searchField = new TextFieldWidget(this.textRenderer, this.width/2+10, 15, 145, 18, Text.of(""));
    }
    
    @Inject(method="render", at=@At("RETURN"))
    public void renderSearchField(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        StatsSearch.statHandler = this.statHandler;
        if (searchField != null) {
            searchField.render(context, mouseX, mouseY, delta);
        }
    }
    
    @Inject(method="renderStatItem", at=@At("HEAD")) 
    public void highlightStatItem(DrawContext context, int x, int y, Item item, CallbackInfo ci) {
    }
    
    @Redirect(method="render", at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/DrawContext;drawCenteredTextWithShadow(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/text/Text;III)V"))
    public void drawNotSoCenteredText(DrawContext context, TextRenderer textRenderer,
            Text text, int centerX, int Y, int color) {
        context.drawTextWithShadow(textRenderer, text, centerX-145, Y, color);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchField != null && searchField.keyPressed(keyCode, scanCode, modifiers)) {
            StatsSearch.setSearchString(searchField.getText());
            recreateStatsLists();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (searchField != null) {
            if (searchField.mouseClicked(mouseX, mouseY, mouseButton)) {
                searchField.setFocused(true);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }    
    
    @Override
    public boolean charTyped(char chr, int keyCode) {
        if (searchField.charTyped(chr, keyCode)) {
            StatsSearch.setSearchString(searchField.getText());
            recreateStatsLists();
            return true;
        }
        return super.charTyped(chr, keyCode);
    }
    
    private void recreateStatsLists() {
        int curState = 0;
        if (selectedList == this.generalStats) {
            curState = 0;
        } else if (selectedList == this.itemStats) {
            curState = 1;
        } else if (selectedList == this.mobStats) {
            curState = 2;
        }
        createLists();
        if (curState == 0) {
            selectStatList(this.generalStats);
        } else if (curState == 1) {
            selectStatList(this.itemStats);
        } else if (curState == 2)  {
            selectStatList(this.mobStats);
        }
    }
}
