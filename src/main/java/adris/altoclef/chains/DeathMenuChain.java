package adris.altoclef.chains;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.mixins.DeathScreenAccessor;
import adris.altoclef.tasksystem.TaskChain;
import adris.altoclef.tasksystem.TaskRunner;
import adris.altoclef.util.time.TimerGame;
import adris.altoclef.util.time.TimerReal;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.*;
import net.minecraft.client.gui.screen.multiplayer.*;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;

public class DeathMenuChain extends TaskChain {

    // Sometimes we fuck up, so we might want to retry considering the death screen.
    private final TimerReal deathRetryTimer = new TimerReal(8);
    private final TimerGame reconnectTimer = new TimerGame(1);
    private final TimerGame waitOnDeathScreenBeforeRespawnTimer = new TimerGame(2);
    private ServerInfo prevServerEntry = null;
    private boolean reconnecting = false;
    private int deathCount = 0;
    private Class<? extends Screen> prevScreen = null;


    public DeathMenuChain(TaskRunner runner) {
        super(runner);
    }

    private boolean shouldAutoRespawn(AltoClef mod) {
        return mod.getModSettings().isAutoRespawn();
    }

    private boolean shouldAutoReconnect(AltoClef mod) {
        return mod.getModSettings().isAutoReconnect();
    }

    @Override
    protected void onStop(AltoClef mod) {

    }

    @Override
    public void onInterrupt(AltoClef mod, TaskChain other) {

    }

    @Override
    protected void onTick(AltoClef mod) {

    }

    @Override
    public float getPriority(AltoClef mod) {
        //MinecraftClient.getInstance().getCurrentServerEntry().address;
//        MinecraftClient.getInstance().
        Screen screen = MinecraftClient.getInstance().currentScreen;

        // This might fix Weird fail to respawn that happened only once
        if (prevScreen == DeathScreen.class) {
            if (deathRetryTimer.elapsed()) {
                Debug.logMessage("(RESPAWN RETRY WEIRD FIX...)");
                deathRetryTimer.reset();
                prevScreen = null;
            }
        } else {
            deathRetryTimer.reset();
        }
        // Keep track of the last server we were on so we can re-connect.
        if (AltoClef.inGame()) {
            prevServerEntry = MinecraftClient.getInstance().getCurrentServerEntry();
        }

        if (screen instanceof DeathScreen) {
            if (waitOnDeathScreenBeforeRespawnTimer.elapsed()) {
                waitOnDeathScreenBeforeRespawnTimer.reset();
                if (shouldAutoRespawn(mod)) {
                    deathCount++;
                    Debug.logMessage("RESPAWNING... (this is death #" + deathCount + ")");
                    assert MinecraftClient.getInstance().player != null;
                    Text screenMessage = ((DeathScreenAccessor) screen).getMessage();
                    String deathMessage = screenMessage != null ? screenMessage.getString() : "Unknown"; //"(not implemented yet)"; //screen.children().toString();
                    MinecraftClient.getInstance().player.requestRespawn();
                    MinecraftClient.getInstance().setScreen(null);
                    for (String i : mod.getModSettings().getDeathCommand().split(" & ")) {
                        String command = i.replace("{deathmessage}", deathMessage);
                        String prefix = mod.getModSettings().getCommandPrefix();
                        while (MinecraftClient.getInstance().player.isAlive()) ;
                        if (!command.isEmpty()) {
                            if (command.startsWith(prefix)) {
                                AltoClef.getCommandExecutor().execute(command, () -> {
                                }, Throwable::printStackTrace);
                            } else if (command.startsWith("/")) {
                                MinecraftClient.getInstance().player.networkHandler.sendChatCommand(command.substring(1));
                            } else {
                                MinecraftClient.getInstance().player.networkHandler.sendChatMessage(command);
                            }
                        }
                    }
                } else {
                    // Cancel if we die and are not auto-respawning.
                    mod.cancelUserTask();
                }
            }
        } else {
            if (AltoClef.inGame()) {
                waitOnDeathScreenBeforeRespawnTimer.reset();
            }
            if (screen instanceof DisconnectedScreen) {
                if (shouldAutoReconnect(mod)) {
                    Debug.logMessage("RECONNECTING: Going to Multiplayer Screen");
                    reconnecting = true;
                    MinecraftClient.getInstance().setScreen(new MultiplayerScreen(new TitleScreen()));
                } else {
                    // Cancel if we disconnect and are not auto-reconnecting.
                    mod.cancelUserTask();
                }
            } else if (screen instanceof MultiplayerScreen && reconnecting && reconnectTimer.elapsed()) {
                reconnectTimer.reset();
                Debug.logMessage("RECONNECTING: Going ");
                reconnecting = false;

                if (prevServerEntry == null) {
                    Debug.logWarning("Failed to re-connect to server, no server entry cached.");
                } else {
                    MinecraftClient client = MinecraftClient.getInstance();
                    ConnectScreen.connect(screen, client, ServerAddress.parse(prevServerEntry.address), prevServerEntry, false,null);
                    //ConnectScreen.connect(screen, client, ServerAddress.parse(_prevServerEntry.address), _prevServerEntry);
                    //client.setScreen(new ConnectScreen(screen, client, _prevServerEntry));
                }
            }
        }
        if (screen != null)
            prevScreen = screen.getClass();
        return Float.NEGATIVE_INFINITY;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public String getName() {
        return "Death Menu Respawn Handling";
    }
}
