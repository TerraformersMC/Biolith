package com.terraformersmc.biolith.impl;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class BiolithPreLaunch implements PreLaunchEntrypoint {
	public void onPreLaunch() {
		MixinExtrasBootstrap.init();
	}
}
