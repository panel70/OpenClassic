package com.mojang.minecraft.entity.particle;

import com.mojang.minecraft.level.Level;

public class WaterDropParticle extends Particle {

	public WaterDropParticle(Level level, float x, float y, float z) {
		super(level, x, y, z, 0.0F, 0.0F, 0.0F);
		this.xd *= 0.3F;
		this.yd = (float) Math.random() * 0.2F + 0.1F;
		this.zd *= 0.3F;
		this.rCol = 1.0F;
		this.gCol = 1.0F;
		this.bCol = 1.0F;
		this.tex = 16;
		this.setSize(0.01F, 0.01F);
		this.lifetime = (int) (8.0D / (Math.random() * 0.8D + 0.2D));
	}

	public void tick() {
		this.xo = this.x;
		this.yo = this.y;
		this.zo = this.z;
		this.yd = (float) (this.yd - 0.06D);
		this.move(this.xd, this.yd, this.zd);
		this.xd *= 0.98F;
		this.yd *= 0.98F;
		this.zd *= 0.98F;
		if(this.lifetime-- <= 0) {
			this.remove();
		}

		if(this.onGround) {
			if(Math.random() < 0.5D) {
				this.remove();
			}

			this.xd *= 0.7F;
			this.zd *= 0.7F;
		}

	}
}
