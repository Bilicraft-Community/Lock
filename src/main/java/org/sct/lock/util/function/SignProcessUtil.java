package org.sct.lock.util.function;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.block.SignChangeEvent;
import org.sct.easylib.util.BasicUtil;
import org.sct.lock.Lock;
import org.sct.lock.data.LockData;
import org.sct.lock.enumeration.ConfigType;
import org.sct.lock.enumeration.LangType;
import org.sct.lock.file.Config;
import org.sct.lock.file.Lang;
import org.sct.lock.util.function.nms.HoverText_v16;

import java.util.Map;

/**
 * @author LovesAsuna
 * @date 2020/3/23 14:06
 */

public class SignProcessUtil {
    private static final HoverText_v16 hoverTextAPI = new HoverText_v16();

    public static void processSign(SignChangeEvent e) {
        /*如果世界不匹配,返回*/
        if (!LocationUtil.checkWorld(e.getBlock().getLocation())) {
            e.getPlayer().sendMessage(BasicUtil.convert(Lang.getString(LangType.LANG_DENYWORLD.getPath())));
            return;
        }

        if (e.getLine(0).equalsIgnoreCase(Config.getString(ConfigType.SETTING_LOCKSYMBOL.getPath())) && !e.getLine(1).isEmpty()) {
            processNormalMsg(e);
            processConditions(e);
        }
        LockData.INSTANCE.getPlayerSignLocation().remove(e.getPlayer());
        LockData.INSTANCE.getPlayerDoorLocation().remove(e.getPlayer());
    }

    private static void processNormalMsg(SignChangeEvent e) {
        /*替换第一行Symbol*/
        e.setLine(0, BasicUtil.convert(Config.getString(ConfigType.SETTING_SYMBOLREPLACE.getPath())));

        /*替换第四行为玩家名*/
        e.setLine(3, "§l" + e.getPlayer().getName());

        /*替换第二行价格*/
        e.setLine(1, BasicUtil.convert(Config.getString(ConfigType.SETTING_CHARGE.getPath()) + e.getLine(1)));
    }

    private static void processConditions(SignChangeEvent e) {

        StringBuffer buffer = new StringBuffer();
        String line = e.getLine(2);
        boolean direction = false;

        String flagEnter = Config.getString(ConfigType.SETTING_FLAGENTER.getPath());
        String enterReplace = BasicUtil.convert(Config.getString(ConfigType.SETTING_ENTERREPLACE.getPath()));

        String flagLeave = Config.getString(ConfigType.SETTING_FLAGLEAVE.getPath());
        String leaveReplace = BasicUtil.convert(Config.getString(ConfigType.SETTING_LEAVEREPLACE.getPath()));

        String flagEmpty = Config.getString(ConfigType.SETTING_FLAGEMPTY.getPath());
        String emptyReplace = BasicUtil.convert(Config.getString(ConfigType.SETTING_EMPTYREPLACE.getPath()));

        String flagMoney = Config.getString(ConfigType.SETTING_FLAGMONEY.getPath());

        String flagEffect = Config.getString(ConfigType.SETTING_FLAGEFFECT.getPath());
        String effectReplace = BasicUtil.convert(Config.getString(ConfigType.SETTING_EFFECTREPLACE.getPath()));
        if (line.contains(flagEnter)) {
            buffer.append(enterReplace);
            direction = true;
        }

        if (line.contains(flagLeave)) {
            buffer.append(leaveReplace);
            direction = true;
        }

        /*开启默认双向*/
        if (!direction) {
            buffer.append(enterReplace + leaveReplace);
        }

        e.setLine(2, buffer.toString());
        buffer.setLength(0);

        /*背包为空条件*/
        if (line.contains(flagEmpty)) {
            buffer.append(emptyReplace);
        }

        /*余额条件*/
        if (line.contains(flagMoney)) {
            int money = BasicUtil.ExtraceInt(line);
            Map<String, Boolean> moneyDetail = LockUtil.getMoneydetail(line, 0, 0);
            String symbol = moneyDetail.keySet().iterator().next();
            buffer.append(BasicUtil.convert(BasicUtil.replace(Config.getString(ConfigType.SETTING_MONEYREPLACE.getPath()), "%needmoney", symbol + money)));
        }

        /*药水效果条件*/
        if (line.contains(flagEffect)) {
            buffer.append(effectReplace);
        }

        Location location = e.getBlock().getLocation();
        Bukkit.getScheduler().runTaskLater(Lock.getInstance(), () -> {
            hoverTextAPI.saveText(location, buffer.toString());
        }, 1);
    }

    public static HoverText_v16 getHoverTextAPI() {
        return hoverTextAPI;
    }
}
