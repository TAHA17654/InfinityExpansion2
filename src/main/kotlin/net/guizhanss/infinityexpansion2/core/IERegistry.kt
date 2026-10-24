package net.guizhanss.infinityexpansion2.core

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import net.guizhanss.guizhanlib.kt.slimefun.items.builder.MaterialType
import net.guizhanss.infinityexpansion2.api.mobsim.MobDataCardProps
import net.guizhanss.infinityexpansion2.core.recipes.MachineRecipe
import net.guizhanss.infinityexpansion2.implementation.items.materials.Singularity
import org.bukkit.inventory.ItemStack
import kotlin.reflect.KClass

/**
 * Everything that is needed to create a second, compressed instance of a machine.
 */
internal class CompressibleMachine(
    val clazz: KClass<out SlimefunItem>,
    val id: String,
    val material: MaterialType,
    val baseStack: SlimefunItemStack,
    val args: List<Any?>, // the constructor arguments after itemGroup, itemStack, recipeType and recipe
)

internal object IERegistry {

    val itemMapping = mutableMapOf<String, ItemStack>() // ID to ItemStack, either vanilla or SlimefunItemStack
    val singularities = mutableListOf<Singularity>()
    val singularityIngredientMap = mutableMapOf<ItemStack, Singularity>() // ingredient to Singularity
    val infinityRecipes = mutableListOf<MachineRecipe>() // infinity workbench recipes
    val mobDataCards = mutableMapOf<String, MobDataCardProps>() // id to props
    val mobDataInfuserRecipes = mutableListOf<MachineRecipe>() // mob data infuser recipes
    val storageForgeRecipes = mutableListOf<MachineRecipe>() // storage forge recipes
    val compressibleMachines = mutableListOf<CompressibleMachine>() // machines that have a compressed version
    val compressedMachines = mutableListOf<Pair<SlimefunItemStack, SlimefunItemStack>>() // machine to compressed machine
}
