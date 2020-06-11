package xyz.n7mn.dev.mspplugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;


public class MinecartEvent implements Listener {
    JavaPlugin plugin = null;
    boolean b = false;
    Material BlockTypeBak1;
    Material BlockTypeBak2;
    Location BlockBakLoc1;
    Location BlockBakLoc2;
    Minecart CartBak;
    Material CartBlockTypeBak;

    public MinecartEvent(JavaPlugin p){
        plugin = p;
    }

    @EventHandler
    public void createMineCart(VehicleCreateEvent e){
        if (e.getVehicle() instanceof  Minecart){
            Minecart cart = (Minecart) e.getVehicle();
            cart.setMaxSpeed(0.4d);
        }
    }

    @EventHandler
    public void setSpeed(VehicleMoveEvent e){
        if (e.getVehicle() instanceof Minecart) {
            Minecart cart = (Minecart) e.getVehicle();

            int moveX = e.getTo().getBlockX() - e.getFrom().getBlockX();
            int moveZ = e.getTo().getBlockZ() - e.getFrom().getBlockZ();

            Block block1 = null;
            Block block2 = null;

            String moveRotate = "Y";

            if (moveX != 0) {
                block1 = cart.getWorld().getBlockAt(cart.getLocation().getBlockX(), cart.getLocation().getBlockY(), cart.getLocation().getBlockZ() - 1);
                block2 = cart.getWorld().getBlockAt(cart.getLocation().getBlockX(), cart.getLocation().getBlockY(), cart.getLocation().getBlockZ() + 1);
                moveRotate = "X";
            }
            if (moveZ != 0) {
                block1 = cart.getWorld().getBlockAt(cart.getLocation().getBlockX() - 1, cart.getLocation().getBlockY(), cart.getLocation().getBlockZ());
                block2 = cart.getWorld().getBlockAt(cart.getLocation().getBlockX() + 1, cart.getLocation().getBlockY(), cart.getLocation().getBlockZ());
                moveRotate = "Z";
            }

            Sign sign = null;
            if (block1 != null) {
                if (SignCheck(block1.getType())) {
                    sign = (Sign) block1.getState();
                }
            }
            if (block2 != null) {
                if (SignCheck(block2.getType())) {
                    sign = (Sign) block2.getState();
                }
            }
            if (sign != null && sign.getLine(0).equals("[msp]")) {
                double speed = cart.getMaxSpeed();

                double tSpeed = -1d;
                try {
                    tSpeed = Double.parseDouble(sign.getLine(1));
                }catch (Exception ex){
                    tSpeed = -1d;
                }
                if (tSpeed != -1d) {
                    cart.setMaxSpeed(0.4d * Double.parseDouble(sign.getLine(1)));
                }

                if (sign.getLine(1).equals("Stop")) {
                    cart.setMaxSpeed(0.4d);

                    int x = cart.getLocation().getBlockX();
                    int y = cart.getLocation().getBlockY();
                    int z = cart.getLocation().getBlockZ();

                    CartBlockTypeBak = cart.getWorld().getBlockAt(cart.getLocation()).getType();
                    CartBak = cart;
                    cart.getWorld().getBlockAt(cart.getLocation()).setType(Material.RAIL);
                    if (moveRotate.equals("X")) {
                        BlockTypeBak1 = cart.getWorld().getBlockAt(x + 1, y, z).getType();
                        BlockTypeBak2 = cart.getWorld().getBlockAt(x - 1, y, z).getType();
                        BlockBakLoc1 = cart.getWorld().getBlockAt(x + 1, y, z).getLocation();
                        BlockBakLoc2 = cart.getWorld().getBlockAt(x - 1, y, z).getLocation();

                        cart.getWorld().getBlockAt(x + 1, y, z).setType(Material.STONE);
                        cart.getWorld().getBlockAt(x - 1, y, z).setType(Material.STONE);
                    } else if (moveRotate.equals("Z")) {
                        BlockTypeBak1 = cart.getWorld().getBlockAt(x, y, z - 1).getType();
                        BlockTypeBak2 = cart.getWorld().getBlockAt(x, y, z + 1).getType();
                        BlockBakLoc1 = cart.getWorld().getBlockAt(x, y, z + 1).getLocation();
                        BlockBakLoc2 = cart.getWorld().getBlockAt(x, y, z - 1).getLocation();

                        cart.getWorld().getBlockAt(x, y, z + 1).setType(Material.STONE);
                        cart.getWorld().getBlockAt(x, y, z - 1).setType(Material.STONE);
                    }
                    b = true;
                }
            }

            if (sign != null){
                if (sign.getLine(0).equals("[msp]") && sign.getLine(1).equals("SpeedCheck")){
                    sign.setLine(1,(cart.getMaxSpeed() / 0.4d)+"");
                    sign.setLine(2,"( "+((cart.getMaxSpeed() / 0.4d)*28.8) + "km/h )");
                    sign.update();
                }
            }

            // Stop看板 1block手前で減速させる
            block1 = null;
            block2 = null;
            if (moveRotate.equals("X")){
                if (moveX > 0){
                    block1 = cart.getWorld().getBlockAt(cart.getLocation().getBlockX() + 1, cart.getLocation().getBlockY(), cart.getLocation().getBlockZ() + 1);
                    block2 = cart.getWorld().getBlockAt(cart.getLocation().getBlockX() + 1, cart.getLocation().getBlockY(), cart.getLocation().getBlockZ() - 1);
                }else{
                    block1 = cart.getWorld().getBlockAt(cart.getLocation().getBlockX() - 1, cart.getLocation().getBlockY(), cart.getLocation().getBlockZ() + 1);
                    block2 = cart.getWorld().getBlockAt(cart.getLocation().getBlockX() - 1, cart.getLocation().getBlockY(), cart.getLocation().getBlockZ() - 1);
                }
            }else if (moveRotate.equals("Z")){
                if (moveZ > 0){
                    block1 = cart.getWorld().getBlockAt(cart.getLocation().getBlockX() + 1, cart.getLocation().getBlockY(), cart.getLocation().getBlockZ() + 1);
                    block2 = cart.getWorld().getBlockAt(cart.getLocation().getBlockX() - 1, cart.getLocation().getBlockY(), cart.getLocation().getBlockZ() + 1);
                }else{
                    block1 = cart.getWorld().getBlockAt(cart.getLocation().getBlockX() + 1, cart.getLocation().getBlockY(), cart.getLocation().getBlockZ() - 1);
                    block2 = cart.getWorld().getBlockAt(cart.getLocation().getBlockX() - 1, cart.getLocation().getBlockY(), cart.getLocation().getBlockZ() - 1);
                }
            }
            sign = null;
            if (block1 != null && SignCheck(block1.getType())){
                sign = (Sign)block1.getState();
            }
            if (block2 != null && SignCheck(block2.getType())){
                sign = (Sign)block2.getState();
            }
            if (sign != null && sign.getLine(0).equals("[msp]") && sign.getLine(1).equals("Stop")){
                cart.setMaxSpeed(0.4d);
            }

        }
    }

    @EventHandler
    public void setSignSpeed(SignChangeEvent e) {
        if (e.getLine(0).equals("[msp]")){

            double speed = 0d;
            try {
                speed = Double.parseDouble(e.getLine(1));
            }catch (Exception ex){
                speed = 0d;
            }
            if (speed >= 0.1d && speed <= 255d){
                e.setLine(2,"Speed :");
                e.setLine(3,"   "+(speed * 28.8d)+" km/h");
            }else if(!e.getLine(1).equals("Stop") && !e.getLine(1).equals("SpeedCheck")){
                e.setLine(1,"1");
                e.setLine(2,"Speed :");
                e.setLine(3,"   28.8 km/h");
            }
        }
    }

    @EventHandler
    public void BlockEvent(VehicleBlockCollisionEvent e){
        if (b){

            Location tempBlockLoc = e.getBlock().getLocation();
            if (
                (tempBlockLoc.getBlockX() == BlockBakLoc1.getBlockX() && tempBlockLoc.getBlockZ() == BlockBakLoc1.getBlockZ()) ||
                (tempBlockLoc.getBlockX() == BlockBakLoc2.getBlockX() && tempBlockLoc.getBlockZ() == BlockBakLoc2.getBlockZ())
            ) {
                /*
                plugin.getLogger().info("Cart x :" + CartBak.getLocation().getBlockX());
                plugin.getLogger().info("Cart z :" + CartBak.getLocation().getBlockZ());
                plugin.getLogger().info("EventBlock x :" + tempBlockLoc.getBlockX());
                plugin.getLogger().info("EventBlock z :" + tempBlockLoc.getBlockZ());
                plugin.getLogger().info("BakBlock1 x :" + BlockBakLoc1.getBlockX());
                plugin.getLogger().info("BakBlock1 z :" + BlockBakLoc1.getBlockZ());
                plugin.getLogger().info("BakBlock2 x :" + BlockBakLoc2.getBlockX());
                plugin.getLogger().info("BakBlock2 z :" + BlockBakLoc2.getBlockZ());
                */

                BlockBakLoc1.getBlock().setType(BlockTypeBak2);
                BlockBakLoc2.getBlock().setType(BlockTypeBak2);
                CartBak.getLocation().getBlock().setType(CartBlockTypeBak);
            }
        }
    }

    private boolean SignCheck(Material mat){
        if (mat == Material.ACACIA_SIGN ||
                mat == Material.BIRCH_SIGN ||
                mat == Material.DARK_OAK_SIGN ||
                mat == Material.JUNGLE_SIGN ||
                mat == Material.OAK_SIGN ||
                mat == Material.SPRUCE_SIGN){
            return true;
        }
        return false;
    }
}

