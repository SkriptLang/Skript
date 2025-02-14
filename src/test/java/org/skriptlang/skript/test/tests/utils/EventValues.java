package org.skriptlang.skript.test.tests.utils;

public class EventValues {
//
//	private final Map<Material, Block> blockMap = new HashMap<>();
//	private final Map<Material, ItemStack> itemMap = new HashMap<>();
//	private final Map<Material, Item> droppedMap = new HashMap<>();
//	private final Map<Class<? extends Entity>, Entity> entityMap = new HashMap<>();
//
//	public Block blockGet(Material material) {
//		if (blockMap.containsKey(material)) {
//			Block block = blockMap.get(material);
//			if (block.getType() == material)
//				return block;
//		}
//
//		Location loc = getTestLocation().add(new Vector(blockMap.size(), 0, 0));
//		Block block = loc.getBlock();
//		block.setType(material);
//		blockMap.put(material, block);
//		return block;
//	}
//
//	public BlockState stateGet(Material material) {
//		Block block = blockGet(material);
//		return block.getState();
//	}
//
//	public ItemStack itemGet(Material material) {
//		if (itemMap.containsKey(material))
//			return itemMap.get(material);
//
//		ItemStack item = new ItemStack(material);
//		itemMap.put(material, item);
//		return item;
//	}
//
//	public Item dropGet(Material material) {
//		if (droppedMap.containsKey(material))
//			return droppedMap.get(material);
//
//		Item drop = getTestWorld().dropItem(getTestLocation(), itemGet(material));
//		droppedMap.put(material, drop);
//		return drop;
//	}
//
//	public Entity entityGet(Class<? extends Entity> entityClass) {
//		if (entityMap.containsKey(entityClass)) {
//			Entity entity = entityMap.get(entityClass);
//			if (entity != null && !entity.isDead())
//				return entity;
//		}
//
//		Entity entity = getTestWorld().spawn(getTestLocation(), entityClass);
//		entityMap.put(entityClass, entity);
//		return entity;
//	}
//
//	private final Player player = EasyMock.niceMock(Player.class);
//	private final DamageSource damageSource = DamageSource.builder(DamageType.FALL).build();
//
//	@Before
//	public void setUp() {
//
//	}
//
//	@Test
//	public void test() {
//		if (true == true)
//			return;
//
//		List<Event> events = new ArrayList<>();
//		events.add(
//			new BeaconEffectEvent(
//				blockGet(Material.BEACON),
//				new PotionEffect(PotionEffectType.HASTE, 1, 1),
//				player,
//				true
//			)
//		);
//		events.add(new BeaconActivatedEvent(blockGet(Material.BEACON)));
//		events.add(new BlockBreakEvent(blockGet(Material.STONE), player));
//		events.add(new BlockBurnEvent(blockGet(Material.OAK_LOG), null));
//		events.add(
//			new BlockPlaceEvent(
//				blockGet(Material.COBBLESTONE),
//				stateGet(Material.COBBLESTONE),
//				blockGet(Material.AIR),
//				itemGet(Material.COBBLESTONE),
//				player,
//				true
//			)
//		);
//		events.add(
//			new BlockFadeEvent(blockGet(Material.DEEPSLATE), stateGet(Material.DEEPSLATE))
//		);
//		events.add(new BlockFormEvent(blockGet(Material.TUFF), stateGet(Material.TUFF)));
//		events.add(
//			new BlockDropItemEvent(blockGet(Material.IRON_ORE), stateGet(Material.IRON_ORE), player, List.of(dropGet(Material.RAW_IRON)))
//		);
//		events.add(
//			new PlayerEditBookEvent(
//				player,
//				0,
//				(BookMeta) itemGet(Material.BOOK).getItemMeta(),
//				(BookMeta) itemGet(Material.BOOK).getItemMeta(),
//				false
//			)
//		);
//		events.add(
//			new PlayerInteractEvent(
//				player,
//				Action.LEFT_CLICK_BLOCK,
//				itemGet(Material.AIR),
//				blockGet(Material.CHEST),
//				BlockFace.NORTH
//			)
//		);
//		events.add(
//			new PlayerCommandPreprocessEvent(player, "eventvaluestest")
//		);
//		events.add(
//			new EntityDamageEvent(entityGet(Zombie.class), DamageCause.FALL, damageSource, 10)
//		);
//		events.add(new EntityDeathEvent((LivingEntity) entityGet(Skeleton.class), damageSource, List.of(itemGet(Material.BONE))));
//		events.add(new EntitySpawnEvent(entityGet(Villager.class)));
//		events.add(
//			new EntityChangeBlockEvent(entityGet(Sheep.class), blockGet(Material.GRASS_BLOCK), blockGet(Material.DIRT).getBlockData())
//		);
//		events.add(
//			new EntityPotionEffectEvent(
//				(LivingEntity) entityGet(Cow.class),
//				null,
//				null,
//				Cause.PLUGIN,
//				EntityPotionEffectEvent.Action.CHANGED,
//				true
//			)
//		);
//		events.add(new EntityTargetEvent(entityGet(Zombie.class), entityGet(Villager.class), TargetReason.CUSTOM));
//		events.add(new EntityTransformEvent(entityGet(Villager.class), List.of(entityGet(ZombieVillager.class)), TransformReason.UNKNOWN));
//		events.add(new PlayerExpChangeEvent(player, 10));
//		events.add(new ExperienceSpawnEvent(10, getTestLocation()));
//		events.add(new FireworkExplodeEvent((Firework) entityGet(Firework.class)));
//		events.add(new PlayerJoinEvent(player, (Component) null));
//		events.add(new PlayerGameModeChangeEvent(player, GameMode.CREATIVE, PlayerGameModeChangeEvent.Cause.PLUGIN, null));
//		events.add(new BlockGrowEvent(blockGet(Material.OAK_SAPLING), stateGet(Material.OAK_SAPLING)));
//		events.add(new EntityRegainHealthEvent(entityGet(Pig.class), 10d, RegainReason.CUSTOM));
//		events.add(new BlockDispenseEvent(blockGet(Material.DISPENSER), itemGet(Material.IRON_INGOT), new Vector()));
//		events.add(new ItemSpawnEvent(dropGet(Material.COPPER_ORE)));
//		events.add(new PlayerDropItemEvent(player, dropGet(Material.IRON_NUGGET)));
//		//events.add(new PrepareItemCraftEvent())
//		//events.add(new CraftItemEvent())
//		events.add(new PlayerPickupItemEvent(player, dropGet(Material.COPPER_INGOT), 30));
//		events.add(new PlayerItemConsumeEvent(player, itemGet(Material.APPLE), EquipmentSlot.HAND));
//		//events.add(new InventoryClickEvent())
//		//events.add(new InventoryMoveItemEvent())
//		//events.add(new PlayerStonecutterRecipeSelectEvent())
//		events.add(new PlayerLeashEntityEvent(entityGet(Llama.class), player, player, EquipmentSlot.HAND));
//		events.add(new EntityUnleashEvent(entityGet(Horse.class), UnleashReason.UNKNOWN, false));
//		events.add(new PlayerLevelChangeEvent(player, 9, 10));
//		events.add(new PlayerMoveEvent(player, getTestLocation(), getTestLocation()));
//
//	}
//
//	@After
//	public void cleanUp() {
//		for (Block block : blockMap.values())
//			block.setType(Material.AIR);
//	}

}
