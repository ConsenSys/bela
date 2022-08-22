package org.hyperledger.bela.windows;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import org.hyperledger.bela.components.KeyControls;
import org.hyperledger.bela.dialogs.BelaDialog;
import org.hyperledger.bela.utils.ConnectionMessageMonitor;
import org.hyperledger.besu.ethereum.p2p.peers.Peer;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.MessageData;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.messages.DisconnectMessage;
import org.hyperledger.besu.ethereum.p2p.rlpx.wire.messages.HelloMessage;

import static org.hyperledger.bela.windows.Constants.KEY_CLOSE;

enum MessageCode {
    HELLO(0x00),
    DISCONNECT(0x01),
    PING(0x02),
    PONG(0x03);

    private final int code;

    MessageCode(final int code) {
        this.code = code;
    }

    static Optional<MessageCode> fromCode(final int code) {
        for (MessageCode messageCode : MessageCode.values()) {
            if (messageCode.code == code) {
                return Optional.of(messageCode);
            }
        }
        return Optional.empty();
    }
}

public class PeerDetailWindow implements BelaWindow {
    private final WindowBasedTextGUI gui;
    private Peer activePeer;
    private List<ConnectionMessageMonitor.DirectedMessage> messages = new ArrayList<>();

    public PeerDetailWindow(final WindowBasedTextGUI gui) {
        this.gui = gui;
    }

    private static String parseMessage(final MessageData message) {
        if (message instanceof DisconnectMessage disconnectMessage) {
            return "DisconnectMessage: " + disconnectMessage.getReason();
        }

        return  "code: "+ message.getCode();
    }

    @Override
    public String label() {
        return "Peer Detail";
    }

    @Override
    public MenuGroup group() {
        return MenuGroup.P2P;
    }

    @Override
    public Window createWindow() {
        final Window window = new BasicWindow(label());
        window.setHints(List.of(Window.Hint.FULL_SCREEN));
        Panel panel = new Panel(new LinearLayout());

        KeyControls controls = new KeyControls()
                .addControl("Close", KEY_CLOSE, window::close);
        window.addWindowListener(controls);
        panel.addComponent(controls.createComponent());

        panel.addComponent(new Label("Peer ID: " + activePeer.getId()));
        panel.addComponent(new Label("Peer URL: " + activePeer.getEnodeURLString()));
        final ActionListBox actionListBox = new ActionListBox(new TerminalSize(60, messages.size()));

        Panel conversation = new Panel(new LinearLayout(Direction.VERTICAL));
        for (ConnectionMessageMonitor.DirectedMessage message : messages) {
            actionListBox.addItem(message.getMessageType().toString()+": "+ parseMessage(message.getMessageData()), () -> {
                BelaDialog.showMessage(gui,"Data", message.getMessageData().getData().toString());
            });
        }
        conversation.addComponent(actionListBox);

        panel.addComponent(conversation);

        window.setComponent(panel);
        return window;
    }

    public void setActivePeer(final Peer peer, final List<ConnectionMessageMonitor.DirectedMessage> conversations) {
        this.activePeer = peer;
        this.messages = conversations;
    }
}
