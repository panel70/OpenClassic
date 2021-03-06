package com.mojang.minecraft.level;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;

import ch.spacebase.openclassic.api.OpenClassic;
import ch.spacebase.openclassic.api.data.NBTData;
import ch.spacebase.openclassic.api.event.level.LevelLoadEvent;
import ch.spacebase.openclassic.api.event.level.LevelSaveEvent;
import ch.spacebase.openclassic.client.level.ClientLevel;
import ch.spacebase.openclassic.client.util.GeneralUtils;
import ch.spacebase.openclassic.game.io.OpenClassicLevelFormat;

import com.zachsthings.onevent.EventManager;

public final class LevelIO {

	public static boolean save(Level level) {
		if(EventManager.callEvent(new LevelSaveEvent(level.openclassic)).isCancelled()) {
			return true;
		}

		try {
			OpenClassicLevelFormat.save(level.openclassic);
			if(level.openclassic.getData() != null) level.openclassic.getData().save(OpenClassic.getGame().getDirectory().getPath() + "/levels/" + level.name + ".nbt");
			return true;
		} catch(IOException e) {
			if(GeneralUtils.getMinecraft() != null) {
				GeneralUtils.getMinecraft().progressBar.setText(String.format(OpenClassic.getGame().getTranslator().translate("level.save-fail"), level.name));
			}

			e.printStackTrace();

			try {
				Thread.sleep(1000L);
			} catch(InterruptedException e1) {
			}

			return false;
		}
	}

	public static Level load(String name) {
		if(GeneralUtils.getMinecraft() != null) {
			GeneralUtils.getMinecraft().progressBar.setVisible(true);
			GeneralUtils.getMinecraft().progressBar.setTitle(OpenClassic.getGame().getTranslator().translate("progress-bar.singleplayer"));
			GeneralUtils.getMinecraft().progressBar.setSubtitle(OpenClassic.getGame().getTranslator().translate("level.loading"));
			GeneralUtils.getMinecraft().progressBar.setText(OpenClassic.getGame().getTranslator().translate("level.reading"));
			GeneralUtils.getMinecraft().progressBar.setProgress(-1);
			GeneralUtils.getMinecraft().progressBar.render();
		}

		try {
			Level level = new Level();
			level = ((ClientLevel) OpenClassicLevelFormat.load(level.openclassic, name, false)).getHandle();
			level.openclassic.data = new NBTData(level.name);
			level.openclassic.data.load(OpenClassic.getGame().getDirectory().getPath() + "/levels/" + level.name + ".nbt");
			EventManager.callEvent(new LevelLoadEvent(level.openclassic));
			GeneralUtils.getMinecraft().progressBar.setVisible(false);
			return level;
		} catch(IOException e) {
			if(GeneralUtils.getMinecraft() != null) {
				GeneralUtils.getMinecraft().progressBar.setText(String.format(OpenClassic.getGame().getTranslator().translate("level.load-fail"), name));
			}

			e.printStackTrace();

			try {
				Thread.sleep(1000L);
			} catch(InterruptedException e1) {
			}

			GeneralUtils.getMinecraft().progressBar.setVisible(false);
			return null;
		}
	}

	public static byte[] processData(InputStream in) {
		DataInputStream din = null;
		try {
			din = new DataInputStream(new GZIPInputStream(in));
			byte[] data = new byte[din.readInt()];
			din.readFully(data);
			return data;
		} catch(Exception e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(din);
		}
	}

}
