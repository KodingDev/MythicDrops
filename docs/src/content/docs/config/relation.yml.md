---
title: relation.yml
description: relation.yml reference
---

MythicDrops has a lot of configuration options. Below is the contents of the relation.yml with inline explanations of
what each configuration option does.

```yaml
version: "1.0.0"
attributed:
  attributes:
    exampleattributes:
      ## Attribute for this particular modifier. Attribute names here:
      ## https://hub.spigotmc.org/javadocs/spigot/org/bukkit/attribute/Attribute.html
      attribute: GENERIC_ARMOR
      ## Decimal value for the minimum amount this attribute adds.
      minimum-amount: 4.0
      ## Decimal value for the maximum amount this attribute adds.
      maximum-amount: 6.0
      operation: ADD_NUMBER
      ## Which equipment slot should this modifier apply to? Not including
      ## this field makes it apply to every equipment slot. Slot names here:
      ## https://hub.spigotmc.org/javadocs/spigot/org/bukkit/inventory/EquipmentSlot.html
      slot: OFF_HAND
binding:
  enchantments:
    BINDING_CURSE:
      minimum_level: 1
      maximum_level: 1
glacier:
  lore:
    - "&bSpeed: -30%"
```
