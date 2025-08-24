package xyz.destiall.addons.commands;

import com.gmail.filoghost.holographicdisplays.object.NamedHologram;
import com.gmail.filoghost.holographicdisplays.object.NamedHologramManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import xyz.destiall.addons.Addons;
import xyz.destiall.addons.items.FunFactory;
import xyz.destiall.addons.managers.HologramManager;
import xyz.destiall.java.reflection.Reflect;

import java.util.Arrays;

public class AddonCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args == null || args.length == 0) {
            if (!sender.hasPermission("addons.gui")) {
                sender.sendMessage("Unknown command. Type \"/help\" for help.");
                return false;
            }
            if (sender instanceof Player) {
                FunFactory.openGUI((Player) sender);
                return true;
            }
        } else if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("addons.reload")) {
                sender.sendMessage("Unknown command. Type \"/help\" for help.");
                return false;
            }
            Addons.INSTANCE.reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "Reloaded Addons");
            return true;
        } else if (args[0].equalsIgnoreCase("sudo")) {
            if (!sender.hasPermission("addons.sudo")) {
                sender.sendMessage("Unknown command. Type \"/help\" for help.");
                return false;
            }
            if (args.length > 3) {
                String name = args[1];
                Player player = Bukkit.getPlayer(name);
                if (player == null || !player.isOnline()) {
                    sender.sendMessage(ChatColor.RED + "Player is offline!");
                    return false;
                }
                String cmd = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                cmd = cmd.replace("{player}", player.getName());
                boolean deop = !player.isOp();
                try {
                    if (deop) player.setOp(true);
                    Bukkit.dispatchCommand(player, cmd);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (deop) player.setOp(false);
                return true;
            }
        } else if (args[0].equalsIgnoreCase("knockback")) {
            if (!sender.hasPermission("addons.knockback")) {
                sender.sendMessage("Unknown command. Type \"/help\" for help.");
                return false;
            }
            if (Reflect.doesExist("pt.foxspigot.jar.FoxSpigot")) {
                if (args.length == 3) {
                    Object profile = Reflect.invokeStaticMethod(Reflect.getClass("pt.foxspigot.jar.knockback.KnockbackModule"), "getByName", args[1].toLowerCase());
                    if (profile != null) {
                        Player playerTarget = Bukkit.getPlayer(args[2]);
                        if (playerTarget == null || !playerTarget.isOnline()) {
                            sender.sendMessage(ChatColor.RED + "Player is offline!");
                            return false;
                        }

                        Object entityPlayer =  Reflect.invokeMethod(playerTarget, "getHandle");
                        Reflect.invokeMethod(entityPlayer, "setKnockback", profile);
                    }
                }
            } else {
                sender.sendMessage(ChatColor.RED + "This server is not using FoxSpigot!");
            }
        } else if (args[0].equalsIgnoreCase("hologram")) {
            if (!sender.hasPermission("addons.hologram")) {
                sender.sendMessage("Unknown command. Type \"/help\" for help.");
                return false;
            }
            if (!Addons.HL) {
                sender.sendMessage(ChatColor.RED + "Holographic Displays is not enabled!");
                return false;
            }

            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You need to be a player to execute this command!");
                return false;
            }
            Player p = (Player) sender;

            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /addons hologram [select/addcmd/addeffect/additem/setremove]");
                return false;
            }

            if (args[1].equalsIgnoreCase("select")) {
                if (args.length > 2) {
                    String name = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

                    if (!NamedHologramManager.isExistingHologram(name)) {
                        sender.sendMessage(ChatColor.RED + "Cannot find hologram with name: " + name);
                        return false;
                    }

                    NamedHologram hologram = NamedHologramManager.getHologram(name);
                    if (hologram == null) {
                        sender.sendMessage(ChatColor.RED + "Error while getting hologram name: " + name);
                        return false;
                    }

                    HologramManager.setSelectedHologram(p, hologram);
                    sender.sendMessage(ChatColor.AQUA + "You have selected hologram " + hologram.getName());
                    return false;
                }
                sender.sendMessage(ChatColor.RED + "Usage: /addons hologram select [name]");
                return false;
            } else if (args[1].equalsIgnoreCase("addcmd")) {
                if (args.length > 2) {
                    NamedHologram selected = HologramManager.getSelectedHologram(p);

                    if (selected == null) {
                        sender.sendMessage(ChatColor.RED + "You have to select a hologram first! /addons hologram select [name]");
                        return false;
                    }

                    String cmd = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    HologramManager.addCommand(selected, cmd);
                    sender.sendMessage(ChatColor.GREEN + "Added command to hologram: " + selected.getName());
                    return false;
                }
                sender.sendMessage(ChatColor.RED + "Usage: /addons hologram addcmd [command]");
                return false;
            } else if (args[1].equalsIgnoreCase("additem")) {
                NamedHologram selected = HologramManager.getSelectedHologram(p);
                if (selected == null) {
                    sender.sendMessage(ChatColor.RED + "You have to select a hologram first! /addons hologram select [name]");
                    return false;
                }
                ItemStack item = p.getItemInHand();
                if (item == null || item.getType() == Material.AIR) {
                    sender.sendMessage(ChatColor.RED + "You have to hold the item you are setting!");
                    return false;
                }
                HologramManager.addItem(selected, item);
                sender.sendMessage(ChatColor.GREEN + "Added item to hologram: " + selected.getName());
                return false;
            } else if (args[1].equalsIgnoreCase("addeffect")) {
                if (args.length == 5) {
                    NamedHologram selected = HologramManager.getSelectedHologram(p);

                    if (selected == null) {
                        sender.sendMessage(ChatColor.RED + "You have to select a hologram first! /addons hologram select [name]");
                        return false;
                    }

                    try {
                        PotionEffectType effect = PotionEffectType.getByName(args[2]);
                        int duration = Integer.parseInt(args[3]);
                        int amp = Integer.parseInt(args[4]);
                        HologramManager.addEffect(selected, effect, duration, amp);
                        sender.sendMessage(ChatColor.GREEN + "Added effect to hologram: " + selected.getName());
                    } catch (Exception e) {
                        sender.sendMessage(ChatColor.RED + "Error while adding effect: " + args[2]);
                        e.printStackTrace();
                    }
                    return false;
                }
                sender.sendMessage(ChatColor.RED + "Usage: /addons hologram addeffect [effect] [duration] [amplifier]");
                return false;
            } else if (args[1].equalsIgnoreCase("setremove")) {
                if (args.length == 3) {
                    NamedHologram selected = HologramManager.getSelectedHologram(p);

                    if (selected == null) {
                        sender.sendMessage(ChatColor.RED + "You have to select a hologram first! /addons hologram select [name]");
                        return false;
                    }

                    try {
                        boolean remove = Boolean.parseBoolean(args[2]);
                        HologramManager.setRemoveOnInteract(selected, remove);
                        sender.sendMessage(ChatColor.GREEN + "Added option to hologram: " + selected.getName() + " [" + remove + "]");
                    } catch (Exception e) {
                        sender.sendMessage(ChatColor.RED + "Error while setting option: " + args[2]);
                        e.printStackTrace();
                    }
                    return false;
                }
                sender.sendMessage(ChatColor.RED + "Usage: /addons hologram setremove [true | false]");
                return false;
            }
        }
        return false;
    }
}
