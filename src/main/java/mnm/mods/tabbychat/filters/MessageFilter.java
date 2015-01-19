package mnm.mods.tabbychat.filters;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import mnm.mods.tabbychat.TabbyChat;
import mnm.mods.tabbychat.api.Channel;
import mnm.mods.tabbychat.api.TabbyAPI;
import mnm.mods.tabbychat.api.filters.Filter;
import mnm.mods.tabbychat.api.filters.FilterEvent;
import mnm.mods.tabbychat.util.MessagePatterns;

/**
 * Base class for filters that just need to set the
 */
public class MessageFilter extends TabFilter {


    @Override
    public Pattern getPattern() {
        try {
            // Quickly update the pattern
            MessagePatterns messege =
                    TabbyChat.getInstance().serverSettings.messegePattern.getValue();
            String pattern =
                    String.format("(?:%s|%s)", messege.getOutgoing(), messege.getIncoming());
            setPattern(pattern);
        } catch (PatternSyntaxException e) {
            TabbyChat.getLogger().error(e);
        }
        return super.getPattern();
    }

    @Override
    public void action(Filter filter, FilterEvent event) {
        String player = event.matcher.group(1);
        Channel dest = TabbyAPI.getAPI().getChat().getChannel(player);
        if (dest.getPrefix().isEmpty()) {
            dest.setPrefix("/msg " + player);
        }
        event.channels.add(dest);
    }
}
