package io.github.hhservers.bloader.commands;

import io.github.hhservers.bloader.commands.admin.ManualTickLoaders;
import io.github.hhservers.bloader.util.LoaderGUI;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

public class Base implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Player p = (Player)src;
        new LoaderGUI().openGUI(p);
        return CommandResult.success();
    }

    public static CommandSpec build(){
       return CommandSpec.builder()
                .child(List.build(), "list")
                .child(Credits.build(), "credits")
                .child(ManualTickLoaders.build(), "manualtick")
                .permission("bloader.user.base")
                .description(Text.of("Base command"))
                .executor(new Base())
                .build();
    }
}
