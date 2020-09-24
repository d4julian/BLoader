package io.github.hhservers.bloader.commands.admin;

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
import org.spongepowered.api.text.Text;

public class RemoveCredits implements CommandExecutor {

    private Util util = new Util();

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Player p = args.<Player>getOne(Text.of("player")).get();
        CreditManager credit = new CreditManager();
        Integer amount = args.<Integer>getOne(Text.of("amount")).get();
        if(credit.removeCredits(p,amount)){ src.sendMessage(util.prefixSerializer("&bSuccessfully removed credits from player balance"));}
        else{src.sendMessage(util.prefixSerializer("&bPlayer does not have enough credits. Player only has:&6 "+credit.getCredits(p)+"&aB&dLoader&bCredits."));}
        return CommandResult.success();
    }

    public static CommandSpec build(){
        return CommandSpec.builder()
                .arguments(GenericArguments.player(Text.of("player")), GenericArguments.integer(Text.of("amount")))
                .permission("bloader.admin.removecredits")
                .description(Text.of("Base command"))
                .executor(new RemoveCredits())
                .build();
    }

}
