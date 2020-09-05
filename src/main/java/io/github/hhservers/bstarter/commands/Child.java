package io.github.hhservers.bstarter.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

public class Child implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        //do things
        return CommandResult.success();
    }

    public static CommandSpec build(){
       return CommandSpec.builder()
                .permission("butils.user.base.child")
                .description(Text.of("Child command of Base"))
                .executor(new Child())
                .build();
    }
}
