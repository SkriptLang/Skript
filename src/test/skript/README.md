# Skript Testing System
Configuration and actual test scripts for Skript's integration test system can
found here.

## Contributing to Tests
We're not very strict about what can be added to our test suite.
Tests don't have to be good code - often, testing edge cases pretty
much requires weird code. That being said, there are a couple of things
to keep in mind:

* Use tabs for indentation
* Write descriptive assert messages and write comments
* Ensure your tests pass with all supported Skript versions
  * Use standard condition for checking MC version
* When writing tests for your pull requests, please ensure that manipulations are cleaned up afterwards. This includes actions such as removing mobs that were manually spawned and resetting manipulated blocks to their original state. Also, try to avoid spawning hostile mobs, as this can cause issues (e.g. a spawned zombie catching fire from the sun).

Note: The test world generate as a default super flat. Bedrock, dirt, grass, air, air...+ A good practice is to be using `spawn of world "world"` as a homing location, or anywhere near 0, 0, 0 is reasonable.

## Test Categories
Scripts under <code>tests</code> are run on environments. Most of them are
stored in subdirectories to help keep them organized.

### Syntax tests
Under <code>syntaxes</code>, there are tests for individual expressions,
effects and commands. Again, each in their own subdirectories. Names of files
should follow names of respective syntax implementations in Java
(e.g. <code>ExprTool.sk</code>).

Contributors can add tests for expressions that do not yet have them, or
improve existing tests.

### Regression tests
Under <code>regressions</code>, there are regression tests. Such tests are
created when bugs are fixed to ensure they do not come back in future.
File names should contain respective issue number and its title. If no issue
is available, PR number and title can be used in place of them.

For example, <code>2381-multiplication in list index.sk</code>. Use only
lower case letters in names, because issue titles are not consistent with
their capitalization.

Contributors should not add regression tests unless they also fix bugs in
Skript. Those who do fix bugs *should* write regression tests.

### Miscellaneous tests
All other tests go in this subdirectory. Contributions for generic tests
will need to meet a few criteria:

* They must not be duplicates of other tests
  * Similar tests are ok
* They must currently pass
* They should not rely on bugs in Skript to pass

Aside these things, pretty much anything goes.

## Testing Syntaxes
Test scripts have all normal Skript syntaxes available. In addition to that,
some syntaxes for test development are available.

* Minecraft version condition <code>running [(1¦below)] minecraft %string%</code>
  * Example: <code>if running minecraft "1.15":</code>
* Event test cases: <code>test %string% [when <.+>]</code>
  * Example: <code>test "test name" when running minecraft "1.18.1":</code>
  * Contents of tests are not parsed when conditions are not met.
  * Typically the condition isn't required.
  * Required to start a test script.
* Assertions are available as effects: <code>assert <.+> [(1¦to fail)] with [error] %string%[, expected [value] %object%, [and] (received|got) [value] %object%]</code>
  * Example: <code>assert {_entity} is a zombie with "failure message"</code> will error if it's not a zombie.
  * The optional 'expected' and 'got' values are used in the error report to show what the assertion expected and what it actually got.
  * Assertions using some conditions, like CondCompare and CondIsSet, may automatically fill in the expected/got values.
  * If the tag `to fail` is defined, it will assume the condition is to fail. If it's successful the string is printed.
* Take a look at existing tests for examples https://github.com/SkriptLang/Skript/tree/master/src/test/skript/tests
  <code>misc/dummy.sk</code> is useful for beginners
* case_equals Function. Returns boolean. Useful to check that all string values equal the same. Examples:
	* <code>case_equals("hi", "Hi") = false</code>
	* <code>case_equals("text", "text", "text") = true</code>
	* <code>case_equals({some list variable::*})</code>

## Test Development
Use Gradle to launch a test development server:

```
gradlew clean skriptTestDev --console=plain
```

Note: adding the tag `clean` will clear the build directory, making Skript generate a new server each time.
Don't include the `clean` tag if you want to keep the same server folder around each test.

The server launched will be running at localhost:25565. You can use console
as normal, though there is some lag due to Gradle. If you're having trouble,
try without <code>--console=plain</code>.

Server files are located at <code>build/test_runners</code>.

To run individual test files, use <code>/sk test \<file\></code>. To run last
used file again, just use <code>/sk test</code>.

## Test Coverage

| Type        | Coverage | Percent |
|-------------|----------|---------|
| Expressions | 159/463  | 34%     |
| Conditions  | 34/157   | 22%     |
| Effects     | 61/140   | 44%     |
| Sections    | 8/8      | 100%    |

### Expressions

---

- [ ] ExprAppliedEffect
- [ ] ExprTPS
- [ ] ExprDamage
- [ ] ExprExplosionYield
- [ ] ExprAppliedEnchantments
- [x] ExprMessage
- [ ] ExprInventoryCloseReason
- [ ] ExprCmdCooldownInfo
- [x] ExprDrops
- [ ] LitNaN
- [ ] ExprHatchingNumber
- [ ] ExprRandomUUID
- [ ] LitFloatMinValue
- [x] ExprWhitelist
- [ ] LitIntMaxValue
- [ ] LitDoubleMinValue
- [ ] ExprPushedBlocks
- [ ] ExprEnchantingExpCost
- [ ] ExprSecCreateWorldBorder
- [ ] ExprHotbarButton
- [ ] ExprPortal
- [ ] ExprWorlds
- [ ] ExprEvtInitiator
- [ ] ExprAbsorbedBlocks
- [ ] LitConsole
- [x] ExprVectorRandom
- [ ] ExprParseError
- [ ] LitNewLine
- [ ] ExprOnScreenKickMessage
- [ ] ExprSentCommands
- [ ] ExprLastLoadedServerIcon
- [ ] LitFloatMaxValue
- [ ] LitLongMinValue
- [ ] ExprSourceBlock
- [ ] ExprOfflinePlayers
- [ ] ExprFertilizedBlocks
- [ ] ExprAttacked
- [ ] ExprBarterInput
- [ ] ExprProtocolVersion
- [ ] ExprAttacker
- [ ] ExprAffectedEntities
- [ ] ExprHanging
- [ ] ExprEnchantmentBonus
- [ ] ExprProjectileForce
- [ ] ExprRespawnLocation
- [ ] ExprHatchingType
- [ ] ExprUnleashReason
- [ ] ExprLoot
- [ ] ExprCaughtErrors
- [ ] LitAt
- [ ] ExprHostname
- [x] ExprHoverList
- [ ] ExprPlugins
- [ ] ExprOps
- [ ] ExprTransformReason
- [ ] ExprExperience
- [ ] ExprMendingRepairAmount
- [ ] ExprBreedingFamily
- [ ] ExprEnchantItem
- [ ] ExprHealAmount
- [ ] ExprAllCommands
- [ ] ExprCommand
- [x] ExprLoopValue
- [x] ExprConfig
- [ ] ExprBarterDrops
- [ ] LitPi
- [ ] ExprMOTD
- [ ] LitLongMaxValue
- [ ] ExprTamer
- [x] LitEternity
- [ ] ExprReadiedArrow
- [ ] LitNegativeInfinity
- [ ] ExprConsumedItem
- [x] ExprTagsOfType
- [ ] ExprScripts
- [ ] ExprChatRecipients
- [ ] ExprScriptsOld
- [ ] ExprVersionString
- [ ] LitIntMinValue
- [ ] LitDoubleMaxValue
- [ ] ExprAllBannedEntries
- [ ] ExprMemory
- [ ] ExprVersion
- [ ] ExprChatFormat
- [ ] ExprMe
- [ ] LitInfinity
- [ ] ExprNow
- [ ] ExprFinalDamage
- [x] ExprNumberOfCharacters
- [ ] ExprShooter
- [ ] ExprClicked
- [ ] ExprAngle
- [x] ExprStringCase
- [ ] ExprWorldFromName
- [x] ExprTimes
- [ ] ExprLastSpawnedEntity
- [ ] ExprLoopIteration
- [ ] ExprArgument
- [x] ExprScript
- [ ] ExprRawName
- [ ] ExprEnchantmentOffer
- [x] ExprVectorSpherical
- [x] ExprVectorCylindrical
- [x] ExprRotate
- [x] ExprLootContext
- [ ] ExprSpawnReason
- [ ] ExprHealReason
- [ ] ExprDamageCause
- [ ] ExprCommandSender
- [ ] ExprFishingWaitTime
- [ ] ExprItem
- [ ] ExprInventoryAction
- [ ] ExprFishingHook
- [ ] ExprEgg
- [ ] ExprCreatedDamageSource
- [ ] ExprTeleportCause
- [ ] ExprFishingBiteTime
- [ ] ExprFishingHookEntity
- [ ] ExprQuitReason
- [ ] ExprFishingApproachAngle
- [ ] ExprArmorChangeItem
- [ ] ExprExperienceCooldownChangeReason
- [ ] ExprSecBlankEquipComp
- [ ] ExprExplodedBlocks
- [ ] ExprBrewingResults
- [ ] ExprInverse
- [ ] ExprTargetedBlock
- [x] ExprKeyed
- [x] ExprBrewingSlot
- [ ] ExprDequeuedQueue
- [x] ExprColoured
- [ ] ExprShuffledList
- [x] ExprFilter
- [x] ExprHash
- [ ] ExprTag
- [ ] ExprSecDamageSource
- [ ] ExprReversedList
- [x] ExprIndices
- [x] ExprName
- [ ] ExprAlphabetList
- [ ] ExprItems
- [x] ExprQueue
- [ ] ExprItemCompCopy
- [ ] ExprLootTableFromString
- [ ] ExprRawString
- [x] ExprBannerItem
- [ ] ExprPlain
- [ ] ExprInput
- [x] ExprVectorNormalize
- [ ] ExprDistance
- [x] ExprResult
- [ ] ExprDirection
- [x] ExprVectorCrossProduct
- [x] ExprNearestEntity
- [x] ExprVectorAngleBetween
- [x] ExprTernary
- [x] ExprEntitySound
- [x] ExprVectorBetweenLocations
- [ ] ExprInventorySlot
- [x] ExprDifference
- [x] ExprExcept
- [ ] ExprDateAgoLater
- [ ] ExprGameRule
- [x] ExprIndicesOfValue
- [x] ExprJoinSplit
- [x] ExprEntityAttribute
- [x] ExprMidpoint
- [ ] ExprRandom
- [x] ExprNewBannerPattern
- [x] ExprWardenEntityAnger
- [x] ExprRepeat
- [ ] ExprItemCooldown
- [ ] ExprNumbers
- [ ] ExprBreakSpeed
- [ ] ExprChestInventory
- [x] ExprVectorDotProduct
- [ ] ExprDamagedItem
- [ ] ExprBlockSphere
- [x] ExprLootItems
- [x] ExprFunction
- [x] ExprVectorFromYawAndPitch
- [x] ExprDefaultValue
- [x] ExprCharacters
- [x] ExprParse
- [ ] ExprPotionEffectTier
- [ ] ExprWithItemFlags
- [ ] ExprVectorProjection
- [ ] ExprFireworkEffect
- [ ] ExprPotionEffect
- [ ] ExprRandomCharacter
- [ ] ExprSubstring
- [x] ExprVectorFromXYZ
- [ ] ExprDropsOfBlock
- [x] ExprRandomNumber
- [ ] ExprSecCreateLootContext
- [x] ExprBlocks
- [x] ExprLocationFromVector
- [x] ExprLocation
- [x] ExprBlock
- [ ] ExprChunk
- [ ] ExprLocationAt
- [x] ExprPercent
- [ ] ExprOnlinePlayersCount
- [ ] ExprServerIcon
- [ ] ExprFurnaceEventItems
- [ ] ExprExplosionBlockYield
- [x] ExprMaxPlayers
- [x] ExprUUID
- [x] ExprUnbreakable
- [ ] ExprGravity
- [ ] ExprAllayJukebox
- [ ] ExprDirectEntity
- [x] ExprVillagerProfession
- [x] ExprLastDamage
- [ ] ExprBeaconRange
- [x] ExprExplosiveYield
- [x] ExprBrewingFuelLevel
- [x] ExprBookTitle
- [ ] ExprLastDeathLocation
- [ ] ExprSeaLevel
- [ ] ExprEyeLocation
- [ ] ExprAnvilText
- [x] ExprNoDamageTicks
- [x] ExprBeehiveFlower
- [ ] ExprWardenAngryAt
- [x] ExprVillagerLevel
- [ ] ExprExperienceCooldown
- [x] ExprTextDisplayLineWidth
- [x] ExprWorldBorderSize
- [ ] ExprHealth
- [x] ExprDisplayShadow
- [ ] ExprEventExpression
- [ ] ExprBeaconTier
- [ ] ExprMinecartDerailedFlyingVelocity
- [x] ExprDisplayViewRange
- [ ] ExprVehicle
- [ ] ExprCharacterFromCodepoint
- [ ] ExprLootContextLocation
- [x] ExprExactItem
- [ ] ExprScoreboardTags
- [x] ExprAmount
- [ ] ExprInventoryInfo
- [ ] ExprQueueStartEnd
- [x] ExprDomestication
- [x] ExprLastDamageCause
- [x] ExprPotionEffects
- [ ] ExprDifficulty
- [ ] ExprAI
- [ ] ExprMiddleOfLocation
- [ ] ExprEquippableComponent
- [ ] ExprLevelProgress
- [ ] ExprFoodLevel
- [ ] ExprResonatingTime
- [x] ExprTextOf
- [ ] ExprLastAttacker
- [ ] ExprEquipCompShearSound
- [ ] ExprFurnaceTime
- [ ] ExprTablistedPlayers
- [ ] ExprFlightMode
- [ ] ExprLootContextLuck
- [ ] ExprFurnaceSlot
- [ ] ExprSeaPickles
- [ ] ExprLocationOf
- [ ] ExprIP
- [ ] ExprUnixTicks
- [x] ExprMaxDurability
- [ ] ExprRedstoneBlockPower
- [x] ExprDisplayInterpolation
- [ ] ExprPermissions
- [ ] ExprLevel
- [x] ExprWorldBorderWarningTime
- [ ] ExprWorld
- [x] ExprUnixDate
- [ ] ExprTimePlayed
- [x] ExprMaxStack
- [x] ExprFirstEmptySlot
- [x] ExprVectorOfLocation
- [ ] ExprAnvilRepairCost
- [x] ExprItemWithEnchantmentGlint
- [x] ExprBookAuthor
- [x] ExprEntityStorageEntityCount
- [x] ExprCarryingBlockData
- [x] ExprXYZComponent
- [ ] ExprFoodExhaustion
- [ ] ExprCausingEntity
- [ ] ExprRecursiveSize
- [x] ExprFreezeTicks
- [x] ExprEntityOwner
- [x] ExprQuaternionAxisAngle
- [x] ExprSkullOwner
- [ ] ExprProjectileCriticalState
- [x] ExprCustomModelData
- [x] ExprColorOf
- [x] ExprWorldBorderDamageAmount
- [x] ExprEntitySize
- [ ] ExprEnchantmentOfferCost
- [ ] ExprBlockHardness
- [ ] ExprSaturation
- [x] ExprTextDisplayOpacity
- [x] ExprTimeSince
- [ ] ExprSpawn
- [x] ExprDisplayGlowOverride
- [ ] ExprMaxHealth
- [ ] ExprTime
- [x] ExprBlockData
- [x] ExprItemThrower
- [ ] ExprCommandInfo
- [ ] ExprLastResourcePackResponse
- [ ] ExprWorldBorder
- [x] ExprItemWithTooltip
- [ ] ExprSpectatorTarget
- [ ] ExprBeaconEffects
- [x] ExprTagContents
- [ ] ExprEnderChest
- [ ] ExprGlowing
- [ ] ExprSimulationDistance
- [x] ExprSortedList
- [ ] ExprAttackCooldown
- [x] ExprTool
- [x] ExprCodepoint
- [ ] ExprCharges
- [ ] ExprAge
- [x] ExprLootTable
- [ ] ExprEntityItemUseTime
- [ ] ExprFromUUID
- [ ] ExprPlayerlistHeaderFooter
- [x] ExprItemFlags
- [ ] ExprDisplayTransformationRotation
- [ ] ExprEquipCompEntities
- [ ] ExprTemperature
- [ ] ExprDamageLocation
- [ ] ExprMaxItemUseTime
- [x] ExprTextDisplayAlignment
- [ ] ExprMaxFreezeTicks
- [ ] ExprNoDamageTime
- [ ] ExprDebugInfo
- [ ] ExprPing
- [ ] ExprBrewingTime
- [ ] ExprSourceLocation
- [x] ExprSpawnerType
- [x] ExprItemOwner
- [ ] ExprEquipCompEquipSound
- [x] ExprDurability
- [ ] ExprGlidingState
- [ ] ExprTimeState
- [ ] ExprLootContextEntity
- [ ] ExprGameMode
- [x] ExprBeehiveHoneyLevel
- [ ] ExprActiveItem
- [ ] ExprPickupDelay
- [ ] ExprHumidity
- [ ] ExprLength
- [ ] ExprPassenger
- [x] ExprYawPitch
- [ ] ExprMaxMinecartSpeed
- [ ] ExprSkull
- [x] ExprPandaGene
- [ ] ExprCoordinate
- [x] ExprDuplicateCooldown
- [ ] ExprCompassTarget
- [ ] ExprBed
- [ ] ExprVelocity
- [x] ExprInventory
- [x] ExprDisplayHeightWidth
- [ ] ExprTablistName
- [x] ExprCommandBlockCommand
- [ ] ExprClientViewDistance
- [ ] ExprCursorSlot
- [x] ExprTotalExperience
- [ ] ExprExhaustion
- [ ] ExprTagKey
- [ ] ExprCurrentInputKeys
- [ ] ExprPlayerProtocolVersion
- [ ] ExprSlotIndex
- [x] ExprBlockSound
- [ ] ExprLastLoginTime
- [ ] ExprHiddenPlayers
- [x] ExprTagsOf
- [ ] ExprItemOfEntity
- [x] ExprVectorSquaredLength
- [ ] ExprDustedStage
- [ ] ExprEquipCompSlot
- [ ] ExprSpeed
- [x] ExprDisplayBrightness
- [ ] ExprFireTicks
- [x] ExprEntitySnapshot
- [ ] ExprRingingTime
- [ ] ExprFacing
- [x] ExprWorldBorderWarningDistance
- [x] ExprItemAmount
- [x] ExprItemDisplayTransform
- [ ] ExprStringColor
- [ ] ExprDamageType
- [ ] ExprFallDistance
- [x] ExprTypeOf
- [x] ExprWorldBorderCenter
- [ ] ExprBrushableItem
- [x] ExprLoveTime
- [x] ExprWorldEnvironment
- [ ] ExprMoonPhase
- [ ] ExprEquipCompCameraOverlay
- [x] ExprWithFireResistance
- [ ] ExprArrowKnockbackStrength
- [ ] ExprAltitude
- [x] ExprVectorLength
- [x] ExprLowestHighestSolidBlock
- [ ] ExprDisplayTeleportDuration
- [x] ExprArrowsStuck
- [ ] ExprSpawnEggEntity
- [ ] ExprLootContextLooter
- [ ] ExprAttachedBlock
- [x] ExprARGB
- [x] ExprWorldBorderDamageBuffer
- [ ] ExprLanguage
- [ ] ExprSeed
- [x] ExprPortalCooldown
- [ ] ExprHotbarSlot
- [ ] ExprOpenedInventory
- [ ] ExprDisplayTransformationScaleTranslation
- [x] ExprVillagerType
- [ ] ExprVectorFromDirection
- [x] ExprRound
- [x] ExprDisplayBillboard
- [ ] ExprPlayerChatCompletions
- [ ] ExprLeashHolder
- [ ] ExprEnchantments
- [ ] ExprEquipCompModel
- [x] ExprRemainingAir
- [ ] ExprTimeLived
- [x] ExprViewDistance
- [x] ExprLootTableSeed
- [x] ExprWeather
- [ ] ExprTimespanDetails
- [x] ExprItemWithCustomModelData
- [ ] ExprSignText
- [x] ExprItemWithLore
- [ ] ExprLore
- [ ] ExprNamed
- [x] ExprItemsIn
- [x] ExprArmorSlot
- [x] ExprLocationVectorOffset
- [ ] ExprFormatDate
- [x] ExprNode
- [ ] ExprValue
- [x] ExprBookPages
- [x] ExprValueWithin
- [ ] ExprEnchantmentLevel
- [ ] ExprBannerPatterns
- [x] ExprMetadata
- [x] ExprAmountOfItems
- [ ] ExprTarget
- [x] ExprElement
- [ ] ExprSubnodeValue
- [ ] ExprBiome
- [ ] ExprLightLevel
- [x] ExprWhether
- [ ] ExprEntity
- [x] ExprAnyOf
- [x] ExprTransform
- [x] ExprSets
- [x] ExprXOf
- [x] ExprArithmetic
- [x] ExprEntities


### Conditions

---

- [ ] CondWillHatch
- [ ] CondFishingLure
- [ ] CondRespawnLocation
- [ ] CondElytraBoostConsume
- [ ] CondLeashWillDrop
- [ ] CondCancelled
- [ ] CondBrewingConsume
- [ ] CondAlphanumeric
- [x] CondIsCustomNameVisible
- [ ] CondDamageCause
- [ ] CondIncendiary
- [ ] CondAnchorWorks
- [ ] CondTextDisplayHasDropShadow
- [ ] CondPlayedBefore
- [ ] CondIsHandRaised
- [ ] CondIsSet
- [ ] CondScriptLoaded
- [ ] CondFromMobSpawner
- [ ] CondMinecraftVersion
- [ ] CondIsBlockRedstonePowered
- [ ] CondIsBanned
- [x] CondChatVisibility
- [ ] CondResourcePack
- [ ] CondEquipCompDamage
- [x] CondPastFuture
- [ ] CondIsPluginEnabled
- [x] CondTooltip
- [ ] CondScalesWithDifficulty
- [ ] CondIsWhitelisted
- [ ] CondPvP
- [ ] CondIgnitionProcess
- [ ] CondChance
- [ ] CondIsUsingFeature
- [x] CondStartsEndsWith
- [ ] CondItemInHand
- [ ] CondHasMetadata
- [x] CondHasLineOfSight
- [ ] CondIsPressingKey
- [ ] CondHasItemCooldown
- [ ] CondDate
- [x] CondHasPotion
- [ ] CondPermission
- [ ] CondIsSpawnable
- [x] CondContains
- [x] CondMatches
- [ ] CondCanHold
- [ ] CondCanSee
- [x] CondIsPreferredTool
- [x] CondIsWithin
- [x] CondIsDivisibleBy
- [x] CondIsLoaded
- [x] CondHasCustomModelData
- [ ] CondEquipCompInteract
- [x] CondIsFlammable
- [ ] CondIsValid
- [ ] CondIsChargingFireball
- [ ] CondEquipCompSwapEquipment
- [ ] CondGoatHasHorns
- [ ] CondEndermanStaredAt
- [ ] CondGlowingText
- [ ] CondPandaIsScared
- [x] CondIsVectorNormalized
- [ ] CondCanAge
- [ ] CondIsOp
- [ ] CondIsEmpty
- [ ] CondIsJumping
- [ ] CondPandaIsRolling
- [ ] CondEquipCompDispensable
- [ ] CondItemDespawn
- [ ] CondIsOnGround
- [ ] CondIsPlayingDead
- [ ] CondIsSilent
- [ ] CondPandaIsOnBack
- [ ] CondHasResourcePack
- [ ] CondIsInOpenWater
- [ ] CondIsRiptiding
- [ ] CondIsDancing
- [ ] CondTextDisplaySeeThroughBlocks
- [ ] CondIsTameable
- [x] CondIsCommandBlockConditional
- [ ] CondLidState
- [x] CondIsTamed
- [ ] CondIsGliding
- [ ] CondAI
- [ ] CondIsPoisoned
- [ ] CondChatFiltering
- [ ] CondIsStackable
- [ ] CondIsEating
- [ ] CondEntityIsWet
- [ ] CondItemDespawn
- [ ] CondEntityStorageIsFull
- [ ] CondIsPassable
- [ ] CondIsAlive
- [ ] CondIsSlimeChunk
- [ ] CondIsSleeping
- [ ] CondIsSkriptCommand
- [ ] CondIsFlying
- [ ] CondPandaIsSneezing
- [x] CondIsInvulnerable
- [ ] CondIsScreaming
- [ ] CondIsPersistent
- [ ] CondIsUnbreakable
- [ ] CondIsInvisible
- [ ] CondAllayCanDuplicate
- [ ] CondCanPickUpItems
- [ ] CondStriderIsShivering
- [ ] CondIsSprinting
- [x] CondIsTransparent
- [ ] CondIsBurning
- [ ] CondIsBlocking
- [ ] CondHasClientWeather
- [ ] CondIsSedated
- [ ] CondIsOccluding
- [x] CondIsEdible
- [ ] CondIsAdult
- [x] CondIsSaddled
- [x] CondIsLootable
- [ ] CondIsSwimming
- [ ] CondIsDashing
- [ ] CondIsResonating
- [ ] CondIsInteractable
- [ ] CondItemEnchantmentGlint
- [ ] CondIsLeftHanded
- [ ] CondIsSheared
- [ ] CondIsOnline
- [ ] CondItemEnchantmentGlint
- [ ] CondIsFuel
- [x] CondIsBlock
- [ ] CondEntityUnload
- [ ] CondIsSneaking
- [ ] CondCanFly
- [x] CondHasLootTable
- [ ] CondIsRinging
- [ ] CondIsInfinite
- [x] CondIsFireResistant
- [ ] CondIsFrozen
- [ ] CondIsClimbing
- [ ] CondWasIndirect
- [ ] CondEquipCompShearable
- [ ] CondChatColors
- [x] CondIsInLove
- [ ] CondLeashed
- [ ] CondCanBreed
- [ ] CondIsBaby
- [x] CondIsTicking
- [ ] CondEntityIsInLiquid
- [x] CondIsCharged
- [x] CondIsSolid
- [x] CondIsTagged
- [ ] CondHasScoreboardTag
- [x] CondIsOfType
- [ ] CondIsPathfinding
- [ ] CondIsRiding
- [x] CondIsEnchanted
- [ ] CondIsWearing
- [ ] CondWithinRadius
- [x] CondCompare

### Effects

---

- [x] EffMakeEggHatch
- [x] EffCancelEvent
- [x] EffElytraBoostConsume
- [ ] EffSuppressTypeHints
- [x] EffDoIf
- [ ] EffFishingLure
- [ ] EffExceptionDebug
- [ ] EffCancelDrops
- [x] EffExit
- [ ] EffCancelCooldown
- [ ] EffEnforceWhitelist
- [ ] EffPullHookedEntity
- [ ] EffBrewingConsume
- [ ] EffStopServer
- [ ] EffSuppressWarnings
- [ ] EffDropLeash
- [x] EffContinue
- [ ] EffPlayerInfoVisibility
- [ ] EffKeepInventory
- [ ] EffClearEntityStorage
- [x] EffGoatHorns
- [ ] EffAllayCanDuplicate
- [x] EffReturn
- [x] EffExplodeCreeper
- [ ] EffToggleCanPickUpItems
- [ ] EffEquipCompDamageable
- [x] EffGlowingText
- [x] EffDetonate
- [ ] EffEntityUnload
- [x] EffLidState
- [ ] EffEquipCompInteract
- [x] EffAllowAging
- [x] EffInvisible
- [ ] EffCancelItemUse
- [ ] EffCommandBlockConditional
- [ ] EffSwingHand
- [x] EffScriptFile
- [ ] EffOp
- [x] EffPandaRolling
- [ ] EffCustomName
- [ ] EffToggleFlight
- [ ] EffMakeFly
- [x] EffTransform
- [x] EffHandedness
- [ ] Delay
- [x] EffInvulnerability
- [ ] EffResetTitle
- [ ] EffCharge
- [x] EffFireResistant
- [x] EffMakeAdultOrBaby
- [ ] EffWorldSave
- [ ] EffAllayDuplicate
- [x] EffSort
- [x] EffScreaming
- [ ] EffLoadServerIcon
- [x] EffPandaSneezing
- [x] EffPlayingDead
- [x] EffKill
- [ ] EffIncendiary
- [x] EffTextDisplaySeeThroughBlocks
- [ ] EffRespawn
- [ ] EffItemDespawn
- [x] EffEating
- [ ] EffHidePlayerFromServerList
- [ ] EffEquipCompDispensable
- [ ] EffEquipCompSwapEquipment
- [x] EffTextDisplayDropShadow
- [ ] EffSprinting
- [x] EffPvP
- [x] EffBreedable
- [x] EffTame
- [x] EffSilence
- [ ] EffTooltip
- [x] EffPersistent
- [ ] EffEquipCompShearable
- [ ] EffShear
- [x] EffStriderShivering
- [ ] EffForceEnchantmentGlint
- [x] EffPandaOnBack
- [x] EffToggle
- [x] EffActionBar
- [ ] EffApplyBoneMeal
- [ ] EffWorldLoad
- [ ] EffGoatRam
- [x] EffCopy
- [ ] EffLeash
- [x] EffRegisterTag
- [x] EffEnchant
- [ ] EffLog
- [x] EffBlockUpdate
- [x] EffZombify
- [ ] EffInsertEntityStorage
- [ ] EffPoison
- [ ] EffMakeSay
- [ ] EffReleaseEntityStorage
- [ ] EffEndermanTeleport
- [x] EffFeed
- [ ] EffBreakNaturally
- [ ] EffKick
- [ ] EffIgnite
- [x] EffVehicle
- [ ] EffBroadcast
- [x] EffRun
- [x] EffCommand
- [ ] EffOpenInventory
- [ ] EffOpenBook
- [ ] EffEntityVisibility
- [x] EffEquip
- [ ] EffStopSound
- [x] EffReplace
- [ ] EffGenerateLoot
- [x] EffForceAttack
- [ ] EffRing
- [ ] EffSendResourcePack
- [ ] EffFireworkLaunch
- [ ] EffBan
- [ ] EffPathfind
- [x] EffHealth
- [x] EffWorldBorderExpand
- [x] EffPush
- [ ] EffSendBlockChange
- [ ] EffConnect
- [ ] EffMessage
- [x] EffPotion
- [x] EffRotate
- [ ] EffLightning
- [x] EffChange
- [ ] EffKnockback
- [x] EffWakeupSleep
- [ ] EffWardenDisturbance
- [ ] EffLook
- [x] EffTeleport
- [ ] EffSendTitle
- [x] EffDancing
- [ ] EffExplosion
- [x] EffPlaySound
- [ ] EffDrop
- [ ] EffTree
- [ ] EffColorItems
- [x] EffVisualEffect

### Sections

---

- [x] SecConditional
- [x] SecWhile
- [x] SecCatchErrors
- [x] SecLoop
- [x] SecFilter
- [x] SecFor
- [x] EffSecShoot
- [x] EffSecSpawn