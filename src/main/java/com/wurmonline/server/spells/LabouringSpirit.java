package com.wurmonline.server.spells;


import com.wurmonline.server.Server;
import com.wurmonline.server.behaviours.ActionEntry;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.items.Item;
import com.wurmonline.server.items.ItemSpellEffects;
import com.wurmonline.server.skills.Skill;
import org.gotti.wurmunlimited.modsupport.actions.ActionEntryBuilder;
import org.gotti.wurmunlimited.modsupport.actions.ModActions;
import org.takino.mods.Config;

public class LabouringSpirit extends ModReligiousSpell {


    public LabouringSpirit() {
        super("Labouring Spirits", ModActions.getNextActionId(), Config.spellCastingTime, Config.spellPrice,
                Config.spellDifficulty, Config.spellLevel, Config.spellCooldown * 1000);
        this.targetItem=true;
        this.targetCreature=false;
        this.effectdesc = "seems to have some spirits bound to it.";
        this.description = "has spirits bound to it, performing mundane tasks.";
        this.enchantment=Config.spellId;
        ActionEntry actionEntry = new ActionEntryBuilder((short) number, name, "casting", new int[] { 2 /* ACTION_TYPE_SPELL */, 36 /* ACTION_TYPE_ALWAYS_USE_ACTIVE_ITEM */, 48 /* ACTION_TYPE_ENEMY_ALWAYS */}).build();
        ModActions.registerAction(actionEntry);
    }

    public static boolean isValidTarget(Item target) {
        return target.isUnenchantedTurret();
    }

    @Override
    public boolean precondition(final Skill castSkill, final Creature performer, final Item target) {
        if (!isValidTarget(target)) {
            performer.getCommunicator().sendNormalServerMessage("The spell will not work on that.");
            return false;
        }
        return true;
    }

    @Override
    public void doEffect(final Skill castSkill, final double power, final Creature performer, final Item target) {
        if (!isValidTarget(target)) {
            performer.getCommunicator().sendNormalServerMessage("The spell fizzles.");
            return;
        }

        ItemSpellEffects effs = target.getSpellEffects();
        if (effs == null) {
            effs = new ItemSpellEffects(target.getWurmId());
        }
        SpellEffect eff = effs.getSpellEffect(Config.spellId);
        if (eff == null) {
            performer.getCommunicator().sendNormalServerMessage("You bind labouring spirits to the " + target.getName() + ".");
            eff = new SpellEffect(target.getWurmId(), Config.spellId, (float) power, 20000000);
            effs.addSpellEffect(eff);
            Server.getInstance().broadCastAction(String.valueOf(performer.getName()) + " looks pleased as " + performer.getHeSheItString() +
                    " binds labouring spirits to the " + target.getName() + ".", performer, 5);
        } else if (eff.getPower() > power) {
            performer.getCommunicator().sendNormalServerMessage("You frown as you fail to summon more spirits to the " + target.getName() + ".");
            Server.getInstance().broadCastAction(String.valueOf(performer.getName()) + " frowns.", performer, 5);
        } else {
            performer.getCommunicator().sendNormalServerMessage("You succeed in binding more spirits to the " + target.getName() + ".");
            eff.improvePower((float) power);
            Server.getInstance().broadCastAction(String.valueOf(performer.getName()) + " looks pleased.", performer, 5);
        }
    }

    public static float getSpellEffect(Item target) {
        if (!isValidTarget(target))
            return 0;

        ItemSpellEffects effs = target.getSpellEffects();
        if (effs == null)
            return 0;

        SpellEffect eff = effs.getSpellEffect(Config.spellId);
        if (eff == null)
            return 0;

        return eff.getPower();
    }
}
