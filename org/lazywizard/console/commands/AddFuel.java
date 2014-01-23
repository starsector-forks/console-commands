package org.lazywizard.console.commands;

import com.fs.starfarer.api.Global;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.BaseCommand.CommandContext;
import org.lazywizard.console.BaseCommand.CommandResult;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;

public class AddFuel implements BaseCommand
{
    @Override
    public CommandResult runCommand(String args, CommandContext context)
    {
        if (context == CommandContext.COMBAT_MISSION)
        {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }

        int amount;
        try
        {
            amount = Integer.parseInt(args);
        }
        catch (NumberFormatException ex)
        {
            Console.showMessage("Error: fuel amount must be a whole number!");
            return CommandResult.BAD_SYNTAX;
        }

        Global.getSector().getPlayerFleet().getCargo().addFuel(amount);
        Console.showMessage("Added " + amount + " fuel to player inventory.");
        return CommandResult.SUCCESS;
    }
}
