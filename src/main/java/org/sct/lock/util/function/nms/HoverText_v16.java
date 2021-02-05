package org.sct.lock.util.function.nms;

import org.bukkit.Location;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * @author LovesAsuna
 * @date 2020/3/22 14:30
 */

public class HoverText_v16 extends AbstractHoverText {
    private Constructor ChatHoverableConstructor;
    private Object EnumHoverActionShowText;

    public HoverText_v16() {
        super();

    }

    @Override
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
            e.printStackTrace();
        }
    }

    @Override
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
                // IChatBaseComponent = b.invoke(ChatHoverable);
            } catch (NullPointerException e) {
                // 牌子上无消息
                return null;
            }

            Method getText = this.IChatBaseComponent.getDeclaredMethod("getText");
            return (String) getText.invoke(IChatBaseComponent);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return null;
        }
    }
}
