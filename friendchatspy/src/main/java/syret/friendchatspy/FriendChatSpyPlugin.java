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
import net.runelite.api.*;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatColorType;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.FriendChatManager;
import net.runelite.client.game.WorldService;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.WorldUtil;
import net.runelite.http.api.worlds.World;
import net.runelite.http.api.worlds.WorldResult;
import okhttp3.HttpUrl;
import org.pf4j.Extension;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Extension
@PluginDescriptor(
        name = "FriendChat Spy",
        description = "FriendChat Spy"
)
@Slf4j
public class FriendChatSpyPlugin extends Plugin {


    private static final DiscordClient DISCORD_CLIENT = new DiscordClient();
    private HttpUrl webhook;
    private String[] whitelistedWords;
    private net.runelite.api.World quickHopTargetWorld;
    private int displaySwitcherAttempts = 0;
    private static final int DISPLAY_SWITCHER_MAX_ATTEMPTS = 3;
    private int ticksWaited = 0;
    private boolean hoppedToWorld = false;

    // Injects our config
    @Inject
    private FriendChatSpyConfig config;

    @Inject
    private FriendChatManager friendChatManager;

    @Inject
    private Client client;

    @Inject
    private WorldService worldService;


    // Provides our config
    @Provides
    FriendChatSpyConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(FriendChatSpyConfig.class);
    }

    @Override
    protected void startUp() {
        this.webhook = HttpUrl.parse(config.webhook());
        this.whitelistedWords = config.whiteListedWords().split(",");

        client.setComparingAppearance(true);
        log.info("Plugin started");
    }

    @Subscribe
    private void onConfigChanged(ConfigChanged event) {
        if (!event.getGroup().equals("FriendChatSpyConfig")) {
            return;
        }
        this.webhook = HttpUrl.parse(config.webhook());
        this.whitelistedWords = config.whiteListedWords().split(",");
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
                break;
            case CHALREQ_TRADE:
                break;
            case BROADCAST:
                break;
            case PRIVATECHAT:
                break;
            case MODPRIVATECHAT:
                break;
            case PRIVATECHATOUT:
                break;
            case MODCHAT:
                break;
            case PUBLICCHAT:
                break;
            case FRIENDSCHAT:
                if(config.onlyscoutonaccount()){
                    if(!client.getLocalPlayer().getName().equalsIgnoreCase(config.onlyscoutaccountname())){
                        return;
                    }
                }

                boolean sendMessage = true;
                String messageAuthor = chatMessage.getName();
                String message = chatMessage.getMessage();
                String worldNumber = "";
                String messageAuthorRank = "";
                FriendsChatManager friendsChatManager = client.getFriendsChatManager();
                if (friendsChatManager == null || friendsChatManager.getCount() == 0) {
                    return;
                }
                if (config.onlySendWhitelistedMessages()) {
                    sendMessage = false;
                    for (String s : whitelistedWords) {
                        if (message.toLowerCase().contains(s.toLowerCase())) {
                            sendMessage = true;
                        }
                    }
                }

                FriendsChatMember[] FCMembers = friendsChatManager.getMembers();
                for (FriendsChatMember fcm : FCMembers) {
                    if (fcm.getName().equalsIgnoreCase(messageAuthor)) {
                        worldNumber = "world:" + fcm.getWorld();
                        messageAuthorRank = "rank:" + fcm.getRank().name();
                        if (sendMessage) {
                            message(messageAuthor, message, worldNumber, messageAuthorRank);
                            if (config.hopToWorld()) {
                                hop(fcm.getWorld());
                                hoppedToWorld = true;
                            }
                        }
                    }
                }
                break;
            case AUTOTYPER:
                break;
            case MODAUTOTYPER:
                break;
            case CONSOLE:
                break;
        }

    }

    private void message(String messageAuthor, String message, String worldNumber, String messageAuthorRank) {
        if (this.webhook == null) {
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

    private void hop(int worldId) {
        assert client.isClientThread();

        WorldResult worldResult = worldService.getWorlds();
        // Don't try to hop if the world doesn't exist
        World world = worldResult.findWorld(worldId);
        if (world == null) {
            return;
        }

        final net.runelite.api.World rsWorld = client.createWorld();
        rsWorld.setActivity(world.getActivity());
        rsWorld.setAddress(world.getAddress());
        rsWorld.setId(world.getId());
        rsWorld.setPlayerCount(world.getPlayers());
        rsWorld.setLocation(world.getLocation());
        rsWorld.setTypes(WorldUtil.toWorldTypes(world.getTypes()));

        if (client.getGameState() == GameState.LOGIN_SCREEN) {
            // on the login screen we can just change the world by ourselves
            client.changeWorld(rsWorld);
            return;
        }

        quickHopTargetWorld = rsWorld;
        displaySwitcherAttempts = 0;
    }

    @Subscribe
    public void onGameTick(GameTick event) {

        if(config.hopBackToWorld() && hoppedToWorld){
            if(ticksWaited >= config.hopBackWorldWaitTicks()){
                log.info("waited ticks" + ticksWaited + " hopping back");
                hop(config.hopBackWorldNumber());
                hoppedToWorld = false;
                ticksWaited = 0;
            }
        }

        if (quickHopTargetWorld == null) {
            if(config.hopBackToWorld() && hoppedToWorld){
                ticksWaited++;
                log.info("waited ticks" + ticksWaited);
            }
            return;
        }

        if (client.getWidget(WidgetInfo.WORLD_SWITCHER_LIST) == null) {
            client.openWorldHopper();

            if (++displaySwitcherAttempts >= DISPLAY_SWITCHER_MAX_ATTEMPTS) {
                String chatMessage = new ChatMessageBuilder()
                        .append(ChatColorType.NORMAL)
                        .append("Failed to quick-hop after ")
                        .append(ChatColorType.HIGHLIGHT)
                        .append(Integer.toString(displaySwitcherAttempts))
                        .append(ChatColorType.NORMAL)
                        .append(" attempts.")
                        .build();

                resetQuickHopper();
            }
        } else {
            client.hopToWorld(quickHopTargetWorld);
            resetQuickHopper();
        }
    }


    private void resetQuickHopper() {
        displaySwitcherAttempts = 0;
        quickHopTargetWorld = null;
    }
}