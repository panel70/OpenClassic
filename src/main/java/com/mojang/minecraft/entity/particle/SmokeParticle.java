package com.mojang.minecraft.entity.particle;

import com.mojang.minecraft.level.Level;

public class SmokeParticle extends Particle {

	public SmokeParticle(Level level, float x, float y, float z) {
		super(level, x, y, z, 0.0F, 0.0F, 0.0F);
		this.xd *= 0.1F;
		this.yd *= 0.1F;
		this.zd *= 0.1F;
		this.rCol = this.gCol = this.bCol = (float) (Math.random() * 0.30000001192092896D);
		this.lifetime = (int) (8.0D / (Math.random() * 0.8D + 0.2D));
		this.noPhysics = true;
	}

	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		if (this.age++ >= this.lifetime) {
			this.remove();
		}

		this.tex = 7 - (this.age << 3) / this.lifetime;
		this.yd = (float) (this.yd + 0.004D);
		this.move(this.xd, this.yd, this.zd);
		this.xd *= 0.96F;
		this.yd *= 0.96F;
		this.zd *= 0.96F;
		if (this.onGround) {
			this.xd *= 0.7F;
			this.zd *= 0.7F;
		}

	}
}
