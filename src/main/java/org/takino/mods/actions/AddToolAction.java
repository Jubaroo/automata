package org.takino.mods.actions;

import com.wurmonline.server.Items;
import com.wurmonline.server.behaviours.Action;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemList;
import com.wurmonline.server.players.Player;
import org.gotti.wurmunlimited.modsupport.actions.ActionPerformer;
import org.gotti.wurmunlimited.modsupport.actions.BehaviourProvider;
import org.gotti.wurmunlimited.modsupport.actions.ModAction;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;
import org.takino.mods.Automata;
import org.takino.mods.helpers.DatabaseHelper;
import org.takino.mods.helpers.WorkerHelper;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class AddToolAction implements ModAction, BehaviourProvider, ActionPerformer {
    private final short actionId;
    private final ActionEntry actionEntry;
    public AddToolAction() {
        actionId = (short) ModActions.getNextActionId();
        actionEntry = ActionEntry.createEntry(
                actionId,
                "Infuse with tool",
                "giving",
                new int[] { 0 }
                //new int[] { 6 /* ACTION_TYPE_NOMOVE */ }	// 6 /* ACTION_TYPE_NOMOVE */, 48 /* ACTION_TYPE_ENEMY_ALWAYS */, 36 /* ACTION_TYPE_ALWAYS_USE_ACTIVE_ITEM */
        );
        ModActions.registerAction(actionEntry);
    }

    @Override
    public List<ActionEntry> getBehavioursFor(Creature performer, Item subject, Item target) {
        try {
            if (performer instanceof Player && (target.isUnenchantedTurret() || target.isEnchantedTurret()) &&
            Automata.getLabouringSpirits(target) > 0 && !WorkerHelper.contains(target.getWurmId()) &&
                    !DatabaseHelper.hasTool(target)) {
                if (subject.getTemplateId() == ItemList.shovel || // shovel
                        subject.getTemplateId() == ItemList.stoneChisel) {
                    return Collections.singletonList(actionEntry);
                }
            }
        } catch (SQLException e) {
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
    public boolean action(Action action, Creature performer, Item usedTool, Item target, short num, float counter) {
        try {
            if(usedTool.getTemplateId() != ItemList.shovel || usedTool.getTemplateId() != ItemList.stoneChisel){
                performer.getCommunicator().sendNormalServerMessage("You must supply a proper tool.");
                return true;
            }
            if (usedTool.getRarity() > 0) {
                performer.getCommunicator().sendNormalServerMessage("You give the tool to the strange device!");
                //action.setTimeLeft(0);
                DatabaseHelper.setTool(usedTool, target);
                Items.destroyItem(usedTool.getWurmId());
            } else {
                performer.getCommunicator().sendNormalServerMessage("Spirits refuse to work with this crude tool. They require something shiny.");
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
