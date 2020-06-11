package xyz.n7mn.dev.mspplugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.vehicle.VehicleBlockCollisionEvent;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;


public class MinecartEvent implements Listener {
    JavaPlugin plugin = null;
    double defaultSpeed = 0.4d;
    Material BackBlockType = null;
    Minecart cart = null;

    public MinecartEvent(JavaPlugin p){
        plugin = p;
    }

    @EventHandler
    public void createMineCart(VehicleCreateEvent e){
        if (e.getVehicle() instanceof  Minecart){
            cart = (Minecart) e.getVehicle();
            cart.setMaxSpeed(defaultSpeed);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void setSpeed(VehicleMoveEvent e){
        if (e.getVehicle() instanceof Minecart) {
            //plugin.getLogger().info("a");
            cart = (Minecart)e.getVehicle();

            double moveX = e.getTo().getX() - e.getFrom().getX();
            double moveZ = e.getTo().getZ() - e.getFrom().getZ();
            int move = 0;

            World world = e.getTo().getWorld();
            Block ToBlock = e.getTo().getBlock();

            int Xplus = 0;
            int Zplus = 0;
            if (moveX != 0d){
                Zplus = 1;
                move = e.getTo().getBlockX() - e.getFrom().getBlockX();
            }
            if (moveZ != 0d){
                Xplus = 1;
                move = e.getTo().getBlockZ() - e.getFrom().getBlockZ();
            }

            // debug
            //plugin.getLogger().info("moveX : "+moveX);
            //plugin.getLogger().info("moveZ : "+moveZ);
            //plugin.getLogger().info("move : "+move);
            //plugin.getLogger().info("MaxSpeed" + cart.getMaxSpeed());

            Block leftBlock1 = world.getBlockAt(ToBlock.getX() - Xplus,ToBlock.getY(),ToBlock.getZ() - Zplus);
            Block rightBlock1 = world.getBlockAt(ToBlock.getX() + Xplus,ToBlock.getY(),ToBlock.getZ() + Zplus);
            Block leftBlock2 = world.getBlockAt(ToBlock.getX() - Xplus,ToBlock.getY() - 1,ToBlock.getZ() - Zplus);
            Block rightBlock2 = world.getBlockAt(ToBlock.getX() + Xplus,ToBlock.getY() - 1,ToBlock.getZ() + Zplus);

            Block advanceBlock = null;
            if (moveX > 0) {
                advanceBlock = world.getBlockAt(ToBlock.getX() + 1,ToBlock.getY(),ToBlock.getZ());
            }else if (moveX < 0) {
                advanceBlock = world.getBlockAt(ToBlock.getX() - 1,ToBlock.getY(),ToBlock.getZ());
            }
            if (moveZ > 0) {
                advanceBlock = world.getBlockAt(ToBlock.getX(),ToBlock.getY(),ToBlock.getZ() + 1);
            }else if (moveZ < 0) {
                advanceBlock = world.getBlockAt(ToBlock.getX(),ToBlock.getY(),ToBlock.getZ() - 1);
            }
            Block turnbackBlock = null;
            if (moveX > 0) {
                turnbackBlock = world.getBlockAt(ToBlock.getX() - 1,ToBlock.getY(),ToBlock.getZ());
            }else if (moveX < 0) {
                turnbackBlock = world.getBlockAt(ToBlock.getX() + 1,ToBlock.getY(),ToBlock.getZ());
            }
            if (moveZ > 0) {
                turnbackBlock = world.getBlockAt(ToBlock.getX(),ToBlock.getY(),ToBlock.getZ() - 1);
            }else if (moveZ < 0) {
                turnbackBlock = world.getBlockAt(ToBlock.getX(),ToBlock.getY(),ToBlock.getZ() + 1);
            }


            // 速度設定
            if (GetSignSpeed(leftBlock1) != -1d){
                cart.setMaxSpeed(GetSignSpeed(leftBlock1));
            }else if (GetSignSpeed(leftBlock2) != -1d){
                cart.setMaxSpeed(GetSignSpeed(leftBlock2));
            }else if (GetSignSpeed(rightBlock1) != -1d){
                cart.setMaxSpeed(GetSignSpeed(rightBlock1));
            }else if (GetSignSpeed(rightBlock2) != -1d){
                cart.setMaxSpeed(GetSignSpeed(rightBlock2));
            }
/*
            plugin.getLogger().info("--- SetSpeed ---");
            plugin.getLogger().info(""+GetSignSpeed(leftBlock1));
            plugin.getLogger().info(""+GetSignSpeed(leftBlock2));
            plugin.getLogger().info(""+GetSignSpeed(rightBlock1));
            plugin.getLogger().info(""+GetSignSpeed(rightBlock2));
*/
            Sign sign = null;
            if (GetSign(leftBlock1) != null){
                sign = GetSign(leftBlock1);
            }
            if (sign == null || !sign.getLine(0).equals("[msp]")){
                sign = GetSign(leftBlock2);
            }
            if (sign == null || !sign.getLine(0).equals("[msp]")){
                sign = GetSign(rightBlock1);
            }
            if (sign == null || !sign.getLine(0).equals("[msp]")){
                sign = GetSign(rightBlock2);
            }

            if (sign != null){
                // 停止
                if (sign.getLine(1).equals("Stop")){
                    cart.setMaxSpeed(defaultSpeed);
                    BackBlockType = advanceBlock.getType();
                    if (move != 0){
                        world.getBlockAt(advanceBlock.getX(),advanceBlock.getY(),advanceBlock.getZ()).setType(Material.GLASS);
                    }
                }
            }
        }
    }

    @EventHandler
    public void setSignSpeed(SignChangeEvent e) {
        // スピード表示
        if (e.getLine(0).equals("[msp]")){

            double speed;
            try {
                speed = Double.parseDouble(e.getLine(1));
            }catch (Exception ex){
                speed = 0d;
            }
            if (speed >= 0.1d && speed <= 50d){
                e.setLine(2,"Speed :");
                e.setLine(3,"   "+GetSpeed(speed)+" km/h");
            }else if(!e.getLine(1).equals("Stop") && !e.getLine(1).equals("SpeedCheck")){
                e.setLine(1,"1");
                e.setLine(2,"Speed :");
                e.setLine(3,"   28.8 km/h");
            }
        }
    }

    @EventHandler
    public void BlockEvent(VehicleBlockCollisionEvent e){
        if (BackBlockType != null){
            Location loc = e.getBlock().getLocation();
            loc.getBlock().setType(BackBlockType);
            BackBlockType = null;
        }
    }

    private boolean SignCheck(Block block){
        try {
            Material mat = block.getType();
            if (mat == Material.ACACIA_SIGN ||
                    mat == Material.BIRCH_SIGN ||
                    mat == Material.DARK_OAK_SIGN ||
                    mat == Material.JUNGLE_SIGN ||
                    mat == Material.OAK_SIGN ||
                    mat == Material.SPRUCE_SIGN ||
                    mat == Material.ACACIA_WALL_SIGN ||
                    mat == Material.BIRCH_WALL_SIGN ||
                    mat == Material.DARK_OAK_WALL_SIGN ||
                    mat == Material.JUNGLE_WALL_SIGN ||
                    mat == Material.OAK_WALL_SIGN ||
                    mat == Material.SPRUCE_WALL_SIGN
            ) {
                return true;
            }
        }catch (Exception e){
            return false;
        }
        return false;
    }

    private double GetSignSpeed(Block block){
        String[] text = GetSignText(block);
        double speed = -1d;
        if (text[0].equals("[msp]")){
            if (!text[1].equals("SpeedCheck") && !text[1].equals("Stop")){
                try {
                    speed = Double.parseDouble(text[1]) * defaultSpeed;
                }catch (Exception e){
                    speed = defaultSpeed;
                }
            }
        }
        return speed;
    }

    private String[] GetSignText(Block block){

        Sign sign = null;
        if(SignCheck(block)){
            sign = (Sign)block.getState();
        }
        if (sign != null){
            return sign.getLines();
        }
        return new String[]{"", "", "", ""};
    }
    private Sign GetSign(Block block){

        Sign sign = null;
        if(SignCheck(block)){
            sign = (Sign)block.getState();
        }

        return sign;
    }
    private double GetSpeed(double Speed){
        int t = (int)((Speed * 28.8d) * 100);
        return t / 100d;
    }
}

