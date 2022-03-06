package io.github.watermel0nium.event;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public class AttackHandlers {

    public static void init() {
        AttackEntityCallback.EVENT.register(AttackHandlers::onAttackEntity);
    }

    private static ActionResult onAttackEntity(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult ehr) {
        if(!world.isClient()) {

        }
        return ActionResult.PASS;
    }
}
