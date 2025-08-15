package me.rtx4090;


import me.rtx4090.type.History;
import me.rtx4090.type.Stats;
import me.rtx4090.utils.Drawer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.io.File;
import java.io.IOException;
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
                    .setActivity(Activity.playing("!analyze <keyword>"))
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File analyze(String kw, Guild guild) throws IOException {
        botMember = guild.getSelfMember();
        UUID uuid = UUID.randomUUID();
        System.out.println("Starting analysis: " + uuid);

        Stats stats = new Stats(guild);
        kw = kw.toLowerCase(Locale.ROOT); // 統一大小寫

        String finalKw = kw;
        guild.getTextChannels().forEach(channel -> {
            if (!botMember.hasAccess(channel)) return;

            String channelName = channel.getName();
            System.out.println("Fetching messages from #" + channelName);

            // 初始分頁
            List<Message> batch = channel.getHistory().retrievePast(100).complete();

            while (!batch.isEmpty()) {
                for (Message message : batch) {
                    // 過濾
                    if (message.getAuthor().isBot() || message.isWebhookMessage()) continue;
                    if (message.getType() != net.dv8tion.jda.api.entities.MessageType.DEFAULT) continue;

                    // 關鍵字比對
                    if (message.getContentRaw().toLowerCase(Locale.ROOT).contains(finalKw) && !message.getContentRaw().startsWith("!analyze")) {
                        System.out.println("Found keyword in #" + channelName + " from " + message.getAuthor().getName() + ": " + message.getContentRaw());
                        stats.userMessageCount.merge(message.getAuthor(), 1, Integer::sum);
                    }
                }
                // 分頁遞迴
                long oldestId = batch.get(batch.size() - 1).getIdLong();
                batch = channel.getHistoryBefore(oldestId, 100).complete().getRetrievedHistory();

                try { Thread.sleep(350); } catch (InterruptedException ignored) {}
            }
        });

        // 移除計數為 0 的使用者
        stats.userMessageCount.entrySet().removeIf(e -> e.getValue() <= 0);

        System.out.println("Final counts: " + stats.userMessageCount);

        Drawer drawer = new Drawer(stats.userMessageCount,
                java.nio.file.Paths.get(uuid.toString() + ".png"),
                "Keyword Analysis: " + kw);
        drawer.exportToPNG(800, 800);

        return java.nio.file.Paths.get(uuid.toString() + ".png").toFile();
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