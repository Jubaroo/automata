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

import java.util.Collections;
import java.util.List;

public class FuelUpAction implements ModAction, BehaviourProvider, ActionPerformer {
    private final short actionId;
    private final ActionEntry actionEntry;
    public FuelUpAction() {
        actionId = (short) ModActions.getNextActionId();
        actionEntry = ActionEntry.createEntry(
                actionId,
                "Power up",
                "powering",
                new int[] { 0 }
                //new int[] { 6 /* ACTION_TYPE_NOMOVE */ }	// 6 /* ACTION_TYPE_NOMOVE */, 48 /* ACTION_TYPE_ENEMY_ALWAYS */, 36 /* ACTION_TYPE_ALWAYS_USE_ACTIVE_ITEM */
        );
        ModActions.registerAction(actionEntry);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item subject, Item target) {

        try {
            if (performer instanceof Player && (target.isUnenchantedTurret() || target.isEnchantedTurret()) &&
                    Automata.getLabouringSpirits(target) > 0) {
                if (subject.isGem()) {
                    return Collections.singletonList(actionEntry);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
    public boolean action(Action action, Creature performer, Item vesseledGem, Item target, short num, float counter) {
        try {
            if (vesseledGem.isGem() && vesseledGem.getData1() >0) {
                performer.getCommunicator().sendNormalServerMessage("Spirits hungrily consume the power!");
                action.setTimeLeft(0);
                int multiplier = target.getRarity() + 1;
                int power = vesseledGem.getData1() * multiplier;
                vesseledGem.setData1(0);
                vesseledGem.setQualityLevel(vesseledGem.getCurrentQualityLevel() - (power/multiplier));
                DatabaseHelper.increasePower(target, power);
               // DatabaseHelper.setTool(vesseledGem, target);
               // Items.destroyItem(vesseledGem.getWurmId());

            } else {
                performer.getCommunicator().sendNormalServerMessage("Gem is not vesseled.");
            }
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
