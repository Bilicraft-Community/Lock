package org.sct.lock.util.function.nms;

import org.bukkit.Location;
import org.sct.easylib.EasyLib;
import org.sct.easylib.util.reflectutil.Reflections;
import org.sct.easylib.util.reflectutil.VersionChecker;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author LovesAsuna
 **/
public abstract class AbstractHoverText implements HoverTextAPI {
    protected Reflections reflections;
    protected Class<?> craftWorld;
    protected Class<?> worldServer;
    protected Class<?> tileEntitySign;
    protected Class<?> ChatModifier;
    protected Class<?> BlockPosition;
    protected Class<?> IChatBaseComponent;
    protected Class<?> ChatHoverable;
    protected Class<?> ChatComponentText;

    protected Method getHandle;

    protected Method getTileEntity;
    protected Method getChatModifier;
    protected Method setChatHoverable;
    protected Method getHoverEvent;

    protected Field lines;

    protected Constructor<?> BlockPositionConstructor;
    protected Constructor<?> ChatComponentTextConstructor;


    //todo 修复1.16.5不能使用的bug
    public AbstractHoverText() {
        Reflections reflections = new Reflections(EasyLib.getInstance());
        try {
            craftWorld = reflections.getBukkitClass("CraftWorld");
            worldServer = reflections.getMinecraftClass("WorldServer");
            tileEntitySign = reflections.getMinecraftClass("TileEntitySign");
            ChatModifier = reflections.getMinecraftClass("ChatModifier");
            BlockPosition = reflections.getMinecraftClass("BlockPosition");
            IChatBaseComponent = reflections.getMinecraftClass("IChatBaseComponent");
            ChatHoverable = reflections.getMinecraftClass("ChatHoverable");
            ChatComponentText = reflections.getMinecraftClass("ChatComponentText");
            getHandle = craftWorld.getDeclaredMethod("getHandle");

            if (VersionChecker.Version.getCurrent().isEqualOrHigher(VersionChecker.Version.v1_13_R3)) {
                /*大于1.15*/
                if (VersionChecker.Version.getCurrent().isEqualOrHigher(VersionChecker.Version.v1_15_R1)) {
                    getTileEntity = worldServer.getDeclaredMethod("getTileEntity", BlockPosition, boolean.class);
                } else {
                    getTileEntity = worldServer.getDeclaredMethod("getTileEntity", BlockPosition);
                }
                /*大于1.13版本*/
                getHoverEvent = ChatModifier.getDeclaredMethod("getHoverEvent");
            } else {
                getTileEntity = worldServer.getDeclaredMethod("getTileEntity", BlockPosition);
                getHoverEvent = ChatModifier.getDeclaredMethod("i");
            }
            getTileEntity.setAccessible(true);
            getChatModifier = IChatBaseComponent.getDeclaredMethod("getChatModifier");
            getChatModifier.setAccessible(true);
            setChatHoverable = ChatModifier.getDeclaredMethod("setChatHoverable", ChatHoverable);

            BlockPositionConstructor = BlockPosition.getDeclaredConstructor(int.class, int.class, int.class);
            ChatComponentTextConstructor = ChatComponentText.getDeclaredConstructor(String.class);

            lines = tileEntitySign.getDeclaredField("lines");
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public abstract void saveText(Location location, String text);

    @Override
    public abstract String getText(Location location);

    protected Object getChatModifier(Location location) {
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        try {
            Object worldServer = getHandle.invoke(location.getWorld());
            Object blockPosition = BlockPositionConstructor.newInstance(x, y, z);
            Object tileEntitySign;
            if (VersionChecker.Version.getCurrent().isEqualOrHigher(VersionChecker.Version.v1_15_R1)) {
                tileEntitySign = getTileEntity.invoke(worldServer, blockPosition, true);
            } else {
                tileEntitySign = getTileEntity.invoke(worldServer, blockPosition);
            }

            if (this.tileEntitySign.isInstance(tileEntitySign)) {
                Object line = ((Object[]) lines.get(tileEntitySign))[0];
                Object ChatModifier = getChatModifier.invoke(line);
                return ChatModifier;
            } else {
                return null;
            }
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    protected Class<Enum<?>> getAnyMineCraftEnum() {
        try {
            return (Class<Enum<?>>) ChatHoverable.getClasses()[0];
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Object enumField(Class<Enum<?>> enumClass, String name) {
        try {
            Method valueOf = Enum.class.getMethod("valueOf", Class.class, String.class);
            return valueOf.invoke(null, enumClass, name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}