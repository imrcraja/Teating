# LightSpeedX

**Minecraft Java 26.1.2 + Fabric**  
**Author / Publisher: RC Empire**

LightSpeedX is a lightweight high-speed rocket flight mod.

## Controls
- Use the **LightSpeedX Rocket Core** to toggle rocket mode.
- Look in any direction: forward, sideways and vertical flight follow the camera direction.
- Sneak gives controlled downward travel.
- Set your own speed with `/lightspeedx speed <blocksPerTick>`.
- Examples: `/lightspeedx speed 0.5` for slow exploration, `/lightspeedx speed 10` for fast travel, or values above the conventional speed-of-light equivalent.
- `/lightspeedx info` shows the current speed.
- `/lightspeedx on` and `/lightspeedx off` toggle flight.

## Rocket
The rocket is visually assembled from Minecraft-style block cuboids using vanilla block textures. These are **rendered item-model geometry**, not real world blocks: terrain is never placed, broken, replaced, or modified.

The model is deliberately compact and uses vanilla textures to keep resource and rendering overhead low.

## Physics
- Camera direction controls the 3D launch vector.
- Entity collision uses a narrow swept corridor and applies a strong slowdown/recoil effect.
- Very high speeds are supported, including values beyond the conventional real-world speed of light expressed in Minecraft blocks/tick.

## Performance
The flight loop avoids whole-world scans. Particle output is deliberately tiny and collision queries are bounded so low-end devices do not get a huge per-tick rendering workload.

## Branding
**LightSpeedX — by RC Empire**

MIT License — Copyright RC Empire, 2026.
