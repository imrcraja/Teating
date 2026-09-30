# LightSpeedX

Minecraft Java 26.1.2 + Fabric.

Current foundation:
- Rocket Core toggles rocket flight.
- Look direction controls 3D travel.
- Sprint provides a bounded high-speed boost.
- Sneak provides downward travel.
- Entity-only collision slows the rocket and pushes the entity hit.
- No Minecraft blocks are placed, broken, moved, or replaced.
- Tiny particle budget and no per-block world scanning.

Next layer: a client-rendered block-state rocket blueprint. It will be purely visual, so the Minecraft world remains untouched while the rocket can animate ignition, exhaust, banking and collision.
