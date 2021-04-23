package syret.friendchatspy;

import com.google.inject.Provides;

import javax.inject.Inject;

import com.openosrs.http.api.discord.DiscordClient;
import com.openosrs.http.api.discord.DiscordEmbed;
import com.openosrs.http.api.discord.DiscordMessage;
import com.openosrs.http.api.discord.embed.AuthorEmbed;
import com.openosrs.http.api.discord.embed.FieldEmbed;
import com.openosrs.http.api.discord.embed.FooterEmbed;
import com.openosrs.http.api.discord.embed.ThumbnailEmbed;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.FriendsChatManager;
import net.runelite.api.FriendsChatMember;
import net.runelite.api.MessageNode;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.FriendChatManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import okhttp3.HttpUrl;
import org.pf4j.Extension;

import java.util.ArrayList;
import java.util.List;

@Extension
@PluginDescriptor(
        name = "FriendChat Spy",
        description = "FriendChat Spy"
)
@Slf4j
public class FriendChatSpyPlugin extends Plugin {


    private static final DiscordClient DISCORD_CLIENT = new DiscordClient();
    private HttpUrl webhook;

    // Injects our config
    @Inject
    private FriendChatSpyConfig config;

    @Inject
    private FriendChatManager friendChatManager;

    @Inject
    private Client client;

    // Provides our config
    @Provides
    FriendChatSpyConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(FriendChatSpyConfig.class);
    }

    @Override
    protected void startUp() {
        this.webhook = HttpUrl.parse(config.webhook());

        client.setComparingAppearance(true);
        log.info("Plugin started");
    }

    @Subscribe
    private void onConfigChanged(ConfigChanged event)
    {
        if (!event.getGroup().equals("FriendChatSpyConfig"))
        {
            return;
        }
        this.webhook = HttpUrl.parse(config.webhook());
    }

    @Override
    protected void shutDown() {
        // runs on plugin shutdown
        client.setComparingAppearance(false);
        log.info("Plugin stopped");
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        MessageNode messageNode = chatMessage.getMessageNode();
        boolean update = false;

        switch (chatMessage.getType()) {
            case TRADEREQ:
            case CHALREQ_TRADE:
            case BROADCAST:
            case PRIVATECHAT:
            case MODPRIVATECHAT:
            case PRIVATECHATOUT:
            case MODCHAT:
            case PUBLICCHAT:
            case FRIENDSCHAT:
                String messageAuthor = chatMessage.getName();
                String message = chatMessage.getMessage();
                String worldNumber = "";
                String messageAuthorRank = "";
                FriendsChatManager friendsChatManager = client.getFriendsChatManager();
                if (friendsChatManager == null || friendsChatManager.getCount() == 0) {
                    return;
                }
                FriendsChatMember[] FCMembers = friendsChatManager.getMembers();
                for (FriendsChatMember fcm : FCMembers) {
                    if (fcm.getName().equalsIgnoreCase(messageAuthor)) {
                        worldNumber = "world:" + fcm.getWorld();
                        messageAuthorRank = "rank:" + fcm.getRank().name();
                        message(messageAuthor, message, worldNumber, messageAuthorRank);
                    }
                }
                break;
            case AUTOTYPER:
            case MODAUTOTYPER:
            case CONSOLE:
        }

    }

    private void message(String messageAuthor, String message, String worldNumber, String messageAuthorRank) {
        if(this.webhook == null){
            log.info("webhook is null");
            return;
        }

        List<FieldEmbed> fields = new ArrayList<>();
        fields.add(FieldEmbed.builder().name("Rank").value(messageAuthorRank).build());
        fields.add(FieldEmbed.builder().name("world").value(worldNumber).build());
        fields.add(FieldEmbed.builder().name("message").value(message).build());

        DiscordEmbed embed = new DiscordEmbed(
                AuthorEmbed.builder().icon_url("https://images.emojiterra.com/twitter/512px/1f5e3.png").name(messageAuthor).build(),
                ThumbnailEmbed.builder().build(),
                " ",
                FooterEmbed.builder().build(),
                "16711935",
                fields

        );

        DiscordMessage discordMessage = embed.toDiscordMessage("Friendchat spy", " ", "https://icon-library.com/images/eyes-icon/eyes-icon-4.jpg");

        DISCORD_CLIENT.message(this.webhook, discordMessage);

        log.info("tried to send message");
    }
}