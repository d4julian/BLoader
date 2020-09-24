package io.github.hhservers.bloader.commands.admin;

import io.github.hhservers.bloader.util.Util;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;

public class ManualTickLoaders implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        new Util().manualTickLoaders();
        return CommandResult.success();
    }

    public static CommandSpec build(){
        return CommandSpec.builder()
                .permission("bloader.admin.manualtick")
                .arguments(GenericArguments.player(Text.of("player")))
                .description(Text.of("List Others Loaders command"))
                .executor(new ManualTickLoaders())
                .build();
    }

}
