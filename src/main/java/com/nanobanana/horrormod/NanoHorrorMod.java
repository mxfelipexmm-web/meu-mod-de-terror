package com.nanobanana.horrormod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(NanoHorrorMod.MOD_ID)
public class NanoHorrorMod {
    public static final String MOD_ID = "nanohorror";

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    // Blocos
    public static final RegistryObject<Block> BLOCO_SANGRENTO = BLOCKS.register("bloco_sangrento",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.SLIME_BLOCK)
                    .requiresCorrectToolForDrops()));

    // Itens dos Blocos
    public static final RegistryObject<Item> BLOCO_SANGRENTO_ITEM = ITEMS.register("bloco_sangrento",
            () -> new BlockItem(BLOCO_SANGRENTO.get(), new Item.Properties()));

    // Espada do Bloco de Comando (Hit Kill Instantâneo)
    public static final RegistryObject<Item> ESPADA_COMANDO = ITEMS.register("espada_comando",
            () -> new SwordItem(CommandTier.INSTANCE, 99999, 10.0F, new Item.Properties().rarity(Rarity.EPIC).fireResistant()));

    // Armadura Sangrenta
    public static final RegistryObject<Item> CAPACETE_SANGRENTO = ITEMS.register("capacete_sangrento",
            () -> new ArmorItem(BloodyArmorMaterial.INSTANCE, ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> PEITORAL_SANGRENTO = ITEMS.register("peitoral_sangrento",
            () -> new ArmorItem(BloodyArmorMaterial.INSTANCE, ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> CALCA_SANGRENTA = ITEMS.register("calca_sangrenta",
            () -> new ArmorItem(BloodyArmorMaterial.INSTANCE, ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> BOTA_SANGRENTA = ITEMS.register("bota_sangrenta",
            () -> new ArmorItem(BloodyArmorMaterial.INSTANCE, ArmorItem.Type.BOOTS, new Item.Properties()));

    // Entidades
    public static final RegistryObject<EntityType<CavaloSangrentoEntity>> CAVALO_SANGRENTO = ENTITIES.register("cavalo_sangrento",
            () -> EntityType.Builder.of(CavaloSangrentoEntity::new, MobCategory.MONSTER)
                    .sized(1.4F, 1.6F)
                    .build("cavalo_sangrento"));

    public static final RegistryObject<EntityType<ChefeComandoEntity>> CHEFE_COMANDO = ENTITIES.register("chefe_comando",
            () -> EntityType.Builder.of(ChefeComandoEntity::new, MobCategory.MONSTER)
                    .sized(2.5F, 4.5F)
                    .build("chefe_comando"));

    // Aba Criativa
    public static final RegistryObject<CreativeModeTab> TAB_HORROR = TABS.register("tab_horror",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.nanohorror"))
                    .icon(() -> new ItemStack(ESPADA_COMANDO.get()))
                    .build());

    public NanoHorrorMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        ENTITIES.register(modEventBus);
        TABS.register(modEventBus);

        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(this::registerAttributes);

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(TAB_HORROR.getKey())) {
            event.accept(ESPADA_COMANDO);
            event.accept(BLOCO_SANGRENTO_ITEM);
            event.accept(CAPACETE_SANGRENTO);
            event.accept(PEITORAL_SANGRENTO);
            event.accept(CALCA_SANGRENTA);
            event.accept(BOTA_SANGRENTA);
        }
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(CAVALO_SANGRENTO.get(), CavaloSangrentoEntity.createAttributes().build());
        event.put(CHEFE_COMANDO.get(), ChefeComandoEntity.createAttributes().build());
    }

    // Tier da Espada de Comando (1 Hit Kill)
    public static class CommandTier implements Tier {
        public static final CommandTier INSTANCE = new CommandTier();
        @Override public int getUses() { return 9999; }
        @Override public float getSpeed() { return 15.0F; }
        @Override public float getAttackDamageBonus() { return 99999.0F; }
        @Override public int getLevel() { return 4; }
        @Override public int getEnchantmentValue() { return 30; }
        @Override public Ingredient getRepairIngredient() { return Ingredient.of(Blocks.COMMAND_BLOCK); }
    }

    // Material da Armadura Sangrenta
    public static class BloodyArmorMaterial implements ArmorMaterial {
        public static final BloodyArmorMaterial INSTANCE = new BloodyArmorMaterial();
        @Override public int getDurabilityForType(ArmorItem.Type type) { return 500; }
        @Override public int getDefenseForType(ArmorItem.Type type) { return 8; }
        @Override public int getEnchantmentValue() { return 20; }
        @Override public net.minecraft.sounds.SoundEvent getEquipSound() { return SoundEvents.ARMOR_EQUIP_NETHERITE; }
        @Override public Ingredient getRepairIngredient() { return Ingredient.of(BLOCO_SANGRENTO_ITEM.get()); }
        @Override public String getName() { return "nanohorror:sangrento"; }
        @Override public float getToughness() { return 3.0F; }
        @Override public float getKnockbackResistance() { return 0.1F; }
    }

    // Entidade: Cavalo Sangrento
    public static class CavaloSangrentoEntity extends Monster {
        public CavaloSangrentoEntity(EntityType<? extends Monster> type, Level level) {
            super(type, level);
        }

        public static AttributeSupplier.Builder createAttributes() {
            return Monster.createMonsterAttributes()
                    .add(Attributes.MAX_HEALTH, 40.0D)
                    .add(Attributes.MOVEMENT_SPEED, 0.45D)
                    .add(Attributes.ATTACK_DAMAGE, 12.0D)
                    .add(Attributes.FOLLOW_RANGE, 40.0D);
        }

        @Override
        protected void registerGoals() {
            this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2D, true));
            this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8D));
            this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        }

        @Override
        public void aiStep() {
            super.aiStep();
            if (this.level().isClientSide && this.random.nextInt(2) == 0) {
                this.level().addParticle(ParticleTypes.CRIMSON_SPORE,
                        this.getRandomX(0.8D), this.getRandomY(), this.getRandomZ(0.8D),
                        0.0D, 0.0D, 0.0D);
            }
        }
    }

    // Entidade: Chefe Supremo do Comando
    public static class ChefeComandoEntity extends Monster {
        public ChefeComandoEntity(EntityType<? extends Monster> type, Level level) {
            super(type, level);
        }

        public static AttributeSupplier.Builder createAttributes() {
            return Monster.createMonsterAttributes()
                    .add(Attributes.MAX_HEALTH, 800.0D)
                    .add(Attributes.MOVEMENT_SPEED, 0.35D)
                    .add(Attributes.ATTACK_DAMAGE, 25.0D)
                    .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D)
                    .add(Attributes.FOLLOW_RANGE, 50.0D);
        }

        @Override
        protected void registerGoals() {
            this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3D, false));
            this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.7D));
            this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        }

        @Override
        public boolean doHurtTarget(Entity target) {
            boolean success = super.doHurtTarget(target);
            if (success && target instanceof LivingEntity livingTarget) {
                livingTarget.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 160, 0));
                livingTarget.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.WITHER_DEATH, SoundSource.HOSTILE, 1.0F, 0.1F);
            }
            return success;
        }

        @Override
        public void aiStep() {
            super.aiStep();
            if (this.level().isClientSide) {
                for (int i = 0; i < 3; i++) {
                    this.level().addParticle(ParticleTypes.PORTAL,
                            this.getRandomX(1.2D), this.getRandomY(), this.getRandomZ(1.2D),
                            0.0D, 0.0D, 0.0D);
                }
            }
        }
    }

    // Eventos do Servidor (Sons Macabros Aleatórios e Gerador de Rituais)
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ServerHorrorEvents {

        @SubscribeEvent
        public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
                Player player = event.player;
                Level level = player.level();

                // Tocador de Sons Macabros Aleatórios
                if (level.random.nextInt(1200) == 0) { // A cada ~1 minuto
                    net.minecraft.sounds.SoundEvent[] sons = new net.minecraft.sounds.SoundEvent[]{
                            SoundEvents.GHAST_SCREAM,
                            SoundEvents.ENDERMAN_SCREAM,
                            SoundEvents.PHANTOM_SWOOP,
                            SoundEvents.ZOMBIE_HORSE_DEATH,
                            SoundEvents.AMBIENT_CAVE.get()
                    };
                    net.minecraft.sounds.SoundEvent somSorteado = sons[level.random.nextInt(sons.length)];
                    level.playSound(null, player.getX() + (level.random.nextInt(20) - 10),
                            player.getY(), player.getZ() + (level.random.nextInt(20) - 10),
                            somSorteado, SoundSource.AMBIENT, 0.8F, 0.3F);
                }

                // Geração Espontânea de Altar de Ritual no Mapa
                if (level.random.nextInt(5000) == 0) {
                    BlockPos pos = player.blockPosition().offset(level.random.nextInt(30) - 15, -1, level.random.nextInt(30) - 15);
                    if (level.getBlockState(pos).isSolid()) {
                        level.setBlockAndUpdate(pos, BLOCO_SANGRENTO.get().defaultBlockState());
                        level.setBlockAndUpdate(pos.above(), Blocks.RED_CANDLE.defaultBlockState());
                        level.playSound(null, pos, SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 0.5F, 0.1F);
                    }
                }
            }
        }
    }

    // Eventos do Cliente (Névoa de Terror Densa Cobrindo todo o Mundo)
    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientHorrorEvents {

        @SubscribeEvent
        public static void onRenderFog(ViewportEvent.RenderFog event) {
            event.setNearPlaneDistance(0.0F);
            event.setFarPlaneDistance(18.0F); // Névoa densa cobrindo a visão do jogador
            event.setCanceled(true);
        }

        @SubscribeEvent
        public static void onFogColor(ViewportEvent.ComputeFogColor event) {
            // Cor avermelhada escura para clima de pesadelo sangrento
            event.setRed(0.08F);
            event.setGreen(0.01F);
            event.setBlue(0.01F);
        }
    }
}
