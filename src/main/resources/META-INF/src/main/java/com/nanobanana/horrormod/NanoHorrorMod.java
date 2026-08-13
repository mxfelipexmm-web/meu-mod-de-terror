package com.nanobanana.horrormod;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollingGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(NanoHorrorMod.MOD_ID)
public class NanoHorrorMod {
    public static final String MOD_ID = "nanohorror";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final RegistryObject<Item> OLHO_AMALDICOADO = ITEMS.register("olho_amaldicoado",
            () -> new OlhoAmaldicoadoItem(new Item.Properties().rarity(Rarity.EPIC).stacksTo(1)));

    public static final RegistryObject<EntityType<VultoAnscestralEntity>> VULTO_ANCESTRAL = ENTITIES.register("vulto_ancestral",
            () -> EntityType.Builder.of(VultoAnscestralEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 2.2F)
                    .build("vulto_ancestral"));

    public static final RegistryObject<CreativeModeTab> TAB_HORROR = TABS.register("tab_horror",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.nanohorror"))
                    .icon(() -> new ItemStack(OLHO_AMALDICOADO.get()))
                    .build());

    public NanoHorrorMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ITEMS.register(modEventBus);
        ENTITIES.register(modEventBus);
        TABS.register(modEventBus);

        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::registerAttributes);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == TAB_HORROR.get()) {
            event.accept(OLHO_AMALDICOADO);
        }
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(VULTO_ANCESTRAL.get(), VultoAnscestralEntity.createAttributes().build());
    }

    public static class OlhoAmaldicoadoItem extends Item {
        public OlhoAmaldicoadoItem(Properties properties) {
            super(properties);
        }

        @Override
        public boolean isFoil(ItemStack stack) {
            return true;
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack itemstack = player.getItemInHand(hand);
            
            if (!level.isClientSide) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0));
                player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0));
                
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENDERMAN_SCREAM, SoundSource.PLAYERS, 1.0F, 0.5F);
                
                player.getCooldowns().addCooldown(this, 200);
            } else {
                for (int i = 0; i < 20; i++) {
                    level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                            player.getX() + (level.random.nextDouble() - 0.5D),
                            player.getY() + 1.0D + (level.random.nextDouble() - 0.5D),
                            player.getZ() + (level.random.nextDouble() - 0.5D),
                            0.0D, 0.05D, 0.0D);
                }
            }

            return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
        }
    }

    public static class VultoAnscestralEntity extends Monster {
        public VultoAnscestralEntity(EntityType<? extends Monster> entityType, Level level) {
            super(entityType, level);
        }

        public static AttributeSupplier.Builder createAttributes() {
            return Monster.createMonsterAttributes()
                    .add(Attributes.MAX_HEALTH, 50.0D)
                    .add(Attributes.MOVEMENT_SPEED, 0.40D)
                    .add(Attributes.ATTACK_DAMAGE, 10.0D)
                    .add(Attributes.FOLLOW_RANGE, 35.0D);
        }

        @Override
        protected void registerGoals() {
            this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, false));
            this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollingGoal(this, 0.8D));
            this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        }

        @Override
        public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
            boolean success = super.doHurtTarget(target);
            if (success && target instanceof LivingEntity livingTarget) {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 0));
                livingTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
                
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 0.8F, 0.2F);
            }
            return success;
        }

        @Override
        public void aiStep() {
            super.aiStep();
            if (this.level().isClientSide && this.random.nextInt(3) == 0) {
                this.level().addParticle(ParticleTypes.LARGE_SMOKE,
                        this.getRandomX(0.5D), this.getRandomY(), this.getRandomZ(0.5D),
                        0.0D, 0.0D, 0.0D);
            }
        }
    }
}
