package net.darkhax.tesla.lib;

import java.util.ArrayList;
import java.util.List;

import net.darkhax.tesla.api.ITeslaConsumer;
import net.darkhax.tesla.api.ITeslaProducer;
import net.darkhax.tesla.capability.TeslaCapabilities;
import net.minecraft.client.resources.I18n;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

public class TeslaUtils {
    
    /**
     * The smallest unit of power measurement.
     */
    public static final int TESLA = 1;
    
    /**
     * The amount of Tesla in a KiloTesla. One thousand.
     */
    public static final int KILOTESLA = 1000;
    
    /**
     * The amount of Telsa in a MegaTesla. One Million.
     */
    public static final int MEGATESLA = 1000000;
    
    /**
     * The amount of Tesla in a GigaTesla. One Billion.
     */
    public static final int GIGATESLA = 1000000000;
    
    /**
     * The amount of Telsa in a TeraTesla. One Trillion.
     */
    public static final long TERATESLA = 1000000000000L;
    
    /**
     * The amount of Tesla in a PentaTesla. One Quadrilli.on
     */
    public static final long PENTATESLA = 1000000000000000L;
    
    /**
     * The amount of Tesla in a ExaTesla. One Quintilian.
     * 
     * The ExaTesla should not be treated as the largest unit of Tesla. The naming scheme can
     * go on indefinitely. The next unit would be a ZettaTesla, followed by YottaTesla,
     * BronoTesla, GeopTesla and so on. While it is possible to define these measurements,
     * there really isn't a point.
     */
    public static final long EXATESLA = 1000000000000000000L;
    
    /**
     * Converts an amount of Tesla units into a human readable String. The string amount is
     * only rounded to one decimal place.
     * 
     * @param tesla The amount of Tesla units to display.
     * @return A human readable display of the Tesla units.
     */
    public static String getDisplayableTeslaCount (long tesla) {
        
        if (tesla < 1000)
            return tesla + " T";
            
        final int exp = (int) (Math.log(tesla) / Math.log(1000));
        final char unitType = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sT", tesla / Math.pow(1000, exp), unitType);
    }
    
    /**
     * Gets the abbreviated name of the best unit to describe the provided power. For example,
     * anything less than 1000 will return t for tesla, while anything between 999 and one
     * million will return kt for kilo tesla. This method has support for up to Exatesla.
     * 
     * @param tesla The amount of Tesla to get the unit for.
     * @return The abbreviated name for the unit used to describe the provided power amount.
     */
    public static String getUnitType (long tesla) {
        
        if (tesla < 1000)
            return tesla + "t";
            
        final int exp = (int) (Math.log(tesla) / Math.log(1000));
        return "kmgtpe".charAt(exp - 1) + "t";
    }
    
    /**
     * Gets the name of the the power unit that best represents the amount of provided power.
     * The name will be translated to the local language, or english if that language is not
     * yet supported.
     * 
     * @param tesla The amount of Tesla to get the unit name for.
     * @return The name of the power unit that best represents the amount of power provided.
     */
    public static String getLocalizedUnitType (long tesla) {
        
        return I18n.format("unit.tesla." + getUnitType(tesla));
    }
    
    /**
     * Gets a list of all capabilities that touch a BlockPos. This will search for tile
     * entities touching the BlockPos and then query them for access to their capabilities.
     * 
     * @param capability The capability you want to retrieve.
     * @param world The world that this is happening in.
     * @param pos The position to search around.
     * @return A list of all capabilities that are being held by connected blocks.
     */
    public static <T> List<T> getConnectedCapabilities (Capability<T> capability, World world, BlockPos pos) {
        
        final List<T> capabilities = new ArrayList<T>();
        
        for (final EnumFacing side : EnumFacing.values()) {
            
            final TileEntity tile = world.getTileEntity(pos.offset(side));
            
            if (tile != null && !tile.isInvalid() && tile.hasCapability(capability, side.getOpposite()))
                capabilities.add(tile.getCapability(capability, side.getOpposite()));
        }
        
        return capabilities;
    }
    
    /**
     * Attempts to give power to all consumers touching the given BlockPos.
     * 
     * @param world The world that this is happening in.
     * @param pos The position to search around.
     * @param amount The amount of power to offer to each individual face.
     * @param simulated Whether or not this is being ran as part of a simulation.
     * @return The amount of power that was consumed.
     */
    public static long distributePowerToAllFaces (World world, BlockPos pos, long amount, boolean simulated) {
        
        long consumedPower = 0L;
        
        for (final ITeslaConsumer consumer : getConnectedCapabilities(TeslaCapabilities.CAPABILITY_CONSUMER, world, pos))
            consumedPower += consumer.givePower(amount, simulated);
            
        return consumedPower;
    }
    
    /**
     * Attempts to consume power from all producers touching the given BlockPos.
     * 
     * @param world The world that this is happening in.
     * @param pos The position to search around.
     * @param amount The amount of power to request from each individual face.
     * @param simulated Whether or not this is being ran as part of a simulation.
     * @return The amount of power that was successfully consumed.
     */
    public static long consumePowerFromAllFaces (World world, BlockPos pos, long amount, boolean simulated) {
        
        long recievedPower = 0L;
        
        for (final ITeslaProducer producer : getConnectedCapabilities(TeslaCapabilities.CAPABILITY_PRODUCER, world, pos))
            recievedPower += producer.takePower(amount, simulated);
            
        return recievedPower;
    }
}