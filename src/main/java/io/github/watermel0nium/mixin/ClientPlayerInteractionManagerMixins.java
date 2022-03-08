package io.github.watermel0nium.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.watermel0nium.ClientEventHandler;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixins {

	@Inject(method = "breakBlock(Lnet/minecraft/util/math/BlockPos;)Z", at = @At("HEAD"))
	private void onBreak(BlockPos pos, CallbackInfoReturnable<Boolean> ci) {
		MinecraftClient client = MinecraftClient.getInstance();
		BlockState state = client.world.getBlockState(pos);
		ClientEventHandler.onBreak(client.player, state);
	}

	@Inject(method = "attackEntity", at = @At("HEAD"))
	private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
		ClientEventHandler.onAttack(player, target);
	}
}
