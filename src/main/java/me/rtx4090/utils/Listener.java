package me.rtx4090.utils;

import me.rtx4090.Main;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class Listener extends ListenerAdapter {
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String args[] = event.getMessage().getContentRaw().split(" ");
        if (args[0].equalsIgnoreCase("!analyze")) {
            if (args.length < 2) {
                event.getChannel().sendMessage("Usage: !analyze <keyword>").queue();
                return;
            }

            System.out.println("A ananlyze command has been received from " + event.getAuthor().getName() + " in " + event.getGuild().getName());

            String kw = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            Guild guild = event.getGuild();
            try {
                File file = Main.analyze(kw, guild);
                if (file.exists()) {
                    event.getChannel().sendMessage("").addFiles(FileUpload.fromData(file)).queue();
                } else {
                    event.getChannel().sendMessage("Analysis failed, please try again.").queue();
                }
            } catch (IOException e) {
                event.getChannel().sendMessage("An error occurred while analyzing the keyword.").queue();
                throw new RuntimeException(e);
            }

        } else return;
    }
}