package me.rtx4090.type;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

import java.util.HashMap;
import java.util.Map;

public class Stats {
    public Map<User, Integer> userMessageCount;

    public Stats(Guild guild) {
        // Initialize userMessageCount or any other stats you want to track
        this.userMessageCount = new HashMap<>();

        for (Member member : guild.getMembers()) {
            User user = member.getUser();
            userMessageCount.put(user, 0); // Initialize each user's message count to 0
        }
    }
}
