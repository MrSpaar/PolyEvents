package ci.polyevent;

import org.bson.Document;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EventsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0 || sender instanceof BlockCommandSender) return true;

        if (args[0].equals("list")) {
            StringBuilder builder = new StringBuilder("Liste des évènements :\n");

            for (Document event: MongoDB.getEvents()) {
                builder.append(ChatColor.GREEN).append(ChatColor.BOLD).append(event.get("_id")).append(" : ").append(ChatColor.RESET)
                        .append(ChatColor.ITALIC).append(ChatColor.AQUA).append(event.get("description")).append(".\n");
            }

            String message = builder.toString();

            if (message.equals("Liste des évènements :\n")) sender.sendMessage(ChatColor.RED + "Aucun évènement en cours.");
            else sender.sendMessage(builder.toString());

            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Cette commande n'est accessible qu'aux joueurs !");
            return true;
        }

        if (args[0].equals("join")) {
            Document event = MongoDB.findEvent(args[1]);

            if (event == null) {
                player.sendMessage(ChatColor.RED + "L'évènement \"" + args[1] + "\" n'existe pas !");
                return true;
            }

            List<Double> coords = event.getList("location", Double.class);

            World world = player.getServer().getWorld((String) event.get("world"));
            Location eventLocation = new Location(world, coords.get(0), coords.get(1), coords.get(2));
            Location playerLocation = player.getLocation();

            MongoDB.updatePlayer(
                    args[1], player.getName(), player.getWorld().getName(),
                    playerLocation.getX(), playerLocation.getY(), playerLocation.getZ()
            );

            player.teleport(eventLocation);
            player.sendMessage(ChatColor.GREEN + "Tu as rejoint l'évènement \"" + args[1] + "\".");
        } else if (args[0].equals("leave")) {
            Document document = MongoDB.findPlayer(args[1], player.getName());

            if (document == null) {
                player.sendMessage(ChatColor.RED + "Tu n'as pas rejoint l'évènement \"" + args[1] + "\" !");
                return true;
            }

            List<Double> coords = document.getList("location", Double.class);

            World world = player.getServer().getWorld((String) document.get("world"));
            Location location = new Location(world, coords.get(0), coords.get(1), coords.get(2));

            player.teleport(location);
            player.sendMessage(ChatColor.RED + "Tu as quitté l'évènement \"" + args[1] + "\"");

            MongoDB.removePlayer(args[1], player.getName());
        }

        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "Tu n'as pas la permission de faire ça !");
            return true;
        }

        if (args[0].equals("create")) {
            Document event = MongoDB.findEvent(args[1]);
            if (event != null) {
                player.sendMessage(ChatColor.RED + "L'évènement \"" + args[1] + "\" existe déjà !");
                return true;
            }

            Location location = player.getLocation();

            StringBuilder builder = new StringBuilder();
            for (int i = 2; i < args.length; i++) builder.append(args[i]).append(" ");

            MongoDB.createEvent(
                    args[1], builder.toString(), player.getWorld().getName(),
                    location.getX(), location.getY(), location.getZ()
            );

            player.sendMessage(ChatColor.GREEN + "Tu as créé l'évènement \"" + args[1] + "\".");
        } else if (args[0].equals("delete")) {
            Document event = MongoDB.findEvent(args[1]);
            if (event == null) {
                player.sendMessage(ChatColor.RED + "L'évènement \"" + args[1] + "\" n'existe pas !");
                return true;
            }

            MongoDB.deleteEvent(args[1]);
            player.sendMessage(ChatColor.GREEN + "Tu as supprimé l'évènement \"" + args[1] + "\".");
        }

        return true;
    }
}
