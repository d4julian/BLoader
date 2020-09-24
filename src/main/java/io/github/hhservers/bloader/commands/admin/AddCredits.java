package io.github.hhservers.bloader.commands.admin;

import io.github.hhservers.bloader.util.CreditManager;
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

public class AddCredits implements CommandExecutor {

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        User p = args.<Player>getOne(Text.of("user")).get();
        CreditManager credit = new CreditManager();
        Integer amount = args.<Integer>getOne(Text.of("amount")).get();
        credit.addCredits(p, amount);
        return CommandResult.success();
    }

    public static CommandSpec build(){
        return CommandSpec.builder()
                .arguments(GenericArguments.user(Text.of("user")), GenericArguments.integer(Text.of("amount")))
                .permission("bloader.admin.addcredits")
                .description(Text.of("Base command"))
                .executor(new AddCredits())
                .build();
    }

}
