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
import java.util.logging.Logger;

public class FuelUpAction implements ModAction {
    private static Logger logger = Logger.getLogger(FuelUpAction.class.getName());

    private final short actionId;
    private final ActionEntry actionEntry;
    public FuelUpAction() {
        actionId = (short) ModActions.getNextActionId();
        actionEntry = ActionEntry.createEntry(
                actionId,
                "Power up",
                "powering",
                new int[] { 6 }
                //new int[] { 6 /* ACTION_TYPE_NOMOVE */ }	// 6 /* ACTION_TYPE_NOMOVE */, 48 /* ACTION_TYPE_ENEMY_ALWAYS */, 36 /* ACTION_TYPE_ALWAYS_USE_ACTIVE_ITEM */
        );
        ModActions.registerAction(actionEntry);
    }

    @Override
    public BehaviourProvider getBehaviourProvider()
    {
        return new BehaviourProvider() {
            // Menu with activated object
            @Override
            public List<ActionEntry> getBehavioursFor(Creature performer, Item source, Item object)
            {
                if (performer instanceof Player && (object.isUnenchantedTurret() || object.isEnchantedTurret()) &&
                        Automata.getLabouringSpirits(object) > 0 && source.isGem()) {
                    return Collections.singletonList(actionEntry);
                }
                return null;
            }
        };
    }

    @Override
    public ActionPerformer getActionPerformer()
    {
        return new ActionPerformer() {

            @Override
            public short getActionId() {
                return actionId;
            }

            // With activated object
            @Override
            public boolean action(Action act, Creature performer, Item source, Item target, short action, float counter)
            {
                try {
                    if (source.isGem() && source.getData1() >0) {
                        performer.getCommunicator().sendNormalServerMessage("The spirits hungrily consume the power!");
                        int multiplier = target.getRarity() + 1;
                        int power = source.getData1() * multiplier;
                        source.setData1(0);
                        source.setQualityLevel(source.getCurrentQualityLevel() - (power/multiplier));
                        DatabaseHelper.increasePower(target, power);
                    } else {
                        performer.getCommunicator().sendNormalServerMessage("The gem must be vesseled.");
                    }
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return true;
                }
            }
        }; // ActionPerformer
    }
}
