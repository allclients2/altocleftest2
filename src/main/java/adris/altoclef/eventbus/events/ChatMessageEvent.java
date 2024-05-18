package adris.altoclef.eventbus.events;

import net.minecraft.text.Style;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.text.Text;

/**
 * Whenever chat appears
 */
public class ChatMessageEvent {
    Text content;


    public ChatMessageEvent(Text content2) {
        this.content = content2;
    }
    public String messageContent() {
        return content.getString();
    }

    public Style messageStyle() {
        return content.getStyle();
    }

    public Optional<String> messageSender() {
        String regex = "^(\\w+)\\s";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(messageContent());

        System.out.println("message: |" + messageContent());
        // Find the username
        if (matcher.find()) {
            System.out.println("Matched!");
            return Optional.of(matcher.group(1));
        } else {
            return Optional.empty();
        }
    }
}
