# Pyrite Textures Needed

This directory requires 5 texture files for the pyrite (fool's gold) material system.

## Required Textures:

### 1. **ingot.png** (16x16 pixels)
- Pyrite ingot texture
- Should look similar to gold ingot but with a slightly different hue
- Suggested: Brassy/bronze color (more yellowish-brown than pure gold)
- Can be created by:
  - Recoloring the vanilla gold_ingot.png texture
  - Adjusting hue/saturation to make it look like "fool's gold"
  - Pyrite (FeS₂) typically has a brass-yellow color with metallic luster

### 2. **pickaxe.png** (16x16 pixels)
- Pyrite pickaxe texture
- Based on golden_pickaxe.png from vanilla Minecraft
- Recolor to match the pyrite ingot color scheme
- Keep the same shape as golden pickaxe

### 3. **axe.png** (16x16 pixels)
- Pyrite axe texture
- Based on golden_axe.png from vanilla Minecraft
- Recolor to match the pyrite ingot color scheme
- Keep the same shape as golden axe

### 4. **shovel.png** (16x16 pixels)
- Pyrite shovel texture
- Based on golden_shovel.png from vanilla Minecraft
- Recolor to match the pyrite ingot color scheme
- Keep the same shape as golden shovel

### 5. **hoe.png** (16x16 pixels)
- Pyrite hoe texture
- Based on golden_hoe.png from vanilla Minecraft
- Recolor to match the pyrite ingot color scheme
- Keep the same shape as golden hoe

## Color Guide:

**Pyrite ("Fool's Gold") Color Profile:**
- Base: Brass yellow (#B5A642 or similar)
- Slightly more greenish-brown than pure gold
- Less saturated than gold's bright yellow
- Can have slight metallic sheen/highlights

**How to Create from Gold Textures:**
1. Copy vanilla gold textures from Minecraft assets
2. Open in image editor (GIMP, Photoshop, etc.)
3. Adjust Hue: +10 to +20 degrees (shift toward green/brown)
4. Reduce Saturation: -10% to -15%
5. Optionally add slight texture/grain for "rough" metallic look
6. Save as PNG with transparency preserved

## Quick Setup (Temporary):

For testing purposes, you can:
1. Copy the vanilla gold textures and rename them
2. They will work functionally but won't be visually distinct
3. Replace with properly colored versions later

## File Paths:

Vanilla gold textures are located in Minecraft's assets:
```
assets/minecraft/textures/item/golden_pickaxe.png
assets/minecraft/textures/item/golden_axe.png
assets/minecraft/textures/item/golden_shovel.png
assets/minecraft/textures/item/golden_hoe.png
assets/minecraft/textures/item/gold_ingot.png
```

Extract these, recolor, and place in this directory with the names above.
