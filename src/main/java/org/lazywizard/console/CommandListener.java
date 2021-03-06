package org.lazywizard.console;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazywizard.console.BaseCommand.CommandContext;
import org.lazywizard.console.BaseCommand.CommandResult;

/**
 * Provides an interface to listen for command execution, with the option to intercept the command and execute your own
 * code instead.
 * <p>
 * <b>Important performance note:</b> {@link CommandListener}s are persistent through the entire game session, so be
 * careful with your memory management!
 *
 * @author LazyWizard
 * @since 3.0
 */
public interface CommandListener
{
    /**
     * Called before a console command is executed, and gives the listener a chance to intercept execution and run its
     * own code.
     *
     * @param command            The command that is about to be run. Will always be lowercase.
     * @param args               The arguments passed into the command.
     * @param context            The current console {@link CommandContext}.
     * @param alreadyIntercepted Whether another, higher-priority {@link CommandListener} has already declared they will
     *                           intercept execution for this command. If {@code true}, your listener should not do
     *                           anything that affects execution of the command.
     *
     * @return {@code true} if your listener will take over execution of the command from its normal implementation,
     *         {@code false} otherwise. If {@code true}, {@link #execute(String, String, CommandContext)} will be
     *         called by the console if your listener was the highest priority listener to request execution takeover.
     *
     * @since 3.0
     */
    boolean onPreExecute(@NotNull String command, @NotNull String args, @NotNull CommandContext context, boolean alreadyIntercepted);

    /**
     * Called when your {@link CommandListener} declares that it wants to take over execution of a command.
     * <p>
     * This is <i>only</i> called if your listener returned {@code true} during {@link
     * #onPreExecute(String, String, CommandContext, boolean)} and was the highest priority listener to do so.
     *
     * @param command The command to be executed. Will always be lowercase.
     * @param args    The arguments passed into the command.
     * @param context The current console {@link CommandContext}.
     *
     * @return A {@link CommandResult} describing the result of execution.
     *
     * @see BaseCommand#runCommand(String, CommandContext)
     * @since 3.0
     */
    CommandResult execute(@NotNull String command, @NotNull String args, @NotNull CommandContext context);

    /**
     * Called after a command is run, regardless of whether execution was intercepted by a {@link CommandListener} or
     * not.
     *
     * @param command       The command that was executed. Will always be lowercase.
     * @param args          The arguments passed into the command.
     * @param result        The result of the command's execution.
     * @param context       The current console {@link CommandContext}.
     * @param interceptedBy The {@link CommandListener} that intercepted the commands execution, if any. Will be {@code
     *                      null} if no listener's {@link #onPreExecute(String, String, CommandContext, boolean)}
     *                      returned {@code true}.
     *
     * @since 3.0
     */
    void onPostExecute(@NotNull String command, @NotNull String args, @NotNull CommandResult result,
                       @NotNull CommandContext context, @Nullable CommandListener interceptedBy);
}
