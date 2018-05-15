package org.takino.mods;

import com.wurmonline.server.FailedException;
import com.wurmonline.server.Items;
import com.wurmonline.server.NoSuchItemException;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.NoSuchTemplateException;
import org.gotti.wurmunlimited.modloader.interfaces.*;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;
import org.takino.mods.actions.AddToolAction;
import org.takino.mods.actions.FuelUpAction;
import org.takino.mods.actions.StartWorkAction;
import org.takino.mods.actions.StopWorkAction;
import org.takino.mods.helpers.DatabaseHelper;
import org.takino.mods.helpers.WorkerHelper;
import org.takino.mods.tools.ChiselJob;
import org.takino.mods.tools.ShovelJob;
import org.takino.mods.tools.ToolJob;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

import static org.takino.mods.helpers.DatabaseHelper.getAttachedTool;

public class Automata implements WurmServerMod, PreInitable, Initable, Configurable, ServerStartedListener, ServerShutdownListener {
    private Timer timer;
    private static final Logger LOG = Logger.getLogger(Automata.class.getName());


    @Override
    public void configure(Properties properties) {
        Integer maxQuality = Integer.parseInt(properties.getProperty("max_quality", "20"));
        Integer baseQuantity = Integer.parseInt(properties.getProperty("quantity", "1"));
        Integer pollTimer = Integer.parseInt(properties.getProperty("polltimer", "30"));

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

    public static float getLabouringSpirits(Item item){
        return item.getBonusForSpellEffect((byte) 121);
    }


    private static void workOnThings(Item item) {
        //find attached tool
        try {
            debug("Wee-wee, getting a tool");
            ToolType type = getAttachedTool(item);
            ToolJob job = WorkerHelper.getJob(type);
            if (job == null) {
                debug("No tool :(");
                WorkerHelper.removeJob(item.getWurmId());
            } else {
                debug("Some tool :)");
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

        registerRunner();

        ModActions.registerAction(new StartWorkAction());
        ModActions.registerAction(new StopWorkAction());
        ModActions.registerAction(new AddToolAction());
        ModActions.registerAction(new FuelUpAction());
        registerToolJobs();
    }


    private void registerToolJobs() {
        WorkerHelper.registerToolJob(ToolType.SHOVEL, new ShovelJob());
        WorkerHelper.registerToolJob(ToolType.CHISEL, new ChiselJob());
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
