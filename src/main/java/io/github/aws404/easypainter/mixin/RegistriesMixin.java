package io.github.aws404.easypainter.mixin;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SimpleRegistry.class)
public class RegistriesMixin {

    //@Inject(method="freezeRegistries()V", at = @At("HEAD"), cancellable = true)
    @Inject(method = "assertNotFrozen(Lnet/minecraft/registry/RegistryKey;)V", at = @At("HEAD"), cancellable = true)
    private void freezeRegistries(RegistryKey<?> key, CallbackInfo ci) {
        //System.out.println("CANCELLED!");
        ci.cancel();
    }
}
