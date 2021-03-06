package com.mojang.minecraft.entity.mob;

import org.lwjgl.opengl.GL11;

import ch.spacebase.openclassic.api.render.RenderHelper;

import com.mojang.minecraft.entity.model.HumanoidModel;
import com.mojang.minecraft.level.Level;
import com.mojang.minecraft.render.TextureManager;

public class HumanoidMob extends Mob {

	public boolean helmet = Math.random() < 0.20000000298023224D;
	public boolean armor = Math.random() < 0.20000000298023224D;

	public HumanoidMob(Level level, float x, float y, float z) {
		super(level);
		this.modelName = "humanoid";
		this.setPos(x, y, z);
	}

	public void renderModel(TextureManager textures, float animStep, float dt, float runProgress, float yaw, float pitch, float scale) {
		super.renderModel(textures, animStep, dt, runProgress, yaw, pitch, scale);
		HumanoidModel model = (HumanoidModel) modelCache.getModel(this.modelName);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		if(this.allowAlpha) {
			GL11.glEnable(GL11.GL_CULL_FACE);
		}

		if(this.hasHair) {
			GL11.glDisable(GL11.GL_CULL_FACE);
			model.hair.yaw = model.head.yaw;
			model.hair.pitch = model.head.pitch;
			model.hair.render(scale);
			GL11.glEnable(GL11.GL_CULL_FACE);
		}

		if(this.armor || this.helmet) {
			RenderHelper.getHelper().bindTexture("/armor/plate.png", true);
			GL11.glDisable(GL11.GL_CULL_FACE);
			HumanoidModel armored = (HumanoidModel) modelCache.getModel("humanoid.armor");
			armored.head.render = this.helmet;
			armored.body.render = this.armor;
			armored.rightArm.render = this.armor;
			armored.leftArm.render = this.armor;
			armored.rightLeg.render = false;
			armored.leftLeg.render = false;
			armored.head.yaw = model.head.yaw;
			armored.head.pitch = model.head.pitch;
			armored.rightArm.pitch = model.rightArm.pitch;
			armored.rightArm.roll = model.rightArm.roll;
			armored.leftArm.pitch = model.leftArm.pitch;
			armored.leftArm.roll = model.leftArm.roll;
			armored.rightLeg.pitch = model.rightLeg.pitch;
			armored.leftLeg.pitch = model.leftLeg.pitch;
			armored.head.render(scale);
			armored.body.render(scale);
			armored.rightArm.render(scale);
			armored.leftArm.render(scale);
			armored.rightLeg.render(scale);
			armored.leftLeg.render(scale);
			GL11.glEnable(GL11.GL_CULL_FACE);
		}

		GL11.glDisable(GL11.GL_ALPHA_TEST);
	}
}
