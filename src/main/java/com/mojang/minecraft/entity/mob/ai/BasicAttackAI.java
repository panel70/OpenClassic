package com.mojang.minecraft.entity.mob.ai;

import ch.spacebase.openclassic.api.math.MathHelper;

import com.mojang.minecraft.entity.Entity;
import com.mojang.minecraft.entity.item.Arrow;
import com.mojang.minecraft.entity.model.Vector;

public class BasicAttackAI extends BasicAI {

	public int damage = 6;

	public void update() {
		super.update();
		if(this.mob.health > 0) {
			this.doAttack();
		}
	}

	protected void doAttack() {
		Entity player = this.level.getPlayer();
		if(this.attackTarget != null && this.attackTarget.removed) {
			this.attackTarget = null;
		}

		if(player != null && this.attackTarget == null) {
			float xDistance = player.x - mob.x;
			float yDistance = player.y - mob.y;
			float zDistance = player.z - mob.z;
			float sqDistance = xDistance * xDistance + yDistance * yDistance + zDistance * zDistance;

			if(sqDistance < 256) {
				this.attackTarget = player;
			}
		}

		if(this.attackTarget != null) {
			float xDistance = player.x - mob.x;
			float yDistance = player.y - mob.y;
			float zDistance = player.z - mob.z;
			float sqDistance = xDistance * xDistance + yDistance * yDistance + zDistance * zDistance;

			if(sqDistance > 1024 && this.random.nextInt(100) == 0) {
				this.attackTarget = null;
			}

			if(this.attackTarget != null) {
				float distance = (float) Math.sqrt(sqDistance);
				this.mob.yaw = (float) (Math.atan2(zDistance, xDistance) * MathHelper.DRAD_TO_DEG) - 90.0F;
				this.mob.pitch = -((float) (Math.atan2(yDistance, (float) Math.sqrt(distance)) * MathHelper.DRAD_TO_DEG));
				if((float) Math.sqrt(sqDistance) < 2 && this.attackDelay == 0) {
					this.attack(this.attackTarget);
				}
			}

		}
	}

	public boolean attack(Entity entity) {
		if(this.level.clip(new Vector(this.mob.x, this.mob.y, this.mob.z), new Vector(entity.x, entity.y, entity.z)) != null) {
			return false;
		} else {
			this.mob.attackTime = 5;
			this.attackDelay = this.random.nextInt(20) + 10;
			int damage = (int) ((this.random.nextFloat() + this.random.nextFloat()) / 2.0F * this.damage + 1.0F);
			entity.hurt(this.mob, damage);
			this.noActionTime = 0;
			return true;
		}
	}

	public void hurt(Entity cause, int damage) {
		super.hurt(cause, damage);
		if(cause instanceof Arrow) {
			cause = ((Arrow) cause).getOwner();
		}

		if(cause != null && !cause.getClass().equals(this.mob.getClass())) {
			this.attackTarget = cause;
		}

	}
}
