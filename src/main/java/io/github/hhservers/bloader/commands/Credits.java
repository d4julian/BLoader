package io.github.hhservers.bloader.commands;

import io.github.hhservers.bloader.commands.admin.AddCredits;
import io.github.hhservers.bloader.commands.admin.RemoveCredits;
import io.github.hhservers.bloader.util.CreditManager;
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
import org.spongepowered.api.text.Text;

import java.util.Optional;

public class Credits implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Player p = (Player) src;
        Util util = new Util();
        CreditManager creditManager = new CreditManager();
        if (args.hasAny(Text.of("player"))) {
            if (p.hasPermission("bloader.admin")) {
                Optional<User> opUser = args.<User>getOne(Text.of("player"));
                if (opUser.isPresent()) {
                    User u = opUser.get();
                    p.sendMessage(util.prefixSerializer("&a" + u.getName() + "&b currently has &l&6" + creditManager.getCredits(u) + "&r&aB&dLoader&bCredits"));
                }
            } else {p.sendMessage(util.prefixSerializer("&bYou do not have permission to list other players &aB&dLoader&bCredits"));}
        } else {
            p.sendMessage(util.prefixSerializer("&bYou currently have &l&6" + creditManager.getCredits(p) + "&r&aB&dLoader&bCredits"));
        }
        return CommandResult.success();
    }

    public static CommandSpec build(){
        return CommandSpec.builder()
                .permission("bloader.user.credits")
                .arguments(GenericArguments.optional(GenericArguments.onlyOne(GenericArguments.user(Text.of("player")))))
                .description(Text.of("Base command"))
                .child(AddCredits.build(), "add")
                .child(RemoveCredits.build(), "remove")
                .executor(new Credits())
                .build();
    }

}
