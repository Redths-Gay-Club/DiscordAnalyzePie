package me.rtx4090;


import me.rtx4090.type.History;
import me.rtx4090.type.Stats;
import me.rtx4090.utils.Drawer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

public class Main {
    public static JDA jda;
    private static Member botMember;

    public static void main(String[] args) {
        startBot(Token.TOKEN);

    }

    public static void startBot(String botToken) {
        try {
            jda = JDABuilder.createDefault(botToken)
                    .enableIntents(GatewayIntent.GUILD_MEMBERS
                            , GatewayIntent.GUILD_MESSAGES
                            , GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(new me.rtx4090.utils.Listener())
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File analyze(String kw, Guild guild) throws IOException {
        botMember = guild.getSelfMember();
        UUID uuid = UUID.randomUUID();
        System.out.println("Starting a analysis function with uuid: " + uuid);

        ArrayList<History> messageHistories = new ArrayList<>();
        Stats stats = new Stats(guild);

        GuildChannel[] channels = guild.getTextChannels().toArray(new GuildChannel[0]);
        for (GuildChannel channel : channels) {
            if (!botMember.hasAccess(channel)) continue;
            messageHistories.add(new History((MessageChannel) channel));
        }
        System.out.println("Message histories have been collected, starting to search for keyword: " + kw);

        for (History history : messageHistories) {
            searchHistory(history.messages, kw, stats);
        }
        System.out.println("Keyword search completed, clean zeros...");

        List<User> toRemove = new ArrayList<>();
        for (Map.Entry<User, Integer> entry : stats.userMessageCount.entrySet()) {
            if (entry.getValue() <= 0) toRemove.add(entry.getKey());
        }
        for (User user : toRemove) {
            stats.userMessageCount.remove(user);
        }
        System.out.println(Arrays.toString(stats.userMessageCount.entrySet().toArray()));

        System.out.println("Drawing stats to a PNG file...");
        Drawer drawer = new Drawer(stats.userMessageCount, java.nio.file.Paths.get(uuid.toString() + ".png"), "Analyzation of Keyword: " + kw);
        drawer.exportToPNG(800, 800);

        System.out.println("Stats have been drawn, returning the file...");
        return Paths.get(uuid.toString() + ".png").toFile();

    }

    public static void searchHistory(List<Message> messages, String kw, Stats stats) {
        for (Message message : messages) {
            if (message.getContentRaw().contains(kw)) {
                stats.userMessageCount.merge(message.getAuthor(), 1, Integer::sum);
                //stats.userMessageCount.put(message.getAuthor(), stats.userMessageCount.get(message.getAuthor()) + 1);
            }
        }
    }
}