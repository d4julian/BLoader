package io.github.hhservers.bloader.commands;

import io.github.hhservers.bloader.util.Util;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;

import java.util.Optional;

public class List implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Player p = (Player) src;
        Util util = new Util();
        if (args.hasAny(Text.of("player"))) {
            if (p.hasPermission("bloader.admin.listother")) {
                Optional<User> opUser = args.<User>getOne(Text.of("player"));
                if (opUser.isPresent()) {
                    User u = opUser.get();
                    PaginationList.builder()
                            .contents(util.offlineUserLoaderList(u, p))
                            .padding(util.textSerializer("&l&8="))
                            .title(util.textSerializer("&6" + u.getName() + "&b's &aB&dLoaders"))
                            .sendTo(p);
                }
            } else {
                p.sendMessage(util.prefixSerializer("&bYou do not have permission to list other players &aB&dLoader&bs"));
            }
        } else {
            PaginationList.builder()
                    .contents(util.loaderList(p))
                    .padding(util.textSerializer("&l&8="))
                    .title(util.textSerializer("&aB&dLoaders"))
                    .sendTo(p);
        }
        return CommandResult.success();
    }

    public static CommandSpec build() {
        return CommandSpec.builder()
                .permission("bloader.user.list")
                .arguments(GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.user(Text.of("player")))))
                .description(Text.of("Base command"))
                .executor(new List())
                .build();
    }
}
