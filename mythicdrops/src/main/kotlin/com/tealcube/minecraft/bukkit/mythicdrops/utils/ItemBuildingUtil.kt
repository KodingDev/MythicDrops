/*
 * This file is part of MythicDrops, licensed under the MIT License.
 *
 * Copyright (C) 2019 Richard Harrah
 *
 * Permission is hereby granted, free of charge,
 * to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.tealcube.minecraft.bukkit.mythicdrops.utils

import com.tealcube.minecraft.bukkit.mythicdrops.MythicDropsPlugin
import com.tealcube.minecraft.bukkit.mythicdrops.api.MythicDrops
import com.tealcube.minecraft.bukkit.mythicdrops.api.enchantments.MythicEnchantment
import com.tealcube.minecraft.bukkit.mythicdrops.api.tiers.Tier
import kotlin.math.max
import kotlin.math.min
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack

object ItemBuildingUtil {
    private val mythicDrops: MythicDrops by lazy {
        MythicDropsPlugin.getInstance()
    }

    fun getSafeEnchantments(
        isSafe: Boolean,
        enchantments: Collection<MythicEnchantment>,
        itemStack: ItemStack
    ): Collection<MythicEnchantment> {
        return enchantments.filter {
            if (isSafe) {
                it.enchantment.canEnchantItem(itemStack)
            } else {
                true
            }
        }
    }

    fun getBaseEnchantments(itemStack: ItemStack, tier: Tier): Map<Enchantment, Int> {
        if (tier.baseEnchantments.isEmpty()) {
            return emptyMap()
        }
        val safeEnchantments = getSafeEnchantments(tier.isSafeBaseEnchantments, tier.baseEnchantments, itemStack)
        return safeEnchantments.map { mythicEnchantment ->
            val enchantment = mythicEnchantment.enchantment
            val minimumLevel = if (tier.isAllowHighBaseEnchantments) {
                max(max(1, mythicEnchantment.minimumLevel), enchantment.startLevel)
            } else {
                min(max(1, mythicEnchantment.minimumLevel), enchantment.startLevel)
            }
            val maximumLevel = max(mythicEnchantment.maximumLevel, enchantment.maxLevel)
            when {
                !tier.isSafeBaseEnchantments -> enchantment to (minimumLevel..maximumLevel).random()
                tier.isAllowHighBaseEnchantments -> {
                    enchantment to (minimumLevel..mythicEnchantment.maximumLevel).random()
                }
                else -> enchantment to getAcceptableEnchantmentLevel(
                    enchantment,
                    (minimumLevel..maximumLevel).random()
                )
            }
        }.toMap()
    }

    fun getBonusEnchantments(itemStack: ItemStack, tier: Tier): Map<Enchantment, Int> {
        if (tier.bonusEnchantments.isEmpty()) {
            return emptyMap()
        }
        val bonusEnchantmentsToAdd = (tier.minimumBonusEnchantments..tier.maximumBonusEnchantments).random()
        var tierBonusEnchantments =
            getSafeEnchantments(tier.isSafeBonusEnchantments, tier.bonusEnchantments, itemStack)
        if (tierBonusEnchantments.isEmpty()) {
            return emptyMap()
        }
        val bonusEnchantments = mutableMapOf<Enchantment, Int>()
        repeat(bonusEnchantmentsToAdd) {
            if (tierBonusEnchantments.isEmpty()) {
                return@repeat
            }
            val mythicEnchantment = tierBonusEnchantments.random()
            if (mythicDrops.settingsManager.configSettings.options.isOnlyRollBonusEnchantmentsOnce) {
                tierBonusEnchantments -= mythicEnchantment
            }
            val enchantment = mythicEnchantment.enchantment
            val randomizedLevelOfEnchantment =
                bonusEnchantments[enchantment]?.let {
                    min(
                        mythicEnchantment.maximumLevel,
                        it + mythicEnchantment.getRandomLevel()
                    )
                }
                    ?: mythicEnchantment.getRandomLevel()
            val trimmedLevel = if (!tier.isAllowHighBonusEnchantments) {
                getAcceptableEnchantmentLevel(enchantment, randomizedLevelOfEnchantment)
            } else {
                randomizedLevelOfEnchantment
            }
            bonusEnchantments[enchantment] = trimmedLevel
        }
        return bonusEnchantments
    }

    private fun getAcceptableEnchantmentLevel(enchantment: Enchantment, level: Int): Int {
        return max(min(level, enchantment.maxLevel), enchantment.startLevel)
    }
}