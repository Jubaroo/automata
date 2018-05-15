package org.takino.mods.actions;

import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.players.Player;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;
import org.takino.mods.Automata;
import org.takino.mods.helpers.DatabaseHelper;
import org.takino.mods.helpers.WorkerHelper;

import java.util.Collections;
import java.util.List;

public class StartWorkAction implements ModAction, BehaviourProvider, ActionPerformer {
    private final short actionId;
    private final ActionEntry actionEntry;

    public StartWorkAction() {
        actionId = (short) ModActions.getNextActionId();
        actionEntry = ActionEntry.createEntry(
                actionId,
                "Start working",
                "commanding",
                new int[]{0}
                //new int[] { 6 /* ACTION_TYPE_NOMOVE */ }	// 6 /* ACTION_TYPE_NOMOVE */, 48 /* ACTION_TYPE_ENEMY_ALWAYS */, 36 /* ACTION_TYPE_ALWAYS_USE_ACTIVE_ITEM */
        );
        ModActions.registerAction(actionEntry);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item target) {

        // TODO: Replace (byte) 121 with SpellcraftSpell.LABOURING_SPIRIT.getEnchant()
        if (performer instanceof Player && (target.isUnenchantedTurret() || target.isEnchantedTurret()) &&
                !WorkerHelper.contains(target.getWurmId())) {
            return Collections.singletonList(actionEntry);
        }
        return null;
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item subject, Item target) {

        return this.getBehavioursFor(performer, target);
    }

    @Override
    public BehaviourProvider getBehaviourProvider() {
        return this;
    }


    @Override
    public ActionPerformer getActionPerformer() {
        return this;
    }


    @Override
    public boolean action(Action action, Creature performer, Item source, Item target, short num, float counter) {
        return action(action, performer, target, num, counter);
    }

    @Override
    public boolean action(Action action, Creature performer, Item target, short num, float counter) {
        // return false;
        try {
            if (!(target.isUnenchantedTurret() || target.isEnchantedTurret())) {
                performer.getCommunicator().sendNormalServerMessage("You cannot command this object to work for you.");
                return true;
            }
            if (Automata.getLabouringSpirits(target) <= 0) {
                performer.getCommunicator().sendNormalServerMessage("The device must be enchanted with Labouring Spirits before it can begin working.");
                return true;
            }
            if (WorkerHelper.contains(target.getWurmId())) {
                performer.getCommunicator().sendNormalServerMessage("The device is already working!");
                return true;
            }
            if (!WorkerHelper.hasEnoughPower(target, DatabaseHelper.getUsage(target))) {
                performer.getCommunicator().sendNormalServerMessage("Strange device did not move after your command.");
            }
            performer.getCommunicator().sendNormalServerMessage("You command the strange device to start working!");
            //action.setTimeLeft(0);
            WorkerHelper.addJob(target, performer);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    @Override
    public short getActionId() {
        return actionId;
    }
}
