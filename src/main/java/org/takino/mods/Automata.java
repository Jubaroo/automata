package org.takino.mods;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.deities.Deity;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.NoSuchTemplateException;
import com.wurmonline.server.spells.LabouringSpirit;
import com.wurmonline.server.spells.Spells;
import org.gotti.wurmunlimited.modloader.ReflectionUtil;
import org.gotti.wurmunlimited.modloader.interfaces.*;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;
import org.takino.mods.actions.AddToolAction;
import org.takino.mods.actions.FuelUpAction;
import org.takino.mods.actions.StartWorkAction;
import org.takino.mods.helpers.DatabaseHelper;
import org.takino.mods.helpers.WorkerHelper;
import org.takino.mods.tools.ShovelJob;
import org.takino.mods.tools.ToolJob;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

import static org.takino.mods.helpers.DatabaseHelper.getAttachedTool;

public class Automata implements WurmServerMod, PreInitable, Initable, Configurable, ServerStartedListener, ServerShutdownListener {
    private Timer timer;
    private static final Logger LOG = Logger.getLogger(Automata.class.getName());


    @Override
    public void configure(Properties properties) {
        Integer difficulty = Integer.parseInt(properties.getProperty("difficulty", "90"));
        Integer favor = Integer.parseInt(properties.getProperty("favor", "120"));
        Integer level = Integer.parseInt(properties.getProperty("faith", "31"));
        Integer cooldown = Integer.parseInt(properties.getProperty("cooldown", "1800"));
        Integer castTime = Integer.parseInt(properties.getProperty("castingTime", "60"));
        Byte spellId = Byte.parseByte(properties.getProperty("spellid", "111"));
        Integer maxQuality = Integer.parseInt(properties.getProperty("max_quality", "20"));
        Integer baseQuantity = Integer.parseInt(properties.getProperty("quantity", "1"));
        Integer pollTimer = Integer.parseInt(properties.getProperty("polltimer", "30"));

        Config.spellDifficulty = difficulty;
        Config.spellPrice = favor;
        Config.spellCooldown = cooldown;
        Config.spellLevel = level;
        Config.spellCastingTime = castTime;
        Config.spellId = spellId;
        Config.maxQualityLevel = maxQuality;
        Config.defaultQuantity = baseQuantity;
        Config.pollTimer = pollTimer;
    }

    @Override
    public void init() {

    }

    @Override
    public void preInit() {
        ModActions.init();
        /*
          private boolean poll(Item parent, int parentTemp, boolean insideStructure,
          boolean deeded, boolean saveLastMaintained, boolean inMagicContainer, boolean inTrashbin) {

         */

    }


    private static void workOnThings(Item item) {
        //find attached tool
        try {
            ToolType type = getAttachedTool(item);
            ToolJob job = WorkerHelper.getJob(type);
            if (job == null) {
                WorkerHelper.removeJob(item.getWurmId());
            } else {
                job.doWork(item);
            }
        } catch (SQLException | NoSuchTemplateException | FailedException | IOException e) {
            e.printStackTrace();
        }


    }


    public static void debug(String msg) {
        LOG.info(msg);
    }


    @Override
    public void onServerStarted() {
        DatabaseHelper.createTable();
        debug("Registering spell");

        LabouringSpirit spell = new LabouringSpirit();

        try {
            ReflectionUtil.callPrivateMethod(Spells.class, ReflectionUtil.getMethod(Spells.class, "addSpell"), spell);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        for (Deity deity : Deities.getDeities()) {
            deity.addSpell(spell);
        }
        registerRunner();

        ModActions.registerAction(new StartWorkAction());
        ModActions.registerAction(new AddToolAction());
        ModActions.registerAction(new FuelUpAction());
        registerToolJobs();
    }


    private void registerToolJobs() {
        WorkerHelper.registerToolJob(ToolType.SHOVEL, new ShovelJob());
    }

    private void registerRunner() {
        TimerTask runner = new TimerTask() {
            @Override
            public void run() {
                List<Long> list = new ArrayList<>(WorkerHelper.runningJobs); // vs CME while removing jobs from within timer
                for (Long itemid : list) {
                    try {
                        Item target = Items.getItem(itemid);
                        workOnThings(target);
                    } catch (NoSuchItemException e) {
                        WorkerHelper.removeJob(itemid);
                    }
                }
            }
        };
        timer = new Timer();
        timer.schedule(runner, 0, Config.pollTimer*1000);
    }

    @Override
    public void onServerShutdown() {
        WorkerHelper.shutDown();
        timer.cancel();
    }
}
