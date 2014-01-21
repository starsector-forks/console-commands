package org.lazywizard.console;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import org.apache.log4j.Level;
import org.json.JSONException;
import org.json.JSONObject;
import org.lazywizard.console.CommandStore.StoredCommand;
import org.lazywizard.lazylib.JSONUtils;
import org.lazywizard.lazylib.StringUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;

public class Console
{
    private static int CONSOLE_KEY;
    private static Color OUTPUT_COLOR;
    private static int OUTPUT_LINE_LENGTH;
    private static final long MILLISECONDS_BETWEEN_INPUT = 1_500l;
    private static long lastInput = Long.MIN_VALUE;
    private static final List<String> output = new ArrayList<>();

    private static boolean checkInput(CommandContext context)
    {
        String input = JOptionPane.showInputDialog(null,
                "Enter command, or 'help' for a list of valid commands.");
        if (input == null)
        {
            return false;
        }

        String[] tmp = input.split(" ", 1);
        String com = tmp[0].toLowerCase();
        String args = (tmp.length > 1 ? tmp[1] : "");

        try
        {
            StoredCommand stored = CommandStore.retrieveCommand(com);
            if (stored == null)
            {
                showMessage("No such command \"" + com + "\" registered!");
                Global.getLogger(Console.class).log(Level.ERROR,
                        "No such command \"" + com + "\" registered!");
                return false;
            }

            BaseCommand command = stored.getCommandClass().newInstance();
            return command.runCommand(args, context);
        }
        catch (Exception ex)
        {
            showMessage("Failed to execute command \"" + input
                    + "\" in context " + context);
            Global.getLogger(Console.class).log(Level.ERROR,
                    "Failed to execute command \"" + input
                    + "\" in context " + context, ex);
            return false;
        }
    }

    public static void showMessage(String message)
    {
        output.addAll(Arrays.asList(StringUtils.wrapString(message,
                OUTPUT_LINE_LENGTH).split("\n")));
    }

    public static void reloadSettings() throws IOException, JSONException
    {
        JSONObject settings = Global.getSettings().loadJSON(
                "data/console/console_settings.json");
        CONSOLE_KEY = settings.getInt("consoleKey");
        OUTPUT_COLOR = JSONUtils.toColor(settings.getJSONArray("outputColor"));

        // Console pop-up appearance settings (temporary)
        Color color = JSONUtils.toColor(settings.getJSONArray("backgroundColor"));
        UIManager.put("Panel.background", color);
        UIManager.put("OptionPane.background", color);
        UIManager.put("TextArea.background", color);
        UIManager.put("TextField.background", color);
        UIManager.put("Button.background", color);
        UIManager.put("SplitPane.background", color);

        color = JSONUtils.toColor(settings.getJSONArray("foregroundColor"));
        UIManager.put("OptionPane.messageForeground", color);

        color = JSONUtils.toColor(settings.getJSONArray("textColor"));
        UIManager.put("TextArea.foreground", color);
        UIManager.put("TextField.foreground", color);
        UIManager.put("TextField.caretForeground", color);

        color = JSONUtils.toColor(settings.getJSONArray("buttonColor"));
        UIManager.put("Button.foreground", color);
        UIManager.put("SplitPane.foreground", color);
    }

    static void advance(CommandContext context)
    {
        if ((lastInput + MILLISECONDS_BETWEEN_INPUT <= System.currentTimeMillis())
                && Keyboard.isKeyDown(CONSOLE_KEY))
        {
            lastInput = System.currentTimeMillis();
            checkInput(context);
        }

        // TODO: Extract this into its own method
        if (!output.isEmpty())
        {
            if (context == CommandContext.CAMPAIGN)
            {
                for (String tmp : output)
                {
                    Global.getSector().getCampaignUI().addMessage(tmp, OUTPUT_COLOR);
                }
            }
            else
            {
                CombatEngineAPI engine = Global.getCombatEngine();
                ShipAPI player = engine.getPlayerShip();

                if (player == null || engine.isEntityInPlay(player))
                {
                    // Print output later, once there's a player on-screen
                    return;
                }

                // Merge all output into one big String
                // This prevents output from overlapping itself
                StringBuilder sb = new StringBuilder();
                for (String tmp : output)
                {
                    sb.append(tmp + "\n");
                }

                // TODO: add font size setting and modify this block to use it
                String message = sb.toString();
                String[] lines = message.split("\n");
                for (int x = 0; x < lines.length; x++)
                {
                    engine.addFloatingText(Vector2f.add(
                            new Vector2f(-message.length() / 20f,
                                    -(player.getCollisionRadius() + 50 + (x * 25))),
                            player.getLocation(), null),
                            lines[x], 25f, OUTPUT_COLOR, player, 0f, 0f);
                }
            }

            output.clear();
        }
    }
}