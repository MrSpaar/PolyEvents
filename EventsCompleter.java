package ci.polyevent;

import org.bson.Document;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventsCompleter implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1 && sender.isOp()) return Arrays.asList("list", "join", "leave", "create", "delete");
        else if (args.length == 1) return Arrays.asList("list", "join", "leave");

        List<String> completions = new ArrayList<>();

        if (args[0].equals("delete") || args[0].equals("join")) {
            for (Document event: MongoDB.getEvents()) {
                completions.add((String) event.get("_id"));
            }

            return completions;
        } else if (args[0].equals("leave")) {
            for (Document event: MongoDB.filterEvents(sender.getName())) {
                completions.add((String) event.get("_id"));
            }

            return completions;
        }

        return null;
    }
}
