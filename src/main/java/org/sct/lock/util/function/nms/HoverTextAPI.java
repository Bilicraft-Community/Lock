package org.sct.lock.util.function.nms;

import org.bukkit.Location;

/**
 * @author LovesAsuna
 **/
public interface HoverTextAPI {
    /**
     * @param location 位置
     * @param text 文本
     * 将制定文本绑定到牌子上
     **/
    void saveText(Location location, String text);

    /**
     * @param location 位置
     * @return String 返回牌子绑定的文本
     **/
    String getText(Location location);
}
