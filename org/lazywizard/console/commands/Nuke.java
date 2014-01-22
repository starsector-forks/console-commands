package org.lazywizard.console.commands;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.mission.FleetSide;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.BaseCommand.CommandContext;
import org.lazywizard.console.BaseCommand.CommandResult;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;

public class Nuke implements BaseCommand
{
    @Override
    public CommandResult runCommand(String args, CommandContext context)
    {
        if (context == CommandContext.CAMPAIGN)
        {
            Console.showMessage(CommonStrings.ERROR_COMBAT_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }

        CombatEngineAPI engine = Global.getCombatEngine();

        for (ShipAPI ship : engine.getShips())
        {
            if (ship.isHulk() || ship.isShuttlePod())
            {
                continue;
            }

            if (ship.getOwner() == FleetSide.ENEMY.ordinal())
            {
                engine.applyDamage(ship, ship.getLocation(), 500_000,
                        DamageType.ENERGY, 500_000, true, false, null);
            }
        }

        Console.showMessage("Nuke activated. All enemy ships destroyed.");
        return CommandResult.SUCCESS;
    }
}