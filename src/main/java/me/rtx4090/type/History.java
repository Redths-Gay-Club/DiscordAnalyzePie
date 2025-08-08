package me.rtx4090.type;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;

import java.util.List;

public class History {
    MessageHistory history;
    public List<Message> messages;

    public History(MessageChannel channel) {
        this.history = MessageHistory.getHistoryFromBeginning(channel).complete();
        messages = history.getRetrievedHistory();

    }
}
