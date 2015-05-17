package io.github.yannici.bedwars.Shop.Specials;

import io.github.yannici.bedwars.ChatWriter;
import io.github.yannici.bedwars.Main;
import io.github.yannici.bedwars.Game.Game;
import io.github.yannici.bedwars.Game.GameState;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableMap;

public class RescuePlatformListener implements Listener {

    public RescuePlatformListener() {
        super();
    }
    
    @EventHandler
    public void onInteract(PlayerInteractEvent ev) {
        Player player = ev.getPlayer();
        Game game = Game.getGameOfPlayer(player);
        
        if(game == null) {
            return;
        }
        
        if(game.getState() != GameState.RUNNING) {
            return;
        }
        
        if(ev.getAction().equals(Action.LEFT_CLICK_AIR)
                || ev.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            return;
        }
        
        RescuePlatform platform = new RescuePlatform();
        if(!ev.getMaterial().equals(platform.getItemMaterial())) {
            return;
        }
        
        for(SpecialItem item : game.getSpecialItems()) {
            if(!(item instanceof RescuePlatform)) {
                continue;
            }
            
            RescuePlatform rescuePlatform = (RescuePlatform)item;
            if(!rescuePlatform.getPlayer().equals(player)) {
                continue;
            }
            
            int waitleft = Main.getInstance().getConfig().getInt("specials.rescue-platform.using-wait-time", 20) - rescuePlatform.getLivingTime();
            player.sendMessage(ChatWriter.pluginMessage(Main._l("ingame.specials.rescue-platform.left", ImmutableMap.of("time", String.valueOf(waitleft)))));
            return;
        }
        
        if(player.getLocation().getBlock().getRelative(BlockFace.DOWN).getType() != Material.AIR) {
            player.sendMessage(ChatWriter.pluginMessage(ChatColor.RED + Main._l("errors.notinair")));
            return;
        }
        
        Location mid = player.getLocation().clone();
        mid.setY(mid.getY()-1.0D);
        
        ItemStack usedStack = player.getInventory().getItemInHand();
        usedStack.setAmount(usedStack.getAmount()-1);
        player.getInventory().setItem(player.getInventory().getHeldItemSlot(), usedStack);
        player.updateInventory();
        for(BlockFace face : BlockFace.values()) {
            if(face.equals(BlockFace.DOWN) || face.equals(BlockFace.UP)) {
                continue;
            }
            
            Block placed = mid.getBlock().getRelative(face);
            if(placed.getType() != Material.AIR) {
                continue;
            }
            
            placed.setType(Material.GLASS);
            
            game.getRegion().addPlacedUnbreakableBlock(placed, null);
            platform.addPlatformBlock(placed);
        }
        
        platform.setActivatedPlayer(player);
        platform.setGame(game);
        platform.runTask();
        
        game.addSpecialItem(platform);
    }

}
