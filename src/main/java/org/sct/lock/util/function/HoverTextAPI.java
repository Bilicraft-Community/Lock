package org.sct.lock.util.function;

import org.bukkit.Location;
import org.sct.plugincore.util.function.StackTrace;
import org.sct.plugincore.util.reflectutil.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author LovesAsuna
 * @date 2020/3/22 14:30
 */

public class HoverTextAPI {
    private Reflections reflections;
    private Class<?> craftWorld;
    private Class<?> worldServer;
    private Class<?> tileEntitySign;
    private Class<?> ChatModifier;
    private Class<?> BlockPosition;
    private Class<?> IChatBaseComponent;
    private Class<?> ChatHoverable;
    private Class<?> ChatComponentText;
    private Class<Enum<?>> EnumHoverAction;

    private Method getHandle;
    //todo 版本兼容
    private Method getTileEntity;
    private Method getChatModifier;
    private Method setChatHoverable;
    private Method getHoverEvent;
    private Method b;

    private Field lines;

    private Constructor<?> BlockPositionConstructor;
    private Constructor<?> ChatHoverableConstructor;
    private Constructor<?> ChatComponentTextConstructor;

    private Object EnumHoverActionShowText;

    public HoverTextAPI() {
        Reflections reflections = new Reflections();
        try {
            craftWorld = reflections.getBukkitClass("CraftWorld");
            worldServer = reflections.getMinecraftClass("WorldServer");
            tileEntitySign = reflections.getMinecraftClass("TileEntitySign");
            ChatModifier = reflections.getMinecraftClass("ChatModifier");
            BlockPosition = reflections.getMinecraftClass("BlockPosition");
            IChatBaseComponent = reflections.getMinecraftClass("IChatBaseComponent");
            ChatHoverable = reflections.getMinecraftClass("ChatHoverable");
            ChatComponentText = reflections.getMinecraftClass("ChatComponentText");
            EnumHoverAction = getAnyMineCraftEnum();
            getHandle = craftWorld.getDeclaredMethod("getHandle");
            getTileEntity = worldServer.getDeclaredMethod("getTileEntity", BlockPosition, boolean.class);
            getTileEntity.setAccessible(true);
            getChatModifier = IChatBaseComponent.getDeclaredMethod("getChatModifier");
            getChatModifier.setAccessible(true);
            setChatHoverable = ChatModifier.getDeclaredMethod("setChatHoverable", ChatHoverable);
            getHoverEvent = ChatModifier.getDeclaredMethod("getHoverEvent");
            b = ChatHoverable.getDeclaredMethod("b");

            BlockPositionConstructor = BlockPosition.getDeclaredConstructor(int.class, int.class, int.class);
            ChatHoverableConstructor = ChatHoverable.getDeclaredConstructor(EnumHoverAction, IChatBaseComponent);
            ChatComponentTextConstructor = ChatComponentText.getDeclaredConstructor(String.class);

            EnumHoverActionShowText = enumField(EnumHoverAction, "SHOW_TEXT");

            lines = tileEntitySign.getDeclaredField("lines");
        } catch (ReflectiveOperationException e) {
            StackTrace.printStackTrace(e);
        }
    }

    public void saveText(Location location, String text) {
        try {
            Object ChatModifier = getChatModifier(location);

            if (ChatModifier == null) {
                // 不是牌子
                return;
            }
            Object ChatComponentText = ChatComponentTextConstructor.newInstance(text);
            Object ChatHoverable = ChatHoverableConstructor.newInstance(EnumHoverActionShowText, ChatComponentText);

            setChatHoverable.invoke(ChatModifier, ChatHoverable);
        } catch (ReflectiveOperationException | NullPointerException e) {
            StackTrace.printStackTrace(e);
        }
    }

    public String getText(Location location) {
        try {
            Object ChatModifier = getChatModifier(location);
            if (ChatModifier == null) {
                // 不是牌子
                return null;
            }

            Object ChatHoverable = getHoverEvent.invoke(ChatModifier);
            Object IChatBaseComponent = null;
            try {
                IChatBaseComponent = b.invoke(ChatHoverable);
            } catch (NullPointerException e) {
                // 牌子上无消息
                return null;
            }

            Method getText = this.IChatBaseComponent.getDeclaredMethod("getText");
            return (String) getText.invoke(IChatBaseComponent);
        } catch (ReflectiveOperationException e) {
            StackTrace.printStackTrace(e);
            return null;
        }
    }

    public Object getChatModifier(Location location) {
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        try {
            Object worldServer = getHandle.invoke(location.getWorld());
            Object blockPosition = BlockPositionConstructor.newInstance(x, y, z);
            Object tileEntitySign = getTileEntity.invoke(worldServer, blockPosition, true);

            if (this.tileEntitySign.isInstance(tileEntitySign)) {
                Object line = ((Object[]) lines.get(tileEntitySign))[0];
                Object ChatModifier = getChatModifier.invoke(line);
                return ChatModifier;
            } else {
                return null;
            }
        } catch (ReflectiveOperationException e) {
            StackTrace.printStackTrace(e);
            return null;
        }
    }


    @SuppressWarnings("unchecked")
    private Class<Enum<?>> getAnyMineCraftEnum() {
        try {
            return (Class<Enum<?>>) ChatHoverable.getClasses()[0];
        } catch (Exception e) {
            StackTrace.printStackTrace(e);
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