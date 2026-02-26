package com.uniaball.circularrendering.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.objectweb.asm.tree.ClassNode;

import java.util.List;
import java.util.Set;

public class CircularRenderingMixinPlugin implements IMixinConfigPlugin {
    private static final String VANILLA_MIXIN = "com.uniaball.circularrendering.mixin.WorldRendererMixin";
    private static final String SODIUM_MIXIN = "com.uniaball.circularrendering.mixin.SodiumOcclusionCullerMixin";

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() { return null; }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        boolean sodiumLoaded = FabricLoader.getInstance().isModLoaded("sodium");

        if (mixinClassName.equals(VANILLA_MIXIN)) {
            return !sodiumLoaded;
        }
        if (mixinClassName.equals(SODIUM_MIXIN)) {
            return sodiumLoaded;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() { return null; }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}