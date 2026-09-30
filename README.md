# LightSpeedX

**By RC Empire**

Minecraft Java **26.1.2 + Fabric**.

## Rocket controls

The Rocket Core activates the flight system. Look in any direction to travel in that direction.

- Sprint: high-speed boost is controlled by the selected speed value.
- Sneak: vertical downward travel.
- Look up/down: vertical travel.
- Look left/right: horizontal travel.
- Entity collision: the rocket slows and transfers momentum to the entity.
- World blocks are never placed, broken, or replaced by the rocket system.

## Custom speed

Every player can choose their own speed:

`/lightspeedx speed 20`

The value is in **blocks per tick**. The mod accepts values from 0.01 up to 50,000,000 blocks/tick, so players can explore at normal high speed or use deliberately extreme/faster-than-light-style gameplay.

Useful commands:

- `/lightspeedx speed <blocksPerTick>`
- `/lightspeedx on`
- `/lightspeedx off`
- `/lightspeedx info`

The default is 6 blocks/tick.

## Performance

LightSpeedX intentionally keeps the hot movement loop small: it performs one swept entity query and uses a tiny particle budget rather than scanning or modifying blocks every tick. The visual rocket should remain lightweight by using a compact block-based model rather than hundreds of separate entities.

## Branding

Author / publisher: **RC Empire**
Mod: **LightSpeedX**
Target: **Minecraft 26.1.2 Fabric**
