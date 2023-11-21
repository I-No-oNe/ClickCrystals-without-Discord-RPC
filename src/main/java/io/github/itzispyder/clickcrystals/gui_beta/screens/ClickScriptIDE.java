package io.github.itzispyder.clickcrystals.gui_beta.screens;

import io.github.itzispyder.clickcrystals.client.clickscript.ClickScript;
import io.github.itzispyder.clickcrystals.events.listeners.UserInputListener;
import io.github.itzispyder.clickcrystals.gui_beta.elements.AbstractElement;
import io.github.itzispyder.clickcrystals.gui_beta.elements.display.LoadingIconElement;
import io.github.itzispyder.clickcrystals.gui_beta.elements.interactive.TextFieldElement;
import io.github.itzispyder.clickcrystals.gui_beta.misc.ChatColor;
import io.github.itzispyder.clickcrystals.gui_beta.misc.Gray;
import io.github.itzispyder.clickcrystals.gui_beta.misc.Tex;
import io.github.itzispyder.clickcrystals.gui_beta.misc.brushes.RoundRectBrush;
import io.github.itzispyder.clickcrystals.modules.modules.ScriptedModule;
import io.github.itzispyder.clickcrystals.modules.scripts.*;
import io.github.itzispyder.clickcrystals.util.StringUtils;
import io.github.itzispyder.clickcrystals.util.minecraft.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class ClickScriptIDE extends DefaultBase {

    public static final TextFieldElement.TextHighlighter CLICKSCRIPT_HIGHLIGHTER = new TextFieldElement.TextHighlighter() {{
        ChatColor og = getOriginalColor();
        Function<ChatColor, Function<String, String>> applyColor = c -> s -> "%s%s%s".formatted(c, s, og);

        this.put(ChatColor.ORANGE, ClickScript.collectNames());
        this.put(ChatColor.GRAY, "then");
        this.put(s -> StringUtils.startsWithAny(s, ":", "#"), applyColor.apply(ChatColor.DARK_GREEN));
        this.put(s -> s.replaceAll("[0-9><=!.+-]", "").isEmpty(), applyColor.apply(ChatColor.DARK_AQUA));
        this.put(ChatColor.YELLOW, Arrays.stream(InputCmd.Action.values()).map(e -> e.name().toLowerCase()).toList());
        this.put(ChatColor.YELLOW, Arrays.stream(OnEventCmd.EventType.values()).map(e -> e.name().toLowerCase()).toList());
        this.put(ChatColor.YELLOW, Arrays.stream(ModuleCmd.Action.values()).map(e -> e.name().toLowerCase()).toList());
        this.put(ChatColor.YELLOW, Arrays.stream(TurnToCmd.Mode.values()).map(e -> e.name().toLowerCase()).toList());
        this.put(ChatColor.YELLOW, Arrays.stream(IfCmd.ConditionType.values()).map(e -> e.name().toLowerCase()).toList());
    }};
    private final ScriptedModule module;
    private final LoadingIconElement loading;
    private final AbstractElement saveButton, saveAndCloseButton, closeButton, discardChangesButton, openFileButton, openScriptsButton;

    public TextFieldElement textField = new TextFieldElement(contentX, contentY + 21, contentWidth, contentHeight - 21) {{
        this.setHighlighter(CLICKSCRIPT_HIGHLIGHTER);
    }};

    public ClickScriptIDE(ScriptedModule module) {
        super("ClickScript IDE");
        this.addChild(textField);
        this.module = module;

        this.loading = new LoadingIconElement(contentX + contentWidth / 2 - 10, contentY + contentHeight / 2 - 10, 20);
        this.loading.setRendering(false);
        this.addChild(loading);
        this.loadContents();

        // init
        this.navlistModules.forEach(this::removeChild);
        this.removeChild(buttonSearch);

        saveButton = AbstractElement.create().dimensions(navWidth, 12)
                .tooltip("Save contents")
                .onPress(button -> saveContents())
                .onRender((context, mouseX, mouseY, button) -> {
                    if (button.isHovered(mouseX, mouseY)) {
                        RoundRectBrush.drawRoundHoriLine(context, button.x, button.y, navWidth, button.height, Gray.LIGHT_GRAY);
                    }
                    RenderUtils.drawText(context, "Save", button.x + 7, button.y + button.height / 3, 0.7F, false);
                }).build();
        saveAndCloseButton = AbstractElement.create().dimensions(navWidth, 12)
                .tooltip("Save contents then close IDE")
                .onPress(button -> {
                    saveContents();
                    mc.setScreen(new ModuleScreen());
                })
                .onRender((context, mouseX, mouseY, button) -> {
                    if (button.isHovered(mouseX, mouseY)) {
                        RoundRectBrush.drawRoundHoriLine(context, button.x, button.y, navWidth, button.height, Gray.LIGHT_GRAY);
                    }
                    RenderUtils.drawText(context, "Save & Close", button.x + 7, button.y + button.height / 3, 0.7F, false);
                }).build();
        closeButton = AbstractElement.create().dimensions(navWidth, 12)
                .tooltip("Close without saving")
                .onPress(button -> mc.setScreen(new ModuleScreen()))
                .onRender((context, mouseX, mouseY, button) -> {
                    if (button.isHovered(mouseX, mouseY)) {
                        RoundRectBrush.drawRoundHoriLine(context, button.x, button.y, navWidth, button.height, Gray.GENERIC_LOW);
                    }
                    RenderUtils.drawText(context, "Close", button.x + 7, button.y + button.height / 3, 0.7F, false);
                }).build();
        discardChangesButton = AbstractElement.create().dimensions(navWidth, 12)
                .tooltip("Undo all modifications")
                .onPress(button -> loadContents())
                .onRender((context, mouseX, mouseY, button) -> {
                    if (button.isHovered(mouseX, mouseY)) {
                        RoundRectBrush.drawRoundHoriLine(context, button.x, button.y, navWidth, button.height, Gray.GENERIC_LOW);
                    }
                    RenderUtils.drawText(context, "Discard Changes", button.x + 7, button.y + button.height / 3, 0.7F, false);
                }).build();
        openFileButton = AbstractElement.create().dimensions(navWidth, 12)
                .tooltip("Open file in File Explorer")
                .onPress(button -> system.openFile(module.filepath))
                .onRender((context, mouseX, mouseY, button) -> {
                    if (button.isHovered(mouseX, mouseY)) {
                        RoundRectBrush.drawRoundHoriLine(context, button.x, button.y, navWidth, button.height, Gray.LIGHT_GRAY);
                    }
                    RenderUtils.drawText(context, "Open .CCS File", button.x + 7, button.y + button.height / 3, 0.7F, false);
                }).build();
        openScriptsButton = AbstractElement.create().dimensions(navWidth, 12)
                .tooltip("Open scripts folder")
                .onPress(button -> system.openFile(ScriptedModule.PATH))
                .onRender((context, mouseX, mouseY, button) -> {
                    if (button.isHovered(mouseX, mouseY)) {
                        RoundRectBrush.drawRoundHoriLine(context, button.x, button.y, navWidth, button.height, Gray.LIGHT_GRAY);
                    }
                    RenderUtils.drawText(context, "Open Scripts", button.x + 7, button.y + button.height / 3, 0.7F, false);
                }).build();

        this.addChild(saveButton);
        this.addChild(saveAndCloseButton);
        this.addChild(closeButton);
        this.addChild(discardChangesButton);
        this.addChild(openFileButton);
        this.addChild(openScriptsButton);
    }

    @Override
    public void baseRender(DrawContext context, int mouseX, int mouseY, float delta) {
        RenderUtils.fillGradient(context, 0, 0, windowWidth, windowHeight, 0xA03873A9, 0xA0000000);

        context.getMatrices().push();
        context.getMatrices().translate(baseX, baseY, 0);

        // backdrop
        RoundRectBrush.drawRoundRect(context, 0, 0, baseWidth, baseHeight, 10, Gray.BLACK);
        RoundRectBrush.drawTabTop(context, 110, 10, 300, 230, 10, Gray.DARK_GRAY);

        // navbar
        String text;
        int caret = 10;

        RenderUtils.drawTexture(context, Tex.ICON, 8, caret - 2, 10, 10);
        text = "ClickCrystals v%s".formatted(version);
        RenderUtils.drawText(context, text, 22, 11, 0.7F, false);
        caret += 10;
        RenderUtils.drawHorizontalLine(context, 10, caret, 90, 1, Gray.GRAY.argb);
        caret += 6;
        buttonHome.x = baseX + 10;
        buttonHome.y = baseY + caret;
        caret += 12;
        buttonModules.x = baseX + 10;
        buttonModules.y = baseY + caret;
        caret += 12;
        buttonNews.x = baseX + 10;
        buttonNews.y = baseY + caret;
        caret += 12;
        buttonSettings.x = baseX + 10;
        buttonSettings.y = baseY + caret;

        caret += 16;
        RenderUtils.drawHorizontalLine(context, 10, caret, 90, 1, Gray.GRAY.argb);
        caret += 6;
        saveButton.x = baseX + 10;
        saveButton.y = baseY + caret;
        caret += 16;
        saveAndCloseButton.x = baseX + 10;
        saveAndCloseButton.y = baseY + caret;
        caret += 16;
        closeButton.x = baseX + 10;
        closeButton.y = baseY + caret;
        caret += 16;
        discardChangesButton.x = baseX + 10;
        discardChangesButton.y = baseY + caret;

        caret += 16;
        RenderUtils.drawHorizontalLine(context, 10, caret, 90, 1, Gray.GRAY.argb);
        caret += 6;
        openFileButton.x = baseX + 10;
        openFileButton.y = baseY + caret;
        caret += 16;
        openScriptsButton.x = baseX + 10;
        openScriptsButton.y = baseY + caret;

        context.getMatrices().pop();


        // content
        caret = contentY + 10;
        RenderUtils.drawTexture(context, Tex.ICON_CLICKSCRIPT, contentX + 10, caret - 7, 15, 15);
        RenderUtils.drawText(context, "Editing '%s'".formatted(module.filename), contentX + 30, caret - 4, false);
        caret += 10;
        RenderUtils.drawHorizontalLine(context, contentX, caret, 300, 1, Gray.BLACK.argb);
    }

    public void loadContents() {
        if (loading.isRendering()) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            loading.setRendering(true);
            try {
                File file = new File(module.filepath);
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String str = "";

                for (var i = reader.lines().iterator(); i.hasNext();) {
                    str = str.concat(i.next() + " \n");
                }
                reader.close();

                String finalStr = str;
                textField.clear();
                textField.onInput(input -> textField.insertInput(finalStr));
                textField.shiftEnd();
            }
            catch (Exception ex) {
                ex.printStackTrace();
                UserInputListener.openPreviousScreen();
            }
        }).thenRun(() -> {
            loading.setRendering(false);
        });
    }

    public void saveContents() {
        if (loading.isRendering()) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            loading.setRendering(true);
            try {
                File file = new File(module.filepath);
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));

                writer.write(textField.getContent());
                writer.close();
            }
            catch (Exception ex) {
                ex.printStackTrace();
                UserInputListener.openPreviousScreen();
            }
            system.reloadScripts();
        }).thenRun(() -> {
            loading.setRendering(false);
        });
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        mc.setScreen(new ClickScriptIDE(module));
    }
}
