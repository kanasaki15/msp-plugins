package xyz.n7mn.dev.mspplugin;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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

    @EventHandler
    public void setSpeed(VehicleMoveEvent e){
        if (e.getVehicle() instanceof Minecart) {
            //plugin.getLogger().info("a");
            cart = (Minecart)e.getVehicle();

            double moveX = e.getTo().getX() - e.getFrom().getX();
            double moveZ = e.getTo().getZ() - e.getFrom().getZ();
            int move = 0;

            World world = e.getTo().getWorld();
            Block ToBlock = e.getTo().getBlock();

            if (moveX != 0d){
                move = e.getTo().getBlockX() - e.getFrom().getBlockX();
            }
            if (moveZ != 0d){
                move = e.getTo().getBlockZ() - e.getFrom().getBlockZ();
            }

            // debug
            //plugin.getLogger().info("moveX : "+moveX);
            //plugin.getLogger().info("moveZ : "+moveZ);
            //plugin.getLogger().info("move : "+move);
            //plugin.getLogger().info("MaxSpeed" + cart.getMaxSpeed());

            Block SignBlock = null;
            if (moveX != 0d){
                SignBlock = GetSignBlock(ToBlock,move,"X");
            }else if(moveZ != 0d){
                SignBlock = GetSignBlock(ToBlock,move,"Z");
            }

            if (SignBlock != null){
                cart.setMaxSpeed(GetSignSpeed(SignBlock));
            }

            if (SignBlock != null){
                // 停止
                if (GetSignText(SignBlock)[1].equals("Stop")){
                    cart.setMaxSpeed(defaultSpeed);
                    Block tempB = null;
                    if (moveX > 0){
                        tempB = world.getBlockAt(ToBlock.getX() + 1,ToBlock.getY(),ToBlock.getZ());
                    }else if (moveX < 0){
                        tempB = world.getBlockAt(ToBlock.getX() - 1,ToBlock.getY(),ToBlock.getZ());
                    }else if (moveZ > 0){
                        tempB = world.getBlockAt(ToBlock.getX(),ToBlock.getY(),ToBlock.getZ() + 1);
                    }else if (moveZ < 0){
                        tempB = world.getBlockAt(ToBlock.getX(),ToBlock.getY(),ToBlock.getZ() - 1);
                    }

                    BackBlockType = tempB.getType();
                    if (move != 0){
                        world.getBlockAt(tempB.getX(),tempB.getY(),tempB.getZ()).setType(Material.GLASS);
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
                    mat == Material.CRIMSON_SIGN ||
                    mat == Material.DARK_OAK_SIGN ||
                    mat == Material.JUNGLE_SIGN ||
                    mat == Material.OAK_SIGN ||
                    mat == Material.SPRUCE_SIGN ||
                    mat == Material.WARPED_SIGN ||
                    mat == Material.ACACIA_WALL_SIGN ||
                    mat == Material.BIRCH_WALL_SIGN ||
                    mat == Material.CRIMSON_WALL_SIGN ||
                    mat == Material.DARK_OAK_WALL_SIGN ||
                    mat == Material.JUNGLE_WALL_SIGN ||
                    mat == Material.OAK_WALL_SIGN ||
                    mat == Material.SPRUCE_WALL_SIGN ||
                    mat == Material.WARPED_WALL_SIGN
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

    private double GetSpeed(double Speed){
        int t = (int)((Speed * 28.8d) * 100);
        return t / 100d;
    }

    private Block GetSignBlock (Block b, int move, String rotate){

        int MoveX = 0;
        int MoveZ = 0;
        if (rotate.equals("X")){
            MoveX = 1;
        }
        if (rotate.equals("Z")){
            MoveZ = 1;
        }

        Block tempBlock;
        if (move < 0){
            move = move * -1;
        }
        for(int i = 0; i < move; i++){
            Block leftBlock = null;
            Block rightBlock = null;
            if (MoveX != 0){
                tempBlock = b.getWorld().getBlockAt(b.getX() - i,b.getY(),b.getZ());
                leftBlock = b.getWorld().getBlockAt(tempBlock.getX(),tempBlock.getY(),tempBlock.getZ() - 1);
                rightBlock = b.getWorld().getBlockAt(tempBlock.getX(),tempBlock.getY(),tempBlock.getZ() + 1);
            }
            if (MoveZ != 0){
                tempBlock = b.getWorld().getBlockAt(b.getX(),b.getY(),b.getZ() - i);
                leftBlock = b.getWorld().getBlockAt(tempBlock.getX() - 1,tempBlock.getY(),tempBlock.getZ());
                rightBlock = b.getWorld().getBlockAt(tempBlock.getX() + 1,tempBlock.getY(),tempBlock.getZ());
            }

            if (SignCheck(leftBlock) && GetSignText(leftBlock)[0].equals("[msp]")){
                return leftBlock;
            }
            if (SignCheck(rightBlock) && GetSignText(rightBlock)[0].equals("[msp]")){
                return rightBlock;
            }
        }
        return null;
    }
}

