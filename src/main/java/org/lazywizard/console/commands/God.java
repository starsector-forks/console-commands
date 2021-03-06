package org.lazywizard.console.commands;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;
import org.lazywizard.console.cheatmanager.CheatPlugin;
import org.lazywizard.console.cheatmanager.CheatTarget;
import org.lazywizard.console.cheatmanager.CombatCheatManager;
import org.lazywizard.lazylib.CollectionUtils;

public class God implements BaseCommand
{
    private static final String CHEAT_ID = "lw_console_god";

    @Override
    public CommandResult runCommand(String args, CommandContext context)
    {
        if (!context.isInCombat())
        {
            Console.showMessage(CommonStrings.ERROR_COMBAT_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }

        // If no argument is entered:
        // - If cheat is not enabled, enable it for the player only
        // - If cheat is already enabled, disable it
        CheatTarget appliesTo = null;
        if (args.isEmpty())
        {
            if (CombatCheatManager.isEnabled(CHEAT_ID))
            {
                CombatCheatManager.disableCheat(CHEAT_ID);
                Console.showMessage("God mode disabled.");
                return CommandResult.SUCCESS;
            }
            else
            {
                appliesTo = Console.getSettings().getDefaultCombatCheatTarget();
            }
        }

        // If argument is entered, try to parse it as a valid cheat target
        if (appliesTo == null)
        {
            appliesTo = CombatCheatManager.parseTargets(args);
            if (appliesTo == null)
            {
                Console.showMessage("Bad target! Valid targets: " + CollectionUtils.implode(CheatTarget.class) + ".");
                return CommandResult.ERROR;
            }
        }

        CombatCheatManager.enableCheat(CHEAT_ID, "God Mode (" + appliesTo.name() + ")",
                new GodPlugin(), appliesTo);
        Console.showMessage("God mode enabled for " + appliesTo.name() + ".");
        return CommandResult.SUCCESS;
    }

    private static class GodPlugin extends CheatPlugin
    {
        @Override
        public void advance(@NotNull ShipAPI ship, float amount)
        {
            final MutableShipStatsAPI stats = ship.getMutableStats();
            stats.getCombatEngineRepairTimeMult().modifyMult(CHEAT_ID, 0f);
            stats.getCombatWeaponRepairTimeMult().modifyMult(CHEAT_ID, 0f);
            stats.getEnergyDamageTakenMult().modifyMult(CHEAT_ID, 0f);
            stats.getKineticDamageTakenMult().modifyMult(CHEAT_ID, 0f);
            stats.getHighExplosiveDamageTakenMult().modifyMult(CHEAT_ID, 0f);
            stats.getFragmentationDamageTakenMult().modifyMult(CHEAT_ID, 0f);
            stats.getEmpDamageTakenMult().modifyMult(CHEAT_ID, 0f);
            stats.getArmorDamageTakenMult().modifyMult(CHEAT_ID, 0.00001f);
        }

        @Override
        public void unapply(@NotNull ShipAPI ship)
        {
            final MutableShipStatsAPI stats = ship.getMutableStats();
            stats.getCombatEngineRepairTimeMult().unmodify(CHEAT_ID);
            stats.getCombatWeaponRepairTimeMult().unmodify(CHEAT_ID);
            stats.getEnergyDamageTakenMult().unmodify(CHEAT_ID);
            stats.getKineticDamageTakenMult().unmodify(CHEAT_ID);
            stats.getHighExplosiveDamageTakenMult().unmodify(CHEAT_ID);
            stats.getFragmentationDamageTakenMult().unmodify(CHEAT_ID);
            stats.getEmpDamageTakenMult().unmodify(CHEAT_ID);
            stats.getArmorDamageTakenMult().unmodify(CHEAT_ID);
        }

        @Override
        public boolean runWhilePaused()
        {
            return false;
        }
    }
}
