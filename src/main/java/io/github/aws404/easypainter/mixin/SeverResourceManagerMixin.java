package io.github.aws404.easypainter.mixin;

import io.github.aws404.easypainter.EasyPainter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.DataPackContents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.Unit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(DataPackContents.class)
public class SeverResourceManagerMixin {

    @Inject(method = "reload", at = @At(value = "RETURN", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD)
    private static void onReloadDataPacks(ResourceManager manager, CombinedDynamicRegistries<ServerDynamicRegistryType> dynamicRegistries, FeatureSet enabledFeatures, CommandManager.RegistrationEnvironment environment, int functionPermissionLevel, Executor prepareExecutor, Executor applyExecutor, CallbackInfoReturnable<CompletableFuture<DataPackContents>> cir) {
        if (EasyPainter.customMotivesManager != null) {
            EasyPainter.customMotivesManager.reload(manager);
        }
    }
}
