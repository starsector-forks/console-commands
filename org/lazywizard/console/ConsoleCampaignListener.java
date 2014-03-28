package org.lazywizard.console;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.InteractionDialogPlugin;
import com.fs.starfarer.api.combat.EngagementResultAPI;
import com.fs.starfarer.api.graphics.PositionAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import java.util.List;
import org.lazywizard.console.BaseCommand.CommandContext;
import org.lwjgl.input.Keyboard;

public class ConsoleCampaignListener implements EveryFrameScript
{
    @Override
    public boolean isDone()
    {
        return false;
    }

    @Override
    public boolean runWhilePaused()
    {
        return true;
    }

    private boolean checkInput()
    {
        KeyStroke key = Console.getSettings().getConsoleSummonKey();
        boolean modPressed = true;

        if (key.requiresShift() && !(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)
                || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)))
        {
            modPressed = false;
        }

        if (key.requiresControl() && !(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
                || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)))
        {
            modPressed = false;
        }

        if (key.requiresAlt() && !(Keyboard.isKeyDown(Keyboard.KEY_LMENU)
                || Keyboard.isKeyDown(Keyboard.KEY_RMENU)))
        {
            modPressed = false;
        }

        if (modPressed && Keyboard.isKeyDown(key.getKey()))
        {
            return true;
        }

        return false;
    }

    @Override
    public void advance(float amount)
    {
        if (checkInput())
        {
            Global.getSector().getCampaignUI().showInteractionDialog(
                    new CampaignPopup(), null);
        }

        Console.advance(CommandContext.CAMPAIGN_MAP);
    }

    // TODO: Hook this into console output
    private class CampaignPopup implements InteractionDialogPlugin
    {
        private final Object LEAVE = new Object();
        private InteractionDialogAPI dialog;
        private KeyListener keyListener;
        private float timeOpen = 0f;

        @Override
        public void init(InteractionDialogAPI dialog)
        {
            this.dialog = dialog;
            keyListener = new KeyListener();
            timeOpen = 0f;

            dialog.getVisualPanel().showCustomPanel(0f, 0f, keyListener);
            dialog.getTextPanel().addParagraph(CommonStrings.INPUT_QUERY);
            dialog.setPromptText("Input: ");

            dialog.getOptionPanel().addOption("Cancel", LEAVE);
            dialog.setOptionOnEscape("Cancel", LEAVE);
            /*KeyStroke key = Console.getSettings().getConsoleSummonKey();
             dialog.getOptionPanel().setShortcut(LEAVE,
             key.getKey(), key.requiresControl(), key.requiresAlt(),
             key.requiresShift(), true);*/
        }

        @Override
        public void optionSelected(String optionText, Object optionData)
        {
            if (optionData == LEAVE)
            {
                dialog.dismiss();
            }
        }

        @Override
        public void optionMousedOver(String optionText, Object optionData)
        {
        }

        @Override
        public void advance(float amount)
        {
            timeOpen += amount;
            dialog.setPromptText("Input: " + keyListener.currentInput.toString()
                    + ((((int) timeOpen) & 1) == 0 ? "|" : ""));
        }

        @Override
        public void backFromEngagement(EngagementResultAPI battleResult)
        {
        }

        @Override
        public Object getContext()
        {
            return null;
        }

        private class KeyListener implements CustomUIPanelPlugin
        {
            StringBuilder currentInput = new StringBuilder();

            public void clearInput()
            {
                currentInput.setLength(0);
            }

            @Override
            public void positionChanged(PositionAPI position)
            {
            }

            @Override
            public void render(float alphaMult)
            {
            }

            @Override
            public void advance(float amount)
            {
            }

            @Override
            public void processInput(List<InputEventAPI> events)
            {
                // TODO: temporary test code; this needs cleanup badly
                for (InputEventAPI event : events)
                {
                    if (event.isConsumed() || !event.isKeyDownEvent()
                            || event.isModifierKey())
                    {
                        continue;
                    }

                    if (event.getEventValue() == Keyboard.KEY_BACK
                            && currentInput.length() > 0)
                    {
                        // Shift+backspace, delete entire line
                        if (event.isShiftDown())
                        {
                            clearInput();
                        }
                        // Control+backspace, delete last word
                        else if (event.isCtrlDown())
                        {
                            int lastSpace = currentInput.lastIndexOf(" ");
                            if (lastSpace == -1)
                            {
                                clearInput();
                            }
                            else
                            {
                                currentInput.delete(lastSpace, currentInput.length());
                            }
                        }
                        // Regular backspace, delete last character
                        else
                        {
                            currentInput.deleteCharAt(currentInput.length() - 1);
                        }

                        event.consume();
                    }
                    else if (event.getEventValue() == Keyboard.KEY_RETURN)
                    {
                        String command = currentInput.toString();
                        dialog.getTextPanel().addParagraph("Running command \""
                                + command + "\"", Console.getSettings().getOutputColor());
                        Console.parseInput(command, CommandContext.CAMPAIGN_MAP);
                        currentInput.setLength(0);
                        event.consume();
                    }
                    else
                    {
                        // Goodbye, international character support...
                        char character = event.getEventChar();
                        if (character >= 0x20 && character <= 0x7e)
                        {
                            currentInput.append(character);
                            event.consume();
                        }
                    }
                }
            }
        }
    }
}
